package her.gradle.plugin.doctools.components.kcmatcher

import her.gradle.plugin.doctools.models.Chapter

interface ChapterMatcher {
    fun match(chapterName: String): List<Chapter>
}