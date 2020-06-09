// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.github.ben-manes.versions") version (Deps.manes_versions)
}
    buildscript {
        //ext.kotlin_version = "1.3.61"
        repositories {
            google()
            jcenter()
        }
        dependencies {

            classpath(Deps.comandroidtools)
            classpath(Deps.kotlingradleplugin)
            // classpath "com.android.tools.build:gradle:4.0.0-beta01"
            // classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.kotlinVer}"
            //classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.1.0")
            classpath("com.github.ben-manes:gradle-versions-plugin:${Deps.manes_versions}")

            // NOTE: Do not place your application dependencies here; they belong
            // in the individual module build.gradle files
        }
    }

    allprojects {
        repositories {
            google()
            jcenter()
        }
    }
    tasks.register("clean").configure{
        delete("build")
    }

