import Head from 'next/head';
import Layout, { siteTitle } from '../components/layout';
import useSWR from 'swr';

const fetcher = async (arg1) => {
  const uri = `http://localhost:8310/${arg1}`;
  try {
    const res = await fetch(uri);
    const json = await res.json();
    if (json && json.result && !json.error) return json.result;
  } catch (_) {
    return null;
  }
};

export default function Index() {
  const { data: listpeers }: { data?: any, error?: any } = useSWR('listpeers', fetcher, { refreshInterval: 5 * 1000, refreshWhenHidden: true, refreshWhenOffline: true });
  const { data: listchannels }: { data?: any, error?: any } = useSWR('listchannels', fetcher, { refreshInterval: 5 * 1000, refreshWhenHidden: true, refreshWhenOffline: true });
  const { data: getnodeid }: { data?: any, error?: any } = useSWR('getnodeid', fetcher, { refreshInterval: 5 * 1000, refreshWhenHidden: true, refreshWhenOffline: true });

  return (
      <Layout index>
          <Head>
              <title>{siteTitle}</title>
          </Head>

          <div className="d-flex flex-column min-vh-100 justify-content-center align-items-center">
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
      </Layout>
  );
}
