// Equihash test vectors

const crypto = require('crypto');
const equihash = require('..');
const zcashVectors = require('./zcash-vectors');

const api = {
  // vectors to test
  tests: [],
  // vectors to benchmark
  benchmarks: []
};
const t = api.tests;
const b = api.benchmarks;

module.exports = api;

// ["int1", "int2", ...] => [int1, int2, ...]
function arrayFromStringArray(data, base=10) {
  return data.map(v => parseInt(v, base));
}

// ["hex1", "hex2", ...] => [int1, int2, ...]
function arrayFromHexArray(data) {
  return arrayFromStringArray(data, 16);
}

// buffer with n little endian uint32_t values
function bufferSeedNx32(value, n) {
  const a = new ArrayBuffer(n * 4);
  const dv = new DataView(a);
  for(let i = 0; i < n; ++i) {
    dv.setUint32(i * 4, value, true);
  }
  return new Buffer(a);
}

// buffer of n bytes with start data and remaining zeroed
function bufferSeedData(data, n) {
  if(data.length > n) {
    throw new Error('Data input too large.');
  }
  const buffer = Buffer.alloc(n, 0);
  data.copy(buffer);
  return buffer;
}

// [int1, int2, ...] to buffer with big endian uint32_t
api.bufferFromArray = function(data) {
  const a = new ArrayBuffer(data.length * 4);
  const dv = new DataView(a);
  for(let i = 0; i < data.length; ++i) {
    dv.setUint32(i * 4, data[i]);
  }
  return new Buffer(a);
}

// test properties:
//   n: uint
//   k: uint
//   seed: little endian buffer
//   nonce: uint
//   inputs: array of array of uint32_t
//   expect: true/false (optional)
//   label: string (optional)

function customTests(t) {
  // tests from khovratovich cli tool
  t.push({
    label: 'custom n=96,k=5',
    n: 96,
    k: 5,
    seed: bufferSeedNx32(1, 16),
    nonce: 2,
    inputs: [arrayFromHexArray('20e9  1396c  719e  175d9  326b  16c4a  62f7  7bc9  2760  cd1e  129fc  15899  f7c3  17082  17add  1efa4  6993  18388  17964  1c6e3  e156  152b4  10bae  11973  7a51  aba9  91bd  dde1  c85f  1dfff  10094  1bed3'.split('  '))]
  });
  b.push(t[t.length-1]);

  t.push({
    label: 'custom n=96,k=4',
    n: 96,
    k: 4,
    seed: bufferSeedNx32(1, 16),
    nonce: 6,
    inputs: [arrayFromHexArray('8d07  2e520  279f8  cc9eb  45f08  87df0  9253c  d8fd2  18154  428b7  188d6  ecd9e  46c3b  7c489  5d41d  e5e5d'.split('  '))]
  });

  t.push({
    label: 'custom n=128,k=7',
    n: 128,
    k: 7,
    seed: bufferSeedNx32(1, 16),
    nonce: 595,
    inputs: [arrayFromHexArray('458  12f4b  fd85  1aa43  633d  1299f  a173  1928f  11ca  1ca17  1541c  1d430  4283  139b2  1bf1c  1c528  477a  11027  4d81  1e39d  5a3a  1dc98  706e  dfea  8a8f  1ea95  d251  192f2  e869  1fb07  19393  1af44  14f2  19ca2  17b80  1845c  3b22  1db24  c37d  cad2  25a1  b739  ae17  125b9  b046  147da  b12a  1af43  8589  1f2ba  1543e  1ba68  a220  f9b9  dbf1  100c8  b491  168aa  15909  16ad0  1107b  1e5bd  19a84  1b2b9  46e  1d491  7c3b  186df  33e2  19415  4521  da80  acc  35ea  5db7  c1da  2527  5b36  d179  d397  1641  19722  1596b  18225  1ff4  1de18  966e  16023  18f8  1fa32  12152  19fa4  26d6  19a9b  1a064  1c656  2528  5949  126ae  1ff04  7fed  fcba  f3ed  153e5  4d01  19ed9  dae6  1d4c1  b59f  14c93  c5ce  1514d  48be  8b47  6850  7084  b2f0  1cb5f  cb20  1714e  6bc2  1570f  cb8e  17e6d  e02d  1085f  1a4a8  1dd92'.split('  '))]
  });
  b.push(t[t.length-1]);

  t.push({
    label: 'custom n=90,k=4',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(1, 16),
    nonce: 2,
    inputs: [arrayFromHexArray('1a10  38d73  665ff  76007  205b3  5d820  3df0f  6f58f  2c655  3cc14  3c13f  727b1  46230  4fbb5  75cbc  7f37a'.split('  '))]
  });
  b.push(t[t.length-1]);

  t.push({
    label: 'custom n=90,k=5',
    n: 90,
    k: 5,
    seed: bufferSeedNx32(1, 16),
    nonce: 5,
    inputs: [arrayFromHexArray('2ce  1d7c  a592  eaa0  232c  9b7b  379a  fbd0  2ce2  7e1d  438b  d621  e23f  efa9  eb30  f96d  146e  437f  22fb  2996  5a0f  649b  bdda  f66a  1636  220f  96de  bfd9  367e  f067  5437  eab4'.split('  '))]
  });
  b.push(t[t.length-1]);

  t.push({
    label: 'custom n=90,k=4, not distinct',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(1, 16),
    nonce: 2,
    inputs: [arrayFromHexArray('75cbc  7f37a  46230  4fbb5  3c13f  727b1  2c655  3cc14  665ff  76007  1a10  38d73  3df0f  6f58f  205b3  75cbc'.split('  '))],
    expect: false
  });

  t.push({
    label: 'custom n=90,k=4, bad indices order',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(1, 16),
    nonce: 2,
    inputs: [arrayFromHexArray('75cbc  7f37a  46230  4fbb5  3c13f  727b1  2c655  3cc14  665ff  76007  1a10  38d73  3df0f  6f58f  5d820  205b3'.split('  '))],
    expect: false
  });

  t.push({
    label: 'custom n=90,k=4, changed index',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(1, 16),
    nonce: 2,
    inputs: [arrayFromHexArray('75cbd  7f37a  46230  4fbb5  3c13f  727b1  2c655  3cc14  665ff  76007  1a10  38d73  3df0f  6f58f  5d820  205b3'.split('  '))],
    expect: false
  });

  t.push({
    label: 'custom n=90,k=4, reordered indices',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(1, 16),
    nonce: 2,
    inputs: [arrayFromHexArray('46230  4fbb5  75cbc  7f37a  3c13f  727b1  2c655  3cc14  665ff  76007  1a10  38d73  3df0f  6f58f  205b3  5d820'.split('  '))],
    expect: false
  });

  t.push({
    label: 'custom n=90,k=4, bad seed',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(2, 16),
    nonce: 2,
    inputs: [arrayFromHexArray('75cbc  7f37a  46230  4fbb5  3c13f  727b1  2c655  3cc14  665ff  76007  1a10  38d73  3df0f  6f58f  205b3  5d820'.split('  '))],
    expect: false
  });

  t.push({
    label: 'custom n=90,k=4, bad nonce',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(1, 16),
    nonce: 1,
    inputs: [arrayFromHexArray('75cbc  7f37a  46230  4fbb5  3c13f  727b1  2c655  3cc14  665ff  76007  1a10  38d73  3df0f  6f58f  205b3  5d820'.split('  '))],
    expect: false
  });

  t.push({
    label: 'custom n=90,k=4, bad everything',
    n: 90,
    k: 4,
    seed: bufferSeedNx32(0xabcd, 16),
    nonce: 0xdcba,
    inputs: [arrayFromHexArray('1  2  3  4  5  6  7  8  9  a  b  c  d  e  f  0'.split('  '))],
    expect: false
  });

  t.push({
    label: 'custom n=90,k=5',
    n: 90,
    k: 5,
    seed: bufferSeedData(
      crypto.createHash('sha256').update('hello world', 'utf8').digest(), 64),
    nonce: 4,
    inputs: [[
      1017, 1840, 5344, 64928, 3465, 4792, 10664, 16664,
      3589, 9086, 11664, 42036, 11044, 30541, 20245, 29662,
      1807, 52253, 22130, 30987, 5483, 35597, 40754, 58884,
      2276, 7026, 50298, 59296, 7104, 38859, 23848, 35739,
    ]]
  });

  t.push({
    label: 'custom n=64,k=3',
    n: 64,
    k: 3,
    seed: Buffer.from('nVaX3bLwLzVDbNxaJ9/ILLDei1W2TZ/b8MCEnQEK6E0=', 'base64'),
    nonce: 1,
    inputs: [
      [ 341, 10838, 44873, 129717, 24932, 42303, 100937, 107040 ]
    ]
  });
}

customTests(t);
//zcashVectors.zcashTests(t);
