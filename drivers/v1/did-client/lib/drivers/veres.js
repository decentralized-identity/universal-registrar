/*!
 * Copyright (c) 2017-2018 Digital Bazaar, Inc. All rights reserved.
 */
'use strict';

const chalk = require('chalk');
const didv1 = require('did-veres-one');
const didcv1 = require('did-client-veres-one');
const fs = require('fs');
const keyStorage = require('../storage');
const promisify = require('util.promisify');
const r2 = require('r2');
const util = require('../util');

const readFile = promisify(fs.readFile);

// configure libraries
const jsonld = require('jsonld')();
const documentLoader = jsonld.documentLoader;
jsonld.documentLoader = async url => {
  if(url in didv1.contexts) {
    return {
      contextUrl: null,
      documentUrl: url,
      document: didv1.contexts[url]
    };
  }
  return documentLoader(url);
};
const jsigs = require('jsonld-signatures');
jsigs.use('jsonld', jsonld);
const eproofs = require('equihash-signature');
eproofs.install(jsigs);
didv1.use('jsonld', jsonld);
didv1.use('jsonld-signatures', jsigs);

const api = {};
module.exports = api;

function __log(options, f, msg, ...rest) {
  f(
    `[${chalk.bold('Veres One')}][${chalk.bold(options.mode)}] ${msg}`,
    ...rest);
}

function _log(options, ...rest) {
  if(!options.quiet) {
    __log(options, console.log, ...rest);
  }
}

function _error(options, ...rest) {
  __log(options, console.error, chalk.bold.red('ERROR'), ...rest);
}

function _debug(options, ...rest) {
  if(options.verbose >= 2) {
    _log(options, ...rest);
  }
}

function _verbose(options, ...rest) {
  if(options.verbose >= 1) {
    _log(options, ...rest);
  }
}

// known testnet hostnames (as of 2018-02)
const _testnetHostnames = [
  'alturas',
  'frankfurt',
  'genesis',
  'mumbai',
  'saopaulo',
  'singapore',
  'tokyo'
].map(nick => `${nick}.testnet.veres.one`);

// get default mode hostname
function _getDefaultHostname(options) {
  if(options.mode === 'dev') {
    return 'genesis.veres.one.localhost:42443';
  } else if(options.mode === 'test') {
    return 'genesis.testnet.veres.one';
  } else if(options.mode === 'live') {
    return 'veres.one';
  } else {
    throw new Error('Unknown mode');
  }
}

// get all mode hostnames
function _getModeHostnames(options) {
  if(options.mode === 'test') {
    return _testnetHostnames;
  } else {
    return [_getDefaultHostname(options)];
  }
}

// get option hostnames
function _getOptionHostnames(options) {
  const hostnames = options.hostname || [];
  return Array.isArray(hostnames) ? hostnames : [hostnames];
}

// get single hostname, allow single override
function _getHostname(options) {
  const optHostnames = _getOptionHostnames(options);
  if(optHostnames.length > 1) {
    throw new Error('Too many hostnames provided');
  }
  if(optHostnames.length === 1) {
    return optHostnames[0];
  }
  return _getDefaultHostname(options);
}

// get default hostname or all overrides
function _getHostnames(options) {
  const optHostnames = _getOptionHostnames(options);
  if(optHostnames.length > 0) {
    return optHostnames;
  }
  return [_getDefaultHostname(options)];
}

async function _lockDid(did, options) {
  _debug(options, 'did: locking');
  return keyStorage.lockDid(did, options);
}

async function _lockConfig(options) {
  _debug(options, 'config: locking');
  return keyStorage.lockConfig(options);
}

async function _storeConfig(config, options) {
  _debug(options, 'config: storing');
  return keyStorage.storeConfig(config, options);
}

async function _loadConfig(options) {
  _debug(options, 'config: loading');
  const config = await keyStorage.loadConfig(options);
  if(config) {
    if(config['urn:did-client:config:version'] !== '1') {
      throw new Error('Unknown config file version');
    }
    return config;
  }
  // default
  return keyStorage.defaults.config();
}

const _keyTypes = {
  ed25519: 'Ed25519VerificationKey2018',
  rsa: 'RsaVerificationKey2018'
};

async function _send(
  {options, didDocument, privateDidDocument, operationType = 'create'}) {
  const hostname = _getHostname(options);

  if(operationType === 'create') {
    _log(options, 'Preparing to register a DID on Veres One...');
  } else {
    _log(options, 'Preparing to update a DID Document on Veres One...');
  }

  // wrap DID Document in a web ledger operation
  let operation = didv1.wrap({didDocument, operationType});

  if(options.accelerator) {
    // use accelerator
    _log(options, 'Using accelerator...');
    if(!options.auth) {
      throw new Error('Authorizaion DID required');
    }
    const authDid = await keyStorage.load(options.auth);

    // send DID Document to a Veres One accelerator
    _log(options, 'Generating accelerator signature...');
    const _opts = {
      operation,
      hostname: options.accelerator,
      env: options.mode,
      keyId: authDid.authentication[0].publicKey[0].id
    };
    if(authDid.authentication[0].publicKey[0].privateKey.privateKeyPem) {
      _opts.key =
        authDid.authentication[0].publicKey[0].privateKey.privateKeyPem;
    } else {
      // _opts.key =
      //   authDid.authentication[0].publicKey[0].privateKey.privateKeyBase58;
      throw new Error(
        'Unsupported authentication DID. Only RSA keys supported.');
    }
    const response = await didcv1.sendToAccelerator(_opts);
    // FIXME: verify wrapped operation
    operation = await response.json();
  } else {
    // attach an equihash proof
    _log(options, 'Generating Equihash proof of work... (60-120 seconds)');
    operation = await didv1.attachEquihashProof({operation});
  }

  // get public key ID
  const creator = didDocument.invokeCapability[0].publicKey[0].id;

  // get private key
  const privateKey = privateDidDocument.invokeCapability[0].publicKey[0]
    .privateKey;

  if(!privateKey) {
    throw new Error('Private key required to perform a send');
  }

  // attach capability invocation proof
  _log(options, 'Attaching LD-OCAP invocation proof...');
  operation = await didv1.attachInvocationProof({
    operation,
    capability: didDocument.id,
    capabilityAction: operationType === 'create' ?
      'RegisterDid' : 'UpdateDidDocument',
    creator,
    privateKeyPem: privateKey.privateKeyPem,
    privateKeyBase58: privateKey.privateKeyBase58,
  });

  // send DID Document to a Veres One ledger node
  if(operationType === 'create') {
    _log(options, 'Registering DID on Veres One...');
  } else {
    _log(options, 'Updating DID Document on Veres One...');
  }
  const response = await didcv1.send({
    operation,
    hostname,
    env: options.mode
  });

  if(response.status === 204) {
    if(operationType === 'create') {
      _log(options, 'DID registration send to ledger.');
    } else {
      _log(options, 'DID Document update sent to the Veres One ledger.');
    }
    _log(options, 'Please wait ~15-30 seconds for ledger consensus.');
    _log(options, 'You may use the `info` command to monitor the ' +
      'registration of your DID.');

    if(options.notes) {
      // save ledger if requested
      const release = await _lockConfig(options);
      const config = await _loadConfig(options);
      const notes = {};
      if(jsonld.hasValue(config, 'urn:did-client:notes:auto', 'ledger')) {
        jsonld.addValue(notes, 'ledger', `${options.ledger}:${options.mode}`);
      }
      await release();
      await _notesAddMany(didDocument.id, notes, options);
    }
  } else {
    _error(options, 'Failed to register DID Document.');
    _error(options, 'Status Code: ' + response.status);
    _error(options, 'Response Body: ' +
      JSON.stringify(await response.json(), null, 2));
  }
}

async function _import(options, privateDidDocument) {
  // save private DID Document
  _log(options, 'Storing DID Document on disk...');
  const filename = await keyStorage.store(privateDidDocument);
  _log(options, 'DID Document stored in:', filename);
}

api.generate = async options => {
  // called to check hostname set properly
  _getHostname(options);

  // check known key types
  if(!(options.type in _keyTypes)) {
    throw new Error('Unknown key type');
  }

  _log(options, 'Generating a new Veres One DID...');
  _log(options, 'Generating keypair... (5-15 seconds)');

  // generate a DID Document
  let passphrase = options.passphrase;
  if(passphrase === undefined) {
    passphrase = null;
  }
  const {publicDidDocument: didDocument, privateDidDocument} =
    await didv1.generate(
      {keyType: _keyTypes[options.type], passphrase});

  _log(options, `DID: ${didDocument.id}`);

  // FIXME: encrypt private key pair

  if(options.import) {
    await _import(options, privateDidDocument);
  } else {
    console.log(JSON.stringify(privateDidDocument, null, 2));
  }

  // get config
  const release = await _lockConfig(options);
  const config = await _loadConfig(options);

  // set explicit notes
  const notes = {};
  if(options.name) {
    _debug(options, 'adding "name" note');
    jsonld.addValue(notes, 'name', options.name);
  }
  if(options.description) {
    _debug(options, 'adding "description" note');
    jsonld.addValue(notes, 'description', options.description);
  }
  // check if ok to set auto notes
  if(options.notes) {
    if(jsonld.hasValue(config, 'urn:did-client:notes:auto', 'created')) {
      jsonld.addValue(notes, 'created', new Date().toISOString());
    }
  }
  await release();
  await _notesAddMany(didDocument.id, notes, options);

  _log(options, 'Local DID generation successful.');

  if(options.register) {
    await _send({options, didDocument, privateDidDocument});
  } else {
    _log(options, 'To register the DID globally, use the `register` command.');
  }
};

api.import = async options => {
  let data;
  if(options.filename) {
    _debug(options, 'import from file', {filename: options.filename});
    data = await readFile(options.filename);
  } else {
    _debug(options, 'import from stdin');
    const getStdin = require('get-stdin');
    data = await getStdin();
  }
  const privateDidDocument = JSON.parse(data);
  await _import(options, privateDidDocument);
  const didDocument = await didv1.publicDidDocument({privateDidDocument});
  if(options.register) {
    await _send({options, didDocument, privateDidDocument});
  }
};

api.export = async options => {
  const privateDidDocument = await keyStorage.load(options.did);
  if(options.private) {
    _debug(options, 'export private');
    console.log(JSON.stringify(privateDidDocument, null, 2));
  } else if(options.public) {
    _debug(options, 'export public');
    const didDocument = await didv1.publicDidDocument({privateDidDocument});
    console.log(JSON.stringify(didDocument, null, 2));
  }
};

api.register = async options => {
  let privateDidDocument;
  if(options.did) {
    _debug(options, 'register DID', {did: options.did});
    privateDidDocument = await keyStorage.load(options.did);
  } else {
    const getStdin = require('get-stdin');
    privateDidDocument = JSON.parse(await getStdin());
    _debug(options, 'register DID from stdin', {did: privateDidDocument.id});
  }
  const didDocument = await didv1.publicDidDocument({privateDidDocument});
  return _send({options, didDocument, privateDidDocument});
};

api.receive = async options => {
  throw new Error('receive not implemented');
};

/*
api.revoke = async options => {
  throw new Error('revoke not implemented');
};
*/

async function _getLocal(did, options) {
  _verbose(options, 'Retrieving local DID Document...');
  let timeMs;
  try {
    const start = new Date();
    const meta = await keyStorage.meta(options.did);
    timeMs = new Date() - start;
    return {
      found: true,
      timeMs: timeMs,
      type: 'LocalDidDocument',
      did: did,
      filename: meta.filename,
      doc: meta.doc
    };
  } catch(err) {
    return {
      found: false,
      // don't retry local files
      retry: false,
      time: timeMs,
      type: 'LocalDidDocument',
      did: did,
      error: err
    };
  }
}

async function _getLedger(did, hostname, reqOptions, options) {
  const https = require('https');
  let agent;
  const didUrl = `https://${hostname}/dids/${did}`;
  if(options.mode === 'dev') {
    agent = new https.Agent({rejectUnauthorized: false});
  }

  _verbose(options, 'Retrieving remote DID Document...', {hostname});
  _debug(options, 'remote request', {url: didUrl});

  const start = new Date();
  const response = await r2({
    url: didUrl,
    method: 'GET',
    agent,
    headers: {
      'accept': 'application/ld+json, application/json'
    }
  }).response;

  const status = response.status;
  const body = await response.json();
  const reqMs = new Date() - start;
  const timeMs = new Date() - reqOptions.start;

  if(response.status !== 200) {
    _debug(options, 'remote failure response', {hostname, status, body});
    return {
      found: false,
      retry: true,
      timeMs,
      reqMs,
      start: reqOptions.start,
      retries: reqOptions.retries + 1,
      type: 'LedgerDidDocument',
      did,
      hostname,
      error: {status, body}
    };
  }
  const result = {
    found: true,
    retry: false,
    timeMs,
    reqMs,
    start: reqOptions.start,
    retries: reqOptions.retries + 1,
    type: 'LedgerDidDocument',
    did,
    hostname,
    doc: body
  };
  if(result.found && options.retrySimFailure !== 0.0 &&
    Math.random() < options.retrySimFailure) {
    _debug(options, 'remote simulated failure response', {hostname, status});
    result.found = false;
    result.retry = true;
  } else {
    _debug(options, 'remote success response', {hostname, status});
  }
  return result;
}

const _foundStr = chalk.bold.green('FOUND');
const _notFoundStr = chalk.bold.red('NOT FOUND');

// array of suite types to display
const _suiteInfo = [
  {
    type: 'authentication',
    name: 'Authentication'
  }, {
    type: 'grantCapability',
    name: 'Grant Capability'
  }, {
    type: 'invokeCapability',
    name: 'Invoke Capability'
  }
];

async function _info(result, options) {
  if(options.format === 'found') {
    const info = {
      timeMs: result.timeMs
    };
    if(options.retry && result.retries > 1) {
      info.reqMs = result.reqMs;
      info.retries = result.retries;
    }
    if(result.type === 'LocalDidDocument') {
      if(result.found) {
        _log(options, `${_foundStr} @ local`, Object.assign({}, info, {
          filename: result.filename
        }));
      } else {
        _log(options, `${_notFoundStr} @ local`, info);
      }
    } else {
      const ledgerInfo = Object.assign({}, info, {
        hostname: result.hostname
      });
      if(result.found) {
        _log(options, `${_foundStr} @ ledger`, ledgerInfo);
      } else {
        _log(options, `${_notFoundStr} @ ledger`, ledgerInfo);
      }
    }
  } else if(options.format === 'human') {
    // FIXME: frame data
    if(result.type === 'LocalDidDocument') {
      console.log(`---- local ----`);
      if(result.found) {
        console.log(`Result: ${_foundStr}`);
        console.log('Filename:', result.filename);
      }
    } else {
      console.log(`---- ledger: ${result.hostname} ----`);
      if(result.found) {
        console.log(`Result: ${_foundStr}`);
        console.log('Hostname:', result.hostname);
      }
    }
    if(result.found) {
      // YAML-like output
      const doc = result.doc;
      console.log('DID:', doc.id);
      for(const suiteInfo of _suiteInfo) {
        const info = doc[suiteInfo.type] || [];
        console.log(`- ${suiteInfo.name} [${info.length}]:`);
        for(const suite of info) {
          const keys = suite.publicKey || [];
          console.log(`  Type: ${suite.type}`);
          console.log(`  Keys: [${keys.length}]`);
          for(const key of keys) {
            console.log(`  - ID: ${key.id}`);
            console.log(`    Type: ${key.type}`);
            if(options.publicKey) {
              const value = key.publicKeyBase58 || key.publicKeyPem;
              console.log(`    Public Key: ${value}`);
            }
            if(options.privateKey && key.privateKey) {
              const value =
                key.privateKey.privateKeyBase58 ||
                key.privateKey.privateKeyPem;
              console.log(`    Private Key: ${value}`);
            }
          }
        }
      }
      /*
      const auth = doc.authentication;
      if(options.publicKey && auth && auth.length > 0) {
        console.log('Public Key #1:');
        console.log('ID:', auth[0].publicKey.id);
        console.log('Type:', auth[0].publicKey.type);
        console.log('Owner:', auth[0].publicKey.owner);
        console.log(auth[0].publicKey.publicKeyPem);
      }
      if(options.privateKey && auth && auth.length > 0) {
        console.log('Private Key #1:');
        console.log('ID:', auth[0].publicKey.id);
        console.log('Type:', auth[0].publicKey.type);
        console.log('Owner:', auth[0].publicKey.owner);
        console.log(auth[0].publicKey.privateKeyPem);
      }
      */
    } else {
      console.log(`Result: ${_notFoundStr}`);
    }
  } else if(options.format === 'json') {
    if(result.found) {
      console.log(JSON.stringify(result.doc, null, 2));
    }
  }
}

function _delay(delay) {
  return new Promise(resolve => setTimeout(resolve, delay));
}

api.info = async options => {
  _verbose(options, 'DID:', options.did);

  // info from requested locations
  const locations = [];

  // check local
  if(['any', 'both', 'local', 'all'].includes(options.location)) {
    _verbose(options, 'searching @ local');
    locations.push(_getLocal(options.did, options));
  }
  // fast path if local found
  let done = false;
  if(options.location === 'any') {
    const results = await Promise.all(locations);
    done = results[0].found;
  }
  let hostnames = [];
  // add default hostname or option hostnames
  if(!done && ['any', 'ledger', 'both'].includes(options.location)) {
    hostnames = [..._getHostnames(options)];
  }
  // add all mode hostnames and option hostnames
  if(!done && ['ledger-all', 'all'].includes(options.location)) {
    hostnames = [
      ...hostnames,
      ..._getModeHostnames(options),
      ..._getOptionHostnames(options)
    ];
  }
  if(!done) {
    _verbose(options, 'searching @ hostnames', hostnames);
    for(const hostname of hostnames) {
      locations.push(_getLedger(options.did, hostname, {
        start: new Date(),
        retries: 0
      }, options));
    }
  }

  async function looper(result) {
    // omit "not found" info if requested
    if(!options.retryShowFound || (options.retryShowFound && result.found)) {
      _info(result, options);
    }
    // output final failure if timeout or max retries
    if(!result.found && options.retry &&
      (result.timeMs >= options.retryTimeoutMs ||
        result.retries >= options.retryMax)) {
      _info(result, options);
      return;
    }
    // retry if needed
    if(!result.found && options.retry && result.retry) {
      _debug(options, 'retry delay', {
        hostname: result.hostname,
        delayMs: options.retryMs
      });
      // TODO: exponential backoff
      await _delay(options.retryMs);
      return looper(await _getLedger(result.did, result.hostname, {
        start: result.start,
        retries: result.retries
      }, options));
    }
  }

  await Promise.all(locations.map(location => location.then(looper)));
};

function _showNote(did, notes, property, options) {
  const fmt = options.format;
  if(fmt === 'plain') {
    const properties = property ? [property] : Object.keys(notes).sort();
    for(const p of properties) {
      console.log(`${did} ${p} ${notes[p]}`);
    }
  } else if(fmt === 'json' || fmt === 'json-compact') {
    const json = Object.assign({}, notes);
    json.id = did;
    if(fmt === 'json') {
      console.log(JSON.stringify(json, null, 2));
    } else {
      console.log(JSON.stringify(json));
    }
  }
}

function _notesOne(did, config, options) {
  // run commands in order: clear, add, remove, set, delete, find
  _debug(options, 'notes: processing did', {did});

  let store = false;

  // only allow mutable operations if did or --all specified
  const readonly = !(options.did || options.all);
  if(readonly && (options.clear ||
    options.add || options.remove ||
    options.set || options.delete)) {
    throw new Error('readonly mode: specify DID or use --all');
  }

  if(options.clear) {
    delete config.dids[did];
    store = true;
  }
  const target = config.dids[did] = config.dids[did] || {};

  if(options.add) {
    if(options.add[0] === 'id' || options.add[0] === '@id') {
      throw new Error('Can not add "id"');
    }
    jsonld.addValue(
      target, options.add[0], options.add[1], {allowDuplicate: false});
    store = true;
  }
  if(options.remove) {
    if(options.remove[0] === 'id' || options.remove[0] === '@id') {
      throw new Error('Can not remove "id"');
    }
    jsonld.removeValue(target, options.remove[0], options.remove[1]);
    store = true;
  }
  if(options.get) {
    if(options.get in target) {
      _showNote(did, target, options.get, options);
    }
  }
  if(options.set) {
    if(options.set[0] === 'id' || options.set[0] === '@id') {
      throw new Error('Can not set "id"');
    }
    target[options.set[0]] = options.set[1];
    store = true;
  }
  if(options.delete) {
    delete target[options.delete];
    store = true;
  }
  if(options.find) {
    if(options.find[0] in target &&
      jsonld.hasValue(target, options.find[0], options.find[1])) {
      console.log(did);
    }
  }

  // if no options, show all notes
  if(!options.clear &&
    !options.add && !options.remove &&
    !options.set && !options.get && !options.delete &&
    !options.find) {
    _showNote(did, target, null, options);
  }

  // remove if all properties gone
  if(Object.keys(target).length === 0) {
    delete config.dids[did];
    store = true;
  }

  return store;
}

// used by create, etc.
// pass in object with key/value pairs, all will be set
async function _notesAddMany(did, properties, options) {
  if(Object.keys(properties).length === 0) {
    return;
  }

  const release = await _lockConfig(options);
  const config = await _loadConfig(options);
  for(const property of Object.keys(properties)) {
    const target = config.dids[did] = config.dids[did] || {};
    if(property === 'id' || property === '@id') {
      throw new Error('Can not set "id"');
    }
    jsonld.addValue(
      target, property, properties[property], {allowDuplicate: false});
  }
  await _storeConfig(config, options);
  return await release();
}

api.notes = async options => {
  const release = await _lockConfig(options);
  const config = await _loadConfig(options);

  let store = false;
  if(options.did) {
    store = _notesOne(options.did, config, options);
  } else {
    for(const did of Object.keys(config.dids).sort()) {
      store = _notesOne(did, config, options) || store;
    }
  }

  if(store) {
    await _storeConfig(config, options);
  }
  return await release();
};

// all known suites
const _suiteIds = [
  'authentication',
  'grantCapability',
  'invokeCapability'
];

// map suite choice to full suite name
const _suiteIdMap = {
  'authn': 'authentication',
  'authentication': 'authentication',
  'grant': 'grantCapability',
  'grantCapability': 'grantCapability',
  'invoke': 'invokeCapability',
  'invokeCapability': 'invokeCapability'
};

// map suite choice to id prefix
const _keyPrefixMap = {
  'authentication': 'authn-key-',
  'grantCapability': 'grant-key-',
  'invokeCapability': 'invoke-key-'
};

function _hasKeyId(didDocument, id) {
  // check all suites
  for(const suiteId of _suiteIds) {
    for(const suiteParams of jsonld.getValues(didDocument, suiteId)) {
      for(const key of jsonld.getValues(suiteParams, 'publicKey')) {
        if(key.id === id) {
          return true;
        }
      }
    }
  }
  return false;
}

// find next unused key id
// TODO: improve algorithm to handle large number of keys
function _nextKeyId(didDocument, suiteId) {
  const keyPrefix = _keyPrefixMap[suiteId];
  let n = 0;
  let nextId;
  do {
    nextId = `${didDocument.id}#${keyPrefix}${++n}`;
  } while(_hasKeyId(didDocument, nextId));
  return nextId;
}

const _suiteParamsTypeMap = {
  authentication: {
    ed25519: 'Ed25519SignatureAuthentication2018',
    rsa: '...'
  },
  grantCapability: {
    ed25519: 'Ed25519SignatureCapabilityAuthorization2018',
    rsa: '...'
  },
  invokeCapability: {
    ed25519: 'Ed25519SignatureCapabilityAuthorization2018',
    rsa: '...'
  }
};

// get or create params
function _getParamsForType(didDocument, suiteId, type) {
  for(const suiteParams of jsonld.getValues(didDocument, suiteId)) {
    if(suiteParams.type === _suiteParamsTypeMap[suiteId][type]) {
      return suiteParams;
    }
  }
  // not found, create
  const params = {
    type: _suiteParamsTypeMap[suiteId][type],
    publicKey: []
  };
  jsonld.addValue(didDocument, suiteId, params);
  return params;
}

api['authn-add'] = async options => {
  _debug(options, 'authn-add', {did: options.did});
  const did = options.did;
  // lock local
  const release = await _lockDid(did, options);
  // get local did doc
  const privateDidDocument = await keyStorage.load(did);
  // get remote did doc
  const hostname = _getHostname(options);
  const didDocument = await didcv1.getObservable(
    {did, hostname, env: options.mode});

  // FIXME: move most of this to ext library
  const suiteId = _suiteIdMap[options.suite];
  // get next key not in local or ledger docs
  let nextKeyId;
  do {
    nextKeyId = _nextKeyId(privateDidDocument, suiteId);
  } while(_hasKeyId(didDocument, nextKeyId));
  const params = _getParamsForType(privateDidDocument, suiteId, options.type);
  const ledgerParams = _getParamsForType(didDocument, suiteId, options.type);

  // public ledger key
  const newLedgerKey = {
    id: nextKeyId,
    owner: privateDidDocument.id
  };
  if(options.type === 'ed25519') {
    newLedgerKey.type = 'Ed25519VerificationKey2018';
    if(options.public) {
      newLedgerKey.publicKeyBase58 = options.public;
    }
  } else if(options.type === 'rsa') {
    throw new Error('not implemented');
  }
  // clone and add to private local key
  const newLocalKey = util.deepClone(newLedgerKey);
  if(options.type === 'ed25519') {
    if(options.private) {
      newLocalKey.privateKey = {
        privateKeyBase58: options.private
      };
    }
  } else if(options.type === 'rsa') {
    throw new Error('not implemented');
  }
  // add to local and remote docs
  jsonld.addValue(params, 'publicKey', newLedgerKey);
  jsonld.addValue(ledgerParams, 'publicKey', newLocalKey);

  // FIXME: reusing import code, rename it
  _import(options, privateDidDocument);

  await release();

  _log(options, 'DID local update successful.');

  //if(options.send) {
    await _send(
      {options, didDocument, privateDidDocument, operationType: 'update'});
  //}
};

function _removeKeyId(didDocument, id) {
  // check all suites
  for(const suiteId of _suiteIds) {
    for(const suiteParams of jsonld.getValues(didDocument, suiteId)) {
      suiteParams.publicKey = jsonld.getValues(suiteParams, 'publicKey')
        .filter(key => key.id !== id);
    }
  }
}

api['authn-remove'] = async options => {
  _debug(options, 'authn-remove', {did: options.did, key: options.key});
  const did = options.did;
  // lock local
  const release = await _lockDid(did, options);
  // get local did doc
  const privateDidDocument = await keyStorage.load(did);
  // get remote did doc
  const hostname = _getHostname(options);
  const didDocument = await didcv1.getObservable(
    {did, hostname, env: options.mode});

  // FIXME: move most of this to ext library
  // remove key
  _removeKeyId(privateDidDocument, options.key);
  _removeKeyId(didDocument, options.key);

  // FIXME: reusing import code, rename it
  _import(options, privateDidDocument);

  await release();

  _log(options, 'DID local update successful.');

  //if(options.send) {
    await _send(
      {options, didDocument, privateDidDocument, operationType: 'update'});
  //}
};

api['authn-rotate'] = async options => {
  // TODO: remove old, add new, track old id as used, rovoke old
  throw new Error('authn-rotate not implemented');
};

api['ocap-add'] = async options => {
  throw new Error('ocap-add not implemented');
};

api['ocap-revoke'] = async options => {
  throw new Error('ocap-add not implemented');
};
