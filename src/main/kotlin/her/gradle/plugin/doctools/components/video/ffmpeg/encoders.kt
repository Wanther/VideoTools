package her.gradle.plugin.doctools.components.video.ffmpeg

import her.gradle.plugin.doctools.components.video.*


interface Encoder {
    fun getOptions(source: VideoInfo, template: ConvertTemplate): List<String>
}

class AudioEncoder : Encoder {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): List<String> {
        if (source.audioStream == null || template.audio == null) {
            return emptyList()
        }

        if (source.audioStream.codecName == template.audio.codec.name.toLowerCase()
            && source.audioStream.sampleRate <= template.audio.sampleRate
            && source.audioStream.bitRate <= template.audio.bitRate) {
            return listOf("-c:a", "copy")
        }

        return listOf(
            "-c:a", "aac",
            "-b:a", Math.min(source.audioStream.bitRate, template.audio.bitRate).toString(),
            "-ar", Math.min(source.audioStream.sampleRate, template.audio.sampleRate).toString()
        )
    }
}

class VideoEncoder : Encoder {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): List<String> {
        if (source.videoStream == null || template.video == null) return emptyList()

        val options = mutableListOf<String>()

        options.add("-b:v")
        options.add(Math.min(source.videoStream.bitRate, template.video.bitRate).toString())

        return options
    }
}

class VideoEncoderH264Nvenc : Encoder {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): List<String> {
        if (source.videoStream == null || template.video == null) return emptyList()

        val options = mutableListOf(
            "-c:v", "h264_nvenc",
            "-preset:v", "slow",
            "-profile:v", template.video.profile.name.toLowerCase(),
            "-rc-lookahead", "20",
            "-b:v", Math.min(source.videoStream.bitRate, template.video.bitRate).toString()
        )

        return options
    }
}