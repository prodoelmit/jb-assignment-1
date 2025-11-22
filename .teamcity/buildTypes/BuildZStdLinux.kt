package buildTypes

import LinuxArch
import appendBashMultiline
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import linux
import vcsRoots.ZStd

class BuildZStdLinux(val archs: Collection<LinuxArch>) : BuildType({
    name = "Build ZStd Linux"
    id("BuildZStdLinux")

    vcs {
        root(ZStd, "+:.")
    }

    val dockerTag = "alpine_with_cross_compilers:local"


    val outDir = "out"

    artifactRules = """
        $outDir/**/*
    """.trimIndent()

    steps {
        dockerCommand {
            name = "Prepare docker with cross-compilers"
            this.commandType = build {
                val deps = archs.flatMap { it.dependencies }
                source = content {
                    this.content = buildString {
                        appendLine("FROM alpine:latest")
                        appendLine()

                        appendBashMultiline(
                            "RUN apk add --no-cache",
                            "wget",
                            *deps.toTypedArray(),
                        )
                        appendLine()
                        appendLine()
                        appendLine("RUN wget -q https://musl.cc/aarch64-linux-musl-cross.tgz && \\")
                        appendLine("    tar -xzf aarch64-linux-musl-cross.tgz -C /opt && \\")
                        appendLine("    rm aarch64-linux-musl-cross.tgz")
                    }
                }

                this.namesAndTags = dockerTag
            }
        }
        archs.forEach { arch ->

            script {
                name = "Build ${arch.humanReadableName}"


                val dirForArtifact = "$outDir/${arch.os}/${arch.architecture}"
                scriptContent = """
                    make clean
                    ${arch.compilerEnvString} make zstd LDFLAGS="-static"

                    mkdir -p "$dirForArtifact"
                    cp programs/zstd "$dirForArtifact/${arch.filename}"
            """.trimIndent()
                dockerImage = dockerTag
            }

        }
    }

    requirements {
        linux()
    }
}) {
}