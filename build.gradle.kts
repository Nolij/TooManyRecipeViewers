import xyz.wagyourtail.unimined.api.unimined

plugins {
	id("java")
	id("maven-publish")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.unimined")
	id("com.github.gmazzo.buildconfig")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

buildConfig {
	className("TooManyRecipeViewersConstants")
	packageName("dev.nolij.toomanyrecipeviewers")

	useJavaOutput()

	buildConfigField("MOD_ID", "mod_id"())
}

rootProject.group = "maven_group"()
rootProject.version = "${"mod_version"()}+jei.${"jei_version"()}"

base {
	archivesName = "mod_id"()
}

repositories {
	maven("https://repo.spongepowered.org/maven")
	maven("https://jitpack.io/")
	exclusiveContent {
		forRepository { maven("https://api.modrinth.com/maven") }
		filter {
			includeGroup("maven.modrinth")
		}
	}
	exclusiveContent {
		forRepository { maven("https://cursemaven.com") }
		filter {
			includeGroup("curse.maven")
		}
	}
	maven("https://maven.blamejared.com")
	maven("https://maven.taumc.org/releases")
	maven("https://maven.terraformersmc.com/")
	maven("https://maven.blamejared.com/")
	maven("https://maven.parchmentmc.org")
}

dependencies {
	compileOnly("org.jetbrains:annotations:${"jetbrains_annotations_version"()}")

	compileOnly("systems.manifold:manifold-rt:${"manifold_version"()}")
	annotationProcessor("systems.manifold:manifold-exceptions:${"manifold_version"()}")
}

tasks.withType<JavaCompile> {
	if (name !in arrayOf("compileMcLauncherJava", "compilePatchedMcJava")) {
		options.encoding = "UTF-8"
		sourceCompatibility = "21"
		options.release = 21
		javaCompiler = javaToolchains.compilerFor {
			languageVersion = JavaLanguageVersion.of(21)
		}
		options.compilerArgs.addAll(arrayOf("-Xplugin:Manifold no-bootstrap"))
		options.forkOptions.jvmArgs?.add("-XX:+EnableDynamicAgentLoading")
	}
}

tasks.processResources {
	inputs.file(rootDir.resolve("gradle.properties"))
	inputs.property("version", rootProject.version)

	filteringCharset = "UTF-8"

	val props = mutableMapOf<String, String>()
	props.putAll(rootProject.properties
		.filterValues { value -> value is String }
		.mapValues { entry -> entry.value as String })
	props["mod_version"] = rootProject.version as String

	filesMatching(listOf("fabric.mod.json", "mcmod.info", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
		expand(props)
	}
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<GenerateModuleMetadata> {
	enabled = false
}

val shade: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}
val modCompileOnly: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
}
val modCompileOnlyShaded: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
}
val modRuntimeOnly: Configuration by configurations.creating {
	configurations.runtimeClasspath.get().extendsFrom(this)
}
val mod: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

unimined.minecraft {
	version("minecraft_version"())

	neoForge {
		loader("neoforge_version"())
	}

	mappings {
		mojmap()
		parchment(mcVersion = "minecraft_version"(), version = "parchment_version"())
	}
	
	mods {
		remap(modCompileOnly)
		remap(modRuntimeOnly)
		remap(modCompileOnlyShaded)
		remap(mod)
	}

	defaultRemapJar = false
}

dependencies {
	val minecraftLibraries by configurations.getting
	minecraftLibraries.isTransitive = true
	
//	shade("dev.nolij:libnolij:${"libnolij_version"()}")
	
	mod("dev.emi:emi-neoforge:${"emi_version"()}")
	
	modCompileOnlyShaded("mezz.jei:jei-${"minecraft_version"()}-neoforge-api:${"jei_version"()}")
	runtimeOnly(project(":jei-api"))
	
	// for testing purposes
	modRuntimeOnly("maven.modrinth:just-enough-professions-jep:4.0.3")
	modRuntimeOnly("maven.modrinth:justenoughbreeding:mxmXy9Cs")
	modRuntimeOnly("maven.modrinth:just-enough-effect-descriptions-jeed:m7gSD9ey")
	modRuntimeOnly("maven.modrinth:mekanism:10.7.8.70")
	modRuntimeOnly("maven.modrinth:mekanism-generators:10.7.8.70")
	modRuntimeOnly("maven.modrinth:mekanism-additions:10.7.8.70")
	modRuntimeOnly("maven.modrinth:mekanism-tools:10.7.8.70")
	modRuntimeOnly("maven.modrinth:mekanism_extra:1.21.1-1.0.5")
	modRuntimeOnly("curse.maven:mekanism-weapons-929829:5906398")
	modRuntimeOnly("maven.modrinth:just-enough-mekanism-multiblocks:7.2")
	modRuntimeOnly("maven.modrinth:actually-additions:1.3.12")
}

tasks.jar {
	enabled = false
}

val sourcesJar by tasks.registering(Jar::class) {
	group = "build"

	archiveClassifier = "sources"

	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}

	sourceSets.forEach {
		from(it.allSource) { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
	}
}

tasks.shadowJar {
	from("LICENSE") {
		rename { "${it}_${"mod_id"()}" }
	}

	exclude("*.xcf")
	exclude("LICENSE_libnolij")

	configurations = listOf(shade, modCompileOnlyShaded)
	archiveClassifier = ""

	relocate("dev.nolij.libnolij", "dev.nolij.toomanyrecipeviewers.libnolij")
}

tasks.assemble {
	dependsOn(tasks.shadowJar)
}