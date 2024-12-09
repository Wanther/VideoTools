package her.gradle.plugin.doctools

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input

import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.models.Doc
import her.gradle.plugin.doctools.enums.Subject
import her.gradle.plugin.doctools.enums.Period
import her.gradle.plugin.doctools.enums.Grade


open class GuessCommonAttr : DefaultTask() {

	companion object {
		val PERIOD_MATCHING = mapOf(
			Period.PRIMARY to Regex(".*小学.*"),
			Period.JUNIOR to Regex(".*初中.*"),
			Period.SENIOR to Regex(".*高中.*")
		)

		val GRADE_MATCHING = mapOf(
			Grade.LEVEL_1 to Regex(".*小学.*一年级.*"),
			Grade.LEVEL_2 to Regex(".*小学.*二年级.*"),
			Grade.LEVEL_3 to Regex(".*小学.*三年级.*"),
			Grade.LEVEL_4 to Regex(".*小学.*四年级.*"),
			Grade.LEVEL_5 to Regex(".*小学.*五年级.*"),
			Grade.LEVEL_6 to Regex(".*小学.*六年级.*"),
			Grade.LEVEL_7 to Regex(".*(七年级|初一).*"),
			Grade.LEVEL_8 to Regex(".*(八年级|初二).*"),
			Grade.LEVEL_9 to Regex(".*(九年级|初三|[^期]中考).*"),
			Grade.LEVEL_H_1 to Regex(".*(高一|必修[一二12]).*"),
			Grade.LEVEL_H_2 to Regex(".*(高二|必修[三四34]).*"),
			Grade.LEVEL_H_3 to Regex(".*(高三|高考).*")
		)
	}

    @get:Input
    var unusedKeywords: Array<String> = emptyArray()
    
    @get:Input
    var unusedKeywordPatterns: Array<Regex> = emptyArray()

    @TaskAction
    fun execute() {
        Db.withInstance { sql ->
            val docs = sql.rows("select * from ${project.name}_doc where status>=0") { Doc.create(it) }

            sql.withBatch(1000, "update ${project.name}_doc set title=?, attr0=?, attr1=?, attr2=? where id=?") { ps ->

                docs.forEach {doc->
                    val title = guessTitle(doc)
                    val subject = guessSubject(doc)
                    val grade = guessGrade(doc)
                    val period = guessPeriod(doc)

                    ps.setString(1, title)
                    ps.setString(2, subject?.toString())
                    ps.setString(3, grade?.toString())
                    ps.setString(4, period?.toString())
                    ps.setLong(5, doc.id!!)

                    ps.addBatch()
                }
            }
        }

    }

    private fun guessTitle(doc: Doc): String {
        var title = File(doc.path!!).name

        if (doc.format != null) {
            title = title.replace(".${doc.format!!}", "")
        }

        unusedKeywords.forEach { it ->
            title = title.replace(it, "")
        }

        unusedKeywordPatterns.forEach { it ->
            title = title.replace(it, "")
        }

        return title
    }

    private fun guessSubject(doc: Doc): Subject? {
        return Subject.values().firstOrNull {doc.path!!.contains(it.text)}
    }

    private fun guessGrade(doc: Doc): Grade? {
        return GRADE_MATCHING.entries.firstOrNull {it.value.matches(doc.path!!)}?.key
	}

    private fun guessPeriod(doc: Doc): Period? {
        return PERIOD_MATCHING.entries.firstOrNull {it.value.matches(doc.path!!)}?.key
	}

}
