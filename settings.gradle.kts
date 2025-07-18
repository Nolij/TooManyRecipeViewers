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
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention")
	id("dev.kikugie.stonecutter")
}

rootProject.name = "toomanyrecipeviewers"

include(":jei-api")

stonecutter.create(rootProject) {
	vers("21.1-neoforge", "21.1")
	vers("20.1-lexforge", "20.1")
	branch("jei-api")
}