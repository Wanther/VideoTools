package her.gradle.plugin.doctools.paper

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.models.Doc
import her.gradle.plugin.doctools.models.Location


open class GuessPaperAttr : DefaultTask() {

    @TaskAction
    fun execute() {

        Db.withInstance { sql ->
            val locations = sql.rows("select * from location order by id desc") { Location.create(it) }
            val docs = sql.rows("select * from ${project.name}_doc where status>=0") {Doc.create(it)}


            sql.withBatch(1000, "update ${project.name}_doc set attr3=?, attr4=?, attr6=? where id=?") { ps ->
                docs.forEach {doc->
                    val paperKind = guessPaperKind(doc)
                    val year = guessYear(doc)
                    val province = guessProvince(doc, locations)

                    if (paperKind != null || year != null || province != null) {
                        ps.setString(1, paperKind?.toString())
                        ps.setString(2, province?.toDisplayString())
                        ps.setString(3, year)
                        ps.setLong(4, doc.id!!)
                        ps.addBatch()
                    }
                }
            }
        }

    }

    private fun guessPaperKind(doc: Doc): PaperKind? {
        return PaperKind.values().firstOrNull {kind->
            kind.reg?.matches(doc.path!!) == true
        }
    }

    private fun guessYear(doc: Doc): String? {
        return Regex(".*(20\\d{2})[^\\d].*").matchEntire(doc.path!!)?.groupValues?.getOrNull(1)
    }

    private fun guessProvince(doc: Doc, locations: List<Location>): Location? {
        val location = locations.firstOrNull { loc -> doc.path!!.contains(loc.name!!) }

        location ?: return null

        if (location.level <= 1) {
            return location
        }

        return location.findParents(locations).firstOrNull { it.level == 1 }
    }
}
