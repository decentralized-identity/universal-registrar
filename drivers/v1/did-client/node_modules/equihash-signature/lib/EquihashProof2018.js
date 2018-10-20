/**
 * Copyright (c) 2018 Digital Bazaar, Inc. All rights reserved.
 */
'use strict';

const jsigs = require('jsonld-signatures');

module.exports = class EquihashProof2018
  extends jsigs.suites.LinkedDataSignature {
  constructor(injector, algorithm = 'EquihashProof2018') {
    super(injector, algorithm);
  }

  async createVerifyData(input, options) {
    const proof = options.proof;
    if(!('equihashParameterN' in proof && 'equihashParameterK' in proof)) {
      if(!('parameters' in options)) {
        throw new Error('"options.parameters" must be given.');
      }
      const {n, k} = options.parameters;
      proof.equihashParameterN = n;
      proof.equihashParameterK = k;
    }
    return super.createVerifyData(input, options);
  }

  async createProofNode(verifyData, options) {
    if(!('parameters' in options)) {
      throw new Error('"options.parameters" must be given.');
    }
    const {n, k} = options.parameters;

    if(this.injector.env.nodejs) {
      const hash = _hash(verifyData);
      const equihashOptions = {
        n, k, algorithm: 'khovratovich'
      };
      const result = await _solve(hash, equihashOptions);

      // convert solution to 32 bit big endian buffer
      const ab = new ArrayBuffer(result.solution.length * 4);
      const dv = new DataView(ab);
      result.solution.forEach((v, i) => {
        dv.setUint32(i * 4, v);
      });

      const proof = options.proof;
      Object.assign(proof, {
        equihashParameterN: result.n,
        equihashParameterK: result.k,
        nonce: result.nonce.toString('base64'),
        proofValue: Buffer.from(ab).toString('base64'),
      });
      return proof;
    }

    throw new Error('Not implemented');
  }

  async verify(framed, options) {
    options = Object.assign({}, options || {});

    // make fetching and checking public keys and checking nonces a no-op
    // because they aren't used in the default way by this scheme
    options.publicKey = {};
    options.checkNonce = () => true;
    options.checkKey = () => true;

    return super.verify(framed, options);
  }

  async sanitizeProofNode(proof, options) {
    proof = await super.sanitizeProofNode(proof, options);
    // nonce is an output value of the proof, so remove it here
    delete proof.nonce;
    return proof;
  }

  async verifyProofNode(verifyData, proof, options) {
    if(!proof.proofValue) {
      throw new Error('Missing proof value.');
    }
    if(typeof proof.proofValue !== 'string') {
      throw new TypeError('"proofValue" must be a string.');
    }

    if(this.injector.env.nodejs) {
      const hash = _hash(verifyData);

      // convert 32 bit big endian buffer solution to array
      const b = Buffer.from(proof.proofValue, 'base64');
      const dv = new DataView(b.buffer, b.byteOffset, b.byteLength);
      // convert from 32 bit big endian buffer
      const solution = new Array(b.length / 4);
      for(let i = 0; i < solution.length; ++i) {
        solution[i] = dv.getUint32(i * 4);
      }

      const equihashOptions = {
        n: proof.equihashParameterN,
        k: proof.equihashParameterK,
        nonce: Buffer.from(proof.nonce, 'base64'),
        solution: solution
      };
      return await _verify(hash, equihashOptions);
    }

    throw new Error('Not implemented');
  }

  async validateKey(key, options) { /* not used */ }
};

// TODO: remove once promises API is provided by equihash lib
async function _solve(seed, options) {
  const equihash = require('equihash');
  return new Promise((resolve, reject) => {
    equihash.solve(seed, options, (err, result) =>
      err ? reject(err) : resolve(result));
  });
}
async function _verify(seed, options) {
  const equihash = require('equihash');
  return new Promise((resolve, reject) => {
    equihash.verify(seed, options, (err, result) =>
      err ? reject(err) : resolve(result));
  });
}

function _hash(verifyData) {
  const crypto = require('crypto');
  return crypto.createHash('sha256')
    .update(verifyData.data, verifyData.encoding).digest();
}
