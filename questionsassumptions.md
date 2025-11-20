# Zstd implementation

I see several ways to implement zstd compression in the plugin: 

1. use ready `zstd-jni` lib that's already cross-platform (easy, well-tested)
2. require user to have zstd lib installed (awful)
3. pack platform-specific versions of plugin (is that even supported?)
4. download specific zstd lib on start of plugin (has its problems with security, access to download url etc)

Of all of them only 1 and 4 seem feasible.

For now I'm sticking to 1 (`zstd-jni`), assuming that the size increase is too small to 
cause any problems. 

Q: Is it ok to stick with `zstd-jni` and not care about ~2mb in plugin size? 

# UX 

Currently, I've moved it to File -> Export -> "Compress to zstd", which I assume to be the most logical place to put 
this new functionality

# VirtualFile compression

There are two situations: when VirtualFile has underlying file on disk and when it doesn't 

In my measurements (not very thorough) compressing real file was ~20% faster than compressing VirtualFile inputStream 

So I assume that it's not premature optimization to work with real files when we have them. Even though 
it adds a complexity of flushing the writes to the disk to avoid any concurrency issues. 

(There might be more problems when we generate writes while it's still compressing, but given that huge files 
are opened in read-only mode, I assume that this is highly unlikely) 

Q: Can this highly-unlikely case be ommitted for this task, or should I care about 100% perfect UX with all edge cases? 

# Functionality 

Currently, I've implemented basic stuff (this shouldn't be considered anywhere close to final result in
terms of code quality, tests coverage etc) 

I have the following things in mind that could/should be implemented. Sorted by my opinion on priority, from 1-musthave
to "very optional"

1. Basic tests (implemented some of them, might add more)
2. File picker to choose where to put resulting file
3. Balloon with "Show location" link that would open export directory in explorer/finder .
4. Settings dialog (compression level + output path), like in "Export to HTML"
5. UI tests if previous points are implemented
6. Telemetry/diagnostics.


Q: Which of the items above should be done, and which can be ignored? 

# Testing on Windows 

I'm leaving it until later stages, because I simply don't have windows machine :) I'll see if I can wrap it in autotests
to run in CI instead. 

# Plugin verification bug 

See https://platform.jetbrains.com/t/could-not-find-idea253-22441-33-during-plugin-verification/2842 

For now I've just limited to "Latest version of IDEA Community", as stated in the task.
