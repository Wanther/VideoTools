package her.gradle.plugin.doctools.paper


enum class PaperKind(val value: Int, val text: String, val reg: Regex? = null) {
    UNKNOWN(0, "未知"),
    KIND_1(1, "开学考", ".*开学考.*".toRegex()),
    KIND_2(2, "月考", ".*月考.*|.*[一二三]轮.*|.*选修[一二三四五].*|.*专题.*|.*专项.*".toRegex()),
    KIND_3(3, "期中", ".*期中.*".toRegex()),
    KIND_4(4, "期末", ".*期末.*".toRegex()),
    KIND_5(5, "联考", ".*联考.*".toRegex()),
    KIND_6(6, "教学诊断", ".*诊断.*".toRegex()),
    KIND_7(7, "质量检测", ".*质量.*|.*每日.*|.*单元.*|.*第.+章.*|.*模拟.*".toRegex()),
    KIND_8(8, "会考", ".*会考.*".toRegex()),
    KIND_9(9, "学业水平考试", ".*学业.*".toRegex()),
    KIND_10(10, "真题", ".*真题.*".toRegex()),
    ;

    override fun toString(): String {
        return "[$value]$text"
    }

    companion object {
        fun of(value: Int): PaperKind = values().firstOrNull { it.value == value } ?: PaperKind.UNKNOWN
    }
}
