import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

 plugins {
     id("com.android.application")
     kotlin("android")
     kotlin("plugin.serialization") version "1.5.10"

 }
android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("G:\\mGoogleDrive\\INewsReader\\INRKeyStore2.jks")
            storePassword = "edmund"
            keyAlias = "key0"
            keyPassword = "edmund"
        }
    }
    compileSdk =30
     defaultConfig {
        applicationId="com.begemot.myapplicationz"
        minSdk = 26
        targetSdk= 30
        versionCode= 30
        versionName= "0.3.7"
//        testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose=true
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles( getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug"){
           // isDebuggable = true
           // applicationIdSuffix = ".debug"
            //isMinifyEnabled = true
            proguardFiles( getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

        }

    }
    packagingOptions {
        resources.excludes.add("META-INF/NOTICE.md")
        resources.excludes.add("META-INF/LICENSE.md")

        //exclude("project.properties")
        //exclude("META-INF/*.kotlin_module")
        //pickFirst("META-INF/LICENSE.txt")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        //kotlinCompilerVersion = "1.3.61-dev-withExperimentalGoogleExtensions-20200129"
        // kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"
        //kotlinCompilerVersion="1.4.0-dev-withExperimentalGoogleExtensions-20200720"
       // kotlinCompilerVersion="1.4.21-2"
        kotlinCompilerExtensionVersion = "1.0.0-beta09"
    }


    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        //useIR = true
        freeCompilerArgs += "-Xallow-jvm-ir-dependencies"
        //freeCompilerArgs= listOf("-P","plugin:androidx.compose.plugins.idea:liveLiterals=false")
    }
}

 task("mytask"){
     dependsOn(":app:assembleRelease")
      doLast{
          print("QUINSSSS COLLLONSSS")
      }
 }


 tasks.register<Copy>("myzcopy"){

         from("C:/Users/dad/AndroidStudioProjects/MyApplicationZ/app/release/app-release.apk")
         into("g:/mGoogleDrive/INewsReader/output")
         from("C:/Users/dad/AndroidStudioProjects/MyApplicationZ/app/build/outputs/apk/debug/app-debug.apk")
         into("g:/mGoogleDrive/INewsReader/output")
         from("C:/Users/dad/AndroidStudioProjects/MyApplicationZ/app/release/app-release.aab")
         into("g:/mGoogleDrive/INewsReader/output")
     
 }

 tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>{
     kotlinOptions {
        // languageVersion="1.4"
         jvmTarget = JavaVersion.VERSION_1_8.toString()
        // freeCompilerArgs = listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check","-Xskip-metadata-version-check")
     }
 }


 //androidExtensions{
 //    isExperimental=true
// }

 repositories{
     maven {
         url=uri("https://dl.bintray.com/kotlin/kotlin-eap/")
         url=uri( "https://kotlin.bintray.com/kotlinx")

     }
     mavenLocal(){
         metadataSources {
             mavenPom()
              artifact()
             ignoreGradleMetadataRedirection()
         }
     }
     mavenCentral()
     google()
 }


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(platform("com.begemot.knewsplatform-bom:deps:0.0.1"))
    implementation("com.jakewharton.timber:timber")
    debugImplementation("com.squareup.leakcanary:leakcanary-android")

    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

    implementation( "androidx.compose.runtime:runtime")
    //implementation("androidx.core:core-ktx")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx")
    //implementation("androidx.activity:activity-compose")

    //implementation("io.github.microutils:kotlin-logging-jvm")
    //implementation( "ch.qos.logback:logback-classic")

    implementation("com.sun.mail:android-mail")
    implementation("com.sun.mail:android-activation")

    implementation("com.begemot:KNewsClient")
    implementation("com.begemot:kclib")
    implementation("com.begemot:knewscommon")

    testImplementation("junit:junit")
    androidTestImplementation("androidx.test.ext:junit")
   // implementation("io.ktor:ktor-client-android")
}