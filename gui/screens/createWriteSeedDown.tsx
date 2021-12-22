import {HelloScreen} from '../models/helloScreen';
import {DefaultScreenProps} from '../models/defaultScreenProps';
import Ldk from '../classes/ldk';
import {useEffect, useState} from 'react';
import Util from '../classes/util';

export default function CreateWriteSeedDown(props: DefaultScreenProps) {
  const [seed, setSeed] = useState('');

  useEffect(() => {

    (async () => {
        const ldk = new Ldk();
        await ldk.generate();
        setSeed(ldk.getSecret());
      })();
  }, []);

  return (
      <span>

          <div className="row">
              <div className="col">
                  <h2>Write down seed words:</h2>
              </div>
          </div>

          <div className="row">
              <div className="col">
                  {seed}
              </div>
          </div>

          <div className="row">
              <div className="col">
                  <button type="button" className="btn btn-primary" onClick={async () => {
                      let password = '';
                      for (;;) {
                          const pass1 = prompt("Please enter your payment password", "");
                          const pass2 = prompt("Please repeat your password", "");
                          if (pass1 === pass2 && pass1 != null) {
                              password = pass1;
                              break;
                          }
                      }

                      const ut = new Util(password);
                      const encryptedSeed = ut.encrypt(seed)
                      alert("Wallet successfully seeded!");
                      ut.storeHotSeed(seed);
                      ut.storeEncryptedSeed(encryptedSeed)
                      props.changeScreen(HelloScreen.Gsom);
                  }}>next</button>

              </div>
          </div>

      </span>
  );
}
