package her.gradle.plugin.doctools


import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.models.Doc
import her.gradle.plugin.doctools.models.Category
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction


open class MakeCategory : DefaultTask() {

    @get:Input
    @get:Optional
    var tableName: String = "${project.name}_cate"
    @get:Input
    var treeId: Int = 1
    @get:Input
    var printOnly: Boolean = false

    @TaskAction
    fun execute() {
        val categoryTree = loadCurrentCategoryTree().toMutableList()

        if (!printOnly) {
            Db.withInstance { sql ->
                val docs = sql.rows("select * from ${project.name}_doc where status>=0") { Doc.create(it) }

                docs.forEach {doc ->
                    var path = doc.path!!
                    val lastSeperatorIndex = path.lastIndexOfAny(charArrayOf('/', '\\'))
                    if (lastSeperatorIndex > 0) {
                        path = path.substring(0, lastSeperatorIndex)
                    }
                    val category = pushCategory(path, categoryTree)
                    doc.attr10 = category.id!!.toString()
                }

                sql.withBatch(1000, "update ${project.name}_doc set attr10=? where id=?") { ps->
                    docs.forEach { doc ->
                        ps.setString(1, doc.attr10!!)
                        ps.setLong(2, doc.id!!)

                        ps.addBatch()
                    }
                }
            }
        }

        Category.printTree(categoryTree)
    }

    fun loadCurrentCategoryTree(): List<Category> {
        val categories = Category.findByTreeId(treeId, tableName)
        return Category.buildTree(categories)
    }

    fun pushCategory(path: String, currents: MutableList<Category>, parent: Category? = null): Category {
        val separatorIndex = path.indexOfAny(charArrayOf('/', '\\'))
        val name = if (separatorIndex < 0) path else path.substring(0, separatorIndex)
        
        var targetCategory = currents.firstOrNull { it.name == name }
        if (targetCategory == null) {
            targetCategory = Category.insert(Category(null, name, treeId, parent?.id ?: 0L), tableName)
            currents.add(targetCategory)
        }

        if (separatorIndex > 0) {
            return pushCategory(path.substring(separatorIndex + 1), targetCategory.children, targetCategory)
        }

        return targetCategory
    }
}
