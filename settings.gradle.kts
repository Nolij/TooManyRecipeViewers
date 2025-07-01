pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://maven.taumc.org/releases")
		maven("https://maven.kikugie.dev/releases")
		maven("https://maven.kikugie.dev/snapshots")
		mavenLocal()
	}
	
	plugins {
		operator fun String.invoke(): String = extra[this] as? String ?: error("Property $this not found")
		
		id("org.gradle.toolchains.foojay-resolver-convention") version("foojay_resolver_convention_version"())
		id("dev.kikugie.stonecutter") version("stonecutter_version"())
		id("org.taumc.gradle.stonecutter") version("taugradle_version"())
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention")
	id("dev.kikugie.stonecutter")
	id("org.taumc.gradle.stonecutter")
}

rootProject.name = "toomanyrecipeviewers"

include(":jei-api")
include(":benchmarktool")

tau.stonecutter.create {
	config("21.1", "neoforge")
	config("20.1", "lexforge")
	subproject("jei-api")
}