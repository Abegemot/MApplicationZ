import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

 plugins {
     id("com.android.application")
     kotlin("android")
     kotlin("android.extensions")
    // kotlin("plugin.serialization") version "1.3.72"
    // id("androidx.navigation.safeargs")
 }
android {
    compileSdkVersion(29)
 //   buildToolsVersion("29.0.2")

    defaultConfig {
        applicationId="com.begemot.myapplicationz"
        minSdkVersion(23)
        targetSdkVersion(29)
        versionCode= 1
        versionName= "1.0"

        testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose=true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles( getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    packagingOptions {
        exclude("project.properties")
        exclude("META-INF/*.kotlin_module")
        pickFirst("META-INF/LICENSE.txt")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        //kotlinCompilerVersion = "1.3.61-dev-withExperimentalGoogleExtensions-20200129"
        // kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"
        //kotlinCompilerVersion="1.4.0-dev-withExperimentalGoogleExtensions-20200720"
        kotlinCompilerVersion="1.4.0-rc"
        kotlinCompilerExtensionVersion = "0.1.0-dev16"
    }


    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
 tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>{
     kotlinOptions {
        // languageVersion="1.4"
         jvmTarget = JavaVersion.VERSION_1_8.toString()
         freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check","-Xskip-metadata-version-check")
     }
 }


 androidExtensions{
     isExperimental=true
 }

 repositories{
     maven {
         url=uri("https://dl.bintray.com/kotlin/kotlin-eap/")

     }
     mavenLocal(){
         metadataSources {
             mavenPom()
              artifact()
             ignoreGradleMetadataRedirection()
         }
     }
     mavenCentral()
 }


dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(platform("com.begemot.knewsplatform-bom:deps:0.0.1"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")


    implementation("androidx.core:core-ktx")
    implementation("androidx.appcompat:appcompat")
    implementation("com.google.android.material:material")
    //implementation ("androidx.constraintlayout:constraintlayout:1.1.3")
    //implementation ("androidx.navigation:navigation-fragment-ktx:2.1.0")
    //implementation ("androidx.navigation:navigation-ui-ktx:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jsoup:jsoup")
    implementation("com.sun.mail:android-mail")
    implementation("com.sun.mail:android-activation")
    api( "androidx.compose.runtime:runtime")
    //implementation("com.google.code.gson:gson")
    //implementation ("dev.chrisbanes.accompanist:accompanist-coil:0.1.7")
    //implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    //jcompose

    //implementation ("androidx.appcompat:appcompat:1.1.0")
    // implementation "androidx.ui:ui-layout:$Deps.jcompose"
    // implementation "androidx.ui:ui-material:$Deps.jcompose"
    // implementation "androidx.ui:ui-tooling:$Deps.jcompose"
    // implementation "androidx.ui:ui-framework:$Deps.jcompose"
    // implementation "androidx.ui:ui-foundation:$Deps.jcompose"
//JCompose
    //implementation("com.begemot:kprotolibz")

    api("com.begemot:kclib")
    api("com.begemot:KNewsClient")
    api("com.begemot:knewscommon")
    //implementation("com.begemot:kprotolib3")
    // implementation project(":kclib")
    implementation("com.jakewharton.timber:timber")
    testImplementation("junit:junit")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
   // androidTestImplementation("androidx.test.espresso:espresso-core")
    debugImplementation("com.squareup.leakcanary:leakcanary-android")
    implementation("io.ktor:ktor-client-android")


}