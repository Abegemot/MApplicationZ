import org.jetbrains.kotlin.config.KotlinCompilerVersion

 plugins {
     id("com.android.application")
     kotlin("android")
     kotlin("android.extensions")
     id("androidx.navigation.safeargs")
 }
android {
    compileSdkVersion(28)
 //   buildToolsVersion("29.0.2")

    defaultConfig {
        applicationId="com.begemot.myapplicationz"
        minSdkVersion(22)
        targetSdkVersion(28)
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
        exclude("META-INF/INDEX.LIST")
        pickFirst("META-INF/io.netty.versions.properties")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        //kotlinCompilerVersion = "1.3.61-dev-withExperimentalGoogleExtensions-20200129"
        kotlinCompilerVersion = "1.3.70-dev-withExperimentalGoogleExtensions-20200424"
        kotlinCompilerExtensionVersion = Deps.jcompose
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
 androidExtensions{
     isExperimental=true
 }
 
dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation( fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation ("org.jetbrains.kotlin:kotlin-stdlib:${Deps.kotlinVer}")


    implementation ("androidx.core:core-ktx:1.2.0")
    implementation ("androidx.appcompat:appcompat:1.1.0")
    implementation ("com.google.android.material:material:1.0.0")
    implementation ("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.1.0")
    implementation ("androidx.navigation:navigation-ui-ktx:2.1.0")
    implementation(Deps.jsoup)


    //jcompose

    implementation ("androidx.appcompat:appcompat:1.1.0")
   // implementation "androidx.ui:ui-layout:$Deps.jcompose"
   // implementation "androidx.ui:ui-material:$Deps.jcompose"
   // implementation "androidx.ui:ui-tooling:$Deps.jcompose"
   // implementation "androidx.ui:ui-framework:$Deps.jcompose"
   // implementation "androidx.ui:ui-foundation:$Deps.jcompose"
//JCompose
    implementation("com.begemot:kprotolibz")

    api("com.begemot:kclib")
    //implementation("com.begemot:kprotolib3")
   // implementation project(":kclib")

    testImplementation ("junit:junit:4.12")
    androidTestImplementation ("androidx.test.ext:junit:1.1.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.2.0")
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:${Deps.leakcanary}")

}