plugins {
	id("java")
	id("com.gradleup.shadow")
	id("xyz.wagyourtail.unimined")
}

operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")

project.group = ""
project.version = "jei_version"()

base {
	archivesName = "jei-api"
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

unimined.minecraft {
	version("minecraft_version"())
	
	runs.off = true

	neoForge {
		loader("neoforge_version"())
	}

	mappings {
		mojmap()
		parchment(mcVersion = "minecraft_version"(), version = "parchment_version"())
	}

	defaultRemapJar = false
}

dependencies {
	shade("mezz.jei:jei-${"minecraft_version"()}-neoforge:${"jei_version"()}")
}

tasks.jar {
	enabled = false
}

tasks.shadowJar {
	from("LICENSE") {
		rename { "${it}_jei"}
	}
	
	configurations = emptyList()
	archiveClassifier = ""
	
	shade.resolve().forEach { file ->
		from(zipTree(file.absolutePath))
	}
	
	val included = listOf(
//		"assets/jei/**",
		"assets/jei/atlases/gui.json",
		"assets/jei/textures/jei/atlas/gui/**",
		"META-INF/accesstransformer.cfg",
		"META-INF/neoforge.mods.toml",
		"mezz/jei/api/**",
		"mezz/jei/common/codecs/**",
		"mezz/jei/common/config/BookmarkTooltipFeature.class",
		"mezz/jei/common/config/ClientToggleState.class",
		"mezz/jei/common/config/DebugConfig.class",
		"mezz/jei/common/config/IClientConfig.class",
		"mezz/jei/common/config/IClientToggleState*",
		"mezz/jei/common/config/IIngredientFilterConfig.class",
		"mezz/jei/common/config/IIngredientGridConfig.class",
		"mezz/jei/common/config/IJeiClientConfigs.class",
		"mezz/jei/common/config/file/**",
		"mezz/jei/common/config/IngredientSortStage.class",
		"mezz/jei/common/config/GiveMode.class",
		"mezz/jei/common/config/RecipeSorterStage.class",
		"mezz/jei/common/Constants.class",
		"mezz/jei/common/input/**/I*",
		"mezz/jei/common/gui/**",
		"mezz/jei/common/Internal.class",
		"mezz/jei/common/input/ClickableIngredient.class",
		"mezz/jei/common/input/keys/IJeiKeyMappingCategoryBuilder.class",
		"mezz/jei/common/JeiFeatures.class",
		"mezz/jei/common/network/**",
		"mezz/jei/common/platform/**",
		"mezz/jei/common/transfer/**",
		"mezz/jei/common/util/**",
		"mezz/jei/core/**",
		"mezz/jei/library/**",
		"mezz/jei/neoforge/platform/**",
		"mezz/jei/gui/**",
		"mezz/jei/gui/startup/**",
	)
	val excluded = listOf<String>(
		"mezz/jei/gui/plugins/**",
		"mezz/jei/library/plugins/debug/**",
		"mezz/jei/library/recipes/RecipeManager.class",
		"mezz/jei/library/recipes/RecipeManagerInternal.class",
		"mezz/jei/**/package-info.*"
	)
	
	inputs.property("included", included.sorted().fold("") { out, value -> "${out}${value};" })
	inputs.property("excluded", excluded.sorted().fold("") { out, value -> "${out}${value};" })
	include(included)
	exclude(excluded)
}

tasks.assemble {
	dependsOn(tasks.shadowJar)
}

rootProject.tasks.compileJava {
	dependsOn(tasks.shadowJar)
}