package buildTypes

import addHiddenParam
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import linux
import pluginFilename
import signedPluginFilename

class SignPlugin(buildPlugin: BuildPlugin) : BuildType({
    name = "Sign Plugin"
    id("SignPlugin")

    val outputDir = "output"
    val inputDir = "input"

    artifactRules = """
        $outputDir/**/*
    """.trimIndent()

    val signerFilename = "signer.jar"

    val signerUrlRef = addHiddenParam(
        "tool.signer.name",
        "https://github.com/JetBrains/marketplace-zip-signer/releases/download/0.1.43/marketplace-zip-signer-cli-0.1.43.jar"
    )
    val signerShaRef = addHiddenParam(
        "tool.signer.sha",
        "2958a0f42221d6062b50f22754e413ae5cc42f60c441467202c6700df2e22f44"
    )

    val certFileBase64Ref = addHiddenParam("secret.cert.base64", "credentialsJSON:6809826f-d74f-4ec8-84aa-f57fded27e76")
    val keyFileBase64Ref = addHiddenParam("secret.key.base64", "credentialsJSON:dc35e719-4111-4500-b914-fc59526c91ed")
    val passwordRef = addHiddenParam("secret.key.password", "credentialsJSON:b6f2a655-5777-4d7c-a6e1-89e862409770")

    steps {
        script {
            name = "Prepare signer"
            scriptContent = """
                wget '$signerUrlRef' -O $signerFilename
                sha256 -c '$signerShaRef' $signerFilename
                if [ $? -neq 0 ]; then
                    echo "Signer SHA mismatch"
                    exit 1;
                fi
            """.trimIndent()
        }

        script {
            name = "Sign plugin"
            val tmpDirRef = ParameterRef("system.teamcity.build.tempDir")
            val keyFile = "$tmpDirRef/key.pem"
            val certChainFile = "$tmpDirRef/chain.crt"
            scriptContent = """
                echo '$keyFileBase64Ref' | base64 -d > $keyFile
                echo '$certFileBase64Ref' | base64 -d > $certChainFile
                
                java -jar $signerFilename sign \
                  -in $inputDir/$pluginFilename \
                  -out $outputDir/$signedPluginFilename \
                  -cert-file "$certChainFile" \
                  -key-file "$keyFileBase64Ref" \
                  -key-pass "$passwordRef"
            """.trimIndent()
        }
    }

    vcs {
        root(
            DslContext.settingsRoot, """
            +:.
            -:.teamcity
        """.trimIndent()
        )
    }

    dependencies {
        dependency(buildPlugin) {
            snapshot {
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
                reuseBuilds = ReuseBuilds.SUCCESSFUL
            }
            artifacts {
                artifactRules = """
                    +:* => $inputDir
                """.trimIndent()
                cleanDestination = true
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