package her.gradle.plugin.doctools.models

import her.gradle.plugin.doctools.components.Db

data class Category(
    var id: Long?,
    var name: String,
    var treeId: Int,
    var parentId: Long = 0L
) {
    var parent: Category? = null
    var children: MutableList<Category> = mutableListOf()

    companion object {
        fun create(rs: java.sql.ResultSet): Category {
            return Category(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                treeId = rs.getInt("tree_id"),
                parentId = rs.getLong("pid")
            )
        }

        fun insert(category: Category, tableName: String): Category {
            category.id = Db.withInstance { sql->
                sql.insert("insert into $tableName(name, tree_id, pid) values(?,?,?)", true, { ps ->
                    ps.setString(1, category.name)
                    ps.setInt(2, category.treeId)
                    ps.setLong(3, category.parentId)
                })
            }
            return category
        }

        fun findByTreeId(treeId: Int, tableName: String): List<Category> {
            return Db.withResult("select * from $tableName where tree_id=$treeId") { create(it) }
        }

        fun buildTree(categories: List<Category>): List<Category> {
            categories.forEach { c1 ->
                categories.forEach { c2 ->
                    if (c1.id != c2.id && c1.id == c2.parentId) {
                        c2.parent = c1
                        c1.children.add(c2)
                    }
                }
            }

            return categories.filter { it.parentId == 0L }
        }

        fun printTree(nodes: List<Category>, level: Int = 0) {
            nodes.forEach { node ->
                println(buildString {
                    (0 until level).forEach { append("--") }
                    append("[${node.id}]${node.name}")
                })
                printTree(node.children, level + 1)
            }
        }
    }
}