package her.gradle.plugin.doctools

import her.gradle.plugin.doctools.components.Db
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction


open class EzSqlExec : DefaultTask() {
    @get:Input
    var db: String = "default"

    @get:Input
    var tableName: String = "${project.name}_doc"

    @get:Input
    lateinit var set: String

    @get:Input
    @get:Optional
    var where: String? = null

    @TaskAction
    fun execute() {

        val sql = buildSql()

        Db.use(db).withConnection {connection->
            connection.createStatement().use{statement->
                logger.quiet(sql)
                statement.execute(sql)
            }
        }
    }

    private fun buildSql(): String {
        return buildString {
            append("UPDATE ").append(tableName)
            append(" SET ")
            append(set)
            if (where != null) {
                append(" WHERE ")
                append(where)
            }
        }
    }

}
