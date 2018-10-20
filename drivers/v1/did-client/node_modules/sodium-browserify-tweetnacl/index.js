
var tweetnacl = require('tweetnacl/nacl-fast')
var Sha256 = require('sha.js/sha256')
var ed2curve = require('ed2curve')
var auth = require('tweetnacl-auth')

exports.crypto_hash_sha256 = function (msg) {
  return new Sha256().update(msg).digest()
}

function fix_keys(keys) {
  return {
    publicKey: new Buffer(keys.publicKey),
    secretKey: new Buffer(keys.secretKey),
  }
}

exports.crypto_sign_seed_keypair = function (seed) {
  return fix_keys(tweetnacl.sign.keyPair.fromSeed(seed))
}

exports.crypto_sign_keypair = function () {
  return fix_keys(tweetnacl.sign.keyPair())
}

exports.crypto_sign_detached = function (msg, skey) {
  return new Buffer(tweetnacl.sign.detached(msg, skey))
}

exports.crypto_sign = function (msg, sk) {
  return new Buffer(tweetnacl.sign(msg, sk))
}
exports.crypto_sign_open = function (ctxt, pk) {
  return new Buffer(tweetnacl.sign.open(ctxt, pk))
}

exports.crypto_sign_verify_detached = function (sig, msg, pkey) {
  return tweetnacl.sign.detached.verify(msg, sig, pkey)
}

exports.crypto_box_keypair = function () {
  return fix_keys(tweetnacl.box.keyPair())
}


exports.crypto_hash = function (msg) {
  return new Buffer(tweetnacl.hash(msg))
}

exports.crypto_secretbox_easy = function (msg, key, nonce) {
  return new Buffer(tweetnacl.secretbox(msg, key, nonce))
}

exports.crypto_secretbox_open_easy = function (ctxt, nonce, key) {
  var r = tweetnacl.secretbox.open(ctxt, nonce, key)
  return r ? new Buffer(r) : null
}

exports.crypto_sign_ed25519_pk_to_curve25519 = function (pk) {
  return new Buffer(ed2curve.convertPublicKey(pk))
}
exports.crypto_sign_ed25519_sk_to_curve25519 = function (sk) {
  return new Buffer(ed2curve.convertSecretKey(sk))
}

exports.crypto_box_easy = function (msg, nonce, pkey, skey) {
  return new Buffer(tweetnacl.box(msg, nonce, pkey, skey))
}

exports.crypto_box_open_easy = function (ctxt, nonce, pkey, skey) {
  var r = tweetnacl.box.open(ctxt, nonce, pkey, skey)
  return r ? new Buffer(r) : null
}

exports.crypto_scalarmult = function (pk, sk) {
  return new Buffer(tweetnacl.scalarMult(pk, sk))
}

//exports.crypto_auth = tweetnacl.auth
//exports.crypto_auth_verify = tweetnacl.auth.verify

exports.crypto_auth = function (msg, key) {
  return new Buffer(auth(msg, key))
}

exports.crypto_auth_verify = function (mac, msg, key) {
  var _mac = exports.crypto_auth(msg, key)
  var d = true
  //constant time comparson
  for(var i = 0; i < _mac.length; i++) {
    d = d && (_mac[i] === mac[i])
  }
  return +!d
}

exports.randombytes = function (buf) {
  var b = new Buffer(tweetnacl.randomBytes(buf.length))
  b.copy(buf)
  return null
}


