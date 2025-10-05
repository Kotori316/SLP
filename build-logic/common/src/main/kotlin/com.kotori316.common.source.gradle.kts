plugins {
    idea
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
        excludeDirs = excludeDirs + file("run") + file("runs") + file("run-server")
    }
}
