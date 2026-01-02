pluginManagement {
	repositories {
		gradlePluginPortal {
			content {
				excludeGroup("org.apache.logging.log4j")
			}
		}
		mavenCentral()
		maven("https://maven.taumc.org/releases")
		maven("https://maven.kikugie.dev/releases")
		maven("https://maven.kikugie.dev/snapshots")
		maven("https://maven.wagyourtail.xyz/releases")
		maven("https://maven.wagyourtail.xyz/snapshots")
		mavenLocal()
	}
	
	plugins {
		fun property(name: String): String = extra[name] as? String ?: error("Property ${name} not found")
		
		id("org.gradle.toolchains.foojay-resolver-convention") version(property("foojay_resolver_convention_version"))
		id("dev.kikugie.stonecutter") version(property("stonecutter_version"))
		id("com.gradleup.shadow") version(property("shadow_version"))
		id("xyz.wagyourtail.unimined") version(property("unimined_version"))
		id("xyz.wagyourtail.jvmdowngrader") version(property("jvmdg_version"))
		id("com.github.gmazzo.buildconfig") version(property("buildconfig_version"))
		id("org.taumc.gradle.versioning") version(property("taugradle_version"))
		id("org.taumc.gradle.compression") version(property("taugradle_version"))
		id("org.taumc.gradle.publishing") version(property("taugradle_version"))
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention")
	id("dev.kikugie.stonecutter")
}

rootProject.name = "toomanyrecipeviewers"

include(":jei-api")

stonecutter.create(rootProject) {
	version("21.1-neoforge", "21.1")
	version("20.1-lexforge", "20.1")
	branch("jei-api")
}