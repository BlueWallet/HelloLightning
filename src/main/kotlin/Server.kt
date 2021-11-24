val staticFiles = StaticFiles();

class Server {
    fun serve(text: String): String {
        println(text);
        var ret = "";

        val text2 = text.split(' ')
        val values = text2[1].split('/')

        var file2serve = "";
        if (values.elementAtOrNull(2) == null || values.elementAtOrNull(2) == "") {
            file2serve = "/index.html"; // default
        } else {
            for (i in 2..6) {
                if (values.elementAtOrNull(i) == null) break;
                file2serve += "/" + values[i] // adding directories to path
                if (file2serve.endsWith(".js") || file2serve.endsWith(".css") || file2serve.endsWith(".html") || file2serve.endsWith(".png")) break;
            }
        }

        // now, got a filename to look for. lets look in bundled files:

        val hex = staticFiles.getHex("/gui" + file2serve);
        if (hex != "") {
            val bar = hexStringToByteArray(hex).toString(Charsets.UTF_8)
            if (file2serve.endsWith(".css")) {
                ret += "HTTP/1.0 200 OK\n" + "Content-type: text/css; charset=UTF-8\n\n";
            } else if (file2serve.endsWith(".js")) {
                ret += "HTTP/1.0 200 OK\n" + "Content-type: application/javascript; charset=UTF-8\n\n";
            } else {
                ret += "HTTP/1.0 200 OK\n" + "Content-type: text/html; charset=UTF-8\n\n";
            }
            // TODO: png and other binaries
            ret += bar;
            return ret;
        }

        // not in bundled files

        return "HTTP/1.0 404 OK\n" + "Content-type: text/html; charset=UTF-8\n\n" + "does not exist: " + file2serve;
    }
}