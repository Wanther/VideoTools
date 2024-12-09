package her.gradle.plugin.doctools.components.video.ffmpeg

import java.io.File

import her.gradle.plugin.doctools.components.video.*


open class VideoToolFFmpeg(
    protected val ffmpeg: String = "ffmpeg",
    protected val ffprobe: String = "ffprobe"
) : VideoTool {

    protected open val videoDecoder: Decoder
        get () = DecoderDefault()
    protected open val audioEncoder: Encoder
        get () = AudioEncoder()
    protected open val videoFilter: Filter
        get () = VideoFilterDefault()
    protected open val videoEncoder: Encoder
        get () = VideoEncoder()
    protected open val muxer: Muxer
        get () = ComposedMuxer()

    override fun getVideoInfo(uri: String): VideoInfo {
        val command = listOf(
            ffprobe,
            "-hide_banner",
            "-v", "quiet",
            "-show_format",
            "-show_streams",
            "-print_format", "json",
            "-i", uri
        )
        val infoJson = execCommand(command)
        
        infoJson ?: throw RuntimeException("getVideoInfo Failed: $uri")

        return VideoInfo.create(infoJson)
    }

    override fun videoThumb(uri: String, output: File, time: String) {
        val command = listOf(
            ffmpeg,
            "-hide_banner",
            "-loglevel", "warning",
            "-y",
            "-ss", time,
            "-i", uri,
            "-frames:v", "1",
            "-s", "hqvga",
            output.absolutePath
        )
        if (!output.parentFile.exists()) output.parentFile.mkdirs()
        execCommand(command)
    }

    override fun convert(source: VideoInfo, outputs: List<ConvertOutput>) {

        val command = mutableListOf(ffmpeg)

        command.addAll(getOptions(source, outputs))

        command.addAll(getInfileOptions(source, outputs))

        command.add("-i")
        command.add(source.filename)

        outputs.forEach { output ->

            command.addAll(getOutfileOptions(source, output))

            val outputFile = File(output.uri)
            if (!outputFile.parentFile.exists()) {
                outputFile.parentFile.mkdirs()
            }
        }

        execCommand(command, true, false)
    }

    protected open fun getOptions(source: VideoInfo, outputs: List<ConvertOutput>): List<String> = listOf(
        "-hide_banner",
        "-loglevel", "warning",
        "-y",
        "-vsync", "passthrough"
    )

    protected open fun getInfileOptions(source: VideoInfo, outputs: List<ConvertOutput>) = videoDecoder.getOptions(source)

    protected open fun getOutfileOptions(source: VideoInfo, output: ConvertOutput): List<String> {
        val options = mutableListOf<String>()

        if (source.videoStream == null || output.template.video == null) {
            options.add("-vn")
        } else {
            videoFilter.getOptions(source, output.template)?.let {
                options.add("-vf")
                options.add(it)
            }

            options.addAll(videoEncoder.getOptions(source, output.template))
        }

        if (source.audioStream == null || output.template.audio == null) {
            options.add("-an")
        } else {
            options.addAll(audioEncoder.getOptions(source, output.template))
        }

        options.addAll(muxer.getOptions(source, output))

        return options
    }

    protected fun execCommand(command: List<String>, printInput: Boolean = false, printResult: Boolean = false): String? {

        if (printInput && printResult) println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")

        if (printInput) {
            println(buildString {
                command.forEachIndexed { index, c ->
                    if (index == 0) append(c)
                    else {
                        append(" '")
                        append(c)
                        append("'")
                    }
                }
            })
        }

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        if (printInput && printResult) println("------------------------------------------------------")

        try {
            val result = process.inputStream?.bufferedReader()?.useLines { lines -> buildString { lines.forEach {
                append(it)
                if (printResult) {
                    println(it)
                }
            } } }

            val code = process.waitFor()

            if (printResult) println("<<<<<<<<<<<<<<<<<<<<<<<<--code:$code--<<<<<<<<<<<<<<<<<<<<<<<<<<")

            if (code != 0) {
                throw RuntimeException(result)
            }
            return result
        } finally {
            process.destroy()
        }
    }

}
