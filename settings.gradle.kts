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
        maven { url = uri("https://jitpack.io") } // 🔥 Agregado para GraphHopper
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // 🔥 Para GraphHopper
        maven { url = uri("https://mvnrepository.com/artifact/org.mapsforge") } // 🔥 Agregado para MapsForge
    }
}

rootProject.name = "My Application"
include(":app")
