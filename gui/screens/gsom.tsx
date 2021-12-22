import useSWR from 'swr';
import {DefaultScreenProps} from '../models/defaultScreenProps';
import Ldk from '../classes/ldk';
import Util from '../classes/util';

let lastBlockchainSync = 0;
let lastNetworkGraphSaved = +new Date();
let lastPeersReconnect = 0;
let maturingBalance = 0;
let maturingHeight = 0;

const fetcher = async (arg1) => {
  const ldk = new Ldk();
  const util = new Util('dummy');
  ldk.setSecret(util.getHotSeed());

  if (+new Date() - lastBlockchainSync >  5 * 60 * 1000) { // 5 min
    lastBlockchainSync = +new Date();
    await ldk.setRefundAddress(ldk.unwrapFirstExternalAddressFromMnemonics());
    maturingBalance = await ldk.getMaturingBalance();
    maturingHeight = await ldk.getMaturingHeight();
    ldk.checkBlockchain(); // let it run in the background
    await ldk.broadcastTxsIfNecessary();
  }

  if (+new Date() - lastNetworkGraphSaved >  1 * 60 * 1000) {
    lastNetworkGraphSaved = +new Date();
    ldk.saveNetworkGraph(); // let it run in the background
  }

  if (+new Date() - lastPeersReconnect >  0.5 * 60 * 1000) {
    lastPeersReconnect = +new Date();
    ldk.reconnectPeers(); // let it run in the background
  }


  const uri = `http://localhost:8310/${arg1}`;
  try {
    const res = await fetch(uri);
    const json = await res.json();
    if (json && json.result && !json.error) return json.result;
  } catch (_) {
    return null;
  }
};

export default function Gsom(props: DefaultScreenProps) {
  const { data: listpeers }: { data?: any, error?: any } = useSWR('listpeers', fetcher, { refreshInterval: 2 * 1000, refreshWhenHidden: true, refreshWhenOffline: true });
  const { data: listchannels }: { data?: any, error?: any } = useSWR('listchannels', fetcher, { refreshInterval: 5 * 1000, refreshWhenHidden: true, refreshWhenOffline: true });
  const { data: getnodeid }: { data?: any, error?: any } = useSWR('getnodeid', fetcher, { refreshInterval: 5 * 1000, refreshWhenHidden: true, refreshWhenOffline: true });

  if (!getnodeid) {
    const ldk = new Ldk();
    const util = new Util('dummy');

    ldk.setSecret(util.getHotSeed())
    console.log();

    ;(async () => {
      await ldk.setRefundAddress(ldk.unwrapFirstExternalAddressFromMnemonics());
      await ldk.updateFeerate(); // so any refund claim upon startup would use adequate fee
      await ldk.start(ldk.getEntropyHex());
    })();
  }

  const renderPeersList = () => {
    const listItems = (listpeers || []).map((number) =>
        <li key={number}>{number}</li>
    );
    return (
        <ul>{listItems}</ul>
    );
  };

  return (

      <div className="container">
        <div className="row">
          <div className="col">


            <div className="row">
              <div className="col">
                Total balance<br/>
                0 sat<br/>
                $0.00<br/>
              </div>
              <div className="col">
                <button type="button" className="btn btn-primary">send</button>
              </div>
              <div className="col">
                <button type="button" className="btn btn-primary">receive</button>
              </div>
            </div>


          </div>
          <div className="col">
            channels:<br/>
            todo<br/>
            Peers:<br/>
            {renderPeersList()}
            <button type="button" className="btn btn-outline-primary">manage channels</button><br/>
            <button type="button" className="btn btn-outline-primary" onClick={async () => {
              const uri = prompt('input node uri');
              if (!uri) return;


              const pubkey = uri.split('@')[0];
              const [host, port] = uri.split('@')[1]?.split(':')
              if (!pubkey || !host || !port) return;

              const ldk = new Ldk();
              await ldk.connectPeer(pubkey, host, port);
            }}>connect peer</button><br/>
          </div>
        </div>

      </div>
  );
}



/*

        <div>
            {listpeers ? (
                <div style={{ fontSize: 20 }}>
                    <span>Peers: {JSON.stringify(listpeers)}</span>
                </div>
            ) : null}
            {listchannels ? (
                <div style={{ fontSize: 20 }}>
                    <span>Channels: {JSON.stringify(listchannels)}</span>
                </div>
            ) : null}
            {getnodeid ? (
                <div style={{ fontSize: 20 }}>
                    <span>Node id: {JSON.stringify(getnodeid)}</span>
                </div>
            ) : (<span>not started..?</span>)}
            <br/>
        </div>

 */
