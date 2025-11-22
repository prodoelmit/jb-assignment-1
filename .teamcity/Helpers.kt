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

// Homebrew dependencies: Xcode Command Line Tools (xcode-select --install)
// Native Apple Clang supports cross-compilation via -arch flag
class MacArch(
    humanReadableName: String,
    architecture: String,
    val compilerEnvString: String = "",
): Arch(
    humanReadableName,
    os = "darwin",
    architecture = architecture,
    filename = "zstd",
)

val linuxArchs = listOf(
    LinuxArch("x86_64", "x86_64", null, listOf("build-essential")),
    LinuxArch(
        "aarch64", "aarch64", "aarch64-linux-gnu-gcc",
        listOf(
            "gcc-aarch64-linux-gnu",
            "libc6-dev-arm64-cross",
        )
    ),
)

val macArchs = listOf(
    MacArch("x86_64", "x86_64", "CFLAGS=\"-arch x86_64\" LDFLAGS=\"-arch x86_64\""),
    MacArch("aarch64", "aarch64", "CFLAGS=\"-arch arm64\" LDFLAGS=\"-arch arm64\""),
)


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

fun Requirements.mac() {
    contains("teamcity.agent.jvm.os.name", "Mac")
}

val pluginFilename = "plugin.zip"