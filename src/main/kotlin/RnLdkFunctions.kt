/**
 * Do not modify!
 * Those functions are directly copypasted from
 * https://github.com/BlueWallet/rn-ldk/blob/master/android/src/main/java/com/rnldk/RnLdkModule.kt
 *
 * When adding a new function, don't forget to make an interface for it in Executor.kt
 */
import org.ldk.enums.Currency
import org.ldk.structs.*
import java.io.IOException
import java.net.InetSocketAddress

fun connectPeer(pubkeyHex: String, hostname: String, port: Int, promise: Promise) {
    println("ReactNativeLDK: connecting to peer " + pubkeyHex);
    try {
        nio_peer_handler!!.connect(hexStringToByteArray(pubkeyHex), InetSocketAddress(hostname, port), 9000);
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

fun getNodeId(promise: Promise) {
    val byteArr = channel_manager?._our_node_id;
    if (byteArr != null) {
        promise.resolve(byteArrayToHex(byteArr));
    } else {
        promise.reject("getNodeId failed");
    }
}

fun setFeerate(newFeerateFast: Int, newFeerateMedium: Int, newFeerateSlow: Int, promise: Promise) {
    if (newFeerateFast < 300) return promise.reject("newFeerateFast is too small");
    if (newFeerateMedium < 300) return promise.reject("newFeerateMedium is too small");
    if (newFeerateSlow < 300) return promise.reject("newFeerateSlow is too small");
    feerate_fast = newFeerateFast;
    feerate_medium = newFeerateMedium;
    feerate_slow = newFeerateSlow;
    promise.resolve(true);
}

fun channel2channelObject(it: ChannelDetails): String {
    var short_channel_id: Long = 0;
    if (it._short_channel_id is Option_u64Z.Some) {
        short_channel_id = (it._short_channel_id as Option_u64Z.Some).some
    }

    var confirmations_required = 0;
    if (it._confirmations_required is Option_u32Z.Some) {
        confirmations_required = (it._confirmations_required as Option_u32Z.Some).some;
    }

    var force_close_spend_delay: Short = 0;
    if (it._force_close_spend_delay is Option_u16Z.Some) {
        force_close_spend_delay = (it._force_close_spend_delay as Option_u16Z.Some).some;
    }

    var unspendable_punishment_reserve: Long = 0;
    if (it._unspendable_punishment_reserve is Option_u64Z.Some) {
        unspendable_punishment_reserve = (it._unspendable_punishment_reserve as Option_u64Z.Some).some;
    }

    var channelObject = "{";
    channelObject += "\"channel_id\":" + "\"" + byteArrayToHex(it._channel_id) + "\",";
    channelObject += "\"channel_value_satoshis\":" + it._channel_value_satoshis + ",";
    channelObject += "\"inbound_capacity_msat\":" + it._inbound_capacity_msat + ",";
    channelObject += "\"outbound_capacity_msat\":" + it._outbound_capacity_msat + ",";
    channelObject += "\"short_channel_id\":" + "\"" + short_channel_id + "\",";
    channelObject += "\"is_usable\":" + it._is_usable + ",";
    channelObject += "\"is_funding_locked\":" + it._is_funding_locked + ",";
    channelObject += "\"is_outbound\":" + it._is_outbound + ",";
    channelObject += "\"is_public\":" + it._is_public + ",";
    channelObject += "\"remote_node_id\":" + "\"" + byteArrayToHex(it._counterparty._node_id) + "\","; // @deprecated fixme
    val fundingTxoTxid = it._funding_txo?._txid;
    if (fundingTxoTxid is ByteArray) {
        channelObject += "\"funding_txo_txid\":" + "\"" + byteArrayToHex(fundingTxoTxid) + "\",";
    }
    val fundingTxoIndex = it._funding_txo?._index;
    if (fundingTxoIndex != null) {
        channelObject += "\"funding_txo_index\":" + fundingTxoIndex + ",";
    }
    channelObject += "\"counterparty_unspendable_punishment_reserve\":" + it._counterparty._unspendable_punishment_reserve + ",";
    channelObject += "\"counterparty_node_id\":" + "\"" + byteArrayToHex(it._counterparty._node_id) + "\",";
    channelObject += "\"unspendable_punishment_reserve\":" + unspendable_punishment_reserve + ",";
    channelObject += "\"confirmations_required\":" + confirmations_required + ",";
    channelObject += "\"force_close_spend_delay\":" + force_close_spend_delay + ",";
    channelObject += "\"user_id\":" + it._user_channel_id;
    channelObject += "}";

    return channelObject;
}

fun listChannels(promise: Promise) {
    if (channel_manager == null) {
        promise.reject("Channel manager not inited");
        return;
    }
    val channels = channel_manager?.list_channels();
    var jsonArray = "[";
    var first = true;
    channels?.iterator()?.forEach {
        val channelObject = channel2channelObject(it);

        if (!first) jsonArray += ",";
        jsonArray += channelObject;
        first = false;
    }
    jsonArray += "]";
    promise.resolve(jsonArray);
}

fun listUsableChannels(promise: Promise) {
    if (channel_manager == null) {
        promise.reject("Channel manager not inited");
        return;
    }

    val channels = channel_manager?.list_usable_channels();

    var jsonArray = "[";

    var first = true;
    channels?.iterator()?.forEach {
        val channelObject = channel2channelObject(it);

        if (!first) jsonArray += ",";
        jsonArray += channelObject;
        first = false;
    }

    jsonArray += "]";


    promise.resolve(jsonArray);
}


fun openChannelStep2(txhex: String, promise: Promise) {
    if (temporary_channel_id == null) return promise.reject("openChannelStep2 failed: channel opening is not initiated..?");

    val funding_res = channel_manager?.funding_transaction_generated(temporary_channel_id, hexStringToByteArray(txhex));
    // funding_transaction_generated should only generate an error if the
    // transaction didn't meet the required format (or the counterparty already
    // closed the channel on us):
    if (funding_res !is Result_NoneAPIErrorZ.Result_NoneAPIErrorZ_OK) {
        println("ReactNativeLDK: " + "funding_res !is Result_NoneAPIErrorZ_OK");
        promise.reject("openChannelStep2 failed");
        return;
    }

    // At this point LDK will exchange the remaining channel open messages with
    // the counterparty and, when appropriate, broadcast the funding transaction
    // provided.
    // Once it confirms, the channel will be open and available for use (indicated
    // by its presence in `channel_manager.list_usable_channels()`).

    promise.resolve(true);
}

fun updateBestBlock(headerHex: String, height: Int, promise: Promise) {
    channel_manager?.as_Confirm()?.best_block_updated(hexStringToByteArray(headerHex), height);
    chain_monitor?.as_Confirm()?.best_block_updated(hexStringToByteArray(headerHex), height);
    promise.resolve(true);
}

fun transactionConfirmed(headerHex: String, height: Int, txPos: Int, transactionHex: String, promise: Promise) {
    val tx = TwoTuple_usizeTransactionZ.of(txPos.toLong(), hexStringToByteArray(transactionHex))
    val txarray = arrayOf(tx);
    channel_manager?.as_Confirm()?.transactions_confirmed(hexStringToByteArray(headerHex), txarray, height);
    chain_monitor?.as_Confirm()?.transactions_confirmed(hexStringToByteArray(headerHex), txarray, height);

    promise.resolve(true);
}

fun transactionUnconfirmed(txidHex: String, promise: Promise) {
    channel_manager?.as_Confirm()?.transaction_unconfirmed(hexStringToByteArray(txidHex));
    chain_monitor?.as_Confirm()?.transaction_unconfirmed(hexStringToByteArray(txidHex));
    promise.resolve(true);
}

fun setRefundAddressScript(refundAddressScriptHex: String, promise: Promise) {
    refund_address_script = refundAddressScriptHex;
    promise.resolve(true);
}

fun addInvoice(amtMsat: Int, description: String, promise: Promise) {
    var amountStruct = Option_u64Z.none();
    if (amtMsat != 0) {
        amountStruct = Option_u64Z.some(amtMsat.toLong());
    }

    val invoice = UtilMethods.create_invoice_from_channelmanager(
        channel_manager,
        keys_manager?.as_KeysInterface(),
        Currency.LDKCurrency_Bitcoin,
        amountStruct,
        description
    );

    if (invoice is Result_InvoiceSignOrCreationErrorZ.Result_InvoiceSignOrCreationErrorZ_OK) {
        println("Got invoice: " + invoice.res.to_str())
        promise.resolve(invoice.res.to_str());
    } else {
        promise.reject("addInvoice failed");
    }
}

fun getRelevantTxids(promise: Promise) {
    if (channel_manager === null) {
        promise.reject("Channel manager is not initted");
        return;
    }
    var first = true;
    var json: String = "[";
    channel_manager?.as_Confirm()?._relevant_txids?.iterator()?.forEach {
        if (!first) json += ",";
        first = false;
        json += "\"" + byteArrayToHex(it.reversedArray()) + "\"";
    }
    chain_monitor?.as_Confirm()?._relevant_txids?.iterator()?.forEach {
        if (!first) json += ",";
        first = false;
        json += "\"" + byteArrayToHex(it.reversedArray()) + "\"";
    }
    json += "]";
    promise.resolve(json);
}

fun disconnectByNodeId(pubkeyHex: String, promise: Promise) {
    println("ReactNativeLDK: disconnecting peer " + pubkeyHex);
    try {
        peer_manager?.disconnect_by_node_id(hexStringToByteArray(pubkeyHex), false);
        promise.resolve(true);
    } catch (e: IOException) {
        promise.reject("disconnect_by_node_id exception: " + e.message);
    }
}

fun getMaturingBalance(promise: Promise) {
    var totalSat: Int = 0;
    val balances = chain_monitor?.get_claimable_balances(channel_manager!!.list_channels());
    balances!!.iterator().forEach {
        if (it is Balance.ClaimableAwaitingConfirmations) {
            println("ReactNativeLDK: ClaimableAwaitingConfirmations = " + it.claimable_amount_satoshis + " " + it.confirmation_height);
            totalSat += it.claimable_amount_satoshis.toInt();
        }

        if (it is Balance.ClaimableOnChannelClose) {
            println("ReactNativeLDK: ClaimableOnChannelClose = " + it.claimable_amount_satoshis);
            totalSat += it.claimable_amount_satoshis.toInt();
        }

        if (it is Balance.ContentiousClaimable) {
            println("ReactNativeLDK: ContentiousClaimable = " + it.claimable_amount_satoshis + " " + it.timeout_height);
        }

        if (it is Balance.MaybeClaimableHTLCAwaitingTimeout) {
            println("ReactNativeLDK: MaybeClaimableHTLCAwaitingTimeout = " + it.claimable_amount_satoshis + " " + it.claimable_height);
        }
    }

    promise.resolve(totalSat);
}

fun getMaturingHeight(promise: Promise) {
    var maxHeight: Int = 0;
    val balances = chain_monitor?.get_claimable_balances(channel_manager!!.list_channels());
    balances!!.iterator().forEach {
        if (it is Balance.ClaimableAwaitingConfirmations) {
            println("ReactNativeLDK: ClaimableAwaitingConfirmations = " + it.claimable_amount_satoshis + " " + it.confirmation_height);
            maxHeight = when (it.confirmation_height > maxHeight) {
                true -> it.confirmation_height
                false -> maxHeight
            }
        }

        if (it is Balance.ClaimableOnChannelClose) {
            println("ReactNativeLDK: ClaimableOnChannelClose = " + it.claimable_amount_satoshis);
        }

        if (it is Balance.ContentiousClaimable) {
            println("ReactNativeLDK: ContentiousClaimable = " + it.claimable_amount_satoshis + " " + it.timeout_height);
            maxHeight = when (it.timeout_height > maxHeight) {
                true -> it.timeout_height
                false -> maxHeight
            }
        }

        if (it is Balance.MaybeClaimableHTLCAwaitingTimeout) {
            println("ReactNativeLDK: MaybeClaimableHTLCAwaitingTimeout = " + it.claimable_amount_satoshis + " " + it.claimable_height);
            maxHeight = when (it.claimable_height > maxHeight) {
                true -> it.claimable_height
                false -> maxHeight
            }
        }
    }

    promise.resolve(maxHeight);
}


fun closeChannelCooperatively(channelIdHex: String, promise: Promise) {
    val close_result = channel_manager?.close_channel(hexStringToByteArray(channelIdHex))
    if (close_result is Result_NoneAPIErrorZ.Result_NoneAPIErrorZ_OK) {
        promise.resolve(true);
    } else {
        promise.reject("closeChannelCooperatively failed");
    }
}

fun closeChannelForce(channelIdHex: String, promise: Promise) {
    val close_result = channel_manager?.force_close_channel(hexStringToByteArray(channelIdHex));
    if (close_result is Result_NoneAPIErrorZ.Result_NoneAPIErrorZ_OK) {
        promise.resolve(true);
    } else {
        promise.reject("closeChannelForce failed");
    }
}