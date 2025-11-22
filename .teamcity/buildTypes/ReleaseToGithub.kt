package buildTypes

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import linux
import signedPluginFilename

class ReleaseToGithub(composite: Composite): BuildType( {
    name = "Release to Github"
    id("ReleaseToGithub")

    type = Type.DEPLOYMENT

    val inputDir = "input"
    val dockerTag = "alpine_with_github_cli"

    steps {
        dockerCommand {
            name = "Prepare docker with github-cli"
            this.commandType = build {
                this.source = content {
                    content = buildString {
                        appendLine("FROM alpine:latest")
                        appendLine()
                        appendLine("apk add github-cli")
                    }
                }
                namesAndTags = dockerTag
            }
        }
        script {
            name = "Dummy"
            scriptContent = """
                gh version
            """.trimIndent()
            dockerImage = dockerTag
        }
    }

    dependencies {
        dependency(composite) {
            artifacts {
                artifactRules = """
                    +:$signedPluginFilename
                """.trimIndent()
            }
        }
    }

    requirements {
        linux()
    }
})