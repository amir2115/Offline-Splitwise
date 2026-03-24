pluginManagement {
    repositories {
//        maven(url = "https://maven.myket.ir")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
//        maven(url = "https://maven.myket.ir")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven(url = "https://maven.myket.ir")
        google()
        mavenCentral()
    }
}

rootProject.name = "Offline Splitwise"
include(":app")
