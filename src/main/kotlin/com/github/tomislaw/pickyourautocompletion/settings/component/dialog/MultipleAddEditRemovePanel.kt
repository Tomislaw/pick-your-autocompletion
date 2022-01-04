package com.github.tomislaw.pickyourautocompletion.settings.component.dialog

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.*
import com.intellij.ui.table.JBTable
import com.intellij.util.containers.toMutableSmartList
import com.intellij.util.ui.ComponentWithEmptyText
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StatusText
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer


abstract class MultipleAddEditRemovePanel<T> @JvmOverloads constructor(
    model: TableModel<T>,
    data: MutableList<T>,
    private val addActions: Set<ButtonData>,
    label: String = ""
) :
    PanelWithButtons(), ComponentWithEmptyText {
    lateinit var table: JBTable
        private set
    private val myModel: TableModel<T> = model
    private var myData: MutableList<T>?
    private lateinit var myTableModel: AbstractTableModel
    private val myLabel: String?

    val isUpDownSupported: Boolean
        get() = false

    protected abstract fun addItem(o: ButtonData): T?
    protected abstract fun removeItem(o: T): Boolean
    protected abstract fun editItem(o: T): T?

    private val myAddActions = addActions.map {
        object : DumbAwareAction(it.text, it.description, it.icon) {
            override fun actionPerformed(e: AnActionEvent) {
                doAdd(it)
            }
        }
    }

    private val myRemoveAction = object : DumbAwareAction("", "", AllIcons.General.Remove) {
        override fun actionPerformed(e: AnActionEvent) {
            doRemove()
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = table.selectedRow in 0 until table.model.rowCount
        }
    }

    private val myEditAction = object : DumbAwareAction("", "", AllIcons.Actions.Edit) {
        override fun actionPerformed(e: AnActionEvent) {
            if (table.isEditing) {
                table.cellEditor.stopCellEditing()
                return
            }
            doEdit()
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = table.selectedRow in 0 until table.model.rowCount
        }
    }

    override fun initPanel() {
        layout = BorderLayout()

        val decorator = ToolbarDecorator.createDecorator(table)
            .setActionGroup(DefaultActionGroup(
                myAddActions.toMutableSmartList<AnAction>().apply {
                    add(myEditAction)
                    add(myRemoveAction)
                }).apply {

            })

        if (isUpDownSupported) {
            decorator
                .setMoveUpAction { doUp() }
                .setMoveDownAction { doDown() }
        } else {
            decorator.disableUpAction().disableDownAction()
        }

        val panel = decorator.createPanel()
        add(panel, BorderLayout.CENTER)
        val label = labelText
        if (label != null) {
            UIUtil.addBorder(
                panel,
                IdeBorderFactory.createTitledBorder(label, false, JBUI.insetsTop(8)).setShowLine(false)
            )
        }
    }


    override fun getLabelText(): String? = myLabel

    override fun getEmptyText(): StatusText = table.emptyText


    override fun createMainComponent(): JComponent {
        initTable()
        return ScrollPaneFactory.createScrollPane(table)
    }

    private fun initTable() {
        myTableModel = object : AbstractTableModel() {
            override fun getColumnCount(): Int = myModel.columnCount
            override fun getRowCount(): Int = myData?.size ?: 0
            override fun getColumnClass(columnIndex: Int): Class<*> = myModel.getColumnClass(columnIndex)
            override fun getColumnName(column: Int): String? = myModel.getColumnName(column)
            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any =
                myModel.getField(myData!![rowIndex], columnIndex)

            override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = myModel.isEditable(columnIndex)

            override fun setValueAt(aValue: Any, rowIndex: Int, columnIndex: Int) {
                myModel.setValue(aValue, myData!![rowIndex], columnIndex)
                fireTableRowsUpdated(rowIndex, rowIndex)
            }
        }
        table = createTable().apply {
            model = myTableModel
            setShowColumns(false)
            setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
            object : DoubleClickListener() {
                override fun onDoubleClick(event: MouseEvent): Boolean {
                    doEdit()
                    return true
                }
            }.installOn(this)
        }

    }

    protected fun createTable(): JBTable = JBTable()

    override fun createButtons(): Array<JButton?> = arrayOfNulls(0)

    protected open fun doAdd(addButton: ButtonData) {
        val o = addItem(addButton) ?: return
        myData!!.add(o)
        val index = myData!!.size - 1
        myTableModel.fireTableRowsInserted(index, index)
        table.setRowSelectionInterval(index, index)
    }

    protected open fun doEdit() {
        val selected = table.selectedRow
        if (selected >= 0) {
            val o = editItem(myData!![selected])
            if (o != null) myData!![selected] = o
            myTableModel.fireTableRowsUpdated(selected, selected)
        }
    }

    protected fun doRemove() {
        if (table.isEditing) {
            table.cellEditor.stopCellEditing()
        }
        val selected = table.selectedRows
        if (selected == null || selected.isEmpty()) return
        Arrays.sort(selected)
        for (i in selected.indices.reversed()) {
            val idx = selected[i]
            if (!removeItem(myData!![idx])) continue
            myData!!.removeAt(idx)
        }
        myTableModel.fireTableDataChanged()
        var selection = selected[0]
        if (selection >= myData!!.size) {
            selection = myData!!.size - 1
        }
        if (selection >= 0) {
            table.setRowSelectionInterval(selection, selection)
        }
    }

    protected fun doUp() {
        TableUtil.moveSelectedItemsUp(table)
    }

    protected fun doDown() {
        TableUtil.moveSelectedItemsDown(table)
    }

    var data: MutableList<T>?
        get() = myData
        set(value) {
            myData = value
            myTableModel.fireTableDataChanged()
        }

    fun setRenderer(index: Int, renderer: TableCellRenderer?) {
        table.getColumn(myModel.getColumnName(index)).cellRenderer = renderer
    }

    fun setSelected(o: Any) {
        for (i in 0 until myTableModel.rowCount) {
            if (myData!![i] == o) {
                table.selectionModel.setSelectionInterval(i, i)
                break
            }
        }
    }

    abstract class TableModel<T> {
        abstract val columnCount: Int

        abstract fun getColumnName(columnIndex: Int): String?
        abstract fun getField(o: T, columnIndex: Int): Any
        open fun getColumnClass(columnIndex: Int): Class<*> {
            return String::class.java
        }

        open fun isEditable(column: Int): Boolean {
            return false
        }

        fun setValue(aValue: Any?, data: T, columnIndex: Int) {}
    }

    init {
        myData = data
        myLabel = label
        initTable()
        initPanel()
    }

    data class ButtonData(val text: String, val description: String, val icon: Icon)
}