package her.gradle.plugin.doctools.enums


enum class Grade(val value: Int, val text: String) {
    UNKNOWN(0, "未知"),
    LEVEL_1(19, "一年级"),
    LEVEL_2(18, "二年级"),
    LEVEL_3(17, "三年级"),
    LEVEL_4(16, "四年级"),
    LEVEL_5(15, "五年级"),
    LEVEL_6(14, "六年级"),
    LEVEL_7(9, "初一"),
    LEVEL_8(8, "初二"),
    LEVEL_9(7, "初三"),
    LEVEL_9_OF54(20, "初四"),
    LEVEL_H_1(5, "高一"),
    LEVEL_H_2(4, "高二"),
    LEVEL_H_3(3, "高三"),
    ;

    override fun toString(): String {
        return "[$value]$text"
    }

    companion object {
        fun of(value: Int): Grade = values().firstOrNull { it.value == value } ?: UNKNOWN
    }
}
