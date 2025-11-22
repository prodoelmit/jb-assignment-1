data class LinuxArch(
    val humanReadable: String,
    val binaryName: String,
    val cc: String?,
    val dependencies: List<String>,
) {
    val compilerEnvString: String
    get() = if (cc != null) {
        "CC=\"${cc}\""
    } else {
        ""
    }
}
