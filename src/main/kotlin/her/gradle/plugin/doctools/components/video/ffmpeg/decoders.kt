package her.gradle.plugin.doctools.components.video.ffmpeg

import her.gradle.plugin.doctools.components.video.*


interface Decoder {
    fun support(codecName: String): Boolean
    fun getOptions(source: VideoInfo): List<String>
}

class ComposedDecoder : Decoder {
    val decoders = listOf(DecoderCuvid(), DecoderDefault())

    override fun support(codecName: String): Boolean = true
    override fun getOptions(source: VideoInfo): List<String> {
        val decoder = decoders.firstOrNull { it.support(source.videoStream?.codecName ?: "-") }

        return decoder?.getOptions(source) ?: emptyList()
    }
}

class DecoderDefault : Decoder {
    override fun support(codecName: String): Boolean = true
    override fun getOptions(source: VideoInfo): List<String> = emptyList()
}

class DecoderCuvid (val fallbackDecoder: Decoder = DecoderDefault()) : Decoder {
    companion object {
        val FORMAT_DECODERS = mapOf(
            "h264" to "h264_cuvid",
            "mpeg1video" to "mpeg1_cuvid",
            "mpeg2video" to "mpeg2_cuvid",
            "mpeg4" to "mpeg4_cuvid",
            "vc1" to "vc1_cuvid",
            "vp8" to "vp8_cuvid",
            "vp9" to "vp9_cuvid",
            "hevc" to "hevc_cuvid"
        )
    }

    override fun support(codecName: String): Boolean = FORMAT_DECODERS.keys.contains(codecName)

    override fun getOptions(source: VideoInfo): List<String> {
        val codecName = FORMAT_DECODERS[source.videoStream?.codecName]

        codecName ?: throw RuntimeException("codec not support: codecName=${source.videoStream?.codecName}")

        return listOf("-hwaccel", "cuvid", "-c:v", codecName)
    }
}