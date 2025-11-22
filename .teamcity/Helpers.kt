data class LinuxArch(
    val humanReadable: String,
    val binaryName: String,
    val cc: String?,
    val dependencies: List<String>,
) {
    val compilerEnvString: String
    get() = if (cc != null) {
        "CC=\"${cc}\""
    } else {
        ""
    }
}


fun StringBuilder.appendBashMultiline(vararg lines: String): StringBuilder {
    val indentString = "\t\t"
    lines.forEachIndexed { index, line ->
        if (index > 0) {
            append(indentString)
        }
        append(line)
        if (index != lines.lastIndex) {
            appendLine(" \\")
        }
    }
    return this
}