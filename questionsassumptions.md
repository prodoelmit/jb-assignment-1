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

Update: continuing with option 5: build zstd binaries ourselves and bundle them into the plugin

# Functionality 

Currently, I've implemented basic stuff (this shouldn't be considered anywhere close to final result in
terms of code quality, tests coverage etc) 

I have the following things in mind that could/should be implemented. Sorted by my opinion on priority, from 1-musthave
to "very optional"

1. Basic tests (implemented some of them, might add more)
2. [x] File picker to choose where to put resulting file 
3. [x] Balloon with "Show location" link that would open export directory in explorer/finder .
4. [x] Settings dialog (compression level + output path), like in "Export to HTML"
5. UI tests if previous points are implemented
6. Telemetry/diagnostics.


Q: Which of the items above should be done, and which can be ignored? 

# Testing on Windows 

I'm leaving it until later stages, because I simply don't have windows machine :) I'll see if I can wrap it in autotests
to run in CI instead. 

Update: decided to only support linux for now

# Plugin verification bug 

See https://platform.jetbrains.com/t/could-not-find-idea253-22441-33-during-plugin-verification/2842 

For now I've just limited to "Latest version of IDEA Community", as stated in the task.

# Using right version of zstd

There are several ways to implement it, e.g.: 
* zstd as submodule in this repo
* separate pipelines with some version references in-between
* branching convention between two repos 

For ease of implementation here I've chosen the latter one. `zstd` is forked to my account, which allows me to 
create branches with same name as in my plugin's repo. When building from branches teamcity by default uses the same 
name where possible, so just by choosing plugin's branch we'll get corresponding zstd's branch
(it will fallback to using default branch if the branch with same name doesn't exist)

note: there's a quirk - when running build for default branch, teamcity passes `<default>` downstream, not the actual 
name of branch. That might shoot some kneecaps if used without caution. 