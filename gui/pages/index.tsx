import Head from 'next/head';
import Layout, {siteTitle} from '../components/layout';
import Gsom from '../screens/gsom';
import NoWalletDetected from '../screens/noWalletDetected';
import {HelloScreen} from '../models/helloScreen';
import {useEffect, useState} from 'react';
import CreateWriteSeedDown from '../screens/createWriteSeedDown';
import Util from '../classes/util';
import UnlockWallet from '../screens/unlockWallet';

export default function Index() {
  console.log('rendering Index');
  const [screen, setScreen] = useState<HelloScreen>(HelloScreen.NoWalletDetected);

  useEffect(() => {
      const ut = new Util('dummy');
      if (ut.isSeeded()) {
          if (!ut.getHotSeed())
              setScreen(HelloScreen.UnlockWallet);
          else
              setScreen(HelloScreen.Gsom);
      } else {
          setScreen(HelloScreen.NoWalletDetected);
      }
  }, []);

  const renderScreen = () => {
    console.log('currentScreen = ', screen);
    switch (screen) {
      case HelloScreen.Gsom: return (<Gsom changeScreen={setScreen}/>);
      case HelloScreen.NoWalletDetected: return (<NoWalletDetected changeScreen={setScreen}/>);
      case HelloScreen.CreateWriteDownSeed: return (<CreateWriteSeedDown changeScreen={setScreen}/>);
      case HelloScreen.UnlockWallet: return (<UnlockWallet changeScreen={setScreen}/>);
      default:
        console.warn('default', screen);
        return (<Gsom changeScreen={setScreen}/>);
    }
  };

  return (
      <Layout index>
          <Head>
              <title>{siteTitle}</title>
          </Head>

          <div className="d-flex flex-column min-vh-100 justify-content-center align-items-center">
            <h1>👋 ⚡️</h1>
            {renderScreen()}
          </div>

      </Layout>
  );
}
