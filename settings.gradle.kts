// 实验五「RecyclerView 翻牌游戏应用」顶层设置脚本
// 在实验四工程的基础上继续开发：插件仓库仍为 google / mavenCentral / gradlePluginPortal，
// 模块依赖只允许从 google() 和 mavenCentral() 解析。
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RecyclerViewGame"
include(":app")
