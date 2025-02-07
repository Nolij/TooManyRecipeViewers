import xyz.wagyourtail.unimined.api.unimined

plugins {
	id("java")
	id("maven-publish")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.unimined")
	id("com.github.gmazzo.buildconfig")
	id("org.taumc.gradle.versioning")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

rootProject.group = "maven_group"()
rootProject.version = "${tau.versioning.get("mod_version"())}+jei.${"jei_version"()}"

println("TooManyRecipeViewers version: ${rootProject.version}")

buildConfig {
	className("TooManyRecipeViewersConstants")
	packageName("dev.nolij.toomanyrecipeviewers")

	useJavaOutput()

	buildConfigField("MOD_ID", "mod_id"())
}

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
	
	// TODO: yell at wyt to fix Unimined
	includeEmptyDirs = false
	exclude("assets/minecraft/textures/**")
}

tasks.withType<GenerateModuleMetadata> {
	enabled = false
}

val shade: Configuration by configurations.creating {
	configurations.compileClasspath.get().extendsFrom(this)
	configurations.runtimeClasspath.get().extendsFrom(this)
}

unimined.minecraft {
	version("minecraft_version"())

	neoForge {
		loader("neoforge_version"())
		mixinConfig("toomanyrecipeviewers.mixins.json")
	}

	mappings {
		mojmap()
		parchment(mcVersion = "minecraft_version"(), version = "parchment_version"())
	}

	defaultRemapJar = false
}

dependencies {
	val minecraftLibraries by configurations.getting
	minecraftLibraries.isTransitive = true
	
	shade("dev.nolij:libnolij:${"libnolij_version"()}")
	minecraftLibraries("dev.nolij:libnolij:${"libnolij_version"()}")
	
	implementation("dev.emi:emi-neoforge:${"emi_version"()}")
	
	shade(project(":jei-api"))
	
	// for testing purposes
	runtimeOnly("maven.modrinth:just-enough-professions-jep:4.0.3")
	runtimeOnly("maven.modrinth:justenoughbreeding:mxmXy9Cs")
	runtimeOnly("maven.modrinth:just-enough-effect-descriptions-jeed:m7gSD9ey")
	runtimeOnly("maven.modrinth:mekanism:10.7.8.70")
	runtimeOnly("maven.modrinth:mekanism-generators:10.7.8.70")
	runtimeOnly("maven.modrinth:mekanism-additions:10.7.8.70")
	runtimeOnly("maven.modrinth:mekanism-tools:10.7.8.70")
	runtimeOnly("maven.modrinth:mekanism_extra:1.21.1-1.0.5")
	runtimeOnly("curse.maven:mekanism-weapons-929829:5906398")
	runtimeOnly("maven.modrinth:just-enough-mekanism-multiblocks:7.2")
	runtimeOnly("maven.modrinth:actually-additions:1.3.12")
	runtimeOnly("curse.maven:placebo-283644:6068449")
	runtimeOnly("curse.maven:apotheosis-313970:6078226")
	runtimeOnly("curse.maven:apothic-attributes-898963:6060907")
	runtimeOnly("curse.maven:apothic-enchanting-1063926:6084297")
	runtimeOnly("curse.maven:apothic-spawners-986583:6058055")
	runtimeOnly("maven.modrinth:c2me-neoforge:0.3.0+alpha.0.47+1.21.1")
	runtimeOnly("maven.modrinth:sophisticated-core:1.21.1-1.1.3.836")
	runtimeOnly("maven.modrinth:sophisticated-storage:1.21.1-1.2.6.1038")
	runtimeOnly("maven.modrinth:sophisticated-backpacks:1.21.1-3.22.5.1173")
	runtimeOnly("curse.maven:moderately-enough-effect-descriptions-meed-918638:6100615")
	runtimeOnly("maven.modrinth:geckolib:oNBe6h9g")
	runtimeOnly("maven.modrinth:curios:9.2.2+1.21.1")
	runtimeOnly("curse.maven:ars-nouveau-401955:6123623")
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

	configurations = listOf(shade)
	archiveClassifier = ""

	relocate("dev.nolij.libnolij", "dev.nolij.toomanyrecipeviewers.libnolij")
}

tasks.assemble {
	dependsOn(tasks.shadowJar, sourcesJar)
}