import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

class ClientHandler(client: Socket) {
    private val client: Socket = client
    private val reader: Scanner = Scanner(client.getInputStream())
    private val writer: OutputStream = client.getOutputStream()
    private val executor: Executor = Executor()
    private val server: Server = Server()
    private var running: Boolean = false

    fun run() {
        running = true

        while (running) {
            var text: String = "";
            try {
                text = reader.nextLine()

                if (text == "EXIT") {
                    shutdown()
                    continue
                }

                if (text.indexOf(":") > -1 || text === "") {
                    // http header. skip it
                    continue;
                }

                if (text.startsWith("GET /gui")) {
                    val resp = server.serve(text);
                    write(resp)
                    shutdown();
                    continue;
                }

                var corsHeader = "access-control-allow-origin: http://localhost:8310\n"
                if (ARG_DISABLE_CORS) corsHeader = "access-control-allow-origin: *\n"

                if (text.startsWith("GET /")) {
                    println(text);
                    write("HTTP/1.0 200 OK\n" +
                            corsHeader +
                            "Content-type: text/html; charset=UTF-8\n")
                    val text2 = text.split(' ')
                    val values = text2[1].split('/')
                    val result = executor.execute(
                        values[1],
                        values.elementAtOrNull(2),
                        values.elementAtOrNull(3),
                        values.elementAtOrNull(4),
                        values.elementAtOrNull(5)
                    )
                    write(result)
                    shutdown();
                    continue;
                }

                val values = text.split(' ')
                val result = executor.execute(
                    values[0],
                    values.elementAtOrNull(1),
                    values.elementAtOrNull(2),
                    values.elementAtOrNull(3),
                    values.elementAtOrNull(4),
                )
                write(result)
            } catch (ex: Exception) {
                // TODO: Implement exception handling
                println("Exception handling '" + text + "': ")
                println(ex)
                write(helperJsonResponseFailure("Exception handling '" + text + "': " + ex.toString()))
                shutdown()
            } finally {

            }

        }
    }

    private fun write(message: String) {
        writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun shutdown() {
        running = false
        client.close()
        // println("${client.inetAddress.hostAddress} closed the connection")
    }

}