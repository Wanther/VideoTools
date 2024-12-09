package her.gradle.plugin.doctools.components.video.ffmpeg

import java.io.File

import her.gradle.plugin.doctools.components.video.*


interface Muxer {
    fun getOptions(source: VideoInfo, output: ConvertOutput): List<String>
}


class ComposedMuxer : Muxer {
    val muxers: Map<MuxerFormat, Muxer> = mapOf(
        MuxerFormat.MP4 to MuxerMP4(),
        MuxerFormat.HLS to MuxerHLS()
    )

    override fun getOptions(source: VideoInfo, output: ConvertOutput): List<String> = muxers[output.template.format]!!.getOptions(source, output)
}


class MuxerMP4 : Muxer {
    override fun getOptions(source: VideoInfo, output: ConvertOutput): List<String> = listOf(
        "-f", "mp4",
        "-movflags", "+faststart",
        output.uri
    )
}


class MuxerHLS : Muxer {

    override fun getOptions(source: VideoInfo, output: ConvertOutput): List<String> {

        val fragmentPrefix = output.uri.substring(0, output.uri.lastIndexOf("."))

        return listOf(
            "-f", "hls",
            "-hls_time", (output.template.advanced?.fragmentDuration ?: 10).toString(),
            "-hls_list_size", "0",
            "-hls_segment_filename", "$fragmentPrefix-%05d.ts",
            output.uri
        )
    }
}