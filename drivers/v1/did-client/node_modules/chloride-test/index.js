var assert = require('assert')
var JSONB = require('json-buffer')
var tests = JSONB.parse(require('./data.json'))
var expectedApi = require('./api.json')

module.exports = function (sodium) {
  expectedApi.forEach(function (method) {
    assert.equal(typeof sodium[method], 'function')
  })

  assert.throws(function () {
    sodium.randombytes(32)
  })

  var b = new Buffer(32)
  assert.equal(sodium.randombytes(b), null)

  var b2 = new Buffer(b)
  sodium.randombytes(b2)
  assert.notDeepEqual(b, b2)

  var keys = sodium.crypto_sign_keypair()
  assert.ok(Buffer.isBuffer(keys.publicKey))
  assert.ok(Buffer.isBuffer(keys.secretKey))

  var keys2 = sodium.crypto_box_keypair()
  assert.ok(Buffer.isBuffer(keys2.publicKey))
  assert.ok(Buffer.isBuffer(keys2.secretKey))


  //now test the rest of the interface...

  var isArray = Array.isArray

  function apply (ary) {
    var name = ary[0]
    var fn = name === 'deepEqual' ? assert.deepEqual : sodium['crypto_'+name]
    if(!fn) throw new Error('method: crypto_'+name+' does not exist')

    console.log(ary.slice(1))
    var ret = fn.apply(null, ary.slice(1).map(function (e) {
      return isArray(e) ? apply(e) : e
    }))
    console.log(ret)
    return ret
  }


  var fails = 0, total = 0
  tests.forEach(function (op) {
    var op_name = op[1][0]
    total ++
    try {
      apply(op)
    } catch (err) {
      fails ++
      console.error('FAILED', op_name)
      return console.error(err)
    }
    console.log('PASSED', op_name)
  })
  return {total: total, fail: fails, pass: total - fails}
}




