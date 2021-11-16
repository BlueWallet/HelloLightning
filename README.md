# Hello, Lightning!

Cli lightning network server, based on LDK (rust-lightning).
Provides HTTP-RPC interface.

## Example:

* build it (or download binary from releases)
* run it: `java -jar ./out/artifacts/hello_main_jar/hello.main.jar`
* now HTTP server listens on port 8310
* run nodejs control process from `./cli/` directory (`npm i && npm start`)

## Philosophy

Barebone Java-based server cant do much, out of the box it can only do lightning peers networking and disk persistence.
All the functionality should be implemented on upper level (like, GUI application, or nodejs cli script, etc), that 
includes: providing blockchain data, managing onchain coins to open channels (via PSBT), keeping a list of peers to keep connections etc

## Security

Server is intended to run in a secure environment. Thus, on-disk storage is not encrypted, and RPC server
handles connections without TLS (plain HTTP). Also, even though RPC listens on 127.0.0.1, it has no auth.

## TODO

* ~~port methods from https://github.com/BlueWallet/rn-ldk/blob/master/android/src/main/java/com/rnldk/RnLdkModule.kt while adding DUMB-RPC interface for them~~
* create a GUI app (Electron?)
* ~~create a cli control process~~
* ...
* Profit!

## Available DUMB-RPC/HTTP calls

* [x] start
* [x] stop
* [x] transactionconfirmed
* [x] transactionunconfirmed
* [x] getrelevanttxids
* [x] updatebestblock
* [x] connectpeer
* [x] disconnectbynodeid
* [x] sendpayment
* [x] addinvoice
* [x] listpeers
* [x] getnodeid
* [x] closechannelcooperatively
* [x] closechannelforce
* [x] openchannelstep1
* [x] openchannelstep2
* [x] listusablechannels
* [x] listcChannels
* [x] setrefundaddressscript
* [x] setfeerate
* [x] getmaturingbalance
* [x] getmaturingheight
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

