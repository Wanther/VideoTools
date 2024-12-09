package her.gradle.plugin.doctools.models

import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.enums.Subject
import her.gradle.plugin.doctools.enums.Grade
import her.gradle.plugin.doctools.enums.BookSource


class Book(
    var id: Long?,
    var name: String,
    var subject: Subject,
    var grade: Grade,
    var versionId: Int,
    var rootId: Long,
    var source: BookSource,
    var isActive: Boolean = true
) {

    var rootChapter: Chapter? = null

    val chapters: List<Chapter>
        get() {
            if (rootChapter == null) {
                loadChapters()
            }
            return rootChapter?.children ?: emptyList()
        }

    fun loadChapters() {

        if (rootChapter == null) {
            rootChapter = Chapter.findById(rootId)
        }

        rootChapter ?: return

        Chapter.buildTree(rootChapter!!)

    }

    fun visitChapter(action: (Chapter) -> Unit) {
        chapters.forEach { chapter ->
            chapter.visit(action)
        }
    }

    companion object {
        fun create(rs: java.sql.ResultSet): Book {
            return Book(
                rs.getLong("id"),
                rs.getString("name"),
                Subject.of(rs.getInt("subject_id")),
                Grade.of(rs.getInt("grade_id")),
                rs.getInt("version_id"),
                rs.getLong("root_id"),
                BookSource.of(rs.getInt("source")),
                rs.getBoolean("is_active")
            )
        }

        fun findById(id: Long): Book? {
            return Db.withResultSet("select * from book where id=$id limit 1") { rs ->
                if (rs.next()) {
                    create(rs)
                } else {
                    null
                }
            }
        }

        fun find(subject: Subject? = null, grade: Grade? = null, source: BookSource? = null, versionId: Int? = null, isActive: Boolean? = true): List<Book> {
            val sql = buildString {
                append("select * from book where 1=1")

                if (subject != null) {
                    append(" and subject_id=").append(subject.value)
                }

                if (grade != null) {
                    append(" and grade_id=").append(grade.value)
                }

                if (versionId != null) {
                    append(" and version_id=").append(versionId)
                }

                if (isActive != null) {
                    append(" and is_active=").append(if (isActive) 1 else 0)
                }

                if (source != null) {
                    append(" and source=").append(source.value)
                }
            }

            return Db.withResult(sql) { create(it) }
        }
    }
}
