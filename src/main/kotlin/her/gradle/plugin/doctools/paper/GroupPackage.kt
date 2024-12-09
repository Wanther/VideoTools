package her.gradle.plugin.doctools.paper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.models.Doc

open class GroupPackage : DefaultTask() {
    /**
     * use attr5  --> parentId
     */
    @TaskAction
    fun execute() {
        Db.withInstance { sql ->
            val zipDocs = sql.rows("select * from ${project.name}_doc where format='zip'") { Doc.create(it) }
            val docs = sql.rows("select * from ${project.name}_doc where status>=0 and path like '%-unzipped%'") { Doc.create(it) }

            docs.forEach { doc ->
                val zip = zipDocs.first { it.path == doc.path!!.substring(0, doc.path!!.indexOf("-unzipped")) + ".zip" }
                doc.attr5 = zip.id!!.toString()
                logger.quiet("group doc[${doc.id}] to zip[${zip.id}]")
            }

            sql.withBatch(2000, "update ${project.name}_doc set attr5=? where id=?") { ps ->
                docs.filter { it.attr5 != null }.forEach { doc ->
                    ps.setLong(1, doc.attr5!!.toLong())
                    ps.setLong(2, doc.id!!)
                    ps.addBatch()
                }
            }
        }
    }
}