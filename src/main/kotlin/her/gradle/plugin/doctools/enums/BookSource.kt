package her.gradle.plugin.doctools.enums

enum class BookSource(val value: Int, val text: String) {
    UNKNOWN(-1, "未知"),
    OLD(0, "最早的"),
    YJ(1, "备课与自主预习"),
    YHFA(2, "优化方案"),
    TK(3, "91淘课"),
    ZHX(4, "题库"),
    ;

    companion object {
        fun of(value: Int): BookSource = values().firstOrNull { it.value == value } ?: UNKNOWN
    }
}
