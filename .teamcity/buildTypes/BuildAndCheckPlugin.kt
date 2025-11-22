package buildTypes

import Arch
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.ReuseBuilds
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
                }
                artifacts {
                    cleanDestination = true
                    artifactRules = archs.joinToString("\n") { arch ->
                        buildString {
                            append("${arch.os}/${arch.architecture}/${arch.filename}")
                            append(" => ")
                            append("src/main/resources/${arch.os}/${arch.architecture}/${arch.filename}")
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