/*!
 * Copyright (c) 2018 Digital Bazaar, Inc. All rights reserved.
 */
const keyStorage = require('./storage');
const util = require('./util');

const api = {};
module.exports = api;

api.list = util.callbackify(async options => {
  const dids = await keyStorage.list();
  for(const did of dids) {
    if(options.filename) {
      const meta = await keyStorage.meta(did);
      console.log(`${did} ${meta.filename}`);
    } else {
      console.log(`${did}`);
    }
  }
});

api.remove = util.callbackify(async options => {
  throw new Error('remove not implemented');
});
