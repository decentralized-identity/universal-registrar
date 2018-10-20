# Equihash Proof of Work for Node

[![Build Status](https://travis-ci.org/digitalbazaar/equihash.png?branch=master)](https://travis-ci.org/digitalbazaar/equihash)
[![Build Status](https://ci.digitalbazaar.com/buildStatus/icon?job=equihash)](https://ci.digitalbazaar.com/job/equihash)

Equihash is a tunable asymmetric proof of work algorithm where it is difficult
to generate a proof, but easy to verify one. The algorithm makes it difficult
to build custom hardware to generate the proof by ensuring forced CPU and
memory trade offs. The algorithm is useful for cryptocurrency mining as
well as building solutions that require a proof of work capability.

## Installation

```
npm install equihash
```

## The Equihash API

### Solve

```javascript
solve(seed, options, callback(err, proof))
```

Solve for a single solution for a given seed.

`seed`: `Buffer` of bytes to use as a seed

`options`:
- `n`: Equihash `n` parameter
- `k`: Equihash `k` parameter
- `nonceLength`: number of bytes of nonce data to find (optional)
- `nonce`: initial value of nonce (optional)
- `maxNonces`: max number of nonces to check (optional)
- `algorithm`: `String` id of Equihash algorithm to use (optional)
- `algorithmParameters`: `Object` with algorithm specific parameters (optional)

`err`:
- `Error` if one occurred, else null.

`proof`:
- `n`: Equihash `n` parameter
- `k`: Equihash `k` parameter
- `nonce`: `Buffer` of nonce bytes
- `solution`: `Array` of unsigned integers
- `algorithm`: `String` id of the specific Equihash algorithm
- `algorithmParameters`: `Object` with algorithm specific parameters (optional)

### Verify

```javascript
verify(seed, proof, callback(err, verified))
```

Verify a proof for a given seed.

`seed`: `Buffer` of bytes to use as a seed

`proof`: same as output from `solve()`

`err`:
- `Error` if one occurred, else null. Note some incorrect inputs produce an
  error while others may only return a false verified value.

`verified`:
- true if proof is verified, else false.

### Engine

```javascript
engine(name)
```

Return an engine with a specific name. Required to access special engine APIs.

`name`: `String` id of engine to retrieve

## Engines

Multiple Equihash may be available. The primary `equihash` API can be used with
the above basic options. The engine that is used will be returned in the
`algorithm` field. Engines may have special APIs and/or support special
`algorithmParameters` in addition to the primary API.

### khovratovich

An engine based on the original `khovratovich` reference code.

- `algorithm` id: `khovratovich`
- Uses a modified implementation of the reference `khovratovich` code
- Handles seed value of any length
- `solve()` can start at any nonce of at least 4 bytes
- `solve()` currently only mutates the first 4 bytes of the nonce
- `verify()` can verify a nonce of any length
- Uses BLAKE2b hashing.
- Solution values hashed as 32 bit little-endian unsigned integers
- Has certain `n` and `k` limitations
- Note: Not strictly compatible with zcash implementation due to implementation
  issues.

API additions:
- `BLAKE2B_PERSONALIZATION_BYTES`: The number of supported BLAKE2b
  personalization bytes (16).

`algorithmParameters`:
- `personalization`: `Buffer` of BLAKE2b "personalization" bytes (optional)

## Usage Example

```javascript
const equihash = require('equihash');

// seed for equihash
const seed = crypto.createHash('sha256').update('test1234', 'utf8').digest();
const options = {
  n: 90,
  k: 5
}

equihash.solve(seed, options, (err, proof) => {
  if(err) {
    return console.log('Failed to generate proof:', err);
  }

  console.log('Equihash proof:', proof)

  equihash.verify(seed, proof, (err, verified) => {
    if(err) {
      return console.log('Failed to verify proof:', err);
    }

    console.log('Valid proof? ', verified);
  });
});
```

Use a named Equihash engine:
```javascript
const equihash = require('equihash').engine('khovratovich');
// ...
```

By default the 'khovratovich' engine is used. To set a different default
engine:
```javascript
const equihash = require('equihash');
equihash.engine.default = '...';
// ...
```

Use specific options:
```javascript
const equihash = require('equihash').engine('khovratovich');

// seed for equihash
const seed = crypto.createHash('sha256').update('test1234', 'utf8').digest();

// Create zero filled buffer and add our shorter string
const personalization = Buffer.alloc(equihash.BLAKE2B_PERSONALIZATION_BYTES, 0);
Buffer.from('MyProject').copy(personalization);

const options = {
  n: 90,
  k: 5,
  nonceLength: 32,
  algorithm: 'khovratovich',
  algorithmParameters: {
    personalization
  }
}

equihash.solve(seed, options, (err, proof) => {
  ...
});
```

## Test Suite

```
npm install
npm test
```

## Benchmark

```
npm run benchmark
```

## References

### Papers

- https://www.internetsociety.org/sites/default/files/blogs-media/equihash-asymmetric-proof-of-work-based-generalized-birthday-problem.pdf

### Implementations

- https://github.com/khovratovich/equihash
- https://github.com/xenoncat/equihash-xenon
- https://github.com/tromp/equihash
- https://github.com/nicehash/nheqminer
