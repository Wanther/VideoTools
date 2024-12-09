package her.gradle.plugin.doctools.components.video.ffmpeg

import  her.gradle.plugin.doctools.components.video.*


interface Filter {
    fun getOptions(source: VideoInfo, template: ConvertTemplate): String?
}

open class FilterGraph(val filters: List<Filter>) : Filter {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): String? {
        val options = mutableListOf<String>()

        filters.forEach { 
            val filterExpresion = it.getOptions(source, template)
            if (filterExpresion != null) options.add(filterExpresion)
         }

        return if (options.isEmpty()) null else options.joinToString(separator = ",")
    }
}

class VideoFilterDefault : FilterGraph(listOf(VideoFilterFps(), VideoFilterScale()))
class VideoFilterNvidia : FilterGraph(listOf(VideoFilterFps(), VideoFilterHwUploadCuda(), VideoFilterScaleNpp()))

class VideoFilterFps : Filter {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): String? {
        if (source.videoStream == null || template.video == null || source.videoStream.fps.toInt() <= template.video.fps) return null
        return "fps=fps=${template.video.fps}"
    }
}

class VideoFilterHwUploadCuda : Filter {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): String? {
        return if (DecoderCuvid.FORMAT_DECODERS[source.videoStream?.codecName] == null) "hwupload_cuda" else null
    }
}

open class VideoFilterScale : Filter {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): String? {
        val dimision = getDimision(source.videoStream?.width ?: 0, source.videoStream?.height ?: 0, template.video?.width ?: 0, template.video?.height ?: 0)

        return if (dimision == null) null else "scale=w=${dimision.first}:h=${dimision.first}"
    }

    protected fun getDimision(iw: Int, ih: Int, ow: Int, oh: Int): Pair<Int, Int>? {
        if (ow == 0 || oh == 0) return null

        val needRotate = ow > 0 && oh > 0 && (iw > ih && ow < oh || iw < ih && ow > oh)
        val ooh = if (needRotate) ow else oh
        val oow = if (needRotate) oh else ow

        if (oow > 0 && iw >= oow || ooh > 0 && ih >= ooh) {
            if (oow < 0 || ooh < 0) return (oow to ooh)
            val scale = Math.min(Math.abs(oow.toFloat() / iw), Math.abs(ooh.toFloat() / ih))
            return ((iw * scale).toInt() to (ih * scale).toInt())
        }

        return null
    }
}

class VideoFilterScaleNpp : VideoFilterScale() {
    override fun getOptions(source: VideoInfo, template: ConvertTemplate): String? {

        val dimision = getDimision(source.videoStream?.width ?: 0, source.videoStream?.height ?: 0, template.video?.width ?: 0, template.video?.height ?: 0)

        return if (dimision == null) null else "scale_npp=${dimision.first}:${dimision.second}:yuv420p:cubic,hwdownload,format=yuv420p"
        // return "scale_npp=${template.video!!.width}:${template.video!!.height}:yuv420p:cubic,hwdownload,format=yuv420p"
    }
}