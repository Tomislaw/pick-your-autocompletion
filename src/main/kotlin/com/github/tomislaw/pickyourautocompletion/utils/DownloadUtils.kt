package com.github.tomislaw.pickyourautocompletion.utils

import com.intellij.openapi.progress.util.ProgressIndicatorBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.*
import org.jetbrains.concurrency.isPending
import org.jetbrains.concurrency.runAsync
import java.io.*


class DownloadUtils {
    companion object {

        suspend fun downloadFile(
            url: String, downloadFile: File, indicator: ProgressIndicatorBase? = null
        ): Result<Unit> {

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient.Builder().build()

            try {
                val call = client.newCall(request)

                val responsePromise = runAsync { call.execute() }

                while (responsePromise.isPending) {
                    if (indicator?.isCanceled == true) {
                        call.cancel()
                        return Result.failure(Error("Cancelled"))
                    }
                    withContext(NonCancellable) {
                        delay(10)
                    }
                }

                val response = runCatching { responsePromise.blockingGet(500) }



                if (response.isFailure)
                    return Result.failure(response.exceptionOrNull()!!)
                if (response.getOrNull()!!.body == null)
                    return Result.failure(Error("Empty response body"))

                withContext(Dispatchers.IO) {
                    val body = response.getOrNull()!!.body!!
                    val contentLength = body.contentLength()
                    val outputStream = FileOutputStream(downloadFile)
                    val inputStream = BufferedInputStream(body.byteStream())

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

}