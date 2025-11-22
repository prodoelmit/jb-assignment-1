package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import vcsRoots.ZStd

object BuildZStdLinux : BuildType({
    name = "Build ZStd Linux"
    id("BuildZStdLinux")

    vcs {
        root(ZStd, "+:.")
    }

    val dockerTag =  "ubuntu_with_cross_compilers:local"

    steps {
        dockerCommand {
            name = "Prepare docker with cross-compilers"
            this.commandType = build {
                val deps = listOf(
                    "build-essential",
                    "gcc-aarch64-linux-gnu",
                    "g++-aarch64-linux-gnu",
                )
                this.source = content {
                    this.content = """
                        FROM ubuntu:22
                        
                        apt install -y --no-install-recommends \\${deps.joinToString("\\n")}
                    """.trimIndent()
                }

                this.namesAndTags = dockerTag
            }
        }
        script {
            name = "Build x86_64"
            scriptContent = """
                make
            """.trimIndent()
            dockerImage = dockerTag
        }
        script {
            name = "Build aarch64"
            scriptContent = """
                CC=aarch64-linux-gnu-gcc make
            """.trimIndent()
            dockerImage = dockerTag
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
})