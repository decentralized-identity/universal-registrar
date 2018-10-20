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

const constants = {
  VERES_ONE_V1_CONTEXT: 'https://w3id.org/veres-one/v1'
};

/**
 * Attaches the API to the given object.
 *
 * @param api the object to attach the API to.
 */
function wrap(api) {

const injector = new Injector();

/* Core API */
api.constants = constants;
api.contexts = {
  [constants.VERES_ONE_V1_CONTEXT]: require('./contexts/veres-one-v1')
};

/**
 * Create a cryptonym DID from a public key with encoding `pem`, `base58`,
 * or `forge` (forge is supported privately/internally only).
 */
api.createCryptonymDid = ({publicKey, encoding, env = 'dev'}) => {
  if(!['forge', 'pem', 'base58'].includes(encoding)) {
    throw new TypeError(
      '`encoding` must be `pem` or `base58`.');
  }

  const prefix = (env === 'live') ? 'did:v1:' : 'did:v1:test:';

  let did = prefix + 'nym:';
  if(encoding === 'base58') {
    return did + publicKey;
  }

  const forge = injector.use('forge');

  if(encoding === 'pem') {
    // deserialize key from PEM
    publicKey = forge.pki.publicKeyFromPem(publicKey);
  }

  // use SubjectPublicKeyInfo fingerprint
  const fingerprintBuffer = forge.pki.getPublicKeyFingerprint(
    publicKey, {md: forge.md.sha256.create()});
  return did + util.encodeBase64Url(fingerprintBuffer.bytes(), {forge});
};

/**
 * Generate a new DID Document.
 */
api.generate = util.callbackify(async function(
  {didType = 'nym', keyType = 'RsaVerificationKey2018', passphrase, env = 'dev'}) {
  const publicDidDocument = {
    '@context': constants.VERES_ONE_V1_CONTEXT,
  };
  let privateDidDocument = null;

  // passphrase is a required parameter if generating a nym-based DID
  if(didType === 'nym' && passphrase === undefined) {
    throw new TypeError('"options.passphrase" must be specified.');
  }

  if(didType === 'nym') {
    if(!['RsaVerificationKey2018', 'Ed25519VerificationKey2018']
      .includes(keyType)) {
      throw new Error(`Unknown key type: "${keyType}"`);
    }

    let generateKeyPair;
    let encodePublicKey;
    let encryptPrivateKey;
    let authenticationAppSuiteType;
    let ocapAppSuiteType;
    if(keyType === 'Ed25519VerificationKey2018') {
      authenticationAppSuiteType = 'Ed25519SignatureAuthentication2018';
      ocapAppSuiteType = 'Ed25519SignatureCapabilityAuthorization2018';
      generateKeyPair = api.generateEd25519KeyPair;
      encodePublicKey = api.addEncodedEd25519PublicKey;
      encryptPrivateKey = api.addEncryptedEd25519PrivateKey;
    } else {
      // RSA key
      authenticationAppSuiteType = 'RsaSignatureAuthentication2018';
      ocapAppSuiteType = 'RsaSignatureCapabilityAuthorization2018';
      const keyBits = (keyType === 'RsaVerificationKey2018') ? 2048 : 2048;
      generateKeyPair = async () => api.generateRsaKeyPair({keyBits});
      encodePublicKey = api.addEncodedRsaPublicKey;
      encryptPrivateKey = api.addEncryptedRsaPrivateKey;
    }

    // application suite parameters
    const appSuites = {
      // for authenticating as DID entity
      authentication: {
        type: authenticationAppSuiteType,
        publicKeyHash: 'authn-key-1'
      },
      // for granting capabilities as DID entity
      grantCapability: {
        type: ocapAppSuiteType,
        publicKeyHash: 'ocap-grant-key-1'
      },
      // for invoking capabilities as DID entity
      invokeCapability: {
        type: ocapAppSuiteType,
        publicKeyHash: 'ocap-invoke-key-1'
      }
    };

    // generate a separate key pair for each app suite
    for(const name in appSuites) {
      appSuites[name].keys = await generateKeyPair();
    }

    // generate nym using authentication app suite
    const did = api.createCryptonymDid({
      publicKey: appSuites.authentication.keys.publicKey,
      encoding: typeof appSuites.authentication.keys.publicKey === 'string' ?
        'base58' : 'forge',
      env
    });

    publicDidDocument.id = did;

    // add app suites to DID Document
    for(const name in appSuites) {
      const appSuite = appSuites[name];
      publicDidDocument[name] = [{
        type: appSuite.type,
        publicKey: [encodePublicKey({
          id: did + '#' + appSuite.publicKeyHash,
          type: keyType,
          owner: did
        }, appSuite.keys.publicKey)]
      }];
    }

    // add private key information to the private DID document
    privateDidDocument = util.deepClone(publicDidDocument);
    for(const name in appSuites) {
      const {privateKey} = appSuites[name].keys;
      const {publicKey} = privateDidDocument[name][0];
      publicKey[0].privateKey = await encryptPrivateKey(
        {}, privateKey, passphrase);
    }
  } else {
    const uuid = injector.use('uuid');
    const prefix = (env === 'live') ? 'did:v1:' : 'did:v1:test:';
    publicDidDocument.id = prefix + 'uuid:' + uuid();
    privateDidDocument = util.deepClone(publicDidDocument);
  }

  return {publicDidDocument, privateDidDocument};
});

/**
 * Convert a private key to a public key
 */
api.publicDidDocument = util.callbackify(async function({privateDidDocument}) {
  // whitelist copy of public details
  const publicDidDocument = {
    '@context': util.deepClone(privateDidDocument['@context']),
    id: privateDidDocument.id
  };
  const suites = ['authentication', 'grantCapability', 'invokeCapability'];
  for(const suite of suites) {
    publicDidDocument[suite] = [];
    for(const item of privateDidDocument[suite]) {
      const pubItem = {
        type: util.deepClone(item.type),
        publicKey: []
      };
      if('@context' in item) {
        pubItem['@context'] = util.deepClone(item['@context']);
      }
      for(const key of item.publicKey) {
        const pubKey = {
          id: key.id,
          type: util.deepClone(key.type),
          owner: key.owner
        };
        for(const field of ['@context', 'publicKeyPem', 'publicKeyBase58']) {
          if(field in key) {
            pubKey[field] = util.deepClone(key[field]);
          }
        }
        pubItem.publicKey.push(pubKey);
      }
      publicDidDocument[suite].push(pubItem);
    }
  }
  return publicDidDocument;
});

/**
 * Add an ocap grant proof to a capability DID Document.
 */
api.attachGrantProof = util.callbackify(async function(
  // TODO: support `passphrase` for encrypted private key pem or keep decrypt
  //   as the responsibility of the caller?
  {didDocument, creator, privateKeyPem}) {
  // FIXME: use ldocap.js

  // FIXME: validate didDocument, creator, and privateKeyPem
  // TODO: support `signer` API as alternative to `privateKeyPem`
  const jsigs = injector.use('jsonld-signatures');
  return jsigs.sign(didDocument, {
    algorithm: 'RsaSignature2018',
    creator,
    privateKeyPem,
    proof: {
      '@context': constants.VERES_ONE_V1_CONTEXT,
      proofPurpose: 'grantCapability'
    }
  });
});

/**
 * Wrap a DID Document in a Web Ledger Operation.
 */
api.wrap = ({didDocument, operationType = 'create'}) => {
  const operation = {
    '@context': constants.VERES_ONE_V1_CONTEXT
  };

  switch(operationType) {
    case 'create':
      operation.type = 'CreateWebLedgerRecord';
      operation.record = didDocument;
      break;
    case 'update':
      operation.type = 'UpdateWebLedgerRecord';
      operation.recordPatch = didDocument.commit();
      break;
    default:
      throw new Error(`Unknown operation type "${operationType}"`);
  }

  return operation;
};

/**
 * Add an ocap invocation proof to an operation.
 */
api.attachInvocationProof = util.callbackify(async function(
  // TODO: support `passphrase` for encrypted private key pem or keep decrypt
  //   as the responsibility of the caller?
  {operation, capability, capabilityAction, creator,
    algorithm, privateKeyPem, privateKeyBase58}) {
  // FIXME: use ldocap.js

  // FIXME: use `algorithm` and validate private key, do not switch off of it
  if(privateKeyPem) {
    algorithm = 'RsaSignature2018';
  } else {
    algorithm = 'Ed25519Signature2018';
  }

  // FIXME: validate operation, capability, creator, and privateKeyPem
  // TODO: support `signer` API as alternative to `privateKeyPem`
  const jsigs = injector.use('jsonld-signatures');
  return jsigs.sign(operation, {
    algorithm,
    creator,
    privateKeyPem,
    privateKeyBase58,
    proof: {
      '@context': constants.VERES_ONE_V1_CONTEXT,
      proofPurpose: 'invokeCapability',
      capability,
      capabilityAction
    }
  });
});

/**
 * Add an Equihash proof of work to an operation.
 */
api.attachEquihashProof = util.callbackify(async function(
  {operation, env = 'dev', parameters}) {
  let nParam;
  let kParam;
  if(parameters) {
    if(!(typeof parameters.n === 'number' &&
      typeof parameters.k === 'number')) {
      throw new TypeError(
        '`parameters.n` and `parameters.k` must be integers.');
    }
    nParam = parameters.n;
    kParam = parameters.k;
  } else {
    switch(env) {
      case 'dev':
      case 'test':
        nParam = 64;
        kParam = 3;
        break;
      case 'live':
        // FIXME: determine from ledger config
        nParam = 144;
        kParam = 5;
        break;
      default:
        throw new Error('"env" must be "dev", "test", or "live".');
    }
  }

  const jsigs = injector.use('jsonld-signatures');
  return jsigs.sign(operation, {
    algorithm: 'EquihashProof2018',
    parameters: {
      n: nParam,
      k: kParam
    }
  });
});

api.generateEd25519KeyPair = util.callbackify(async function() {
  // currently node only
  if(!injector.env.nodejs) {
    throw new Error(
      '"Ed25519VerificationKey2018" is not supported on this ' +
      'platform yet.');
  }
  const bs58 = require('bs58');
  const chloride = require('chloride');
  const keypair = chloride.crypto_sign_keypair();
  return {
    publicKey: bs58.encode(keypair.publicKey),
    privateKey: bs58.encode(keypair.secretKey)
  };
});

api.addEncodedEd25519PublicKey = (publicKeyNode, publicKey) => {
  publicKeyNode.publicKeyBase58 = publicKey;
  return publicKeyNode;
};

api.addEncryptedEd25519PrivateKey = async (
  privateKeyNode, privateKey, passphrase) => {
  if(passphrase !== null) {
    privateKeyNode.jwe =
      await encrypt({privateKeyBase58: privateKey}, passphrase);
  } else {
    // no passphrase, do not encrypt private key
    privateKeyNode.privateKeyBase58 = privateKey;
  }
  return privateKeyNode;
};

api.generateRsaKeyPair = util.callbackify(async function(
  {keyBits = 2048, exponent = 0x10001}) {
  const forge = injector.use('forge');

  if(injector.env.nodejs) {
    const ursa = require('ursa');
    const keypair = ursa.generatePrivateKey(keyBits, exponent);
    return {
      privateKey: forge.pki.privateKeyFromPem(keypair.toPrivatePem()),
      publicKey: forge.pki.publicKeyFromPem(keypair.toPublicPem())
    };
  }

  return new Promise((resolve, reject) => {
    forge.pki.rsa.generateKeyPair({bits: keyBits, e: exponent},
    (err, keypair) => {
      if(err) {
        return reject(err);
      }
      resolve(keypair);
    });
  });
});

api.addEncodedRsaPublicKey = (publicKeyNode, publicKey) => {
  const forge = injector.use('forge');
  publicKeyNode.publicKeyPem = forge.pki.publicKeyToPem(publicKey);
  return publicKeyNode;
};

api.addEncryptedRsaPrivateKey = async (
  privateKeyNode, privateKey, passphrase) => {
  const forge = injector.use('forge');
  if(passphrase !== null) {
    privateKeyNode.privateKeyPem = forge.pki.encryptRsaPrivateKey(
      privateKey, passphrase, {algorithm: 'aes256'});
  } else {
    // no passphrase, do not encrypt private key
    privateKeyNode.privateKeyPem = forge.pki.privateKeyToPem(privateKey);
  }
  return privateKeyNode;
};

async function encrypt(privateKey, password) {
  const forge = injector.use('forge');

  const keySize = 32;
  const salt = forge.random.getBytesSync(32);
  const iterations = 4096;
  const key = await pbkdf2(password, salt, iterations, keySize);

  const jweHeader = {
    alg: 'PBES2-A128GCMKW',
    enc: 'A128GCMKW',
    jwk: {
      kty: 'PBKDF2',
      s: util.encodeBase64Url(salt, {forge}),
      c: iterations
    }
  };

  // FIXME: this probably needs to be cleaned up/made more standard

  const iv = forge.random.getBytesSync(12);
  const cipher = forge.cipher.createCipher('AES-GCM', key);
  cipher.start({iv});
  cipher.update(forge.util.createBuffer(JSON.stringify(privateKey)));
  cipher.finish();
  const encrypted = cipher.output.getBytes();
  const tag = cipher.mode.tag.getBytes();

  const jwe = {
    unprotected: jweHeader,
    iv: util.encodeBase64Url(iv, {forge}),
    ciphertext: util.encodeBase64Url(encrypted, {forge}),
    tag: util.encodeBase64Url(tag, {forge})
  };

  return jwe;
}

async function decrypt(jwe, password) {
  const forge = injector.use('forge');

  // FIXME: check header, implement according to JWE standard

  const keySize = 32;
  let {salt, iterations} = jwe.unprotected.jwk;
  salt = util.decodeBase64Url(salt, {forge});
  const key = await pbkdf2(password, salt, iterations, keySize);

  const iv = util.decodeBase64Url(jwe.iv, {forge});
  const tag = util.decodeBase64Url(jwe.tag, {forge});
  const decipher = forge.cipher.createDecipher('AES-GCM', key);
  decipher.start({iv, tag});
  decipher.update(util.decodeBase64Url(jwe.ciphertext, {forge}));
  const pass = decipher.finish();
  if(!pass) {
    throw new Error('Invalid password.');
  }

  const privateKey = JSON.parse(decipher.output.getBytes());
  return privateKey;
}

async function pbkdf2(password, salt, iterations, keySize) {
  const forge = injector.use('forge');
  return new Promise((resolve, reject) => {
    forge.pkcs5.pbkdf2(password, salt, iterations, keySize, (err, key) =>
      err ? reject(err) : resolve(key));
  });
}

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
  if(typeof global.didv1 === 'undefined') {
    global.didv1 = {};
  }
  wrap(global.didv1);
}

})(typeof window !== 'undefined' ? window : this);
