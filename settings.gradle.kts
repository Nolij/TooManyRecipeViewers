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
		operator fun String.invoke(): String = extra[this] as? String ?: error("Property $this not found")
		
		id("org.gradle.toolchains.foojay-resolver-convention") version("foojay_resolver_convention_version"())
		id("dev.kikugie.stonecutter") version("stonecutter_version"())
		id("com.gradleup.shadow") version("shadow_version"())
		id("xyz.wagyourtail.unimined") version("unimined_version"())
		id("xyz.wagyourtail.jvmdowngrader") version("jvmdg_version"())
		id("com.github.gmazzo.buildconfig") version("buildconfig_version"())
		id("org.taumc.gradle.versioning") version("taugradle_version"())
		id("org.taumc.gradle.compression") version("taugradle_version"())
		id("org.taumc.gradle.publishing") version("taugradle_version"())
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