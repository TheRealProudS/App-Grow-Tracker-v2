o finde ich die crach iflepackage com.growtracker.app.util

import android.app.Application
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object CrashLogger {
    private const val TAG = "CrashLogger"
    private const val FILE_NAME = "crash_latest.txt"

    fun install(app: Application) {
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                pw.println("Thread: ${thread.name}")
                throwable.printStackTrace(pw)
                pw.flush()
                val text = sw.toString()
                Log.e(TAG, text)

                val out = File(app.filesDir, FILE_NAME)
                out.writeText(text)
            }.onFailure {
                Log.e(TAG, "Failed to write crash log: ${it.message}")
            }
            // Delegate to previous handler so the system will still show the crash dialog
            prev?.uncaughtException(thread, throwable)
        }
    }

    fun latestCrashFile(app: Application): File = File(app.filesDir, FILE_NAME)
}
