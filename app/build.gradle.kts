import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

 plugins {
     id("com.android.application")
     kotlin("android")
     kotlin("plugin.serialization") version "1.4.30"

 }
android {
    compileSdkVersion(30)
     defaultConfig {
        applicationId="com.begemot.myapplicationz"
        minSdkVersion(24)
        targetSdkVersion(30)
        versionCode= 1
        versionName= "0.1.1"
        testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose=true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
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
        kotlinCompilerExtensionVersion = "1.0.0-beta01"
    }


    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        useIR = true
        //freeCompilerArgs= listOf("-P","plugin:androidx.compose.plugins.idea:liveLiterals=false")
    }
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

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

    api( "androidx.compose.runtime:runtime")
    implementation("androidx.core:core-ktx")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx")
    implementation("androidx.activity:activity-compose:1.3.0-alpha03")

    implementation("com.sun.mail:android-mail")
    implementation("com.sun.mail:android-activation")

    api("com.begemot:KNewsClient")
    api("com.begemot:kclib")
    api("com.begemot:knewscommon")

    testImplementation("junit:junit")
    androidTestImplementation("androidx.test.ext:junit")
    implementation("io.ktor:ktor-client-android")
}