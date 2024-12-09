package her.gradle.plugin.doctools

import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import her.gradle.plugin.DocToolsPluginExtension
import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.enums.DocStatus
import her.gradle.plugin.doctools.models.Doc
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


open class UploadAliOSS : DefaultTask() {

    @get:Input
    var taskQuery: String = "select * from ${project.name}_doc where status=0 limit 1"

    @get:Input
    var uploadAction: (Doc, OSS, DocToolsPluginExtension) -> Unit = { doc, oss, extension ->
        logger.quiet("[${doc.id!!}]${extension.oss.dir}/${doc.shortPath!!}.${doc.format?.toLowerCase()}")
        oss.putObject(extension.oss.bucket, "${extension.oss.dir}/${doc.shortPath!!}.${doc.format?.toLowerCase()}", extension.srcDir.resolve(doc.path!!))
    }

    @TaskAction
    fun execute() {
        upload(uploadAction)
        sendDingtalkMessage("上传完成", project)
    }

    private fun upload(action: (Doc, OSS, DocToolsPluginExtension) -> Unit) {
        val extension = getExtension(project)

        val db = Db.use("default")

        val ossClient = OSSClientBuilder().build(extension.oss.endpoint, extension.oss.accessKeyId, extension.oss.accessKeySecret)

        UploadTasks(db, taskQuery, project).forEach { doc ->
            var status = DocStatus.UPLOADED.value

            try {
                action(doc, ossClient, extension)
            } catch (e: Exception) {
                logger.error(e.message, e)
                status = DocStatus.UPLOAD_FAILED.value
            }

            db.withConnection { connection->
                connection.createStatement().use { statement ->
                    statement.executeUpdate("update ${project.name}_doc set status=$status where id=${doc.id!!}")
                }
            }
        }

        ossClient.shutdown()
    }

    class UploadTasks(private val db: Db, private val query: String, project: Project) : Iterator<Doc> {

        private var next: Doc? = null

        override fun next(): Doc {
            return next!!
        }

        override fun hasNext(): Boolean {
            next = db.withResultSet(query) { rs ->
                if (rs.next()) {
                    Doc.create(rs)
                } else {
                    null
                }
            }
            return next != null
        }
    }

}
