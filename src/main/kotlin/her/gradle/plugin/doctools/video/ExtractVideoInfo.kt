package her.gradle.plugin.doctools.video

import java.io.ByteArrayOutputStream

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

import her.gradle.plugin.DocToolsPluginExtension
import her.gradle.plugin.doctools.components.*
import her.gradle.plugin.doctools.components.video.*
import her.gradle.plugin.doctools.models.*


open class ExtractVideoInfo : DefaultTask() {

    @get:Input
    var debug: Boolean = false
    
    @TaskAction
    fun execute() {
        Db.withInstance { sql ->
            val docs = sql.rows("select * from ${project.name}_doc where status>=0") { Doc.create(it) }
            docs.forEach { doc ->
                try {
                    val videoInfo = getVideoInfo(doc)
                    if (debug) {
                        logger.quiet("[${doc.id!!}]$videoInfo")
                    }

                    sql.withBatch(1000, "update ${project.name}_doc set attr3=?,attr4=?,attr5=?,attr6=?,attr7=?,attr8=?,attr9=? where id=?") { ps ->
                        ps.setInt(1, videoInfo.duration)
                        ps.setString(2, videoInfo.videoStream?.codecName)
                        ps.setInt(3, videoInfo.videoStream?.width ?: 0)
                        ps.setInt(4, videoInfo.videoStream?.height ?: 0)
                        ps.setInt(5, videoInfo.videoStream?.fps?.toInt() ?: 0)
                        ps.setInt(6, videoInfo.videoStream?.bitRate ?: 0)
                        ps.setInt(7, videoInfo.audioStream?.bitRate ?: 0)
                        ps.setLong(8, doc.id!!)

                        ps.addBatch()
                    }
                } catch (e: Exception) {
                    logger.error("[${doc.id!!}] failed, $e")
                }
            }
        }
    }

    private fun getVideoInfo(doc: Doc): VideoInfo {
        val extension = project.extensions.getByType(DocToolsPluginExtension::class.java)

        val videoTool = extension.videoTool

        return videoTool.getVideoInfo(extension.srcDir.resolve(doc.path!!).toString())
    }
}
