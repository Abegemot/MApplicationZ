// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.github.ben-manes.versions") version ("0.39.0")
}
    buildscript {
        //ext.kotlin_version = "1.3.61"
        repositories {

            maven {
                url=uri("https://dl.bintray.com/kotlin/kotlin-eap/")

            }
            mavenLocal()
            google()
            //jcenter()
        }
        dependencies {
            platform("com.begemot.knewsplatform-bom:deps:0.0.1")
            classpath("com.android.tools.build:gradle:7.0.0-beta04")
            classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")


            // NOTE: Do not place your application dependencies here; they belong
            // in the individual module build.gradle files
        }
    }

    allprojects {
        repositories {
            mavenCentral()
            google()
            //jcenter()
        }
    }
    tasks.register("clean").configure{
        delete("build")
    }

