pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://maven.taumc.org/releases")
		maven("https://maven.kikugie.dev/releases")
		maven("https://maven.kikugie.dev/snapshots")
		mavenLocal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
	// https://maven.kikugie.dev/#/releases/dev/kikugie/stonecutter
	// https://maven.kikugie.dev/#/snapshots/dev/kikugie/stonecutter
	id("dev.kikugie.stonecutter") version("0.6-beta.1")
	// https://git.taumc.org/TauMC/TauGradle/releases/latest
	id("org.taumc.gradle.stonecutter") version("0.3.31")
}

rootProject.name = "toomanyrecipeviewers"

include(":jei-api")

tau.stonecutter.create {
	config("21.1", "neoforge")
	config("20.1", "lexforge")
	subproject("jei-api")
}