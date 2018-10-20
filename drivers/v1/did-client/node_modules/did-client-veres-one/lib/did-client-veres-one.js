/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
(function(global) {

'use strict';

const Injector = require('./Injector');
const util = require('./util');

// determine if using node.js or browser
const _nodejs = (
  typeof process !== 'undefined' && process.versions && process.versions.node);
const _browser = !_nodejs &&
  (typeof window !== 'undefined' || typeof self !== 'undefined');

/**
 * Attaches the API to the given object.
 *
 * @param api the object to attach the API to.
 */
function wrap(api) {

const injector = new Injector();

const _env = {
  dev: {hostname: 'genesis.veres.one.localhost:42443'},
  test: {hostname: 'testnet.veres.one'},
  live: {hostname: 'veres.one'}
};

/* Core API */

/**
 * Send an operation to a Veres One ledger node.
 *
 * @return response
 */
api.send = util.callbackify(async function({
  operation,
  hostname = env in _env ? _env[env].hostname : null,
  env = 'dev'
}) {
  if(!(env in _env)) {
    throw new Error('"env" must be "dev", "test", or "live".');
  }

  if(!hostname) {
    throw new Error('"hostname" must be a non-empty string.');
  }

  const https = require('https');
  let agent;
  const ledgerAgentsUrl = `https://${hostname}/ledger-agents`;

  if(env === 'dev') {
    agent = new https.Agent({rejectUnauthorized: false});
  }

  const headers = {
    'accept': 'application/ld+json, application/json'
  };

  const r2 = injector.use('r2');
  const ledgerAgentRes = await r2({
    url: ledgerAgentsUrl,
    method: 'GET',
    agent,
    headers
  }).json;
  const ledgerAgent = ledgerAgentRes.ledgerAgent[0];
  return r2({
    url: ledgerAgent.service.ledgerOperationService,
    method: 'POST',
    agent,
    headers,
    json: operation
  }).response;
});

/**
 * Get a DID Document and any meta data related to it from a Veres One ledger
 * node.
 */
api.get = util.callbackify(async function({
  did,
  hostname = env in _env ? _env[env].hostname : null,
  env = 'dev'
}) {
  if(!(env in _env)) {
    throw new Error('"env" must be "dev", "test", or "live".');
  }

  if(!hostname) {
    throw new Error('"hostname" must be a non-empty string.');
  }

  const https = require('https');
  let agent;
  const ledgerAgentsUrl = `https://${hostname}/ledger-agents`;

  if(env === 'dev') {
    agent = new https.Agent({rejectUnauthorized: false});
  }

  const headers = {
    'accept': 'application/ld+json, application/json'
  };

  const r2 = injector.use('r2');
  const ledgerAgentRes = await r2({
    url: ledgerAgentsUrl,
    method: 'GET',
    agent,
    headers
  }).json;
  const ledgerAgent = ledgerAgentRes.ledgerAgent[0];
  // FIXME: use URLSearchParams or similar
  const queryUrl = ledgerAgent.service.ledgerQueryService + '?id=' + did;
  return r2({
    url: queryUrl,
    method: 'POST',
    agent,
    headers
  }).response;
});

api.getObservable = util.callbackify(async function({
  did,
  hostname = env in _env ? _env[env].hostname : null,
  env = 'dev',
  autoObserve = true
}) {
  const response = await api.get({did, hostname, env});
  if(response.status !== 200) {
    throw new Error('Failed to get DID Document; status=' + response.status);
  }

  const record = await response.json();
  const didDocument = record.object;
  let sequence = record.meta.sequence || 0;

  // add special `observe` and `commit` properties
  const jsonpatch = injector.use('fast-json-patch');
  const didv1 = injector.use('did-veres-one');
  let recordPatch;
  let observer;

  const observe = () => {
    if(observer) {
      jsonpatch.unobserve(didDocument, observer);
    }
    observer = jsonpatch.observe(didDocument);
    recordPatch = null;
  };

  const commit = () => {
    if(!recordPatch) {
      if(!observer) {
        throw new Error('Not observing changes.');
      }
      const patch = jsonpatch.generate(observer);
      jsonpatch.unobserve(didDocument, observer);
      observer = null;
      recordPatch = {
        '@context': didv1.constants.VERES_ONE_V1_CONTEXT,
        target: didDocument.id,
        sequence,
        patch
      };
    }
    return recordPatch;
  };

  Object.defineProperty(didDocument, 'observe', {
    value: observe,
    writable: false,
    enumerable: false,
    configurable: true
  });

  Object.defineProperty(didDocument, 'commit', {
    value: commit,
    writable: false,
    enumerable: false,
    configurable: true
  });

  if(autoObserve) {
    // automatically start observing
    didDocument.observe();
  }

  return didDocument;
});

/**
 * Send an operation to a Veres One accelerator.
 *
 * @return response
 */
api.sendToAccelerator = util.callbackify(async function({
  operation,
  keyId,
  key,
  hostname,
  env = 'dev'
}) {
  if(!(env in _env)) {
    throw new Error('"env" must be "dev", "test", or "live".');
  }

  if(!hostname) {
    throw new Error('"hostname" must be a non-empty string.');
  }

  const https = require('https');
  const httpSignature = require('http-signature');
  const acceleratorPath = '/accelerator/proofs';
  const acceleratorUrl = `https://${hostname}${acceleratorPath}`;
  let agent;

  if(env === 'dev') {
    agent = new https.Agent({rejectUnauthorized: false});
  }

  const headers = {
    'accept': 'application/ld+json, application/json',
    'host': hostname
  };

  if(key && keyId) {
    httpSignature.signRequest({
      getHeader: function (header) {
        // case insensitive lookup
        return headers[Object.keys(headers).find(
          key => key.toLowerCase() === header.toLowerCase())];
      },
      setHeader: function (header, value) {
        headers[header] = value;
      },
      method: 'POST',
      path: acceleratorPath
    }, {
      headers: ['(request-target)', 'date', 'host'],
      keyId,
      key
    });
  }

  const r2 = injector.use('r2');
  return r2({
    url: acceleratorUrl,
    method: 'POST',
    agent,
    headers,
    json: operation
  }).response;
});

// expose injector API
api.use = injector.use.bind(injector);

} // end wrap

// used to generate a new API instance
const factory = function() {
  return wrap(function() {return factory();});
};
wrap(factory);

if(_nodejs) {
  // export nodejs API
  module.exports = factory;
} else if(typeof define === 'function' && define.amd) {
  // export AMD API
  define([], function() {
    return factory;
  });
} else if(_browser) {
  // export simple browser API
  if(typeof global.didcv1 === 'undefined') {
    global.didcv1 = {};
  }
  wrap(global.didcv1);
}

})(typeof window !== 'undefined' ? window : this);
