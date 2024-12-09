package her.gradle.plugin.doctools.models

import her.gradle.plugin.doctools.components.Db

data class Location(
    var id: Int?,
    var type: Int?,
    var name: String?,
    var parentId: Int?,
    var leftId: Int?,
    var rightId: Int?,
    var treeId: Int?,
    var level: Int
) {
    companion object {
        fun create(rs: java.sql.ResultSet): Location {
            return Location(
                rs.getInt("id"),
                rs.getInt("type"),
                rs.getString("name"),
                rs.getInt("parent_id"),
                rs.getInt("left_id"),
                rs.getInt("right_id"),
                rs.getInt("tree_id"),
                rs.getInt("level")
            )
        }

        fun findAll(): List<Location> = Db.withResult("SELECT * FROM location") { Location.create(it) }

    }
    
    fun findParents(datasource: List<Location>? = null): List<Location> {
        if (datasource == null) {
            return Db.withResult("select * from location where tree_id=$treeId and left_id<=$leftId and right_id>=$rightId order by left_id") { Location.create(it) }
        }
        return datasource.filter { it.treeId == treeId && it.leftId!! <= leftId!! && it.rightId!! >= rightId!! }.sortedBy { it.leftId!! }
    }

    fun toDisplayString(): String = "[$id]$name"
}
