class MinecraftVersion {
    companion object {
        fun fromName(name: String): String {
            return name.substring(0, name.lastIndexOf('-'))
        }
    }
}