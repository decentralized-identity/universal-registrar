var isElectron = require('is-electron')

if(process.env.CHLORIDE_JS) {
  module.exports = require('./browser-small')
} else {
  try {
    var cl = module.exports = require('./bindings')

    if(isElectron()) {
      //there is a weird problem with electro.
      //where detached signatures do not work, but other
      //signatures do...

      var keys = cl.crypto_sign_keypair()
      var msg = cl.crypto_hash(new Buffer('test signature'))
      var sig = cl.crypto_sign_detached(msg, keys.secretKey)

      if(!cl.crypto_sign_verify_detached(sig, msg, keys.publicKey)) {
        console.error('detached signatures broken in electron, using workaround')

        var verify = module.exports.crypto_sign_verify_detached
        module.exports.crypto_sign_verify_detached = function (sig, msg, pk) {
          return module.exports.crypto_sign_open(Buffer.concat([sig, msg]), pk)
        }
      }
    }
  } catch (err) {
    console.error('error loading sodium bindings:', err.message)
    console.error('falling back to javascript version.')
    module.exports = require('./browser-small')
  }
}


