status: alpha (not ready for production)

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

So currently repo has ldk-server (compiled from kotlin, considered a lower level), which is supposed to run and provide RPC, and a cli nodejs
process which controls it (considered upper level). Cli process utilizes following apis:

* https://github.com/Blockstream/esplora/blob/master/API.md to fetch blockchain data
* https://1ml.com/api to fetch ip addresses for other ln nodes pubkeys

Whole setup is thus quite lightweight.

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

## Storage

Data is written in `~/.hellolightning` (non-configurable).
There are files with states per each channel, and one for the channel manager.
Upon the first launch of cli control script, given java process is running, HelloLightning will be seeded
with a secure entropy, which is then stored in `seed.txt`.
All events that must be passed from lower level (lightning) to upper level (control script) are served through
their respective RPCs, and also stored as json files.

## License

MIT

