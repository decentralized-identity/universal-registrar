
function Z(n) {
  return new Buffer(n)
}

module.exports = function (na) {
  var exports = {}

  // *** Signatures ***

  exports.crypto_sign_seed_keypair = function (seed) {
    var pk = Z(na.crypto_sign_PUBLICKEYBYTES)
    var sk = Z(na.crypto_sign_SECRETKEYBYTES)
    na.crypto_sign_seed_keypair(pk, sk, seed)
    return {publicKey: pk, secretKey: sk}
  }

  exports.crypto_sign_keypair = function () {
    var pk = Z(na.crypto_sign_PUBLICKEYBYTES)
    var sk = Z(na.crypto_sign_SECRETKEYBYTES)
    na.crypto_sign_keypair(pk, sk)
    return {publicKey: pk, secretKey: sk}
  }

  exports.crypto_sign = function (message, sk) {
    var signed = Z(message.length + na.crypto_sign_BYTES)
    na.crypto_sign(signed, message, sk)
    return signed
  }

  exports.crypto_sign_open = function (signed, pk) {
    var message = Z(signed.length - na.crypto_sign_BYTES)
    if(na.crypto_sign_open(message, signed, pk))
      return message
  }

  exports.crypto_sign_detached = function (message, sk) {
    var signed = Z(na.crypto_sign_BYTES)
    na.crypto_sign_detached(signed, message, sk)
    return signed
  }

  exports.crypto_sign_verify_detached = function (sig, msg, pk) {
    return na.crypto_sign_verify_detached(sig, msg, pk)
  }
  // *** Box ***

  exports.crypto_box_seed_keypair = function (seed) {
    var pk = Z(na.crypto_box_PUBLICKEYBYTES)
    var sk = Z(na.crypto_box_SECRETKEYBYTES)
    na.crypto_box_seed_keypair(pk, sk, seed)
    return {publicKey: pk, secretKey: sk}
  }

  exports.crypto_box_keypair = function () {
    var pk = Z(na.crypto_box_PUBLICKEYBYTES)
    var sk = Z(na.crypto_box_SECRETKEYBYTES)
    na.crypto_box_keypair(pk, sk)
    return {publicKey: pk, secretKey: sk}
  }


  exports.crypto_box_easy = function (ptxt, nonce, pk, sk) {
    var ctxt = Z(ptxt.length + na.crypto_box_MACBYTES)
    na.crypto_box_easy(ctxt, ptxt, nonce, pk, sk)
    return ctxt
  }

  exports.crypto_box_open_easy = function (ctxt, nonce, pk, sk) {
    var ptxt = Z(ctxt.length - na.crypto_box_MACBYTES)
    if(na.crypto_box_open_easy(ptxt, ctxt, nonce, pk, sk))
      return ptxt
  }

  // *** SecretBox ***

  exports.crypto_secretbox_easy = function (ptxt, nonce, key) {
    var ctxt = Z(ptxt.length + na.crypto_secretbox_MACBYTES)
    na.crypto_secretbox_easy(ctxt, ptxt, nonce, key)
    return ctxt
  }

  exports.crypto_secretbox_open_easy = function (ctxt, nonce, key) {
    var ptxt = Z(ctxt.length - na.crypto_secretbox_MACBYTES)
    if(na.crypto_secretbox_open_easy(ptxt, ctxt, nonce, key))
      return ptxt
  }

  // *** Auth (hmac) ***

  exports.crypto_auth = function (input, key) {
    var output = Z(na.crypto_auth_BYTES)
    na.crypto_auth(output, input, key)
    return output
  }

  exports.crypto_auth_verify = function (output, input, key) {
    return na.crypto_auth_verify(output, input, key) ? 0 : 1
  }
  // *** Hash (sha512)

  exports.crypto_hash = function (ptxt) {
    var hash = Z(na.crypto_hash_BYTES)
    na.crypto_hash_sha512(hash, ptxt)
    return hash
  }

  exports.crypto_hash_sha256 = function (ptxt) {
    var hash = Z(na.crypto_hash_sha256_BYTES)
    na.crypto_hash_sha256(hash, ptxt)
    return hash
  }

  // *** scalarmult ***

  exports.crypto_scalarmult = function (sk, pk) {
    var secret = Z(na.crypto_scalarmult_BYTES)
    na.crypto_scalarmult(secret, sk, pk)
    return secret
  }

  // *** Conversions ***

  exports.crypto_sign_ed25519_pk_to_curve25519 = function (ed_pk) {
    var curve_pk = Z(na.crypto_box_PUBLICKEYBYTES)
    na.crypto_sign_ed25519_pk_to_curve25519(curve_pk, ed_pk)
    return curve_pk
  }

  exports.crypto_sign_ed25519_sk_to_curve25519 = function (ed_sk) {
    var curve_sk = Z(na.crypto_box_SECRETKEYBYTES)
    na.crypto_sign_ed25519_sk_to_curve25519(curve_sk, ed_sk)
    return curve_sk

  }

  // *** Randomness **

  exports.randombytes = function (length) {
    return na.randombytes_buf(length)
  }

  return exports
}

