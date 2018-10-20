/**
 * Equihash for Node.js.
 * zcash utilities.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * MIT License
 * <https://github.com/digitalbazaar/equihash/blob/master/LICENSE>
 */
module.exports = {
  // create zcash personalization buffer
  zcashPersonalization(n, k) {
    // setup zcash personal buffer for given n and k
    const p = Buffer.alloc(16, 0);
    // zcash string
    Buffer.from('ZcashPoW').copy(p);
    const dataView = new DataView(p.buffer);
    // 32 bit little endian n and k
    dataView.setUint32(8, n, true);
    dataView.setUint32(12, k, true);
    return p;
  }
};
