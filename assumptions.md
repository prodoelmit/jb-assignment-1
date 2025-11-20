# Zstd implementation

I see several ways to implement zstd compression in the plugin: 

1. use ready `zstd-jni` lib that's already cross-platform (easy, well-tested)
2. require user to have zstd lib installed (awful)
3. pack platform-specific versions of plugin (is that even supported?)
4. download specific zstd lib on start of plugin (has its problems with security, access to download url etc)

Of all of them only 1 and 4 seem feasible.

For now I'm sticking to 1 (`zstd-jni`), assuming that the size increase is too small to 
cause any problems. 