import jetbrains.buildServer.configs.kotlin.Requirements

open class Arch(
    val humanReadableName: String,
    val os: String,
    val architecture: String,
    val filename: String, // to handle ".exe" on windows
)

class LinuxArch(
    humanReadableName: String,
    architecture: String,
    val cc: String?,
    val dependencies: List<String>,
): Arch(
    humanReadableName,
    os = "linux",
    architecture = architecture,
    filename = "zstd",
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

fun Requirements.linux() {
    contains("teamcity.agent.jvm.os.name", "Linux")
}