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
}

kotlin {
	jvmToolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}