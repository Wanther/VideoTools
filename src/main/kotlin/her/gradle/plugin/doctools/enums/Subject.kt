package her.gradle.plugin.doctools.enums

enum class Subject(val value: Int, val text: String) {
    UNKNOWN(0, "未知"),
    CHINESE(3, "语文"),
    MATH(2, "数学"),
    ENGLISH(1, "英语"),
    PHYSICS(4, "物理"),
    CHEMISTRY(5, "化学"),
    BIOLOGY(9, "生物"),
    POLITICS(8, "政治"),
    HISTORY(7, "历史"),
    GEOGRAPHY(6, "地理"),
    SCIENCE(11, "科学"),
    SOCIETY(10, "历史与社会"),
    IT(13, "信息技术"),
    GT(16, "通用技术"),
    MUSIC(14, "音乐"),
    ART(15, "美术"),
    SPORT(18, "体育"),
    MORALITY(17, "品德"),
    QUALITY(12, "综合素质"),
    JAPANESE(19, "日语"),
    ;

    override fun toString(): String {
        return "[$value]$text"
    }


    companion object {
        fun of(value: Int): Subject = values().firstOrNull { it.value == value } ?: UNKNOWN
    }
}
