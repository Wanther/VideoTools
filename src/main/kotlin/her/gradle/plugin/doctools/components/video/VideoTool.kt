package her.gradle.plugin.doctools.components.video

import java.io.File

import her.gradle.plugin.doctools.components.video.*


interface VideoTool {
    // 获取视频信息
    fun getVideoInfo(uri: String): VideoInfo
    // 生成视频缩略图
    fun videoThumb(uri: String, output: File, time: String = "10")
    // 视频转码
    fun convert(source: VideoInfo, outputs: List<ConvertOutput>)
}