package com.justcook.core.common.extensions

import java.util.regex.Pattern

private val EMAIL_PATTERN = Pattern.compile(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun String.isValidEmail(): Boolean {
    return isNotBlank() && EMAIL_PATTERN.matcher(this).matches()
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}
