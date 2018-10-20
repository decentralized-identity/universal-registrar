/**
 * Equihash for Node.js.
 * khovratovich engine.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * MIT License
 * <https://github.com/digitalbazaar/equihash/blob/master/LICENSE>
 */
const addon = require('bindings')('khovratovich');

// blake2b constant
exports.BLAKE2B_PERSONAL_BYTES = 16;

exports.solve = (seed, options, callback) => {
  const n = 'n' in options ? options.n : 90;
  const k = 'k' in options ? options.k : 5;
  const nonceLength = 'nonceLength' in options ? options.nonceLength : 4;
  const personal =
    'personal' in options ? options.personal :
    Buffer.alloc(exports.BLAKE2B_PERSONAL_BYTES, 0);

  if('algorithm' in options && options.algorithm !== 'khovratovich') {
    return callback(
      new Error('Equihash \'algorithm\' unknown.'));
  }

  // k must be in range of [1,7]
  if(!(k >= 1 && k <= 7)) {
    // TODO: Find out why the implementation requires k < 7
    return callback(
      new RangeError('Equihash \'k\' parameter must be from 1 to 7.'));
  }

  if((n / (k + 1)) > 32) {
    return callback(
      new RangeError('Equihash \'n\' and \'k\' must satisfy n/(k+1) <= 32.'));
  }

  if(personal.length > exports.BLAKE2B_PERSONAL_BYTES) {
    return callback(
      new RangeError('Equihash \'personal\' length too large.'));
  }

  if(nonceLength < 4) {
    return callback(
      new RangeError('Equihash \'nonceLength\' must be >= 4 bytes.'));
  }

  // convert nonce to buffer
  let nonce = 'nonce' in options ? options.nonce : 1;
  if(typeof nonce === 'number') {
    const ab = new ArrayBuffer(nonceLength);
    const b = Buffer.from(ab);
    b.fill(0);
    const dv = new DataView(ab);
    dv.setUint32(0, nonce, true);
    nonce = b;
  } else if(!(nonce instanceof Buffer)) {
    return callback(
      new TypeError('Equihash \'nonce\' must be buffer or number.'));
  }

  // check buffer size if length provided
  if('nonceLength' in options) {
    if(options.nonceLength !== nonce.length) {
      return callback(
        new RangeError('Equihash \'nonce\' length and \'nonceLength\' differ.'));
    }
  }

  const maxNonces = 'maxNonces' in options ? options.maxNonces : 0xffff;

  if(maxNonces === 0) {
    return callback(
      new RangeError('Equihash \'maxNonces\' must be at least 1.'));
  }

  const dv = new DataView(nonce.buffer);
  const currentNonce = dv.getUint32(0, true);
  // limit this implementation to uin32_t range nonces
  if((0xffffffff - currentNonce) < (maxNonces - 1)) {
    return callback(
      new RangeError('Equihash \'nonce\' limited to 32 bit values.'));
  }

  addon.solve({n, k, personal, seed, nonce, maxNonces}, function(err, proof) {
    //console.log(err, proof, proof.value.length);
    if(err) {
      return callback(err);
    }

    if(proof.solution.length === 0) {
      return callback(new Error('Equihash solution not found.'));
    }

    callback(null, {
      n: proof.n,
      k: proof.k,
      personal: proof.personal,
      nonce: proof.nonce,
      solution: proof.solution,
      algorithm: 'khovratovich',
      algorithmParamters: {
        personalization: proof.personal
      }
    });
  });
};

exports.verify = (seed, options, callback) => {
  const n = 'n' in options ? options.n : 90;
  const k = 'k' in options ? options.k : 5;
  const nonceLength = 'nonceLength' in options ? options.nonceLength : 4;
  const personal = 'personal' in options ? options.personal :
    Buffer.alloc(exports.BLAKE2B_PERSONAL_BYTES, 0);
  const solution = options.solution;

  if('algorithm' in options && options.algorithm !== 'khovratovich') {
    return callback(
      new Error('Equihash \'algorithm\' unknown.'));
  }

  // k must be in range of [1,7]
  if(!(k >= 1 && k <= 7)) {
    return callback(
      new RangeError('Equihash \'k\' parameter must be from 1 to 7.'));
  }

  if((n / (k + 1)) > 32) {
    return callback(
      new RangeError('Equihash \'n\' and \'k\' must satisfy n/(k+1) <= 32.'));
  }

  if(personal.length > exports.BLAKE2B_PERSONAL_BYTES) {
    return callback(
      new RangeError('Equihash \'personal\' length too large.'));
  }

  // convert nonce to buffer
  let nonce = options.nonce;
  if(typeof nonce === 'number') {
    const ab = new ArrayBuffer(nonceLength);
    const b = Buffer.from(ab);
    b.fill(0);
    const dv = new DataView(ab);
    dv.setUint32(0, nonce, true);
    nonce = b;
  } else if(!(nonce instanceof Buffer)) {
    return callback(
      new TypeError('Equihash \'nonce\' must be buffer or number.'));
  }

  // solution is array of 2^k 32 bit unsigned int values
  // each array element only uses n/(k+1) + 1 bits
  if(solution.length !== Math.pow(2,k)) {
    return callback(
      new Error('Equihash \'solution\' is not 2^k 32 bit values.'));
  }

  // distinct indices check
  // missing from khovratovich implementation
  if(solution.length !== new Set(solution).size) {
    return callback(null, false);
    //return callback(new Error('Equihash indices not distinct.'));
  }

  // ordered indices check
  // missing from khovratovich implementation
  // array is a tree structure, check left nodes < right nodes
  for(let _k = 0; _k < k; ++_k) {
    const stride = 1 << _k;
    for(let i = 0; i < solution.length; i += (2 * stride)) {
      if(solution[i] >= solution[i + stride]) {
        return callback(null, false);
        //return callback(new Error('Equihash indices unordered.'));
      }
    }
  }

  addon.verify({n, k, personal, seed, nonce, solution}, callback);
};

exports.verifySync = (seed, options) => {
  const n = 'n' in options ? options.n : 90;
  const k = 'k' in options ? options.k : 5;
  const nonceLength = 'nonceLength' in options ? options.nonceLength : 4;
  const personal = 'personal' in options ? options.personal :
    Buffer.alloc(exports.BLAKE2B_PERSONAL_BYTES, 0);
  const solution = options.solution;

  // k must be in range of [1,7]
  if(!(k >= 1 && k <= 7)) {
    throw new Error('Equihash \'k\' parameter must be from 1 to 7.');
  }

  if((n / (k + 1)) > 32) {
    throw new Error('Equihash \'n\' and \'k\' must satisfy n/(k+1) <= 32.');
  }

  if(personal.length > exports.BLAKE2B_PERSONAL_BYTES) {
    throw new Error('Equihash \'personal\' length too large.');
  }

  // convert nonce to buffer
  let nonce = options.nonce;
  if(typeof nonce === 'number') {
    const ab = new ArrayBuffer(nonceLength);
    const b = Buffer.from(ab);
    b.fill(0);
    const dv = new DataView(ab);
    dv.setUint32(0, nonce, true);
    nonce = b;
  } else if(!(nonce instanceof Buffer)) {
    throw new Error('Equihash \'nonce\' must be buffer or number.');
  }

  // solution is array of 2^k 32 bit unsigned int values
  // each array element only uses n/(k+1) + 1 bits
  if(solution.length !== Math.pow(2,k)) {
    throw new Error('Equihash \'solution\' is not 2^k 32 bit values.');
  }

  // distinct indices check
  // missing from khovratovich implementation
  if(solution.length !== new Set(solution).size) {
    return false;
    //throw new Error('Equihash indices not distinct.');
  }

  // ordered indices check
  // missing from khovratovich implementation
  // array is a tree structure, check left nodes < right nodes
  for(let _k = 0; _k < k; ++_k) {
    const stride = 1 << _k;
    for(let i = 0; i < solution.length; i += (2 * stride)) {
      if(solution[i] >= solution[i + stride]) {
        return false;
      }
    }
  }

  return addon.verifySync({n, k, personal, seed, nonce, solution});
};

