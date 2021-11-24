import java.io.File
import kotlin.system.exitProcess

class Executor {
    fun execute(command: String, arg1: String?, arg2: String?, arg3: String?, arg4: String? = null, arg5: String? = null): String {
        when (command) {
            "help" -> return helperJsonResponseSuccess("Welcome to the Hello Lightning server!")
            "stop" -> exitProcess(0)
            "start" -> {
                if (arg1 == null || arg2 == null || arg3 == null) return helperJsonResponseFailure("incorrect arguments")
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
                return helperJsonResponseSuccess("ok")
            }
            "ldkversion" -> return helperJsonResponseSuccess((org.ldk.impl.version.get_ldk_java_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_c_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_version()))
            "version" -> return helperJsonResponseSuccess("1.1.0")
            "connectpeer" -> {
                if (arg1 == null || arg2 == null || arg3 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = "";
                connectPeer(arg1, arg2, arg3.toInt(), object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                });
                return retValue;
            }
            "addinvoice" -> {
                if (arg1 == null || arg2 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = ""
                addInvoice(arg1.toInt(), arg2, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                });
                return retValue
            }
            "listpeers" -> {
                var retValue = "";
                listPeers(object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                });
                return retValue;
            }
            "getnodeid" -> {
                var retValue = "";
                getNodeId(object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "listchannels" -> {
                var retValue = "";
                listChannels(object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "listusablechannels" -> {
                var retValue = "";
                listUsableChannels(object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "getmaturingbalance" -> {
                var retValue = "";
                getMaturingBalance(object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "getmaturingheight" -> {
                var retValue = "";
                getMaturingHeight(object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "geteventschannelclosed" ->  {
                val ret = eventsChannelClosed.joinToString(separator = ",", prefix = "[", postfix = "]")
                eventsChannelClosed = arrayOf<String>()
                return helperJsonResponseSuccess(ret)
            }
            "geteventsfundinggenerationready" ->  {
                val ret = eventsFundingGenerationReady.joinToString(separator = ",", prefix = "[", postfix = "]")
                eventsFundingGenerationReady = arrayOf<String>()
                return helperJsonResponseSuccess(ret)
            }
            "geteventsregistertx" -> {
                val ret = eventsRegisterTx.joinToString(separator = ",", prefix = "[", postfix = "]")
                // NOT cleaning it up
                return helperJsonResponseSuccess(ret)
            }
            "geteventsregisteroutput" -> {
                val ret = eventsRegisterOutput.joinToString(separator = ",", prefix = "[", postfix = "]")
                // NOT cleaning it up
                return helperJsonResponseSuccess(ret)
            }
            "geteventstxbroadcast" ->  {
                val ret = eventsTxBroadcast.joinToString(separator = ",", prefix = "[", postfix = "]")
                eventsTxBroadcast = arrayOf<String>()
                return helperJsonResponseSuccess(ret)
            }
            "geteventspaymentsent" ->  {
                val ret = eventsPaymentSent.joinToString(separator = ",", prefix = "[", postfix = "]")
                eventsPaymentSent = arrayOf<String>()
                return helperJsonResponseSuccess(ret)
            }
            "geteventspaymentpathfailed" ->  {
                val ret = eventsPaymentPathFailed.joinToString(separator = ",", prefix = "[", postfix = "]")
                eventsPaymentPathFailed = arrayOf<String>()
                return helperJsonResponseSuccess(ret)
            }
            "geteventspaymentreceived" ->  {
                val ret = eventsPaymentReceived.joinToString(separator = ",", prefix = "[", postfix = "]")
                eventsPaymentReceived = arrayOf<String>()
                return helperJsonResponseSuccess(ret)
            }
            "geteventspaymentforwarded" ->  {
                val ret = eventsPaymentForwarded.joinToString(separator = ",", prefix = "[", postfix = "]")
                eventsPaymentForwarded = arrayOf<String>()
                return helperJsonResponseSuccess(ret)
            }
            "setfeerate" -> {
                if (arg1 == null || arg2 == null || arg3 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = "";
                setFeerate(arg1.toInt(), arg2.toInt(), arg3.toInt(), object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "payinvoice" -> {
                if (arg1 == null || arg2 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = "";
                payInvoice(arg1, arg2.toInt(), object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue;
            }
            "openchannelstep1" -> {
                if (arg1 == null || arg2 == null || arg3 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = "";
                openChannelStep1(arg1, arg2.toInt(), arg3.toInt(), object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue;
            }
            "openchannelstep2" -> {
                if (arg1 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = "";
                openChannelStep2(arg1, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue;
            }
            "closechannelcooperatively" -> {
                if (arg1 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = "";
                closeChannelCooperatively(arg1, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue;
            }
            "closeChannelForce" -> {
                if (arg1 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = "";
                closeChannelForce(arg1, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue;
            }
            "updatebestblock" -> {
                if (arg1 == null || arg2 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = ""
                updateBestBlock(arg1, arg2.toInt(), object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "transactionconfirmed" -> {
                if (arg1 == null || arg2 == null || arg3 == null || arg4 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = ""
                transactionConfirmed(arg1, arg2.toInt(), arg3.toInt(), arg4, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "transactionunconfirmed" -> {
                if (arg1 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = ""
                transactionUnconfirmed(arg1, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "disconnectbynodeid" -> {
                if (arg1 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = ""
                disconnectByNodeId(arg1, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "getrelevanttxids" -> {
                var retValue = ""
                getRelevantTxids(object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "setrefundaddressscript" -> {
                if (arg1 == null) return helperJsonResponseFailure("incorrect arguments")
                var retValue = ""
                setRefundAddressScript(arg1, object : Promise {
                    override fun resolve(var1: String) { retValue = helperJsonResponseSuccess(var1) }
                    override fun reject(var1: String) { retValue = helperJsonResponseFailure(var1) }
                    override fun resolve(var1: Boolean) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Boolean) { retValue = helperJsonResponseFailure(var1.toString()) }
                    override fun resolve(var1: Int) { retValue = helperJsonResponseSuccess(var1.toString()) }
                    override fun reject(var1: Int) { retValue = helperJsonResponseFailure(var1.toString()) }
                })
                return retValue
            }
            "savenetworkgraph" -> {
                File("$homedir/$prefix_network_graph").writeBytes(router!!.write());
                return helperJsonResponseSuccess("true")
            }
            else -> {
                return helperJsonResponseFailure("unknown command: $command")
            }
        }
    }
}