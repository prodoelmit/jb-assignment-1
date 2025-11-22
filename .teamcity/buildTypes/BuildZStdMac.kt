package buildTypes

import MacArch
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import mac
import vcsRoots.ZStd

class BuildZStdMac(val archs: Collection<MacArch>) : BuildType({
    name = "Build ZStd Mac"
    id("BuildZStdMac")

    vcs {
        root(ZStd, "+:.")
    }

    val outDir = "out"

    artifactRules = """
        $outDir/**/*
    """.trimIndent()

    steps {
        archs.forEach { arch ->
            script {
                name = "Build ${arch.humanReadableName}"


                val dirForArtifact = "$outDir/${arch.os}/${arch.architecture}"
                scriptContent = """
                    make clean
                    ${arch.compilerEnvString} make

                    mkdir -p "$dirForArtifact"
                    cp programs/zstd "$dirForArtifact/${arch.filename}"
            """.trimIndent()
            }

        }
    }

    requirements {
        mac()
    }
}) {
}