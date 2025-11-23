package buildTypes

import addHiddenParam
import addPassword
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.ReuseBuilds
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import linux
import signedPluginFilename

class ReleaseToGithub(composite: Composite): BuildType( {
    name = "Release to Github"
    id("ReleaseToGithub")

    type = Type.DEPLOYMENT

    val inputDir = "input"
    val dockerTag = "alpine_with_github_cli"

    // hide "Deploy" button, so we're always explicitly deploying specific build
    addHiddenParam("teamcity.buildType.environmentBuildType.promoteOnly",  "true")

    addPassword("env.GH_TOKEN", "credentialsJSON:5f8dbb57-f443-4ca4-a709-abd14c564b65")

    val tagRef = addHiddenParam("tag", "--to be filled automatically--")

    val branchRef = composite.depParamRefs["vcsroot.${DslContext.settingsRootId.id}.branch"]

    val repoUrlRef = composite.depParamRefs["vcsroot.${DslContext.settingsRootId.id}.url"]

    triggers {
        vcs {
            branchFilter = "+:v*"
        }
    }

    steps {
        script {
            name = "Fail if not tag, extract tag"
            scriptContent = """
                BRANCH="${branchRef}"
                if [[ "${'$'}BRANCH" != refs/tags/* ]]; then
                    echo "Error: This build requires a tag, got: ${'$'}BRANCH"
                    exit 1
                fi
                TAG="${'$'}{BRANCH#refs/tags/}"
                echo "##teamcity[setParameter name='${tagRef.name}' value='${'$'}TAG']"
                echo "Tag detected: ${'$'}TAG"
            """.trimIndent()
        }
        dockerCommand {
            name = "Prepare docker with github-cli"
            this.commandType = build {
                this.source = content {
                    content = buildString {
                        appendLine("FROM alpine:latest")
                        appendLine()
                        appendLine("RUN apk add --no-cache github-cli git")
                    }
                }
                namesAndTags = dockerTag
            }
        }
        script {
            name = "Create GitHub Release"
            scriptContent = """
                set -e

                TAG="$tagRef"
                echo "Creating release for tag: ${'$'}TAG"

                # Convert git@github.com:owner/repo.git to owner/repo
                REPO_URL="${repoUrlRef}"
                REPO="${'$'}{REPO_URL#*:}"
                REPO="${'$'}{REPO%.git}"

                gh release create "${'$'}TAG" \
                    --repo "${'$'}REPO" \
                    --title "${'$'}TAG" \
                    --generate-notes \
                    "${inputDir}/${signedPluginFilename}"

                echo "Release created successfully"
            """.trimIndent()
            dockerImage = dockerTag
        }
    }

    dependencies {
        dependency(composite) {
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
                reuseBuilds = ReuseBuilds.SUCCESSFUL
            }
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