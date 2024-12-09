package her.gradle.plugin.doctools.models

import java.sql.ResultSet

import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.enums.*

data class Knowledge (
        var id: Long?,
        var name: String,
        var parentId: Long? = null,
        var treeId: Int = 0,
        var leftId: Int = 0,
        var rightId: Int = 0,
        var level: Int = 0
) {
    fun isLeaf() = getChildrenCount() <= 0

    fun getChildrenCount() = (rightId - leftId) / 2

    companion object {
        fun create(rs: ResultSet): Knowledge {
            val id = rs.getLong("id")
            val name = rs.getString("name")
            val parentId = rs.getLong("parent_id")
            val treeId = rs.getInt("tree_id")
            val leftId = rs.getInt("left_id")
            val rightId = rs.getInt("right_id")
            val level = rs.getInt("level")

            return Knowledge(id, name, parentId, treeId, leftId, rightId, level)
        }

        fun findById(id: Long): Knowledge? {
            return Db.withResultSet("select * from knowledge where id=$id") { rs ->
                if (rs.next()) {
                    create(rs)
                } else {
                    null
                }
            }
        }

        fun findBySubject(subject: Subject): List<Knowledge> {
            return Db.withResult("select * from knowledge where tree_id=${subject.value} order by left_id") { create(it) }
        }

        fun findBySubjectAndStage(subject: Subject, period: Period, official: Boolean = true): List<Knowledge> {
            var subjectStageRootName = period.text
            if (official) {
                subjectStageRootName = "official$subjectStageRootName"
            }
            return Db.withInstance { sql ->
                val subjectStageRoot = sql.query("select * from knowledge where tree_id=${subject.value} and level=1 and name='$subjectStageRootName'") { rs ->
                    if (rs.next()) {
                        create(rs)
                    } else {
                        null
                    }
                }
                if (subjectStageRoot == null) {
                    emptyList()
                } else {
                    sql.rows("select * from knowledge where tree_id=${subject.value} and left_id>=${subjectStageRoot.leftId} and right_id<=${subjectStageRoot.rightId}") { create(it) }
                }
            }!!
        }
    }
}