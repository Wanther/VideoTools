package her.gradle.plugin.doctools

import her.gradle.plugin.doctools.components.Db
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File


open class SqlExec : DefaultTask() {
    @get:Input
    var db: String = "default"

    @get:Input
    lateinit var sql: Any

    @TaskAction
    fun execute() {

        val sqls = getSqlStatements()

        Db.use(db).withConnection {connection->
            connection.autoCommit = false
            connection.createStatement().use{statement->
                sqls.forEach {sql->
                    logger.quiet(sql)
                    statement.addBatch(sql)
                }
                statement.executeBatch()
            }
            connection.commit()
        }
    }

    private fun getSqlStatements(): List<String> {

        var sqlString: String = when (sql) {
            is File -> (sql as File).readText()
            is String -> sql as String
            else -> throw Exception("sql parameter type ${sql::class.java} not supported")
        }

        sqlString = sqlString.replace("{projectName}", project.name)

        return sqlString.split(";").map{it.trim()}.filter{it.isNotBlank()}
    }

}
