package her.gradle.plugin.doctools.components.video.ffmpeg

import her.gradle.plugin.doctools.components.video.*


class VideoToolFFmpegNvidia(ffmpeg: String = "ffmpeg", ffprobe: String = "ffprobe") : VideoToolFFmpeg(ffmpeg, ffprobe) {

    override val videoDecoder: Decoder
        get () = ComposedDecoder()
    override val videoFilter: Filter
        get () = VideoFilterNvidia()
    override val videoEncoder: Encoder
        get () = VideoEncoderH264Nvenc()

}