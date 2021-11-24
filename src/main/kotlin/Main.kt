import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.batteries.NioPeerHandler
import org.ldk.structs.*
import java.io.File
import java.net.ServerSocket
import kotlin.concurrent.thread

var homedir = ""
val prefix_channel_monitor = "channel_monitor_"
val prefix_channel_manager = "channel_manager"
val prefix_network_graph = "network_graph.bin"

var feerate_fast = 7500; // estimate fee rate in BTC/kB
var feerate_medium = 7500; // estimate fee rate in BTC/kB
var feerate_slow = 7500; // estimate fee rate in BTC/kB

var refund_address_script = "76a91419129d53e6319baf19dba059bead166df90ab8f588ac";

var nio_peer_handler: NioPeerHandler? = null;
var channel_manager: ChannelManager? = null;
var peer_manager: PeerManager? = null;
var chain_monitor: ChainMonitor? = null;
var temporary_channel_id: ByteArray? = null;
var keys_manager: KeysManager? = null;
var channel_manager_constructor: ChannelManagerConstructor? = null;

var router: NetworkGraph? = null; // new for HelloLightning
var tx_filter: Filter? = null; // new for HelloLightning

var eventsChannelClosed: Array<String> = arrayOf<String>()
var eventsFundingGenerationReady: Array<String> = arrayOf<String>()
var eventsRegisterTx: Array<String> = arrayOf<String>()
var eventsRegisterOutput: Array<String> = arrayOf<String>()
var eventsTxBroadcast: Array<String> = arrayOf<String>()
var eventsPaymentSent: Array<String> = arrayOf<String>()
var eventsPaymentPathFailed: Array<String> = arrayOf<String>()
var eventsPaymentReceived: Array<String> = arrayOf<String>()
var eventsPaymentForwarded: Array<String> = arrayOf<String>()

fun main(args: Array<String>) {
    println("Hello Lightning!")
    homedir = System.getProperty("user.home") + "/.hellolightning";
    println("using " + homedir)

    val directory = File(homedir)
    if (!directory.exists()) {
        directory.mkdir()
    }

    val server = ServerSocket(8310)
    println("Server is running on port ${server.localPort}")

    while (true) {
        val client = server.accept()
//        println("Client connected: ${client.inetAddress.hostAddress}")
        // Run client in it's own thread.
        thread { ClientHandler(client).run() }
    }
}


