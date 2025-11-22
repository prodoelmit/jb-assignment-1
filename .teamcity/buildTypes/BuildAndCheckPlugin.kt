package buildTypes

import Arch
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.ReuseBuilds
import jetbrains.buildServer.configs.kotlin.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import linux

typealias DepsAndArchsList = MutableList<Pair<BuildType, List<Arch>>>
class BuildPlugin(deps: DepsAndArchsList) : BuildType({
    name = "Build Plugin"
    id("BuildPlugin")

    steps {
        gradle {
            name = "Build plugin"
            tasks = "buildPlugin"
        }
        gradle {
            name = "Run tests"
            tasks = "check"
        }
        gradle {
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