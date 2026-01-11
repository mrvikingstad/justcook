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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JustCook"

include(":app")

// Core modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:ui")

// Data & Domain layers
include(":data")
include(":domain")

// Feature modules
include(":feature:home")
include(":feature:recipes")
include(":feature:search")
include(":feature:bookmarks")
include(":feature:profile")
include(":feature:auth")
