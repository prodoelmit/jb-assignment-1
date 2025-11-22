package buildTypes

import Arch
import addHiddenParam
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.ReuseBuilds
import jetbrains.buildServer.configs.kotlin.buildFeatures.buildCache
import jetbrains.buildServer.configs.kotlin.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import linux
import pluginFilename

typealias DepsAndArchsList = MutableList<Pair<BuildType, List<Arch>>>
class BuildPlugin(deps: DepsAndArchsList) : BuildType({
    name = "Build Plugin"
    id("BuildPlugin")

    val outputDir = "output"

    artifactRules = """
        $outputDir/**/*
    """.trimIndent()

    val gradleHomeRef = addHiddenParam("env.GRADLE_USER_HOME", "%system.teamcity.build.tempDir%/gradle_cache")

    steps {
        gradle {
            name = "Build plugin"
            tasks = "buildPlugin"
            dockerImage = "amazoncorretto:17"
        }
        gradle {
            name = "Run tests"
            tasks = "check"
            dockerImage = "amazoncorretto:17"
        }
        gradle {
            name = "Run plugin verifier"
            tasks = "verifyPlugin"
            dockerImage = "amazoncorretto:17"
        }
        script {
            name = "Prepare artifact"
            scriptContent = """
                ZIP=$(find build/distributions -name '*.zip' | head -n1)
                
                if [ -z "${'$'}ZIP" ]; then
                    echo "No artifacts available to move"
                    exit 1;
                fi
                
                mkdir -p $outputDir
                cp ${'$'}ZIP $outputDir/$pluginFilename
            """.trimIndent()
        }
    }

    vcs {
        root(DslContext.settingsRoot, """
            +:.
            -:.teamcity
        """.trimIndent())
    }

    dependencies {
        deps.forEach { (buildType, archs) ->
            dependency(buildType) {
                snapshot {
                    reuseBuilds = ReuseBuilds.SUCCESSFUL
                    onDependencyFailure = FailureAction.CANCEL
                    onDependencyCancel = FailureAction.FAIL_TO_START
                }
                artifacts {
                    cleanDestination = true
                    artifactRules = archs.joinToString("\n") { arch ->
                        buildString {
                            append("${arch.os}/${arch.architecture}/${arch.filename}")
                            append(" => ")
                            append("src/main/resources/native/${arch.os}/${arch.architecture}/")
                        }
                    }
                }
            }
        }
    }

    features {
        swabra {
            forceCleanCheckout = true
        }
        buildCache {
            name = "gradle"
            rules = "$gradleHomeRef"
        }
    }

    requirements {
        linux()
    }
}
)