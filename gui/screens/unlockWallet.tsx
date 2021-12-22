import useSWR from 'swr';
import { DefaultScreenProps } from '../models/defaultScreenProps';
import Util from '../classes/util';
import {HelloScreen} from '../models/helloScreen';

export default function UnlockWallet(props: DefaultScreenProps) {
  return (
        <div>
            Wallet is locked, please unlock it
            <br/>
          <button type="button" className="btn btn-primary" onClick={async () => {
            let password = '';
            for (;;) {
              password = prompt("Please enter your payment password", "");
              if (password) {
                const ut = new Util(password);
                try {
                  const decrypted = ut.decrypt(ut.retrieveEncryptedSeed())
                  if (decrypted) {
                    ut.storeHotSeed(decrypted);
                    props.changeScreen(HelloScreen.Gsom);
                    break;
                  } else {
                    alert('incorrect password');
                  }
                } catch (error) {
                  alert("Incorrect password: " + error.message);
                }
              }
            }
          }}>next</button>
        </div>
  );
}
