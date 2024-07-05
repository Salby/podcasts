buildscript {
    extra.apply {
        set("compose_version", "1.7.0-beta02")
        set("material3_version", "1.3.0-beta02")
        set("hilt_version", "2.51")
        set("room_version", "2.6.1")
        set("retrofit_version", "2.11.0")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}