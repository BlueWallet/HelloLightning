/* eslint-disable no-empty,@typescript-eslint/no-inferrable-types,@typescript-eslint/no-var-requires */
import fetch from 'cross-fetch';
import * as bip39 from 'bip39';
const bitcoin = require('bitcoinjs-lib');
const HDNode = require('bip32');
const crypto = require('crypto');
const fs = require('fs');

export default class Ldk {
  private injectedScript2address: ((scriptHex: string) => Promise<string>) | null = null;
  private logs: string[] = [];
  private secret: string = '';
  private _nodeConnectionDetailsCache: any = {};

  logToGeneralLog(...args: any[]) {
    let str = new Date().toUTCString();
    args.map(arg => str += ' ' + JSON.stringify(arg));
    this.logs.push(str)
  }

  getLastLogsLines(num: number) {
    return this.logs.slice(num * -1);
  }

  private async getHeaderHexByHeight(height: number) {
    const response2 = await fetch('https://blockstream.info/api/block-height/' + height);
    const hash = await response2.text();
    const response3 = await fetch('https://blockstream.info/api/block/' + hash + '/header');
    return response3.text();
  }

  private async script2address(scriptHex: string): Promise<string> {
    if (this.injectedScript2address) {
      return await this.injectedScript2address(scriptHex);
    }

    const response = await fetch('https://runkit.io/overtorment/output-script-to-address/branches/master/' + scriptHex);
    return response.text();
  }

  private async getCurrentHeight() {
    const response = await fetch('https://blockstream.info/api/blocks/tip/height');
    return parseInt(await response.text(), 10);
  }


  private async updateBestBlock() {
    this.logToGeneralLog('updating best block');
    const height = await this.getCurrentHeight();
    const response2 = await fetch('https://blockstream.info/api/block-height/' + height);
    const hash = await response2.text();
    const response3 = await fetch('https://blockstream.info/api/block/' + hash + '/header');
    const headerHex = await response3.text();
    this.logToGeneralLog('updateBestBlock():', { headerHex, height });
    const response = await fetch(`http://127.0.0.1:8310/updatebestblock/${headerHex}/${height}`);
    const text = await response.text();
    return this._processResult(text);
  }

  async updateFeerate() {
    this.logToGeneralLog('updating feerate');
    try {
      const response = await fetch('https://blockstream.info/api/fee-estimates');
      const json = await response.json();

      const blockFast = '2'; // indexes in json object
      const blockMedium = '6';
      const blockSlow = '144';

      if (json[blockFast] && json[blockMedium] && json[blockSlow]) {
        const feerateFast = Math.round(json[blockFast]);
        const feerateMedium = Math.round(json[blockMedium]);
        const feerateSlow = Math.round(json[blockSlow]);
        await this.setFeerate(Math.max(feerateFast, 2), Math.max(feerateMedium, 1), Math.max(feerateSlow, 1));
      } else {
        throw new Error('Invalid feerate data:' + JSON.stringify(json));
      }
    } catch (error) {
      console.warn('updateFeerate() failed:', error);
      this.logToGeneralLog('updateFeerate() failed:', error);
    }
  }

  /**
   * Prodives LKD current feerate to use with all onchain transactions (like sweeps after forse-closures)
   *
   * @param newFeerateFast {number} Sat/b
   * @param newFeerateMedium {number} Sat/b
   * @param newFeerateSlow {number} Sat/b
   */
  private async setFeerate(newFeerateFast: number, newFeerateMedium: number, newFeerateSlow: number): Promise<boolean> {
    this.logToGeneralLog('setting feerate', { newFeerateFast, newFeerateMedium, newFeerateSlow });
    const fast = newFeerateFast * 250;
    const medium = newFeerateMedium * 250;
    const slow = newFeerateSlow * 250;
    const response = await fetch(`http://127.0.0.1:8310/setfeerate/${fast}/${medium}/${slow}`);
    const text = await response.text();
    return this._processResult(text);
  }

  /**
   * Fetches from network registered outputs, registered transactions and block tip
   * and feeds this into to native code, if necessary.
   * Should be called periodically.
   */
  async checkBlockchain(progressCallback?: (progress: number) => void) {
    this.logToGeneralLog('checkBlockchain() 1/x');
    if (progressCallback) progressCallback(1 / 8);
    await this.updateBestBlock();

    this.logToGeneralLog('checkBlockchain() 2/x');
    if (progressCallback) progressCallback(2 / 8);
    await this.updateFeerate();

    const confirmedBlocks: any = {};

    // iterating all subscriptions for confirmed txid
    this.logToGeneralLog('checkBlockchain() 3/x');
    if (progressCallback) progressCallback(3 / 8);
    for (const regTx of await this.getRegisteredTxs()) {
      let json;
      try {
        const response = await fetch('https://blockstream.info/api/tx/' + regTx.txid);
        json = await response.json();
      } catch (_) {}
      if (json && json.status && json.status.confirmed && json.status.block_height) {
        // success! tx confirmed, and we need to notify LDK about it

        let jsonPos;
        try {
          const responsePos = await fetch('https://blockstream.info/api/tx/' + regTx.txid + '/merkle-proof');
          jsonPos = await responsePos.json();
        } catch (_) {}

        if (jsonPos && jsonPos.merkle) {
          confirmedBlocks[json.status.block_height + ''] = confirmedBlocks[json.status.block_height + ''] || {};
          const responseHex = await fetch('https://blockstream.info/api/tx/' + regTx.txid + '/hex');
          confirmedBlocks[json.status.block_height + ''][jsonPos.pos + ''] = await responseHex.text();
        }
      }
    }

    // iterating all scripts for spends
    this.logToGeneralLog('checkBlockchain() 4/x');
    if (progressCallback) progressCallback(4 / 8);
    for (const regOut of await this.getRegisteredOutputs()) {
      let txs: any[] = [];
      try {
        const address = await this.script2address(regOut.script_pubkey);
        const response = await fetch('https://blockstream.info/api/address/' + address + '/txs');
        txs = await response.json();
      } catch (_) {}
      for (const tx of txs) {
        if (tx && tx.status && tx.status.confirmed && tx.status.block_height) {
          // got confirmed tx for that output!

          let jsonPos;
          try {
            const responsePos = await fetch('https://blockstream.info/api/tx/' + tx.txid + '/merkle-proof');
            jsonPos = await responsePos.json();
          } catch (_) {}

          if (jsonPos && jsonPos.merkle) {
            const responseHex = await fetch('https://blockstream.info/api/tx/' + tx.txid + '/hex');
            confirmedBlocks[tx.status.block_height + ''] = confirmedBlocks[tx.status.block_height + ''] || {};
            confirmedBlocks[tx.status.block_height + ''][jsonPos.pos + ''] = await responseHex.text();
          }
        }
      }
    }

    // now, got all data packed in `confirmedBlocks[block_number][tx_position]`
    // lets feed it to LDK:

    this.logToGeneralLog('confirmedBlocks=', confirmedBlocks);

    this.logToGeneralLog('checkBlockchain() 5/x');
    if (progressCallback) progressCallback(5 / 8);
    for (const height of Object.keys(confirmedBlocks).sort((a, b) => parseInt(a, 10) - parseInt(b, 10))) {
      for (const pos of Object.keys(confirmedBlocks[height]).sort((a, b) => parseInt(a, 10) - parseInt(b, 10))) {
        await this.transactionConfirmed(await this.getHeaderHexByHeight(parseInt(height, 10)), parseInt(height, 10), parseInt(pos, 10), confirmedBlocks[height][pos]);
      }
    }

    this.logToGeneralLog('checkBlockchain() 6/x');
    if (progressCallback) progressCallback(6 / 8);
    let txidArr = [];
    try {
      txidArr = await this.getRelevantTxids();
      this.logToGeneralLog('getRelevantTxids:', txidArr);
    } catch (error: any) {
      this.logToGeneralLog('getRelevantTxids:', error.message);
      console.warn('getRelevantTxids:', error.message);
    }

    // we need to check if any of txidArr got unconfirmed, and then feed it back to LDK if they are unconf
    this.logToGeneralLog('checkBlockchain() 7/x');
    if (progressCallback) progressCallback(7 / 8);
    for (const txid of txidArr) {
      let confirmed = false;
      try {
        const response = await fetch('https://blockstream.info/api/tx/' + txid + '/merkle-proof');
        const tx: any = await response.json();
        if (tx && tx.block_height) confirmed = true;
      } catch (_) {
        confirmed = false;
      }

      if (!confirmed) await this.transactionUnconfirmed(txid);
    }

    this.logToGeneralLog('checkBlockchain() done');
    if (progressCallback) progressCallback(8 / 8);

    return true;
  }

  private async getRelevantTxids() {
    const response = await fetch('http://127.0.0.1:8310/getrelevanttxids');
    const text = await response.text();
    return this._processResult(text)
  }

  private async transactionConfirmed(headerHex: string, height: number, pos: number, transactionHex: string) {
    const response = await fetch(`http://127.0.0.1:8310/transactionconfirmed/${headerHex}/${height}/${pos}/${transactionHex}`)
    const text = await response.text();
    return this._processResult(text)
  }

  private async transactionUnconfirmed(txid: string) {
    const response = await fetch(`http://127.0.0.1:8310/transactionunconfirmed/${txid}`);
    const text = await response.text();
    return this._processResult(text)
  }

  async broadcastTxsIfNecessary() {
    const txs = await this.getTxsBroadcast();
    for (const tx of txs) {
      this.logToGeneralLog('should broadcast', tx)
      const response = await fetch('https://blockstream.info/api/tx', {
        method: 'POST',
        body: tx.txhex
      });
      this.logToGeneralLog('broadcast result: ' + await response.text());
    }
  }

  async getTxsBroadcast() {
    const response = await fetch(`http://127.0.0.1:8310/geteventstxbroadcast`);
    const text = await response.text();
    return this._processResult(text)
  }

  private async getRegisteredTxs() {
    const response = await fetch(`http://127.0.0.1:8310/geteventsregistertx`);
    const text = await response.text();
    return this._processResult(text)
  }

  private async getRegisteredOutputs() {
    const response = await fetch(`http://127.0.0.1:8310/geteventsregisteroutput`);
    const text = await response.text();
    return this._processResult(text)
  }

  public async listPeers() {
    const response = await fetch(`http://127.0.0.1:8310/listpeers`);
    const text = await response.text();
    return this._processResult(text)
  }

  public async listChannels() {
    const response = await fetch(`http://127.0.0.1:8310/listchannels`);
    const text = await response.text();
    return this._processResult(text)
  }

  public async listUsableChannels() {
    const response = await fetch(`http://127.0.0.1:8310/listusablechannels`);
    const text = await response.text();
    return this._processResult(text)
  }

  public async getNodeId() {
    const response = await fetch(`http://127.0.0.1:8310/getnodeid`);
    const text = await response.text();
    return this._processResult(text);
  }

  public async start(entropy) {
    const tip = await this.getCurrentHeight();
    const response2 = await fetch('https://blockstream.info/api/block-height/' + tip);
    const hash = await response2.text();

    const response = await fetch(`http://127.0.0.1:8310/start/${entropy}/${tip}/${hash}`);
    const text = await response.text();
    return this._processResult(text);
  }

  private _processResult(text: string) {
    const json = JSON.parse(text);
    if (json.error) throw new Error(json.result);
    return json.result;
  }

  async generate() {
    const buf = await this.randomBytes(16);
    this.secret = '' + bip39.entropyToMnemonic(buf.toString('hex'));
  }

  getEntropyHex() {
    let ret = bip39.mnemonicToEntropy(this.secret.replace('', ''));
    while (ret.length < 64) ret = '0' + ret;
    return ret;
  }

  getSecret(): string {
    return this.secret;
  }

  setSecret(secret) {
    this.secret = secret;
  }

  async randomBytes(size): Promise<Buffer> {
    return new Promise((resolve, reject) => {
      crypto.randomBytes(size, (err, data) => {
        if (err) reject(err);
        else resolve(data);
      });
    });
  }

  async saveNetworkGraph() {
    this.logToGeneralLog('saving network graph to disk...');
    const response = await fetch(`http://127.0.0.1:8310/savenetworkgraph`);
    const text = await response.text();
    return this._processResult(text)
  }

  unwrapFirstExternalAddressFromMnemonics() {
    if (!this.getSecret()) throw new Error('no secret');
    const mnemonic = this.getSecret();
    const seed = bip39.mnemonicToSeedSync(mnemonic);
    const root = HDNode.fromSeed(seed);
    const path = "m/84'/0'/0'/0/0";
    const child = root.derivePath(path);

    return bitcoin.payments.p2wpkh({
      pubkey: child.publicKey,
    }).address;
  }

  unwrapFirstExternalWifFromMnemonics() {
    if (!this.getSecret()) throw new Error('no secret');
    const mnemonic = this.getSecret();
    const seed = bip39.mnemonicToSeedSync(mnemonic);
    const root = HDNode.fromSeed(seed);
    const path = "m/84'/0'/0'/0/0";
    const child = root.derivePath(path);

    return child.toWIF();
  }

  async reconnectPeers(homedir: string) {
    const peers2reconnect = {};

    const listPeers = await this.listPeers();
    const listChannels = await this.listChannels();

    // do we have any channels that need reconnection with peers..?
    for (const channel of listChannels) {
      if (!listPeers.includes(channel.counterparty_node_id)) peers2reconnect[channel.counterparty_node_id] = channel.counterparty_node_id
    }

    // do we have any peers stored in file that must be connected..?
    let storedPeers = [];
    try {
      const storedPeersTxt = fs.readFileSync(`${homedir}/peers.json`)
      if (storedPeersTxt) storedPeers = JSON.parse(storedPeersTxt);
    } catch (_) {}
    for (const storedPeer of storedPeers) {
      if (!listPeers.includes(storedPeer)) peers2reconnect[storedPeer] = storedPeer;
    }

    const peers2save = {};
    // dumb dedup:
    for (const peer of storedPeers.concat(listPeers).concat(Object.keys(peers2reconnect))) {
      peers2save[peer] = peer;
    }
    fs.writeFileSync((`${homedir}/peers.json`), JSON.stringify(Object.keys(peers2save)));

    // finally. conencting to the ones that need connection:
    for (const peer of Object.keys(peers2reconnect)) {
      this.logToGeneralLog(`connecting to ${peer}`)
      const details = await this.lookupNodeConnectionDetailsByPubkey(peer);
      this.logToGeneralLog(`(${details.pubkey}@${details.host}:${details.port})`);
      await this.connectPeer(details.pubkey, details.host ,details.port);
    }
  }

  public async connectPeer(pubkey, host, port) {
    const response = await fetch(`http://127.0.0.1:8310/connectpeer/${pubkey}/${host}/${port}`);
    const text = await response.text();
    return this._processResult(text);
  }

  public async version() {
    const response = await fetch(`http://127.0.0.1:8310/version`);
    const text = await response.text();
    return this._processResult(text);
  }

  public async ldkversion() {
    const response = await fetch(`http://127.0.0.1:8310/ldkversion`);
    const text = await response.text();
    return this._processResult(text);
  }

  public async getMaturingBalance() {
    const response = await fetch(`http://127.0.0.1:8310/getmaturingbalance`);
    const text = await response.text();
    return this._processResult(text);
  }

  public async getMaturingHeight() {
    const response = await fetch(`http://127.0.0.1:8310/getmaturingheight`);
    const text = await response.text();
    return this._processResult(text);
  }

  async lookupNodeConnectionDetailsByPubkey(pubkey: string) {
    // first, trying cache:
    if (this._nodeConnectionDetailsCache[pubkey] && +new Date() - this._nodeConnectionDetailsCache[pubkey].ts < 4 * 7 * 24 * 3600 * 1000) {
      // cache hit
      return this._nodeConnectionDetailsCache[pubkey];
    }

    // doing actual fetch and filling cache:
    const response = await fetch(`https://1ml.com/node/${pubkey}/json`);
    const json = await response.json();
    if (json && json.addresses && Array.isArray(json.addresses)) {
      for (const address of json.addresses) {
        if (address.network === 'tcp') {
          const ret = {
            pubkey,
            host: address.addr.split(':')[0],
            port: parseInt(address.addr.split(':')[1]),
          };

          this._nodeConnectionDetailsCache[pubkey] = Object.assign({}, ret, { ts: +new Date() });

          return ret;
        }
      }
    }
  }

  async setRefundAddress(address: string) {
    const script = bitcoin.address.toOutputScript(address);
    const refundAddressScriptHex = script.toString('hex');

    const response = await fetch(`http://127.0.0.1:8310/setrefundaddressscript/${refundAddressScriptHex}`);
    const text = await response.text();
    return this._processResult(text);
  }
}
