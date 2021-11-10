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
* [ ] disconnectByNodeId
* [ ] sendPayment
* [ ] addInvoice
* [x] listPeers
* [x] getNodeId
* [ ] closeChannelCooperatively
* [ ] closeChannelForce
* [x] openChannelStep1
* [x] openChannelStep2
* [ ] listUsableChannels
* [x] listChannels
* [x] setRefundAddressScript
* [x] setFeerate
* [ ] getMaturingBalance
* [ ] getMaturingHeight
* [x] savenetworkgraph
* [x] geteventsfundinggenerationready
* [x] geteventschannelclosed
* [x] ldkversion
* [x] help

* [ ] register_tx event
* [ ] register_output event
* [ ] tx_broadcaster event
* [ ] payment sent event
* [ ] payment failed event
* [ ] payment received event
* [ ] Event.PaymentForwarded
* [ ] Event.ChannelClosed
* [ ] open channel - default un-anounced channel..?

## License

MIT

