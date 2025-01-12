import java.security.MessageDigest

fun String.hash(algorithm: String): String = 
	MessageDigest
		.getInstance(algorithm)
		.digest(this.toByteArray())
		.fold("") { out, char ->
			out + "%02X".format(char)
		}

fun String.sha1(): String = this.hash("SHA-1")

fun String.sha256(): String = this.hash("SHA-256")