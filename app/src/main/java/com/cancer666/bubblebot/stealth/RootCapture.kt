package com.cancer666.bubblebot.stealth

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RootCapture {
    suspend fun capture(): Result<Bitmap> = withContext(Dispatchers.IO) {
        val tmp = File.createTempFile("bb_cap_", ".png")
        try {
            val proc = Runtime.getRuntime().exec("su")
            DataOutputStream(proc.outputStream).use { os ->
                os.writeBytes("screencap -p ${tmp.absolutePath}\n")
                os.writeBytes("exit\n")
                os.flush()
            }
            val code = proc.waitFor()
            if (code != 0 || !tmp.exists() || tmp.length() == 0L) {
                return@withContext Result.failure(
                    IllegalStateException("screencap gagal (exit $code)"),
                )
            }
            val bitmap = BitmapFactory.decodeFile(tmp.absolutePath)
                ?: return@withContext Result.failure(IllegalStateException("decode gagal"))
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tmp.delete()
        }
    }

    fun encode(bitmap: Bitmap, quality: Int = 70): String {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        return android.util.Base64.encodeToString(output.toByteArray(), android.util.Base64.NO_WRAP)
    }
}
