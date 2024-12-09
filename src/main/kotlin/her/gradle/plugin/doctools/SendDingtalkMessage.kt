package her.gradle.plugin.doctools

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


open class SendDingtalkMessage : DefaultTask () {
    @get:Input
    lateinit var message: String

    @TaskAction
    fun execute() {
        try {
            sendDingtalkMessage(message, project)
        } catch (e: Exception) {
            logger.warn("Send Dingtalk Message Failed: $e.message")
        }
    }
}