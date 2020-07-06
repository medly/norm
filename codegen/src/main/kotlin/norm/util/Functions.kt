package norm.util

import org.apache.commons.text.CaseUtils

fun toCamelCase(s: String): String = CaseUtils.toCamelCase(s, false, '_', '-', ' ')

fun toTitleCase(s: String): String = CaseUtils.toCamelCase(s, true, '_', '-')

