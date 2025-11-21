package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import vcsRoots.ZStd

object BuildZStdLinux : BuildType({
    name = "Build ZStd Linux"
    id("BuildZStdLinux")

    vcs {
        root(ZStd, "+:.")
    }

    steps {
        script {
            name = "Install cross-compilers"
            val deps = listOf(
                "build-essential",
                "gcc-aarch64-linux-gnu",
                "g++-aarch64-linux-gnu",
            )
            scriptContent = """
                apt install -y --no-install-recommends ${deps.joinToString(" ")}
            """.trimIndent()
        }
        script {
            name = "Build x86_64"
            scriptContent = """
                make
            """.trimIndent()
        }
        script {
            name = "Build aarch64"
            scriptContent = """
                CC=aarch64-linux-gnu-gcc make
            """.trimIndent()
        }
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }
})