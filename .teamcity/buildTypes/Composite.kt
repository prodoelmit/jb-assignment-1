package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.FailureAction
import vcsRoots.ZStd

class Composite(buildAndCheck: BuildPlugin, signPlugin: SignPlugin): BuildType({
    name = "Build and Sign Plugin"
    id("BuildAndSignPlugin")

    type = Type.COMPOSITE

    vcs {
        root(DslContext.settingsRoot, """
            +:.
            -:.teamcity
        """.trimIndent())
        root(ZStd)
    }

    dependencies {
        dependency(buildAndCheck) {
            artifacts {
                artifactRules = "+:*"
            }
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }
        dependency(signPlugin) {
            artifacts {
                artifactRules = "+:*"
            }
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }
    }

})