package her.gradle.plugin.doctools.video

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

import her.gradle.plugin.doctools.getExtension
import her.gradle.plugin.doctools.replacePathExt
import her.gradle.plugin.doctools.components.*
import her.gradle.plugin.doctools.components.video.*
import her.gradle.plugin.doctools.models.*


open class ExtractThumb : DefaultTask() {

    @get:Input
    var time: String = "10" // 10s
    @get:Input
    var debug: Boolean = false
    
    @TaskAction
    fun execute() {
        val docs = Db.withResult("select * from ${project.name}_doc where status>=0") { Doc.create(it) }
        docs.forEach { doc ->
            saveThumb(doc)
        }
    }

    private fun saveThumb(doc: Doc) {
        val extension = getExtension(project)

        val videoTool = extension.videoTool

        val srcFile = extension.srcDir.resolve(doc.path!!)
        var outFile = File("${extension.outDir.resolve(doc.shortPath!!).absolutePath}.jpg")

        try {
            videoTool.videoThumb(srcFile.absolutePath, outFile, time)
        } catch (e: Exception) {
            logger.error("[${doc.id}]")
            throw e
        }
    }
}
