const SHA256 = require('crypto-js/sha256');
const ENCHEX = require('crypto-js/enc-hex');
const ENCUTF8 = require('crypto-js/enc-utf8');
const AES = require('crypto-js/aes');

const ENCRYPTED_SEED = 'ENCRYPTED_SEED';

export default class Util {
  encryptionMarker = 'encrypted://';
  encryptionKey: string = '';

  constructor(entropy: string) {
    if (!entropy) throw new Error('entropy not provided');

    this.encryptionKey = this.hashIt(this.hashIt('encryption' + entropy));
  }

  hashIt(arg: string) {
    return ENCHEX.stringify(SHA256(arg));
  }

  encrypt(clearData: string): string {
    return this.encryptionMarker + AES.encrypt(clearData, this.encryptionKey).toString();
  }

  decrypt(encryptedData: string | null, encryptionKey: string | null = null): string {
    if (encryptedData === null) return '';
    if (!encryptedData.startsWith(this.encryptionMarker)) return encryptedData;
    const bytes = AES.decrypt(encryptedData.replace(this.encryptionMarker, ''), encryptionKey || this.encryptionKey);
    return bytes.toString(ENCUTF8);
  }

  storeEncryptedSeed(encryptedSeed: string) {
    return localStorage.setItem(ENCRYPTED_SEED, encryptedSeed); // cold
  }

  retrieveEncryptedSeed() {
    return localStorage.getItem(ENCRYPTED_SEED); // cold
  }

  storeHotSeed(seed: string) {
    window[ENCRYPTED_SEED] = seed;
  }

  getHotSeed() {
    return window[ENCRYPTED_SEED];
  }

  isSeeded() {
    return !!localStorage.getItem(ENCRYPTED_SEED);
  }
}
