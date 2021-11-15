import java.io.File

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