package buildTypes

import Arch
import addHiddenParam
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.ReuseBuilds
import alpineImage
import bashScript
import gradleCached
import jetbrains.buildServer.configs.kotlin.buildFeatures.swabra
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

    steps {
        gradleCached {
            name = "Build plugin"
            tasks = "buildPlugin"
        }
        bashScript {
            name = "Prepare artifact"
            scriptContent = """
                ZIP=${'$'}(find build/distributions -name '*.zip' | head -n1)

                if [ -z "${'$'}ZIP" ]; then
                    echo "##teamcity[buildStatus status='FAILURE' text='No artifacts available to move']"
                    exit 1
                fi

                mkdir -p $outputDir
                cp "${'$'}ZIP" $outputDir/$pluginFilename
            """.trimIndent()
            dockerImage = alpineImage
        }
        gradleCached {
            name = "Run tests"
            tasks = "check"
        }
        gradleCached {
            name = "Run plugin verifier"
            tasks = "verifyPlugin"
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
    }

    requirements {
        linux()
    }
}
)