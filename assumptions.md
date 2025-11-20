# Zstd implementation

I see several ways to implement zstd compression in the plugin: 

1. use ready `zstd-jni` lib that's already cross-platform (easy, well-tested)
2. require user to have zstd lib installed (awful)
3. pack platform-specific versions of plugin (is that even supported?)
4. download specific zstd lib on start of plugin (has its problems with security, access to download url etc)

Of all of them only 1 and 4 seem feasible.

For now I'm sticking to 1 (`zstd-jni`), assuming that the size increase is too small to 
cause any problems. 

# UX 

Currently, I've moved it to File -> Export -> "Compress to zstd", which I assume to be the most logical place to put 
this new functionality

# VirtualFile compression

Not all VirtualFiles have underlying local files available, so I'm using VirtualFile's content to compress in all cases. 

In case of very big files compressing local files might actually be faster, but there might be a delay between 
a change in IDE and corresponding write on disk. Also, I doubt that opening a file large enough to feel the difference
would be even adequate 

With all above said, it would be still true to say that for production-grade plugin we would need to measure how big is 
that difference, and maybe optimize it a bit further, while caring about proper flushing. 

# Functionality 

Currently, I've implemented basic stuff (this shouldn't be considered anywhere close to final result in
terms of code quality, tests coverage etc) 

I have the following things in mind that could/should be implemented. Sorted by my opinion on priority, from 1-musthave
to "very optional"

1. Basic tests
2. File picker to choose where to put resulting file
3. Balloon with "Show location" link that would open export directory in explorer/finder .
4. Settings dialog (compression level + output path), like in "Export to HTML"
5. Telemetry/diagnostics. 


# Testing on Windows 

I'm leaving it until later stages, because I simply don't have windows machine :) I'll see if I can wrap it in autotests
to run in CI instead. 

