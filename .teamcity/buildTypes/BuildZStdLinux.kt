package buildTypes

import LinuxArch
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import vcsRoots.ZStd

class BuildZStdLinux(val archs: Collection<LinuxArch>) : BuildType({
    name = "Build ZStd Linux"
    id("BuildZStdLinux")

    vcs {
        root(ZStd, "+:.")
    }

    val dockerTag = "ubuntu_with_cross_compilers:local"


    val outDir = "out"

    artifactRules = archs.joinToString {  arch ->
        "$outDir/${arch.binaryName} => ${arch.binaryName}"
    }

    steps {
        dockerCommand {
            name = "Prepare docker with cross-compilers"
            this.commandType = build {
                val deps = archs.flatMap { arch -> arch.dependencies }
                this.source = content {
                    this.content = """
                        FROM ubuntu:24.04
                        
                        apt install -y --no-install-recommends \
                            ${deps.joinToString("\\\n")}
                    """.trimIndent()
                }

                this.namesAndTags = dockerTag
            }
        }
        archs.forEach { arch ->

            script {
                name = "Build ${arch.humanReadable}"
                scriptContent = """
                make clean
                ${arch.compilerEnvString} make
                
                mkdir -p out
                cp programs/zstd out/${arch.binaryName}"
            """.trimIndent()
                dockerImage = dockerTag
            }

        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
}) {
}