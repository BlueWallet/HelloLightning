# Hello Lightning

Cli lightning network server, based on LDK (rust-lightning).
Provides DUMB-RPC interface (telnet friendly).

## Example:

* Build it
* run it: `java -jar ./out/artifacts/hello_main_jar/hello.main.jar`
* now DUMB-RPC server listens on port 8310
* start the server with `start.sh` script


## TODO

* port methods from https://github.com/BlueWallet/rn-ldk/blob/master/android/src/main/java/com/rnldk/RnLdkModule.kt while adding DUMB-RPC interface for them
* create a GUI app (Electron?)
* ...
* Profit!

## License

MIT

