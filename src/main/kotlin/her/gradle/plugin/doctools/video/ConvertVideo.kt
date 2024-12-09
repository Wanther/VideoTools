package her.gradle.plugin.doctools.video

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

import her.gradle.plugin.DocToolsPluginExtension
import her.gradle.plugin.doctools.sendDingtalkMessage
import her.gradle.plugin.doctools.enums.DocStatus
import her.gradle.plugin.doctools.components.*
import her.gradle.plugin.doctools.components.video.*
import her.gradle.plugin.doctools.models.Doc

open class ConvertVideo : DefaultTask() {

    @get:Input
    var templates: List<ConvertTemplate> = emptyList()

    @TaskAction
    fun execute() {
        convert()
        sendDingtalkMessage("转码完成", project)
    }

    private fun convert() {
        val extension = project.extensions.getByType(DocToolsPluginExtension::class.java)
        val db = Db.use("default")

        ConvertTasks(db, project).forEach { doc ->

            var status = DocStatus.PROCESSED

            try {
                logger.quiet("[id=${doc.id}]")
                val source = extension.videoTool.getVideoInfo(extension.srcDir.resolve(doc.path!!).absolutePath)
                extension.videoTool.convert(source, templates.map { template -> ConvertOutput(template = template, uri = getOutputPath(extension.outDir.resolve(doc.shortPath!!).absolutePath, template)) })
            } catch (e: Exception) {
                logger.error("!!!!! doc.id=${doc.id}, message=${e.message}")
                status = DocStatus.BROKEN_CANNOT_CONVERT
            }

            db.withConnection { connection ->
                connection.createStatement().use { statement ->
                    statement.execute("update ${project.name}_doc set status=${status.value} where id=${doc.id!!}")
                }
            }

        }

    }

    private fun getOutputPath(shortPath: String, template: ConvertTemplate): String {
        val name = if (template.name.isNotBlank()) "-${template.name}" else ""
        return "$shortPath$name.${template.format.extension}"
    }

    class ConvertTasks(private val db: Db, project: Project): Iterator<Doc> {
        private val query: String = "select * from ${project.name}_doc where status=0 limit 1"

        private var next: Doc? = null

        override fun next(): Doc = next!!

        override fun hasNext(): Boolean {
            next = db.withResultSet(query) { rs -> if (rs.next()) Doc.create(rs) else null }
            return next != null
        }
    }
}
