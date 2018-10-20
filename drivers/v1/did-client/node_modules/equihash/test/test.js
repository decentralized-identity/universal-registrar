/**
 * Equihash for Node.js.
 * tests.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * MIT License
 * <https://github.com/digitalbazaar/equihash/blob/master/LICENSE>
 */
const assert = require('assert');
const async = require('async');
const crypto = require('crypto');
const equihash = require('..');

const vectors = require('./test-vectors');

function _seed(s='') {
  return crypto.createHash('sha256').update('test' + s, 'utf8').digest();
};

describe('Equihash', function() {
  it('should fail solve with bad algorithm', function(done) {
    const options = {
      n: 90,
      k: 0,
      algorithm: 'BOGUS'
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert(err);
      done();
    });
  });
  it('should fail solve with k<1', function(done) {
    const options = {
      n: 90,
      k: 0
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert(err);
      done();
    });
  });
  it('should fail solve with k>7', function(done) {
    const options = {
      n: 90,
      k: 8
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert(err);
      done();
    });
  });
  it('should fail solve with n/(k+1) > 32', function(done) {
    const options = {
      n: 257,
      k: 7
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert(err);
      done();
    });
  });
  it('should fail verify with k<1', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      proof.k = 0;
      equihash.verify(seed, proof, (err, verified) => {
        assert(err);
        done();
      });
    });
  });
  it('should fail verify with k>7', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      proof.k = 8;
      equihash.verify(seed, proof, (err, verified) => {
        assert(err);
        done();
      });
    });
  });
  it('should fail verify with n/(k+1) > 32', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      proof.n = 257;
      proof.k = 7;
      equihash.verify(seed, proof, (err, verified) => {
        assert(err);
        done();
      });
    });
  });
  it('should fail solve with bad max nonces', function(done) {
    const options = {
      n: 90,
      k: 5,
      nonce: 0xffffffff,
      maxNonces: 2
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert(err);
      done();
    });
  });
  it('should get error if no solutions found', function(done) {
    // pick known params that have no solution
    const options = {
      n: 90,
      k: 5,
      nonce: 3,
      maxNonces: 1
    };
    const seed = Buffer.from('');

    equihash.solve(seed, options, (err, proof) => {
      assert(err);
      done();
    });
  });
  it('should check proof', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    // raw output from khovratovich cli tool
    const sol = '20e9  1396c  719e  175d9  326b  16c4a  62f7  7bc9  2760  cd1e  129fc  15899  f7c3  17082  17add  1efa4  6993  18388  17964  1c6e3  e156  152b4  10bae  11973  7a51  aba9  91bd  dde1  c85f  1dfff  10094  1bed3';
    const solution = sol.split('  ').map(v => parseInt(v, 16));
    const proof = {
      n: 96,
      k: 5,
      nonce: 2,
      solution: solution
    };
    const seedab = new ArrayBuffer(16 * 4);
    const seed32 = new Uint32Array(seedab);
    seed32.fill(1);
    const seed = new Uint8Array(seedab);

    eh.verify(seed, proof, (err, verified) => {
      assert.ifError(err);
      assert(verified);
      done();
    });
  });

  const totalVectors = vectors.tests.length;
  const totalInputs = vectors.tests.reduce(
    (sum, v) => sum + v.inputs.length, 0);
  const testVectorsMsg =
    `should check ${totalVectors} vectors with ${totalInputs} inputs`;
  it(testVectorsMsg, function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    let i = 0;
    async.eachSeries(vectors.tests, (test, callback) => {
      i++;
      async.eachSeries(test.inputs, (inputs, callback) => {
        const proof = {
          n: test.n,
          k: test.k,
          nonce: test.nonce,
          solution: inputs
        };
        eh.verify(new Uint8Array(test.seed), proof, (err, verified) => {
          assert.ifError(err);
          const expect = 'expect' in test ? test.expect : true;
          assert.equal(verified, expect, test.label || `#${i}`);
          callback();
        });
      }, err => callback(err));
    }, err => done(err));
  });
  it('should generate a n=90,k=5 proof', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    const options = {
      n: 90,
      k: 5
    };
    const seed = Buffer.alloc(64, 0);
    const input =
      crypto.createHash('sha256').update('hello world', 'utf8').digest();
    input.copy(seed);

    eh.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.algorithm, 'khovratovich');
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      const u32sol = new Uint32Array(proof.solution.length);
      const dv = new DataView(u32sol.buffer);
      for(let i = 0; i < proof.solution.length; ++i) {
        dv.setUint32(i * 4, proof.solution[i]);
      }
      const b64proof = Buffer.from(u32sol.buffer).toString('base64');
      assert.equal(b64proof, 'AAAD+QAABzAAABTgAAD9oAAADYkAABK4AAApqAAAQRgAAA4FAAAjfgAALZAAAKQ0AAArJAAAd00AAE8VAABz3gAABw8AAMwdAABWcgAAeQsAABVrAACLDQAAnzIAAOYEAAAI5AAAG3IAAMR6AADnoAAAG8AAAJfLAABdKAAAi5s=');
      done();
    });
  });
  /* too slow
  it('should generate a n=128,k=7 proof', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    const options = {
      n: 128,
      k: 7
    };
    const seed =
      crypto.createHash('sha256').update('hello world', 'utf8').digest();

    eh.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      const b64proof = Buffer.from(proof.solution).toString('base64');
      assert.equal(b64proof, 'AAAD+QAABzAAABTgAAD9oAAAKagAAEEYAAANiQAAErgAACskAAB3TQAATxUAAHPeAAAtkAAApDQAAA4FAAAjfgAABw8AAMwdAABWcgAAeQsAAJ8yAADmBAAAFWsAAIsNAABdKAAAi5sAABvAAACXywAACOQAABtyAADEegAA56A=');
      done();
    });
  });
  */
  /* old behavior, now uses all seed data
  it.skip('should truncate seeds that are too large', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = crypto.randomBytes(128);

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      done();
    });
  });
  */
  /* old behavior, now uses all seed data
  it.skip('should allow seeds that are smaller than 512 bits', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = crypto.randomBytes(1);

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      done();
    });
  });
  */
  it('should verify a valid proof n=90,k=5', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should sync verify a valid proof n=90,k=5', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      const verified = equihash.verifySync(seed, proof);
      assert(verified);
      done();
    });
  });
  it('should verify a valid proof n=64,k=3', function(done) {
    const options = {
      n: 64,
      k: 3
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should verify 10 proofs', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    async.timesSeries(10, (n, callback) => {
      const seed = _seed(n);

      equihash.solve(_seed(n), options, (err, proof) => {
        assert.ifError(err);
        assert.equal(proof.n, options.n);
        assert.equal(proof.k, options.k);
        assert(proof.nonce);
        assert(proof.solution);
        equihash.verify(seed, proof, (err, verified) => {
          assert.ifError(err);
          assert(verified);
          callback();
        });
      });
    }, (err) => done(err));
  });
  it('should verify a valid proof with zero byte seed', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = Buffer.alloc(0, 0);

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should verify a valid proof with one byte seed', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = Buffer.alloc(1, 0);

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should verify a valid proof with 1024 byte seed', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = Buffer.alloc(1024, 0);

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should verify a valid proof with long nonce', function(done) {
    const options = {
      n: 90,
      k: 5,
      nonceLength: 20
    };
    const d = _seed();
    const seed = Buffer.alloc(64, 0);
    d.copy(seed);

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert.equal(proof.nonce.length, options.nonceLength);
      assert(proof.solution);
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should verify a valid proof with start nonce', function(done) {
    const options = {
      n: 90,
      k: 5,
      nonce: 0xffff0000,
      maxNonces: 100,
      nonceLength: 20
    };
    const d = _seed();
    const seed = Buffer.alloc(64, 0);
    d.copy(seed);

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert.equal(proof.nonce.length, options.nonceLength);
      assert(proof.solution);
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should get BLAKE2B_PERSONAL_BYTES', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    assert('BLAKE2B_PERSONAL_BYTES' in eh);
    assert(Number.isInteger(eh.BLAKE2B_PERSONAL_BYTES));
    done();
  });
  it('should verify a valid proof with personal bytes', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    const personal = Buffer.alloc(eh.BLAKE2B_PERSONAL_BYTES, 1);
    const options = {
      n: 90,
      k: 5,
      personal
    };
    const seed = _seed();

    eh.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.personal.equals(options.personal));
      assert(proof.nonce);
      assert(proof.solution);
      eh.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(verified);
        done();
      });
    });
  });
  it('should fail to verify with bad personal bytes', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    const personal = Buffer.alloc(eh.BLAKE2B_PERSONAL_BYTES, 1);
    const options = {
      n: 90,
      k: 5,
      personal
    };
    const seed = _seed();

    eh.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.personal.equals(options.personal));
      assert(proof.nonce);
      assert(proof.solution);
      proof.personal = Buffer.alloc(eh.BLAKE2B_PERSONAL_BYTES, 2);
      eh.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(!verified);
        done();
      });
    });
  });
  it('should fail to solve with too many personal bytes', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    const personal = Buffer.alloc(eh.BLAKE2B_PERSONAL_BYTES + 1, 0);
    const options = {
      n: 90,
      k: 5,
      personal
    };
    const seed = _seed();

    eh.solve(seed, options, (err, proof) => {
      assert(err);
      done();
    });
  });
  it('should fail to verify with too many personal bytes', function(done) {
    const eh = equihash.engine('khovratovich');
    assert(eh);
    const personal = Buffer.alloc(eh.BLAKE2B_PERSONAL_BYTES, 1);
    const options = {
      n: 90,
      k: 5,
      personal
    };
    const seed = _seed();

    eh.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.personal.equals(options.personal));
      assert(proof.nonce);
      assert(proof.solution);
      proof.personal = Buffer.alloc(eh.BLAKE2B_PERSONAL_BYTES + 1, 1);
      eh.verify(seed, proof, (err, verified) => {
        assert(err);
        done();
      });
    });
  });
  it('should fail to verify a proof with input < k', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      proof.solution = [1, 2, 3, 4];
      equihash.verify(seed, proof, (err, verified) => {
        assert(err);
        done();
      });
    });
  });
  it('should fail to verify with an alternate seed', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      const altSeed = crypto.createHash('sha256')
        .update('goodbye cruel world', 'utf8').digest();
      equihash.verify(altSeed, proof, (err, verified) => {
        assert.ifError(err);
        assert(!verified);
        done();
      });
    });
  });
  it('should fail to verify an invalid proof', function(done) {
    const options = {
      n: 90,
      k: 5
    };
    const seed = _seed();

    equihash.solve(seed, options, (err, proof) => {
      assert.ifError(err);
      assert.equal(proof.n, options.n);
      assert.equal(proof.k, options.k);
      assert(proof.nonce);
      assert(proof.solution);
      // corrupt first indices
      proof.solution[0] = 0xde;
      proof.solution[1] = 0xad;
      proof.solution[2] = 0xbe;
      proof.solution[3] = 0xef;
      equihash.verify(seed, proof, (err, verified) => {
        assert.ifError(err);
        assert(!verified);
        done();
      });
    });
  });
});
