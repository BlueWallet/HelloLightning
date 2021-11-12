import Ldk from './class/ldk';




// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export async function main() {
  console.log('start');
  const ldk = new Ldk();
  await ldk.checkBlockchain();
  // console.warn(await ldk.listPeers());
}


main()
