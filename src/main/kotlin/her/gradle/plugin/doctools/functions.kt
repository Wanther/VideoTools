package her.gradle.plugin.doctools

import org.gradle.api.Project
import java.net.HttpURLConnection
import java.net.URL

import her.gradle.plugin.DocToolsPluginExtension


val SIZE_UNITS = arrayOf("B", "K", "M", "G" , "T")


fun getExtension(project: Project): DocToolsPluginExtension {
    return project.extensions.getByType(DocToolsPluginExtension::class.java)
}

fun sizeDisplay(size: Long, afterDot: Int = 2): String {
    var i = 0
    var j = 1L
    while(i < SIZE_UNITS.size && size / j > 1024) {
        j *= 1024
        i++
    }

    var b = (size.toFloat() / j).toString()

    val dotIndex = b.indexOf(".")
    (0 until b.length - dotIndex).forEach { _ -> b += "0" }

    return b.substring(0, dotIndex + 1 + afterDot) + SIZE_UNITS[i]
    
}

fun abcz(i: Int): String {
    return i.toString(26).map {
        if (it in '0'..'9') it + 49 else it + 10
    }.joinToString("")
}

fun replacePathExt(path: String, newExt: String): String {
    return path.substring(0, path.lastIndexOf(".") + 1) + newExt
}

fun sendDingtalkMessage(message: String, project: Project): String? {

    val body = """{"msgtype": "text", "text": {"content": "[${project.name}]$message"}}""".toByteArray()

    val extension = getExtension(project)

    val connection = URL(extension.dingtalkRobotUri).openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "POST"

        connection.doInput = true
        connection.doOutput = true
        connection.useCaches = false

        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Content-Length", body.size.toString())

        connection.outputStream.use { output ->
            output.write(body)
        }

        val responseCode = connection.responseCode

        if (responseCode in 200..399) {
            return connection.inputStream?.bufferedReader()?.use {
                it.readText()
            }
        } else {
            val errorMessage = connection.errorStream?.bufferedReader()?.use {
                it.readText()
            } ?: ""
            throw java.io.IOException(errorMessage)
        }
    } finally {
        connection.disconnect()
    }

}
