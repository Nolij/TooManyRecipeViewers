pluginManagement {
	repositories {
		gradlePluginPortal()
		maven {
			url = uri("https://maven.kikugie.dev/snapshots")
		}
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
	id("dev.kikugie.stonecutter") version "0.6-alpha.7"
}

rootProject.name = "toomanyrecipeviewers"

include(":jei-api")

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"

	create(rootProject) {
		vers("1.21.1-neoforge", "1.21.1")
		vers("1.20.1-forge", "1.20.1")
		branch("jei-api")
	}
}
