import java.awt.Desktop
import java.io.File
import java.math.BigInteger
import java.net.URI
import java.security.MessageDigest
import java.util.*

fun hexStringToByteArray(strArg: String): ByteArray {
    val HEX_CHARS = "0123456789ABCDEF"
    val str = strArg.toUpperCase();

    if (str.length % 2 != 0) return hexStringToByteArray("");

    val result = ByteArray(str.length / 2)

    for (i in 0 until str.length step 2) {
        val firstIndex = HEX_CHARS.indexOf(str[i]);
        val secondIndex = HEX_CHARS.indexOf(str[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}

fun byteArrayToHex(bytesArg: ByteArray): String {
    return bytesArg.joinToString("") { String.format("%02X", (it.toInt() and 0xFF)) }.toLowerCase()
}

fun helperJsonResponseSuccess(message: String): String {
    if (message.startsWith('{') || message.startsWith('[')) return "{\"error\": false, \"result\": $message}"; // json
    return "{\"error\": false, \"result\": \"$message\"}";
}

fun helperJsonResponseFailure(message: String): String {
    if (message.startsWith('{') || message.startsWith('[')) return "{\"error\": true, \"result\": $message}"; // json
    return "{\"error\": true, \"result\": \"$message\"}";
}



fun storeEvent(eventsPath: String, params: WritableMap) {
    val directory = File(eventsPath)
    if (!directory.exists()) {
        directory.mkdir()
    }

    File(eventsPath + "/" + System.currentTimeMillis() + ".json").writeText(params.toString())
}

fun sha256(input:String): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun openInBrowser(uri: String) {
    val osName by lazy(LazyThreadSafetyMode.NONE) { System.getProperty("os.name").lowercase(Locale.getDefault()) }
    val desktop = Desktop.getDesktop()
    when {
        Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) -> desktop.browse(URI(uri))
        "mac" in osName -> Runtime.getRuntime().exec("open $uri")
        "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec("xdg-open $uri")
        else -> throw RuntimeException("cannot open $uri")
    }
}