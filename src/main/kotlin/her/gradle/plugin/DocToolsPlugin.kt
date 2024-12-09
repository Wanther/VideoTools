package her.gradle.plugin

import her.gradle.plugin.doctools.*
import her.gradle.plugin.doctools.components.Db
import her.gradle.plugin.doctools.components.video.VideoTool
import her.gradle.plugin.doctools.components.video.ffmpeg.VideoToolFFmpegNvidia
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File


open class DocToolsPluginExtension (project: Project) {

    lateinit var srcDir: File
    lateinit var outDir: File
    
    val db: Database = project.extensions.create("db", Database::class.java, project)
    val oss: Oss = project.extensions.create("oss", Oss::class.java, project)

    var ffmpeg: String = "ffmpeg"
    var ffprobe: String = "ffprobe"

    var dingtalkRobotUri: String = project.property("DINGTALK_ROBOT_URI") as String

    val videoTool: VideoTool by lazy {
        VideoToolFFmpegNvidia(ffmpeg, ffprobe)
    }

    abstract class Database (project: Project) {
        lateinit var url: String
        lateinit var driver: String
        var user: String = project.property("DB_USER") as String
        var password: String = project.property("DB_PASSWORD") as String
    }

    abstract class Oss (project: Project) {
        var accessKeyId: String = project.property("OSS_ACCESS_KEY_ID") as String
        var accessKeySecret: String = project.property("OSS_ACCESS_KEY_SECRET") as String
        lateinit var endpoint: String
        lateinit var bucket: String
        lateinit var dir: String
    }
}

open class DocToolsPlugin : Plugin<Project> {

    companion object {
        const val TASK_GROUP = "Doc Tools"

        const val EXTENSION_NAME = "docTools"
        const val TASK_INIT_TABLE = "initTable"
        const val TASK_CLEAR_TABLE = "clearTable"
        const val TASK_DROP_TABLE = "dropTable"
        const val TASK_MAKE_INDEX = "makeIndex"
        const val TASK_DOC_SUMMARY = "docSummary"
        const val TASK_GUESS_COMMON_ATTR = "guessCommonAttr"
        const val TASK_SET_SHORT_PATH = "setShortPath"
        const val TASK_UPLOAD_ALIOSS = "uploadAliOSS"
        const val TASK_MAKE_CATEGORY = "makeCategory"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, DocToolsPluginExtension::class.java, project)
        project.afterEvaluate {
            configDb(project)
        }
        configTasks(project)
    }

    private fun configDb(project: Project) {
        val extension = getExtension(project)
        Db.regist("default", extension.db.driver, extension.db.url, extension.db.user, extension.db.password)
    }

    private fun configTasks(project: Project) {
        project.tasks.register(TASK_INIT_TABLE, SqlExec::class.java) {
            group = TASK_GROUP
            description = "执行resources/init.sql, 创建项目表"
            sql = File("./db/init.sql")
        }

        project.tasks.register(TASK_CLEAR_TABLE, SqlExec::class.java) {
            group = TASK_GROUP
            description = "执行resources/clear.sql，清空表数据"
            sql = project.file("${project.projectDir}/resources/db/clear.sql")
        }

        project.tasks.register(TASK_DROP_TABLE, SqlExec::class.java) {
            group = TASK_GROUP
            description = "执行resources/drop.sql, 删除表"
            sql = project.file("${project.projectDir}/resources/db/drop.sql")
        }

        project.tasks.register(TASK_MAKE_INDEX, MakeIndex::class.java) {
            group = TASK_GROUP
            description = "将srcDir下文件索引到数据库"
        }

        project.tasks.register(TASK_DOC_SUMMARY, DocSummary::class.java) {
            group = TASK_GROUP
            description = "统计status>=0的各类型文档数量和大小"
        }

        project.tasks.register(TASK_GUESS_COMMON_ATTR, GuessCommonAttr::class.java) {
            group = TASK_GROUP
            description = "根据文件路径猜测学科(attr0)、年级(attr1)、学段(attr2)属性"
        }

        project.tasks.register(TASK_SET_SHORT_PATH, SetShortPath::class.java) {
            group = TASK_GROUP
            description = "根据文件目录生成简路径"
        }

        project.tasks.register(TASK_MAKE_CATEGORY, MakeCategory::class.java) {
            group = TASK_GROUP
            description = "根据文件目录生成分类树"
        }

        project.tasks.register(TASK_UPLOAD_ALIOSS, UploadAliOSS::class.java) {
            group = TASK_GROUP
            description = "上传到阿里云OSS"
        }
    }
}

