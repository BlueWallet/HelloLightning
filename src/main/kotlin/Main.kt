import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.batteries.ChannelManagerConstructor.EventHandler
import org.ldk.batteries.NioPeerHandler
import org.ldk.enums.ConfirmationTarget
import org.ldk.enums.Network
import org.ldk.structs.*
import org.ldk.structs.Filter.FilterInterface
import org.ldk.structs.Persist.PersistInterface
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

var homedir = ""
val prefix_channel_monitor = "channel_monitor_"
val prefix_channel_manager = "channel_manager"


// borrowed from JS:
const val MARKER_LOG = "log";
const val MARKER_REGISTER_OUTPUT = "marker_register_output";
const val MARKER_REGISTER_TX = "register_tx";
const val MARKER_BROADCAST = "broadcast";
const val MARKER_PERSIST = "persist";
const val MARKER_PAYMENT_SENT = "payment_sent";
const val MARKER_PAYMENT_FAILED = "payment_failed";
const val MARKER_PAYMENT_RECEIVED = "payment_received";
const val MARKER_PERSIST_MANAGER = "persist_manager";
const val MARKER_FUNDING_GENERATION_READY = "funding_generation_ready";
const val MARKER_CHANNEL_CLOSED = "channel_closed";
//

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

fun main(args: Array<String>) {
    println("Hello Lightning!    7")
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
        println("Client connected: ${client.inetAddress.hostAddress}")
        // Run client in it's own thread.
        thread { ClientHandler(client).run() }
    }
}

fun start(
    entropyHex: String,
    blockchainTipHeight: Int,
    blockchainTipHashHex: String,
    serializedChannelManagerHex: String,
    monitorHexes: String
) {
    println("ReactNativeLDK: " + "start")

    // INITIALIZE THE FEEESTIMATOR #################################################################
    // What it's used for: estimating fees for on-chain transactions that LDK wants broadcasted.
    val fee_estimator = FeeEstimator.new_impl { confirmation_target: ConfirmationTarget? ->
        var ret = feerate_fast;
        if (confirmation_target != null) {
            if (confirmation_target.equals(ConfirmationTarget.LDKConfirmationTarget_HighPriority)) ret = feerate_fast;
            if (confirmation_target.equals(ConfirmationTarget.LDKConfirmationTarget_Normal)) ret = feerate_medium;
            if (confirmation_target.equals(ConfirmationTarget.LDKConfirmationTarget_Background)) ret = feerate_slow;
        }
        return@new_impl ret;
    }

    // INITIALIZE THE LOGGER #######################################################################
    // What it's used for: LDK logging
    val logger = Logger.new_impl { arg: String? ->
        println("ReactNativeLDK: " + arg)
//        val params = Arguments.createMap()
//        params.putString("line", arg)
//        sendEvent(MARKER_LOG, params)
    }

    // INITIALIZE THE BROADCASTERINTERFACE #########################################################
    // What it's used for: broadcasting various lightning transactions
    val tx_broadcaster = BroadcasterInterface.new_impl { tx ->
        println("ReactNativeLDK: " + "broadcaster sends an event asking to broadcast some txhex...")
//        val params = Arguments.createMap()
//        params.putString("txhex", byteArrayToHex(tx))
//        sendEvent(MARKER_BROADCAST, params)
    }

    // INITIALIZE PERSIST ##########################################################################
    // What it's used for: persisting crucial channel data in a timely manner
    val persister = Persist.new_impl(object : PersistInterface {
        override fun persist_new_channel(id: OutPoint, data: ChannelMonitor): Result_NoneChannelMonitorUpdateErrZ {
            val channel_monitor_bytes = data.write()
            println("ReactNativeLDK: persist_new_channel")
            File(homedir + "/" + byteArrayToHex(id.to_channel_id())).writeText(byteArrayToHex(channel_monitor_bytes));
            return Result_NoneChannelMonitorUpdateErrZ.ok();
        }

        override fun update_persisted_channel(
            id: OutPoint,
            update: ChannelMonitorUpdate,
            data: ChannelMonitor
        ): Result_NoneChannelMonitorUpdateErrZ {
            val channel_monitor_bytes = data.write()
            println("ReactNativeLDK: update_persisted_channel");
            File(homedir + "/" + prefix_channel_monitor + byteArrayToHex(id.to_channel_id())).writeText(byteArrayToHex(channel_monitor_bytes));
            return Result_NoneChannelMonitorUpdateErrZ.ok();
        }
    })

    // now, initializing channel manager persister that is responsoble for backing up channel_manager bytes

    val channel_manager_persister = object : EventHandler {
        override fun handle_event(event: Event) {
            handleEvent(event);
        }

        override fun persist_manager(channel_manager_bytes: ByteArray?) {
            println("persist_manager");
            if (channel_manager_bytes != null) {
                File("$homedir/$prefix_channel_manager").writeText(byteArrayToHex(channel_manager_bytes));
            }
        }
    }

    // INITIALIZE THE CHAINMONITOR #################################################################
    // What it's used for: monitoring the chain for lighting transactions that are relevant to our
    // node, and broadcasting force close transactions if need be

    // Filter allows LDK to let you know what transactions you should filter blocks for. This is
    // useful if you pre-filter blocks or use compact filters. Otherwise, LDK will need full blocks.
    val tx_filter: Filter? = Filter.new_impl(object : FilterInterface {
        override fun register_tx(txid: ByteArray, script_pubkey: ByteArray) {
            println("ReactNativeLDK: register_tx");
//            val params = Arguments.createMap()
//            params.putString("txid", byteArrayToHex(txid))
//            params.putString("script_pubkey", byteArrayToHex(script_pubkey))
//            sendEvent(MARKER_REGISTER_TX, params);
        }

        override fun register_output(output: WatchedOutput): Option_C2Tuple_usizeTransactionZZ {
            println("ReactNativeLDK: register_output");
//            val params = Arguments.createMap()
//            val blockHash = output._block_hash;
//            if (blockHash is ByteArray) {
//                params.putString("block_hash", byteArrayToHex(blockHash))
//            }
//            params.putString("index", output._outpoint._index.toString())
//            params.putString("script_pubkey", byteArrayToHex(output._script_pubkey))
//            sendEvent(MARKER_REGISTER_OUTPUT, params);
            return Option_C2Tuple_usizeTransactionZZ.none();
        }
    })

    val filter = Option_FilterZ.some(tx_filter);
    System.out.println(org.ldk.impl.version.get_ldk_java_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_c_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_version());

    chain_monitor = ChainMonitor.of(filter, tx_broadcaster, logger, fee_estimator, persister);

    // INITIALIZE THE KEYSMANAGER ##################################################################
    // What it's used for: providing keys for signing lightning transactions
    keys_manager = KeysManager.of(
        hexStringToByteArray(entropyHex),
        System.currentTimeMillis() / 1000,
        (System.currentTimeMillis() * 1000).toInt()
    )

    // READ CHANNELMONITOR STATE FROM DISK #########################################################

    // Initialize the hashmap where we'll store the `ChannelMonitor`s read from disk.
    // This hashmap will later be given to the `ChannelManager` on initialization.


    var channelMonitors = arrayOf<ByteArray>();
    if (monitorHexes != "") {
        println("ReactNativeLDK: initing channel monitors...");
        val channelMonitorHexes = monitorHexes.split(",").toTypedArray();
        val channel_monitor_list = ArrayList<ByteArray>()
        channelMonitorHexes.iterator().forEach {
            val channel_monitor_bytes = hexStringToByteArray(it);
            channel_monitor_list.add(channel_monitor_bytes);
        }
        channelMonitors = channel_monitor_list.toTypedArray();
    }

    // INITIALIZE THE CHANNELMANAGER ###############################################################
    // What it's used for: managing channel state


    try {
        if (serializedChannelManagerHex != "") {
            // loading from disk
            channel_manager_constructor = ChannelManagerConstructor(
                hexStringToByteArray(serializedChannelManagerHex),
                channelMonitors,
                keys_manager?.as_KeysInterface(),
                fee_estimator,
                chain_monitor,
                tx_filter,
                null,
                tx_broadcaster,
                logger
            );
            channel_manager = channel_manager_constructor!!.channel_manager;
            channel_manager_constructor!!.chain_sync_completed(channel_manager_persister);
            peer_manager = channel_manager_constructor!!.peer_manager;
            nio_peer_handler = channel_manager_constructor!!.nio_peer_handler;
        } else {
            // fresh start
            channel_manager_constructor = ChannelManagerConstructor(
                Network.LDKNetwork_Bitcoin,
                UserConfig.with_default(),
                hexStringToByteArray(blockchainTipHashHex),
                blockchainTipHeight,
                keys_manager?.as_KeysInterface(),
                fee_estimator,
                chain_monitor,
                null,
                tx_broadcaster,
                logger
            );
            channel_manager = channel_manager_constructor!!.channel_manager;
            channel_manager_constructor!!.chain_sync_completed(channel_manager_persister);
            peer_manager = channel_manager_constructor!!.peer_manager;
            nio_peer_handler = channel_manager_constructor!!.nio_peer_handler;
        }
//        promise.resolve("hello ldk");
    } catch (e: Exception) {
        println("ReactNativeLDK: can't start, " + e.message);
//        promise.reject(e.message);
    }
}


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


fun sendEvent(eventName: String) {
    // nop
}


fun handleEvent(event: Event) {
    if (event is Event.SpendableOutputs) {
        println("ReactNativeLDK: " + "trying to spend output");
        val txResult = keys_manager?.spend_spendable_outputs(
            event.outputs,
            emptyArray<TxOut>(),
            hexStringToByteArray(refund_address_script),
            feerate_fast
        );

        if (txResult is Result_TransactionNoneZ.Result_TransactionNoneZ_OK) {
            // success building the transaction, passing it to outer code to broadcast
//            val params = Arguments.createMap();
//            params.putString("txhex", byteArrayToHex(txResult.res))
//            this.sendEvent(MARKER_BROADCAST, params)
        }
    }

    if (event is Event.PaymentSent) {
        println("ReactNativeLDK: " + "payment sent, preimage: " + byteArrayToHex((event as Event.PaymentSent).payment_preimage));
//        val params = Arguments.createMap();
//        params.putString("payment_preimage", byteArrayToHex((event as Event.PaymentSent).payment_preimage));
//        this.sendEvent(MARKER_PAYMENT_SENT, params);
    }

    if (event is Event.PaymentPathFailed) {
        println("ReactNativeLDK: " + "payment failed, payment_hash: " + byteArrayToHex(event.payment_hash));
//        val params = Arguments.createMap();
//        params.putString("payment_hash", byteArrayToHex(event.payment_hash));
//        params.putString("rejected_by_dest", event.rejected_by_dest.toString());
//        this.sendEvent(MARKER_PAYMENT_FAILED, params);
    }

    if (event is Event.PaymentReceived) {
        println("ReactNativeLDK: " + "payment received, payment_hash: " + byteArrayToHex(event.payment_hash));
        var paymentPreimage: ByteArray? = null;
        var paymentSecret: ByteArray? = null;

        if (event.purpose is PaymentPurpose.InvoicePayment) {
            paymentPreimage = (event.purpose as PaymentPurpose.InvoicePayment).payment_preimage;
            paymentSecret = (event.purpose as PaymentPurpose.InvoicePayment).payment_secret;
            channel_manager?.claim_funds(paymentPreimage);
        } else if (event.purpose is PaymentPurpose.SpontaneousPayment) {
            paymentPreimage = (event.purpose as PaymentPurpose.SpontaneousPayment).spontaneous_payment;
            channel_manager?.claim_funds(paymentPreimage);
        }

//        val params = Arguments.createMap();
//        params.putString("payment_hash", byteArrayToHex(event.payment_hash));
//        if (paymentSecret != null) {
//            params.putString("payment_secret", byteArrayToHex(paymentSecret));
//        }
//        if (paymentPreimage != null) {
//            params.putString("payment_preimage", byteArrayToHex(paymentPreimage));
//        }
//        params.putString("amt", event.amt.toString());
//        this.sendEvent(MARKER_PAYMENT_RECEIVED, params);
    }

    if (event is Event.PendingHTLCsForwardable) {
        channel_manager?.process_pending_htlc_forwards();
    }

    if (event is Event.FundingGenerationReady) {
        println("ReactNativeLDK: " + "FundingGenerationReady");
        val funding_spk = event.output_script;
        if (funding_spk.size == 34 && funding_spk[0].toInt() == 0 && funding_spk[1].toInt() == 32) {
//            val params = Arguments.createMap();
//            params.putString("channel_value_satoshis", event.channel_value_satoshis.toString());
//            params.putString("output_script", byteArrayToHex(event.output_script));
//            params.putString("temporary_channel_id", byteArrayToHex(event.temporary_channel_id));
//            params.putString("user_channel_id", event.user_channel_id.toString());
            temporary_channel_id = event.temporary_channel_id;
//            this.sendEvent(MARKER_FUNDING_GENERATION_READY, params);
        }
    }

    if (event is Event.PaymentForwarded) {
        // todo. one day, when ldk is a full routing node...
    }

    if (event is Event.ChannelClosed) {
        println("ReactNativeLDK: " + "ChannelClosed");
//        val params = Arguments.createMap();
//        val reason = event.reason;
//        params.putString("channel_id", byteArrayToHex(event.channel_id));
//        params.putString("user_channel_id", event.user_channel_id.toString());

//        if (reason is ClosureReason.CommitmentTxConfirmed) {
//            params.putString("reason", "CommitmentTxConfirmed");
//        }
//        if (reason is ClosureReason.CooperativeClosure) {
//            params.putString("reason", "CooperativeClosure");
//        }
//        if (reason is ClosureReason.CounterpartyForceClosed) {
//            params.putString("reason", "CounterpartyForceClosed");
//            params.putString("text", reason.peer_msg);
//        }
//        if (reason is ClosureReason.DisconnectedPeer) {
//            params.putString("reason", "DisconnectedPeer");
//        }
//        if (reason is ClosureReason.HolderForceClosed) {
//            params.putString("reason", "HolderForceClosed");
//        }
//        if (reason is ClosureReason.OutdatedChannelManager) {
//            params.putString("reason", "OutdatedChannelManager");
//        }
//        if (reason is ClosureReason.ProcessingError) {
//            params.putString("reason", "ProcessingError");
//            params.putString("text", reason.err);
//        }

//        this.sendEvent(MARKER_CHANNEL_CLOSED, params);
    }
}

fun connectPeer(pubkeyHex: String, hostname: String, port: Int, promise: Promise) {
    println("ReactNativeLDK: connecting to peer " + pubkeyHex);
    try {
        nio_peer_handler?.connect(hexStringToByteArray(pubkeyHex), InetSocketAddress(hostname, port), 9000);
        promise.resolve(true)
    } catch (e: IOException) {
        promise.reject("connectPeer exception: " + e.message);
    }
}

fun listPeers(promise: Promise) {
    if (peer_manager === null) {
        promise.reject("no peer manager inited");
        return;
    }
    val peer_node_ids: Array<ByteArray> = peer_manager!!.get_peer_node_ids()
    var json: String = "[";
    var first = true;
    peer_node_ids.iterator().forEach {
        if (!first) json += ",";
        first = false;
        json += "\"" + byteArrayToHex(it) + "\"";
    }
    json += "]";
    promise.resolve(json);
}



class ClientHandler(client: Socket) {
    private val client: Socket = client
    private val reader: Scanner = Scanner(client.getInputStream())
    private val writer: OutputStream = client.getOutputStream()
    private val executor: Executor = Executor()
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

                if (text.startsWith("GET /")) {
                    println(text);
                    write("HTTP/1.0 200 OK\n" +
                            "Content-type: text/html; charset=UTF-8\n")
                    val text2 = text.split(' ')
                    val values = text2[1].split('/')
                    val result = executor.execute(
                        values[1],
                        values.elementAtOrNull(2),
                        values.elementAtOrNull(3),
                        values.elementAtOrNull(4)
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
                    values.elementAtOrNull(3)
                )
                write(result)
            } catch (ex: Exception) {
                // TODO: Implement exception handling
                println("Exception handling '" + text + "': " + ex.message)
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
        println("${client.inetAddress.hostAddress} closed the connection")
    }

}

class Executor {
    fun execute(command: String, arg1: String?, arg2: String?, arg3: String?, arg4: String? = null, arg5: String? = null): String {
        when (command) {
            "help" -> return "Welcome to the Hello Lightning server!\n" +
                        "To exit, type: 'EXIT'.\n" +
                        "Available commands: 'start', 'connectpeer', 'ldkversion', 'help'"
            "start" -> {
                if (arg1 == null || arg2 == null || arg3 == null) return "incorrect arguments"
                println("starting LDK... using " + arg1 + " " + arg2 + " " + arg3)
                var serializedChannelManager = ""
                var serializedMonitors = ""
                var monitors = arrayOf<String>()

                File(homedir).walk().forEach {
                    if (it.name.startsWith(prefix_channel_manager)) {
                        serializedChannelManager = it.absoluteFile.readText(Charsets.UTF_8)
                    }
                    if (it.name.startsWith(prefix_channel_monitor)) {
                        val serializedMonitor = it.absoluteFile.readText(Charsets.UTF_8);
                        monitors = monitors.plus(serializedMonitor)
                    }
                }

                serializedMonitors = monitors.joinToString(separator = ",")

                println("serializedChannelManager = " + serializedChannelManager)
                println("serializedMonitors = " + serializedMonitors)

                start(arg1, arg2.toInt(), arg3, serializedChannelManager, serializedMonitors)
                return "ok";
            }
            "ldkversion" -> return (org.ldk.impl.version.get_ldk_java_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_c_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_version())
            "connectpeer" -> {
                if (arg1 == null || arg2 == null || arg3 == null) return "incorrect arguments"
                var retValue = false;
                connectPeer(arg1, arg2, arg3.toInt(), object : Promise {
                    override fun reject(var1: String) { retValue = false }
                    override fun resolve(var1: String) { }
                    override fun resolve(var1: Boolean) { retValue = var1; }
                });
                return retValue.toString();
            }
            "listpeers" -> {
                var retValue = "";
                listPeers(object : Promise {
                    override fun reject(var1: String) { retValue = var1; }
                    override fun resolve(var1: String) { retValue = var1; }
                    override fun resolve(var1: Boolean) {}
                });
                return retValue;
            }
            else -> {
                return "Something went wrong"
            }
        }
    }
}

interface Promise {
    fun resolve(var1: String)
    fun resolve(var1: Boolean)
    fun reject(var1: String)
}

