package vcsRoots

import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

object ZStd: GitVcsRoot({
    name = "ZStd@Github"
    id("ZStd")
    authMethod = uploadedKey {
        uploadedKey = "github"
    }
    url = "git@github.com:prodoelmit/zstd.git" // Our fork where we control branch naming
    branch = "main"
    useTagsAsBranches = true
    branchSpec = """
        +:*
    """.trimIndent()
}) {
}