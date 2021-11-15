# Hello, Lightning!

Cli lightning network server, based on LDK (rust-lightning).
Provides DUMB-RPC (telnet friendly) and HTTP interface.

## Example:

* build it
* run it: `java -jar ./out/artifacts/hello_main_jar/hello.main.jar`
* now DUMB-RPC/HTTP server listens on port 8310
* start the server with `start.sh` script
* ...or run nodejs control process from `./cli/` directory (`npm i && npm start`)

## Philosophy

Barebone Java-based server cant do much, out of the box it can only do lightning peers networking and disk persistence.
All the functionality should be implemented on upper level (like, GUI application, or nodejs cli script, etc), that 
includes: providing blockchain data, managing onchain coins to open channels (via PSBT), keeping a list of peers to keep connections etc

## Security

Server is intended to run in a secure environment. Thus, on-disk storage is not encrypted, and RPC server
handles connections without TLS (plain HTTP)

## TODO

* ~~port methods from https://github.com/BlueWallet/rn-ldk/blob/master/android/src/main/java/com/rnldk/RnLdkModule.kt while adding DUMB-RPC interface for them~~
* create a GUI app (Electron?)
* create a cli controll process
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

