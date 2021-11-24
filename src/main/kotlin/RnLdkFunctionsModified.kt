/**
 * Unlike RnLdkFunctions.kt, in this file functions are either heavily modified or brand new,
 * so it is safe to edit this file
 */
import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.enums.ConfirmationTarget
import org.ldk.enums.Network
import org.ldk.structs.*
import java.io.File
import java.net.InetSocketAddress
import java.util.ArrayList

fun openChannelStep1(pubkey: String, channelValue: Int, announced: Int, promise: Promise) {
    temporary_channel_id = null;
    val peer_node_pubkey = hexStringToByteArray(pubkey);

    var uc: UserConfig? = null;
    if (announced == 0) {
        // private aka unannounced channel
        uc = null
    } else {
        // public aka announced channel. such channels can route and thus have fees
        uc = UserConfig.with_default()
        val newChannelConfig = ChannelConfig.with_default()
        newChannelConfig.set_announced_channel(true);
        newChannelConfig.set_forwarding_fee_proportional_millionths(10000);
        newChannelConfig.set_forwarding_fee_base_msat(1000);
        uc.set_channel_options(newChannelConfig);
    }

    val create_channel_result = channel_manager?.create_channel(
        peer_node_pubkey, channelValue.toLong(), 0, 42, uc
    );

    if (create_channel_result !is Result__u832APIErrorZ.Result__u832APIErrorZ_OK) {
        println("ReactNativeLDK: " + "create_channel_result !is Result__u832APIErrorZ.Result__u832APIErrorZ_OK, = " + create_channel_result);
        promise.reject("openChannelStep1 failed");
        return;
    }

    promise.resolve(byteArrayToHex(create_channel_result.res));
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
            val params = WritableMap();
            params.putString("txhex", byteArrayToHex(txResult.res))
            storeEvent("$homedir/events_tx_broadcast", params)
            eventsTxBroadcast = eventsTxBroadcast.plus(params.toString())
        }
    }

    if (event is Event.PaymentSent) {
        println("ReactNativeLDK: " + "payment sent, preimage: " + byteArrayToHex((event as Event.PaymentSent).payment_preimage));
        val params = WritableMap()
        params.putString("payment_preimage", byteArrayToHex((event as Event.PaymentSent).payment_preimage));
        storeEvent("$homedir/events_payment_sent", params)
        eventsPaymentSent = eventsPaymentSent.plus(params.toString())
    }

    if (event is Event.PaymentPathFailed) {
        println("ReactNativeLDK: " + "payment failed, payment_hash: " + byteArrayToHex(event.payment_hash));
        val params = WritableMap()
        params.putString("payment_hash", byteArrayToHex(event.payment_hash));
        params.putString("rejected_by_dest", event.rejected_by_dest.toString());
        storeEvent("$homedir/events_payment_path_failed", params)
        eventsPaymentPathFailed = eventsPaymentPathFailed.plus(params.toString())
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

        val params = WritableMap()
        params.putString("payment_hash", byteArrayToHex(event.payment_hash));
        if (paymentSecret != null) {
            params.putString("payment_secret", byteArrayToHex(paymentSecret));
        }
        if (paymentPreimage != null) {
            params.putString("payment_preimage", byteArrayToHex(paymentPreimage));
        }
        params.putString("amt", event.amt.toString());
        storeEvent("$homedir/events_payment_received", params)
        eventsPaymentReceived = eventsPaymentReceived.plus(params.toString())
    }

    if (event is Event.PendingHTLCsForwardable) {
        channel_manager?.process_pending_htlc_forwards();
    }

    if (event is Event.FundingGenerationReady) {
        println("ReactNativeLDK: " + "FundingGenerationReady");
        val funding_spk = event.output_script;
        if (funding_spk.size == 34 && funding_spk[0].toInt() == 0 && funding_spk[1].toInt() == 32) {
            val params = WritableMap()
            params.putString("channel_value_satoshis", event.channel_value_satoshis.toString());
            params.putString("output_script", byteArrayToHex(event.output_script));
            params.putString("temporary_channel_id", byteArrayToHex(event.temporary_channel_id));
            params.putString("user_channel_id", event.user_channel_id.toString());
            temporary_channel_id = event.temporary_channel_id;
            storeEvent("$homedir/events_funding_generation_ready", params)
            eventsFundingGenerationReady = eventsFundingGenerationReady.plus(params.toString())
        }
    }

    if (event is Event.PaymentForwarded) {
        val params = WritableMap()
        params.putString("fee_earned_msat", event.fee_earned_msat.toString())
        if (event.claim_from_onchain_tx) params.putString("claim_from_onchain_tx", "1")
        storeEvent("$homedir/events_payment_forwarded", params)
        eventsPaymentForwarded = eventsPaymentForwarded.plus(params.toString())
    }

    if (event is Event.ChannelClosed) {
        println("ReactNativeLDK: " + "ChannelClosed");
        val params = WritableMap()
        val reason = event.reason;
        params.putString("channel_id", byteArrayToHex(event.channel_id));
        params.putString("user_channel_id", event.user_channel_id.toString());

        if (reason is ClosureReason.CommitmentTxConfirmed) {
            params.putString("reason", "CommitmentTxConfirmed");
        }
        if (reason is ClosureReason.CooperativeClosure) {
            params.putString("reason", "CooperativeClosure");
        }
        if (reason is ClosureReason.CounterpartyForceClosed) {
            params.putString("reason", "CounterpartyForceClosed");
            params.putString("text", reason.peer_msg);
        }
        if (reason is ClosureReason.DisconnectedPeer) {
            params.putString("reason", "DisconnectedPeer");
        }
        if (reason is ClosureReason.HolderForceClosed) {
            params.putString("reason", "HolderForceClosed");
        }
        if (reason is ClosureReason.OutdatedChannelManager) {
            params.putString("reason", "OutdatedChannelManager");
        }
        if (reason is ClosureReason.ProcessingError) {
            params.putString("reason", "ProcessingError");
            params.putString("text", reason.err);
        }
        storeEvent("$homedir/events_channel_closed", params)
        eventsChannelClosed = eventsChannelClosed.plus(params.toString())
    }
}


fun payInvoice(bolt11: String, amtSat: Int, promise: Promise) {
    if (channel_manager_constructor?.payer == null) return promise.reject("payer is null");

    val parsedInvoice = Invoice.from_str(bolt11)
    if (parsedInvoice !is Result_InvoiceNoneZ.Result_InvoiceNoneZ_OK) {
        return promise.reject("cant parse invoice");
    }

    val sendRes = if (amtSat != 0) {
        channel_manager_constructor!!.payer!!.pay_zero_value_invoice(parsedInvoice.res, amtSat.toLong() * 1000)
    } else {
        channel_manager_constructor!!.payer!!.pay_invoice(parsedInvoice.res)
    }

    if (sendRes !is Result_PaymentIdPaymentErrorZ.Result_PaymentIdPaymentErrorZ_OK) {
        return promise.reject("send failed");
    }

    promise.resolve("true");
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
        val params = WritableMap();
        params.putString("txhex", byteArrayToHex(tx))
        storeEvent("$homedir/events_tx_broadcast", params)
        eventsTxBroadcast = eventsTxBroadcast.plus(params.toString())
    }

    // INITIALIZE PERSIST ##########################################################################
    // What it's used for: persisting crucial channel data in a timely manner
    val persister = Persist.new_impl(object : Persist.PersistInterface {
        override fun persist_new_channel(id: OutPoint?, data: ChannelMonitor?, update_id: MonitorUpdateId?): Result_NoneChannelMonitorUpdateErrZ? {
            if (id == null || data == null) return null;
            val channel_monitor_bytes = data.write()
            println("ReactNativeLDK: persist_new_channel")
            File(homedir + "/" + prefix_channel_monitor + byteArrayToHex(id.to_channel_id()) + ".hex").writeText(byteArrayToHex(channel_monitor_bytes));
            return Result_NoneChannelMonitorUpdateErrZ.ok();
        }

        override fun update_persisted_channel(id: OutPoint?, update: ChannelMonitorUpdate?, data: ChannelMonitor?, update_id: MonitorUpdateId?): Result_NoneChannelMonitorUpdateErrZ? {
            if (id == null || data == null) return null;
            val channel_monitor_bytes = data.write()
            println("ReactNativeLDK: update_persisted_channel");
            File(homedir + "/" + prefix_channel_monitor + byteArrayToHex(id.to_channel_id()) + ".hex").writeText(byteArrayToHex(channel_monitor_bytes));
            return Result_NoneChannelMonitorUpdateErrZ.ok();
        }
    })

    // now, initializing channel manager persister that is responsoble for backing up channel_manager bytes

    val channel_manager_persister = object : ChannelManagerConstructor.EventHandler {
        override fun handle_event(event: Event) {
            handleEvent(event);
        }

        override fun persist_manager(channel_manager_bytes: ByteArray?) {
            println("persist_manager");
            if (channel_manager_bytes != null) {
                File("$homedir/$prefix_channel_manager.hex").writeText(byteArrayToHex(channel_manager_bytes));
            }
        }
    }

    // INITIALIZE THE CHAINMONITOR #################################################################
    // What it's used for: monitoring the chain for lighting transactions that are relevant to our
    // node, and broadcasting force close transactions if need be

    // Filter allows LDK to let you know what transactions you should filter blocks for. This is
    // useful if you pre-filter blocks or use compact filters. Otherwise, LDK will need full blocks.
    tx_filter = Filter.new_impl(object : Filter.FilterInterface {
        override fun register_tx(txid: ByteArray, script_pubkey: ByteArray) {
            println("ReactNativeLDK: register_tx");
            val params = WritableMap()
            params.putString("txid", byteArrayToHex(txid))
            params.putString("script_pubkey", byteArrayToHex(script_pubkey))
            storeEvent("$homedir/events_register_tx", params)
            eventsRegisterTx = eventsRegisterTx.plus(params.toString())
        }

        override fun register_output(output: WatchedOutput): Option_C2Tuple_usizeTransactionZZ {
            println("ReactNativeLDK: register_output");
            val params = WritableMap()
            val blockHash = output._block_hash;
            if (blockHash is ByteArray) {
                params.putString("block_hash", byteArrayToHex(blockHash))
            }
            params.putString("index", output._outpoint._index.toString())
            params.putString("script_pubkey", byteArrayToHex(output._script_pubkey))
            storeEvent("$homedir/events_register_output", params)
            eventsRegisterOutput = eventsRegisterOutput.plus(params.toString())
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


    // initialize graph sync #########################################################################

    val f = File("$homedir/$prefix_network_graph");
    if (f.exists()) {
        println("loading network graph...")
        val serialized_graph = File("$homedir/$prefix_network_graph").readBytes()
        val readResult = NetworkGraph.read(serialized_graph)
        if (readResult is Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_OK) {
            router = readResult.res
            println("loaded network graph ok")
        } else {
            println("network graph load failed")
            if (readResult is Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_Err) {
                println(readResult.err);
            }
            // error, creating from scratch
            router = NetworkGraph.of(hexStringToByteArray("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f").reversedArray())
        }
    } else {
        // first run, creating from scratch
        router = NetworkGraph.of(hexStringToByteArray("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f").reversedArray())
    }

    /*val route_handler = NetGraphMsgHandler.of(
        router,
        Option_AccessZ.none(),
        logger
    )*/

    // INITIALIZE THE CHANNELMANAGER ###############################################################
    // What it's used for: managing channel state


    val scorer = LockableScore.of(Scorer.with_default().as_Score())

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
                router,
                tx_broadcaster,
                logger
            );
            channel_manager = channel_manager_constructor!!.channel_manager;
            channel_manager_constructor!!.chain_sync_completed(channel_manager_persister, scorer);
            peer_manager = channel_manager_constructor!!.peer_manager;
            nio_peer_handler = channel_manager_constructor!!.nio_peer_handler;
        } else {
            // fresh start

            // this is gona be fee policy for __incoming__ channels. they are set upfront globally:
            val uc = UserConfig.with_default()
            val newChannelConfig = ChannelConfig.with_default()
            newChannelConfig.set_forwarding_fee_proportional_millionths(10000);
            newChannelConfig.set_forwarding_fee_base_msat(1000);
            uc.set_channel_options(newChannelConfig);
            val newLim = ChannelHandshakeLimits.with_default()
            newLim.set_force_announced_channel_preference(false)
            uc.set_peer_channel_config_limits(newLim)
            //
            channel_manager_constructor = ChannelManagerConstructor(
                Network.LDKNetwork_Bitcoin,
                uc,
                hexStringToByteArray(blockchainTipHashHex),
                blockchainTipHeight,
                keys_manager?.as_KeysInterface(),
                fee_estimator,
                chain_monitor,
                router,
                tx_broadcaster,
                logger
            );
            channel_manager = channel_manager_constructor!!.channel_manager;
            channel_manager_constructor!!.chain_sync_completed(channel_manager_persister, scorer);
            peer_manager = channel_manager_constructor!!.peer_manager;
            nio_peer_handler = channel_manager_constructor!!.nio_peer_handler;
        }

        nio_peer_handler!!.bind_listener(InetSocketAddress("0.0.0.0", 9735))
//        promise.resolve("hello ldk");
    } catch (e: Exception) {
        println("ReactNativeLDK: can't start, " + e.message);
//        promise.reject(e.message);
    }
}