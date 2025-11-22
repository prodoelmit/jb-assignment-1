import buildTypes.BuildPlugin
import buildTypes.BuildZStdLinux
import buildTypes.BuildZStdMac
import buildTypes.DepsAndArchsList
import jetbrains.buildServer.configs.kotlin.*
import vcsRoots.ZStd

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2025.07"

project {

    val depsAndArchs: DepsAndArchsList = mutableListOf()

    vcsRoot(ZStd)


    val zstdLinux = BuildZStdLinux(linuxArchs).also {
        buildType(it)
        depsAndArchs.add(Pair(it, linuxArchs))
    }

    val zstdMac = BuildZStdMac(macArchs).also {
        buildType(it)
        depsAndArchs.add(Pair(it, macArchs))
    }

    val buildPlugin = BuildPlugin(depsAndArchs).also {
        buildType(it)
    }

}
