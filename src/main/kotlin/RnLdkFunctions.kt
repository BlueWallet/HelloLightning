/**
 * Do not modify!
 * Those functions are directly copypasted from
 * https://github.com/BlueWallet/rn-ldk/blob/master/android/src/main/java/com/rnldk/RnLdkModule.kt
 *
 * When adding a new function, don't forget to make an interface for it in Executor.kt
 */
import org.ldk.structs.ChannelDetails
import org.ldk.structs.Option_u16Z
import org.ldk.structs.Option_u32Z
import org.ldk.structs.Option_u64Z
import java.io.IOException
import java.net.InetSocketAddress

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