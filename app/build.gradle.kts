// 实验五「RecyclerView 翻牌游戏应用」应用模块
// 在实验四的 3 个 Activity（玩家信息 / 信息修改 / 翻牌游戏）基础上：
//  1. 主界面增加选项菜单，点击「关于」弹出对话框；
//  2. 翻牌游戏改用 RecyclerView + GridLayoutManager 展示 24 张牌；
//  3. 游戏模型改成三张牌配对，并新增 reset() 方法；
//  4. 通过 values / values-land 的资源限定符支持横屏、竖屏不同牌数。
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
        versionCode = 2
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
