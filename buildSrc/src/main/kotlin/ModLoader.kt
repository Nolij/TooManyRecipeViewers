enum class ModLoader(val id: String) {
    FORGE("forge"),
    NEOFORGE("neoforge");

    companion object {
        fun fromId(id: String) : ModLoader = requireNotNull(ModLoader.values().find { it.id == id }, { "Unable to find modloader named '${id}'" })

        fun fromProjectName(name: String) : ModLoader = fromId(name.substring(name.lastIndexOf('-') + 1))
    }
}