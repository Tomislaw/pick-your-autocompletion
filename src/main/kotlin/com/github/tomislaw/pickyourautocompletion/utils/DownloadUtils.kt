package com.github.tomislaw.pickyourautocompletion.utils

import com.intellij.openapi.progress.util.ProgressIndicatorBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.*
import java.io.IOException


class DownloadUtils {
    companion object {

        suspend fun downloadFile(
            url: String, downloadFile: File, indicator: ProgressIndicatorBase? = null
        ): Result<Unit> {

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient.Builder().build()

            try {
                val callback = ResponseCallback()
                client.newCall(request).enqueue(callback)

                while (!callback.finished) {
                    if (indicator?.isCanceled == true) {
                        client.dispatcher.runningCalls().forEach { it.cancel() }
                        return Result.failure(Error("Cancelled"))
                    }
                    delay(100)
                }
                if (callback.error != null) {
                    return Result.failure(callback.error!!)
                }
                val response = callback.response!!
                if (!response.isSuccessful)
                    return Result.failure(Error(response.message))
                if (response.body == null)
                    return Result.failure(Error("Empty response body"))

                withContext(Dispatchers.IO) {
                    val contentLength = response.body!!.contentLength()
                    val outputStream = FileOutputStream(downloadFile)
                    val inputStream = BufferedInputStream(response.body!!.byteStream())

                    var readByteCount: Int
                    var totalByteCount = 0
                    val data = ByteArray(1024)

                    while (true) {

                        if (indicator?.isCanceled == true) return@withContext Result.failure<Unit>(Error("Cancelled"))

                        readByteCount = inputStream.read(data)
                        if (readByteCount == -1) break

                        if (indicator?.isCanceled == true) return@withContext Result.failure<Unit>(Error("Cancelled"))

                        outputStream.write(data, 0, readByteCount)
                        totalByteCount += readByteCount.coerceAtLeast(0)
                        indicator?.fraction = totalByteCount / contentLength.toDouble()
                    }
                    outputStream.close()
                    inputStream.close()
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
            return Result.success(Unit)
        }
    }

    class ResponseCallback : Callback {
        var response: Response? = null
        var error: Throwable? = null

        val finished get() = response != null || error != null
        override fun onFailure(call: Call, e: IOException) {
            error = e
        }

        override fun onResponse(call: Call, response: Response) {
            this.response = response
        }
    }
}