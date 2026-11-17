// 实验六「多 Fragment 应用」应用模块
// 在实验五 RecyclerView 翻牌游戏的基础上：
//  1. 用 3 个 Fragment 重构 APP（个人信息 / 2 张牌翻牌 / 3 张牌翻牌）；
//  2. 用 BottomNavigationView 切换 Fragment；
//  3. 个人信息和两种牌局都通过 ObjectOutputStream/ObjectInputStream 序列化保存，退出后可恢复。
//
// 说明：AGP 9 已内置 Kotlin 支持（built-in Kotlin），因此这里不再显式应用
// org.jetbrains.kotlin.android 插件，直接使用 android {} 与 kotlin {} 扩展即可。
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "cn.edu.sicnu.cardgame"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cn.edu.sicnu.cardgame"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "1.0"

        // 实验要求：使用 Android Studio 的单元测试与 UI 测试，这里指定 UI 测试的运行器
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    // 打开视图绑定：布局中的 id 会自动生成类型安全的 binding 类，代码里不再写 findViewById()
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.recyclerview)

    // 本地单元测试（JVM）：测试模型层 Card / Deck / CardMatchingGame
    testImplementation(libs.junit)

    // 仪器化 UI 测试（Android 设备 / 模拟器）：测试 3 个 Activity 的交互
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}
