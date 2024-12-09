package her.gradle.plugin.doctools


import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.models.Doc
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


open class SetShortPath : DefaultTask() {

    @TaskAction
    fun execute() {
        Db.withInstance { sql ->
            val docs = sql.rows("select * from ${project.name}_doc") { Doc.create(it) }

            val root = PathNode("root")

            sql.withBatch(1000, "update ${project.name}_doc set short_path = ? where id=?") { ps ->
                docs.forEach {doc ->
                    doc.shortPath = nextShortPath(doc, root)

                    ps.setString(1, doc.shortPath!!)
                    ps.setLong(2, doc.id!!)
                    ps.addBatch()
                }
            }
        }
    }

    fun printNode(node: PathNode, level: Int) {
        println(buildString {
            (0 until level).forEach { append("--") }
            append(node.shortName)
        })

        node.children.forEach { child ->
            printNode(child, level + 1)
        }
    }

    fun nextShortPath(doc: Doc, root: PathNode, format: String? = null): String {
        var current: PathNode? = root

        doc.path!!.split("/", "\\").forEach { pathFragment ->
            current = current!!.addOrGetChild(pathFragment)
        }

        return buildString {
            while (current != root) {
                if (current?.children?.size != 0) {
                    insert(0, "/")
                }
                insert(0, abcz(current!!.parent!!.children.size))
                current = current?.parent
            }

            if (format != null) {
                append(".")
                append(format.toLowerCase())
            }
        }
    }

    class PathNode (val name: String) {
        var shortName: String? = null
        var parent: PathNode? = null
        val children = mutableListOf<PathNode>()

        fun addOrGetChild(childName: String): PathNode {
            var child = children.firstOrNull { it.name == childName }
            if (child == null) {
                child = PathNode(childName)
                children.add(child)
                child.parent = this
            }
            return child
        }

    }
}
