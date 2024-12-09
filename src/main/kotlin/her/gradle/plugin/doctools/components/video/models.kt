package her.gradle.plugin.doctools.components.video

import org.json.JSONObject
import org.json.JSONArray


data class VideoInfo (
    val duration: Int,
    val filename: String,
    val videoStream: VideoStream?,
    val audioStream: AudioStream?
) {

    data class VideoStream(
        val codecName: String,
        val width: Int,
        val height: Int,
        val fps: Float,
        val bitRate: Int
    ) {
        companion object {
            fun create(json: JSONObject): VideoStream {
                val codecName = json.getString("codec_name")
                val width = json.getInt("width")
                val height = json.getInt("height")
                val fpsString = json.getString("r_frame_rate")
                val fps = if (fpsString.indexOf("/") > 0) {
                    fpsString.split("/")[0].toFloat() / fpsString.split("/")[1].toFloat()
                } else {
                    fpsString.toFloat()
                }
                val bitRate = json.getInt("bit_rate")
                return VideoStream(
                    codecName,
                    width,
                    height,
                    fps,
                    bitRate
                )
            }
        }
    }

    data class AudioStream(
        val codecName: String,
        val bitRate: Int,
        val sampleRate: Int,
        val channels: Int
    ) {
        companion object {
            fun create(json: JSONObject): AudioStream {
                return AudioStream(
                    json.getString("codec_name"),
                    json.getInt("bit_rate"),
                    json.getInt("sample_rate"),
                    json.getInt("channels")
                )
            }
        }
    }

    companion object {
        fun create(jsonString: String): VideoInfo {
            val json = try {
                JSONObject(jsonString)
            } catch (e: Exception) {
                throw RuntimeException("jsonString=$jsonString")
            }

            val formatJson = json.getJSONObject("format")
            val streamsJson = json.getJSONArray("streams")

            val duration = formatJson.getFloat("duration").toInt()
            val filename = formatJson.getString("filename")

            var videoStream: VideoStream? = null
            var audioStream: AudioStream? = null
            streamsJson.forEach { streamJson ->
                streamJson as JSONObject

                when(val codecType = streamJson.optString("codec_type")) {
                    "video" -> { 
                        val vstream = VideoStream.create(streamJson)
                        if (videoStream == null || videoStream!!.width < vstream.width && videoStream!!.height < vstream.height) {
                            videoStream = vstream
                        }
                    }
                    "audio" -> {
                        val astream = AudioStream.create(streamJson)
                        if (audioStream == null || audioStream!!.channels < astream.channels) {
                            audioStream = AudioStream.create(streamJson)
                        }
                    }
                    else -> throw Exception("unknown stream of codec type $codecType")
                }
            }

            return VideoInfo(duration, filename, videoStream, audioStream)
        }
    }

}

enum class MuxerFormat(val extension: String) {
    MP4("mp4"), HLS("m3u8")
}

enum class VideoCodec {
    H264
}

enum class AudioCodec {
    AAC
}

enum class CodecLevel {
    BASELINE, MAIN, HIGH
}

data class VideoParams(
    val codec: VideoCodec = VideoCodec.H264,
    val profile: CodecLevel = CodecLevel.MAIN,
    val bitRate: Int = 900 * 1024,
    val width: Int = 960,
    val height: Int = 540,
    val fps: Int = 25,
    val gop: Int = 250
)

data class AudioParams(
    val codec: AudioCodec = AudioCodec.AAC,
    val sampleRate: Int = 44100,
    val bitRate: Int = 96 * 1024,
    val channels: Int = 2
)

data class AdvancedParams(
    // 单位（秒）
    val fragmentDuration: Int = 10
)

data class ConvertTemplate(
    val name: String,
    val descText: String? = null,
    val format: MuxerFormat = MuxerFormat.MP4,
    val video: VideoParams? = null,
    val audio: AudioParams? = null,
    val advanced: AdvancedParams? = null
) {
    fun toHLS(advanced: AdvancedParams = AdvancedParams()): ConvertTemplate = copy(format = MuxerFormat.HLS, advanced = advanced)

    companion object {
        val NHD = ConvertTemplate(
            name = "nhd",
            descText = "流畅",
            video = VideoParams(
                bitRate = 400 * 1024,
                width = 640,
                height = 360,
            ),
            audio = AudioParams(bitRate = 64 * 1024)
        )
        val QHD = ConvertTemplate(name = "qhd", descText = "标清", video = VideoParams(), audio = AudioParams())
        val HD = ConvertTemplate(
            name = "hd",
            descText = "高清",
            video = VideoParams(
                profile = CodecLevel.HIGH,
                bitRate = 1500 * 1024,
                width = 1280,
                height = 720
            ),
            audio = AudioParams(bitRate = 128 * 1024)
        )
        val FHD = ConvertTemplate(
            name = "fhd",
            descText = "超清",
            video = VideoParams(
                profile = CodecLevel.HIGH,
                bitRate = 3000 * 1024,
                width = 1920,
                height = 1080
            ),
            audio = AudioParams(bitRate = 160 * 1024)
        )
    }
}

data class ConvertOutput (
    val template: ConvertTemplate,
    var uri: String
)