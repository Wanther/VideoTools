package her.gradle.plugin.doctools.enums


enum class Period(val value: Int, val text: String) {
    PRIMARY(10, "小学"),
    JUNIOR(6, "初中"),
    SENIOR(2, "高中")
    ;

    override fun toString(): String {
        return "[$value]$text"
    }
}
