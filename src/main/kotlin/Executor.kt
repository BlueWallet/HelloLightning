import java.io.File
import kotlin.system.exitProcess

class Executor {
    fun execute(command: String, arg1: String?, arg2: String?, arg3: String?, arg4: String? = null, arg5: String? = null): String {
        when (command) {
            "help" -> return "Welcome to the Hello Lightning server!\n" +
                    "To exit, type: 'EXIT'.\n" +
                    "Available commands: 'start', 'stop', 'connectpeer', 'listpeers', 'ldkversion', 'help', 'getnodeid', 'setfeerate'\n" +
                    "                    'listchannels'"
            "stop" -> exitProcess(0)
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
                    override fun resolve(var1: String) {}
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
            "getnodeid" -> {
                var retValue = "";
                getNodeId(object : Promise {
                    override fun reject(var1: String) { retValue = var1; }
                    override fun resolve(var1: String) { retValue = var1; }
                    override fun resolve(var1: Boolean) {}
                })
                return retValue
            }
            "listchannels" -> {
                var retValue = "";
                listChannels(object : Promise {
                    override fun reject(var1: String) { retValue = var1; }
                    override fun resolve(var1: String) { retValue = var1; }
                    override fun resolve(var1: Boolean) {}
                })
                return retValue
            }
            "setfeerate" -> {
                if (arg1 == null || arg2 == null || arg3 == null) return "incorrect arguments"
                var retValue = false;
                setFeerate(arg1.toInt(), arg2.toInt(), arg3.toInt(), object : Promise {
                    override fun reject(var1: String) {}
                    override fun resolve(var1: String) {}
                    override fun resolve(var1: Boolean) { retValue = var1; }
                })
                return retValue.toString();
            }
            else -> {
                return "unknown command: $command"
            }
        }
    }
}