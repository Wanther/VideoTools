package her.gradle.plugin.doctools.models

import her.gradle.plugin.doctools.components.Db


class Chapter(
    var id: Long?,
    var name: String,
    var parentId: Long = 0,
    var treeId: Int = 0,
    var leftId: Int = 0,
    var rightId: Int = 0,
    var level: Int = 0
) {
    var parent: Chapter? = null

    var children: MutableList<Chapter> = mutableListOf()

    fun isLeaf() = getChildrenCount() <= 0

    fun getChildrenCount() = (rightId - leftId) / 2

    fun visit(action: (Chapter) -> Unit) {
        action(this)
        children.forEach { child ->
            child.visit(action)
        }
    }

    companion object {
        fun create(rs: java.sql.ResultSet): Chapter {
            return Chapter(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("parent_id"),
                rs.getInt("tree_id"),
                rs.getInt("left_id"),
                rs.getInt("right_id"),
                rs.getInt("level")
            )
        }

        fun findById(id: Long): Chapter? {
            return Db.withResultSet("select * from book_hierarchy where id=$id") { rs ->
                if (rs.next()) {
                    create(rs)
                } else {
                    null
                }
            }
        }

        fun buildTree(chapter: Chapter, datasource: List<Chapter>? = null) {
            val desendents = datasource ?: Db.withResult("select * from book_hierarchy where tree_id=${chapter.treeId} and left_id>${chapter.leftId} and right_id<${chapter.rightId} order by left_id") { create(it) }

            desendents.forEach { d1 ->
                if (d1.parentId == chapter.id) {
                    chapter.children.add(d1)
                }
                desendents.forEach { d2 ->
                    if (d1 != d2 && d2.parentId == d1.id) {
                        d2.parent = d1
                        d1.children.add(d2)
                    }
                }
            }
        }

    }

}
