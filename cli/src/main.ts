import Ldk from './class/ldk';
const Table = require('cli-table')


let lastBlockchainSync = 0;
const ldk = new Ldk();





setInterval(async () => {
  if (+new Date() - lastBlockchainSync >  5 * 60 * 1000) { // 5 min
    lastBlockchainSync = +new Date();
    ldk.checkBlockchain(); // let it run in the background
  }

  console.clear();
  console.log('Hello, Lightning!');

  const peers = await ldk.listPeers();
  const channels = await ldk.listChannels();

  let outbound_capacity_msat = 0;
  let inbound_capacity_msat = 0;

  channels.map(channel => {
    outbound_capacity_msat += channel.outbound_capacity_msat;
    inbound_capacity_msat += channel.inbound_capacity_msat;
  });


  const table = new Table(/*{head: ['', '', '']}*/)
  table.push(
      []
    , ['num peers', 'last blockchain sync time', 'num channels', 'channel balance', 'inbound capacity']
    , [peers.length, Math.floor((+new Date() - lastBlockchainSync)/1000) + ' sec ago', channels.length, msatToBitcoinString(outbound_capacity_msat), msatToBitcoinString(inbound_capacity_msat)]
  )

  console.log(table.toString())





}, 1 * 1000);




function msatToBitcoinString(msat: number): string {
  const sat = msat / 1000;
  return (sat / 100000000).toFixed(8) + ' BTC'; // btc
}
