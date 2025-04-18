import java.util.Properties

plugins {
	id("idea")
	`kotlin-dsl`
}

idea.module {
	isDownloadJavadoc = true
	isDownloadSources = true
}

repositories {
	gradlePluginPortal {
		content {
			excludeGroup("org.apache.logging.log4j")
		}
	}
	mavenCentral()
	maven("https://maven.wagyourtail.xyz/releases")
	maven("https://maven.wagyourtail.xyz/snapshots")
	maven("https://maven.taumc.org/releases")
	mavenLocal()
}

kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

fun DependencyHandler.plugin(id: String, version: String) {
	this.implementation(group = id, name = "$id.gradle.plugin", version = version)
}

val gradleProperties = Properties().apply {
	load(rootDir.parentFile.resolve("gradle.properties").inputStream())
}

operator fun String.invoke(): String = gradleProperties.getProperty(this) ?: error("Property $this not found")

dependencies {
	constraints {
		api("commons-io:commons-io:${"commons_io_constraint"()}")
	}
	
	implementation("org.ow2.asm:asm-tree:${"asm_version"()}")
	implementation("org.apache.ant:ant:${"shadow_ant_version"()}")

	plugin(id = "com.gradleup.shadow", version = "shadow_version"())
	plugin(id = "xyz.wagyourtail.unimined", version = "unimined_version"())
	plugin(id = "xyz.wagyourtail.jvmdowngrader", version = "jvmdg_version"())
	plugin(id = "com.github.gmazzo.buildconfig", version = "buildconfig_version"())
	plugin(id = "org.taumc.gradle.versioning", version = "taugradle_version"())
	plugin(id = "org.taumc.gradle.compression", version = "taugradle_version"())
	plugin(id = "org.taumc.gradle.publishing", version = "taugradle_version"())
}