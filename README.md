# Hello, Lightning!

Cli lightning network server, based on LDK (rust-lightning).
Provides DUMB-RPC (telnet friendly) and HTTP interface.

## Example:

* Build it
* run it: `java -jar ./out/artifacts/hello_main_jar/hello.main.jar`
* now DUMB-RPC/HTTP server listens on port 8310
* start the server with `start.sh` script

## Philosophy

Barebone cli server cant do much, out of the box it can only do lightning peers networking and disk persistence.
All the functionality should be implemented on upper level (like, GUI application), that includes: providing blockchain data,
managing onchain coins to open channels (via PSBT), keeping a list of peers to keep connections etc 

## TODO

* port methods from https://github.com/BlueWallet/rn-ldk/blob/master/android/src/main/java/com/rnldk/RnLdkModule.kt while adding DUMB-RPC interface for them
* create a GUI app (Electron?)
* ...
* Profit!

## Available DUMB-RPC/HTTP calls

* [x] start
* [x] stop
* [x] transactionConfirmed
* [x] transactionUnconfirmed
* [x] getRelevantTxids
* [x] updateBestBlock
* [x] connectPeer
* [x] disconnectByNodeId
* [x] sendPayment
* [x] addInvoice
* [x] listPeers
* [x] getNodeId
* [x] closeChannelCooperatively
* [x] closeChannelForce
* [x] openChannelStep1
* [x] openChannelStep2
* [x] listUsableChannels
* [x] listChannels
* [x] setRefundAddressScript
* [x] setFeerate
* [x] getMaturingBalance
* [x] getMaturingHeight
* [x] savenetworkgraph
* [x] geteventsfundinggenerationready
* [x] geteventschannelclosed
* [x] ldkversion
* [x] help
* [x] geteventsregistertx
* [x] geteventsregisteroutput
* [x] geteventstxbroadcast
* [x] geteventspaymentsent
* [x] geteventspaymentpathfailed
* [x] geteventspaymentreceived
* [x] geteventspaymentforwarded

## License

MIT

