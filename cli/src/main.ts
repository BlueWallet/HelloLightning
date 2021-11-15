/* eslint-disable no-empty,@typescript-eslint/no-inferrable-types,@typescript-eslint/no-var-requires */
import Ldk from './class/ldk';
const Table = require('cli-table')
const fs = require('fs');

let lastBlockchainSync = 0;
let lastNetworkGraphSaved = 0;
let lastPeersReconnect = 0;
const homedir = require('os').homedir() + '/.hellolightning';
const seedfile = `${homedir}/seed.txt`;
const ldk = new Ldk();

async function tick() {
  console.clear();
  console.log('Hello, Lightning!');
  console.log(`using ${homedir}`);

  if (!fs.existsSync(homedir)) {
    fs.mkdirSync(homedir);
  }

  if (!fs.existsSync(seedfile)) {
    console.log('no seed exists, generating...');
    await ldk.generate();
    const seed = ldk.getSecret();
    fs.writeFileSync(seedfile, seed);
    console.log(`mnemonic seed is saved to ${seedfile}`);
    await new Promise(resolve => setTimeout(resolve, 5000)); // sleep
  }

  let started = true;
  let nodeid: string;
  try {
    nodeid = await ldk.getNodeId();
  } catch (error) {
    console.error("Error getting node id: " + error.message);
    console.error("probably hello node not started");
    started = false;
  }

  if (!started) {
    console.log('attempting to start a node...');
    try {
      const seedFromDisk = fs.readFileSync(seedfile, { encoding: 'utf8' })
      ldk.setSecret(seedFromDisk);
      // const hex = ldk.getEntropyHex();
      // await ldk.start(hex);
      await ldk.start("00000000000000000000000000000000000000000000000000000000000000f6"); // fixme
    } catch (error) {
      console.error(error.message);
      await new Promise(resolve => setTimeout(resolve, 10* 1000)); // sleep
    }

    return;
  }


  if (+new Date() - lastBlockchainSync >  5 * 60 * 1000) { // 5 min
    lastBlockchainSync = +new Date();
    ldk.checkBlockchain(); // let it run in the background
  }

  if (+new Date() - lastNetworkGraphSaved >  1 * 60 * 1000) {
    lastNetworkGraphSaved = +new Date();
    ldk.saveNetworkGraph(); // let it run in the background
  }

  if (+new Date() - lastPeersReconnect >  1 * 60 * 1000) {
    lastPeersReconnect = +new Date();
    ldk.reconnectPeers(homedir); // let it run in the background
  }

  const peers = await ldk.listPeers();
  const channels = await ldk.listChannels();
  const activeAhannels = await ldk.listUsableChannels();

  let outbound_capacity_msat = 0;
  let inbound_capacity_msat = 0;

  channels.map(channel => {
    outbound_capacity_msat += channel.outbound_capacity_msat;
    inbound_capacity_msat += channel.inbound_capacity_msat;
  });

  const table = new Table()
  table.push(
    ['num peers', 'last sync', 'num channels', 'num active channels', 'channel balance', 'inbound capacity', 'node id']
    , [peers.length, Math.floor((+new Date() - lastBlockchainSync)/1000) + ' sec ago', channels.length, activeAhannels.length, msatToBitcoinString(outbound_capacity_msat), msatToBitcoinString(inbound_capacity_msat), nodeid]
  )

  console.log(table.toString())
  console.log(ldk.getLastLogsLines(30).join("\n"));
}

async function main() {
  for (;;) {
    await tick();
    await new Promise(resolve => setTimeout(resolve, 1000)); // sleep
  }
}

function msatToBitcoinString(msat: number): string {
  const sat = msat / 1000;
  return (sat / 100000000).toFixed(8) + ' BTC';
}

main();
