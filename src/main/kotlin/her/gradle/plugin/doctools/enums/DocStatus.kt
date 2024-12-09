package her.gradle.plugin.doctools.enums


enum class DocStatus(val value: Int, val text: String) {
    USELESS(-100, "无用的"),
    BROKEN_CANNOT_CONVERT(-91, "无法转码"),
    BROKEN(-90, "文件损坏"),
    ATTR_UNKNOWN(-80, "猜不出来属性"),
    UPLOAD_FAILED(-1, "上传失败"),
    INIT(0, "初始状态"),
    PROCESSED(1, "处理过的：转码过的"),
    UPLOADED(10, "已上传的")
    ;
}
