
var sodium = require('libsodium-wrappers')

function I(b) {
  return Buffer.isBuffer(b) ? new Uint8Array(b) : b
}

function B(b) {
  return (b instanceof Uint8Array) ? new Buffer(b) : b
}

function bufferize(fn) {
  if('function' !== typeof fn)
    throw new Error('not a function')
  return function () {
    var args = [].map.call(arguments, I)
    var r =  B(fn.apply(this, args))
    return r
  }
}

function keys (k) {
  return {
    publicKey: B(k.publicKey),
    secretKey: B(k.secretKey || k.privateKey)
  }
}

exports.crypto_sign_seed_keypair = function (seed) {
  return keys(sodium.crypto_sign_seed_keypair(I(seed)))
}

exports.crypto_sign_keypair = function () {
  return keys(sodium.crypto_sign_keypair())
}

exports.crypto_box_keypair = function () {
  return keys(sodium.crypto_box_keypair())
}

;[
  'sign_verify_detached',
  'sign_detached',
  'sign',
  'sign_open',
  'sign_ed25519_pk_to_curve25519',
  'sign_ed25519_sk_to_curve25519',
  'scalarmult',
  'secretbox_easy',
  'secretbox_open_easy',
  'box_easy',
  'box_open_easy',
  'auth',
  'auth_verify',
  'hash'
].forEach(function (name) {
  if(name === 'auth_verify') {
    //this is inconsistent with sign_verify!!
    var fn = bufferize(sodium.crypto_auth_verify)
    exports['crypto_'+name] = function (msg, tok, key) { return fn(msg, tok, key) ? 0 : 1 }
  }
  else
    exports['crypto_'+name] = bufferize(sodium['crypto_'+name])
})

var Sha256 = require('sha.js/sha256')
exports.crypto_hash_sha256 = function (msg) {
  return new Sha256().update(msg).digest()
}

function nullIfThrew (fn) {
  return function () {
    try { return fn.apply(this, [].slice.call(arguments)) }
    catch (err) { return null }
  }
}

exports.crypto_secretbox_open_easy = nullIfThrew(exports.crypto_secretbox_open_easy)
exports.crypto_box_open_easy = nullIfThrew(exports.crypto_box_open_easy)

exports.randombytes = function (buf) {
  new Buffer(sodium.randombytes_buf(buf.length)).copy(buf)
  return null
}
