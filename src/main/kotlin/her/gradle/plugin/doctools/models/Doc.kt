package her.gradle.plugin.doctools.models

import java.io.File


data class Doc(
    var id: Long?,
    var path: String?,
    var format: String?,
    var size: Long = 0,
    var status: Int = 0,
    var shortPath: String? = null,
    var title: String? = null,
    var attr0: String? = null,
    var attr1: String? = null,
    var attr2: String? = null,
    var attr3: String? = null,
    var attr4: String? = null,
    var attr5: String? = null,
    var attr6: String? = null,
    var attr7: String? = null,
    var attr8: String? = null,
    var attr9: String? = null,
    var attr10: String? = null
) {
    companion object {
        fun create(file: File, srcDir: File): Doc {
            val relativePath = file.absoluteFile.relativeTo(srcDir).toString()
            val format = if (file.name.indexOf(".") > 0) {
                file.name.substring(file.name.lastIndexOf(".") + 1)
            } else {
                null
            }
            val size = file.length()
            return Doc(null, relativePath, format, size)
        }

        fun create(rs: java.sql.ResultSet): Doc {
            return Doc(
                id = rs.getLong("id"),
                path = rs.getString("path"),
                format = rs.getString("format"),
                size = rs.getLong("size"),
                status = rs.getInt("status"),
                shortPath = rs.getString("short_path"),
                title = rs.getString("title"),
                attr0 = rs.getString("attr0"),
                attr1 = rs.getString("attr1"),
                attr2 = rs.getString("attr2"),
                attr3 = rs.getString("attr3"),
                attr4 = rs.getString("attr4"),
                attr5 = rs.getString("attr5"),
                attr6 = rs.getString("attr6"),
                attr7 = rs.getString("attr7"),
                attr8 = rs.getString("attr8"),
                attr9 = rs.getString("attr9"),
                attr10 = rs.getString("attr10")
            )
        }
    }
}
