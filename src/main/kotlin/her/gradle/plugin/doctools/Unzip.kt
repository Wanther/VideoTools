package her.gradle.plugin.doctools

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.Charset


/**
 * 解压 srcDir下所有zip
 */
open class Unzip : DefaultTask() {

    @get:Input
    var useNative: Boolean = false

    @get:Input
    var useCp936: Boolean = false

    @get:Input
    var deleteBroken: Boolean = false

    @get:InputDirectory
    var srcDir: File? = null
        get() = field ?: getExtension(project).srcDir

    @TaskAction
    fun execute() {
        project.fileTree(srcDir!!).filter{it.name.endsWith(".zip")}.forEach {file->
            val unzipDir = File(file.parentFile!!, file.name.replace(".zip", "-unzipped"))

            try {
                if (useNative) {
                    nativeUnzip(file, unzipDir)
                } else {
                    gradleUnpack(file, unzipDir)
                }
            } catch (e: Exception) {
                if (deleteBroken) {
                    logger.quiet("delete broken $file")
                    project.delete(unzipDir)
                    project.delete(file)
                } else {
                    throw e
                }
            }

        }
    }

    private fun nativeUnzip(file: File, unzipDir: File) {
        val osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            expandArchive(file, unzipDir)
        } else {
            unzip(file, unzipDir)
        }
    }

    private fun unzip(file: File, unzipDir: File) {
        val commandLine = mutableListOf("unzip", "-d", unzipDir.absolutePath, file.absolutePath)

        if (useCp936) {
            commandLine.addAll(1, listOf("-O", "cp936"))
        }

        logger.quiet(commandLine.joinToString(" "))
        project.exec {
            setCommandLine(commandLine)
        }
    }

    private fun gradleUnpack(file: File, unzipDir: File) {
        println(file.path)
        project.copy {
            from(project.zipTree(file))
            into(unzipDir)
        }
    }

    private fun expandArchive(file: File, unzipDir: File) {
        val source = file.absolutePath
        val dest = unzipDir.absolutePath

        val commandLine = listOf("expand-archive", source, "-destinationpath", dest)

        logger.quiet(commandLine.joinToString(" "))

        project.exec {
            setCommandLine(commandLine)
        }
    }
}
