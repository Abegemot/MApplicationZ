pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
    includeBuild("H:/prg/KnewsPlatform2")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
    includeBuild("../KCLib")
    includeBuild("H:/prg/KnewsPlatform2")
    //includeBuild("H:/prg/KnewsPlatform")
}


include(":app")
rootProject.name = "My Application Z"
//includeBuild("H:/prg/kprotolib3")
//includeBuild("../KCLib")
//includeBuild("H:/prg/kprotolibz")

includeBuild("H:/prg/KNewsClient")
includeBuild("H:/prg/KNewsCommon")

