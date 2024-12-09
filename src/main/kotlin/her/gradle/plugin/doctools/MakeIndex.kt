package her.gradle.plugin.doctools


import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.models.Doc
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File


open class MakeIndex : DefaultTask() {
    @get:InputDirectory
    var srcDir: File? = null
        get() = field ?: getExtension(project).srcDir

    @TaskAction
    fun execute() {
        Db.withInstance { sql->
            sql.withBatch(1000, "INSERT INTO ${project.name}_doc(path, format, size) VALUES(?,?,?)") { ps ->
                project.fileTree(srcDir!!).forEach { file->
                    val doc = Doc.create(file, srcDir!!)
                    ps.setString(1, doc.path)
                    ps.setString(2, doc.format)
                    ps.setLong(3, doc.size)
                    ps.addBatch()
                }
            }
        }
    }
}
