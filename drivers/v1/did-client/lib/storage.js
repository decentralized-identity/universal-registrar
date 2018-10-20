/*!
 * Copyright (c) 2018 Digital Bazaar, Inc. All rights reserved.
 */

// TODO: use native promisify once node 8+ required
const promisify = require('util.promisify');

const fs = require('fs-extra');
const glob = promisify(require('glob'));
const lockfile = require('proper-lockfile');
const mkdirp = require('mkdirp-promise');
const os = require('os');
const path = require('path');
const util = require('./util');

const api = {};
module.exports = api;

api.defaults = {};
api.defaults.config = () => ({
  '@context': [{
    '@version': 1.1,
    id: '@id',
    type: '@type',
    schema: 'http://schema.org/',
    xsd: 'http://www.w3.org/2001/XMLSchema#',
    name: 'schema:name',
    description: 'schema:description',
    url: 'schema:url',
    ledger: 'urn:did-client:ledger',
    log: {
      '@id': 'urn:did-client:log',
      '@container': '@list'
    },
    created: {
      '@id': 'schema:dataCreated',
      '@type': 'xsd:dateTime'
    },
    modified: {
      '@id': 'schema:dataModified',
      '@type': 'xsd:dateTime'
    },
    published: {
      '@id': 'schema:dataPublished',
      '@type': 'xsd:dateTime'
    },
    dids: {
      '@id': 'urn:did-client:did',
      '@container': '@id'
    }
  }],
  'urn:did-client:config:version': '1',
  'urn:did-client:notes:auto': ['created', 'ledger'],
  dids: {}
});

function _didDir(options = {}) {
  return path.join(os.homedir(), '.testdid');
}

function _didToFilename(did, options = {}) {
  return path.join(_didDir(options), did.replace(/:/g, '-') + '.jsonld');
}

function _configFilename(options = {}) {
  return options.config || path.join(_didDir(options), 'config.jsonld');
}

api.lockDid = util.callbackify(async function(did, options = {}) {
  return await lockfile.lock(_didToFilename(did), {
    retries: 100
  });
});

api.store = util.callbackify(async function(didDocument, options = {}) {
  // TODO: validate params

  const did = didDocument.id;

  if(!did) {
    throw new Error('DID Document missing `id` field: ' + didDocument);
  }

  const filename = _didToFilename(did);

  // create .did directory
  await mkdirp(path.dirname(filename), {mode: '0700'});
  // backup if current exists
  try {
    await fs.copy(filename, filename + '.old');
  } catch(err) {
    if(err.code !== 'ENOENT') {
      throw err;
    }
  }
  // write file
  await fs.writeFile(filename, JSON.stringify(didDocument, null, 2), 'utf8');
  // make read/write for owner
  await fs.chmod(filename, '0600');

  return filename;
});

async function _loadFile(filename) {
  const data = await fs.readFile(filename, 'utf8');
  return JSON.parse(data);
}

api.load = util.callbackify(async function(did, options = {}) {
  return _loadFile(_didToFilename(did));
});

api.remove = util.callbackify(async function(did, options = {}) {
  throw new Error('Key storage .remove() not implemented');
});

api.list = util.callbackify(async function(options = {}) {
  // FIXME: be more async (streams, generators, etc)
  const files = await glob(path.join(_didDir(), 'did-*.jsonld'));
  const ids = [];
  for(const f of files) {
    const data = await _loadFile(f);
    ids.push(data.id);
  }
  return ids;
});

api.meta = util.callbackify(async function(did, options = {}) {
  const filename = _didToFilename(did);
  return {
    did,
    doc: await _loadFile(filename),
    filename
  };
});

api.lockConfig = util.callbackify(async function(options = {}) {
  // FIXME: file creation w/o lock is probably racey
  const configFilename = _configFilename(options);
  if(!fs.existsSync(configFilename)) {
    await api.storeConfig(api.defaults.config(), options);
  }
  return await lockfile.lock(_configFilename(options), {
    retries: 100
  });
});

api.storeConfig = util.callbackify(async function(config, options = {}) {
  // TODO: validate config

  const filename = _configFilename(options);

  // create .did directory if needed
  await mkdirp(path.dirname(filename), {mode: '0700'});
  // backup config if available
  try {
    await fs.copy(filename, filename + '.old');
  } catch(err) {
    if(err.code !== 'ENOENT') {
      throw err;
    }
  }
  // write new config
  await fs.writeFile(filename, JSON.stringify(config, null, 2), 'utf8');

  return filename;
});

api.loadConfig = util.callbackify(async function(options = {}) {
  const filename = options.config || _configFilename();
  try {
    return await _loadFile(filename);
  } catch(err) {
    if(err.code = 'ENOENT') {
      return null;
    }
    throw err;
  }
});
