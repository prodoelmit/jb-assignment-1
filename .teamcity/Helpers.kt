import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.GradleBuildStep
import jetbrains.buildServer.configs.kotlin.ui.add

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
    LinuxArch("x86_64", "x86_64", null, listOf("build-base", "make")),
    LinuxArch(
        "aarch64", "aarch64", "/opt/aarch64-linux-musl-cross/bin/aarch64-linux-musl-gcc",
        listOf(
            "build-base",
            "make",
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
val signedPluginFilename = "signedPlugin.zip"
val alpineImage = "alpine:3.22"
val javaImage = "amazoncorretto:17"

fun BuildType.addParam(name: String, block: ParametrizedWithType.(String) -> Unit = {}): ParameterRef {
    params.add {
        block(name)
    }
    return ParameterRef(name)
}

fun BuildType.addHiddenParam(name: String, value: String): ParameterRef = addParam(name) {
    text(
        it, value, display = ParameterDisplay.HIDDEN
    )
}

fun BuildType.addPassword(name: String, value: String): ParameterRef = addParam(name) {
    password(
        it, value, display = ParameterDisplay.HIDDEN
    )
}

fun BuildSteps.bashScript(init: ScriptBuildStep.() -> Unit): ScriptBuildStep {
    val step = ScriptBuildStep {
        init()
        scriptContent = """
            set -xeuo pipefail
            ${scriptContent}
        """.trimIndent()
    }
    step(step)
    return step
}

val gradleCacheDir = "%teamcity.agent.work.dir%/gradle_cache"

fun BuildSteps.gradleCached(init: GradleBuildStep.() -> Unit): GradleBuildStep {
    val step = GradleBuildStep {
        dockerImage = javaImage
        dockerRunParameters = "-v $gradleCacheDir:$gradleCacheDir -e GRADLE_USER_HOME=$gradleCacheDir"
        init()
    }
    step(step)
    return step
}
