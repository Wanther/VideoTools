package her.gradle.plugin.doctools

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.sizeDisplay


open class DocSummary : DefaultTask() {
    @TaskAction
    fun execute() {

        var totalSize = 0L

        Db.withConnection {connection->
            connection.createStatement().use {statement->

                println("format\tcount\tsize")
                println("------\t-----\t----------")

                statement.executeQuery("select format, count(1) cnt, sum(size) format_size from ${project.name}_doc where status>=0 group by format order by cnt desc").use {rs->
                    while(rs.next()) {
                        val format = rs.getString("format")
                        val count = rs.getLong("cnt")
                        val formatSize = rs.getLong("format_size")

                        totalSize += formatSize

                        println("$format\t$count\t${sizeDisplay(formatSize)}")
                    }
                }
                
            }
        }

        println("-------------------------------------")
        println("Total Size: ${sizeDisplay(totalSize)}")

    }
}
