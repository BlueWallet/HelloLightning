import { HelloScreen } from '../models/helloScreen';
import { DefaultScreenProps } from '../models/defaultScreenProps';

export default function NoWalletDetected(props: DefaultScreenProps) {
  return (
      <div>

          <div className="row">
              <div className="col">
                  <h2>No wallet detected</h2>
              </div>
          </div>

          <div className="row">
              <div className="col">
                  <button type="button" className="btn btn-primary" onClick={async () => {
                    props.changeScreen(HelloScreen.CreateWriteDownSeed);
                  }}>Create</button>
              </div>
          </div>

          <div className="row">
              <div className="col">
                  <button type="button" className="btn light" onClick={() => {
                    alert('todo');
                  }}>Restore from seed</button>
              </div>
          </div>

      </div>
  );
}
