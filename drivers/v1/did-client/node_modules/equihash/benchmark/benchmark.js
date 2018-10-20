/**
 * Equihash for Node.js.
 * benchmark.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * MIT License
 * <https://github.com/digitalbazaar/equihash/blob/master/LICENSE>
 */
const Benchmark = require('benchmark');
const assert = require('assert');
const async = require('async');
const crypto = require('crypto');
const equihash = require('..');
const fs = require('fs');
const path = require('path');
// tests and helpers
const vectors = require('../test/test-vectors');

const suite = new Benchmark.Suite;

function _seed(s='') {
  return crypto.createHash('sha256').update('test:' + s, 'utf8').digest();
}

let seed;
let i;

function _solveRandomInc({n, k, i=0, seedSize=32, minSamples}) {
  let options = {
    _data: {n, k, tag: `inc-${i}`},
    name: `solve n=${n},k=${k},seeds=rand-inc`,
    defer: true,
    onStart: function() {
      seed = _seed(i);
    },
    fn: function(deferred) {
      const options = {
        n: n,
        k: k
      };
      equihash.solve(seed, options, function(err, proof) {
        assert.ifError(err);
        deferred.resolve();
      });
    },
    onCycle: event => {
      const samples = event.target.stats.sample;
      progress({samples, info: event.target._data});
      i++;
      seed = _seed(i);
    }
  };
  if(minSamples) {
    options.minSamples = minSamples;
  }
  return options;
}

function _solveRandom({n, k, seedSize=32, minSamples}) {
  let options = {
    _data: {n, k, tag: `rand`},
    name: `solve n=${n},k=${k},seeds=rand`,
    defer: true,
    onStart: function() {
      seed = crypto.randomBytes(seedSize);
    },
    fn: function(deferred) {
      const options = {
        n: n,
        k: k
      };
      equihash.solve(seed, options, function(err, proof) {
        assert.ifError(err);
        deferred.resolve();
      });
    },
    onCycle: event => {
      const samples = event.target.stats.sample;
      progress({samples, info: event.target._data});
      seed = crypto.randomBytes(seedSize);
    }
  };
  if(minSamples) {
    options.minSamples = minSamples;
  }
  return options;
}

// test deferred test overhead
/*
suite
  .add({
    name: 'noop not deferred',
    defer: false,
    fn: () => {}
  })
  .add({
    name: 'noop deferred',
    defer: true,
    fn: deferred => {
      deferred.resolve();
    }
  });
*/

// test verify
vectors.benchmarks.forEach(test => {
  test.inputs.forEach(inputs => {
    suite.add({
      name: 'verify ' + test.label + ' (async)',
      defer: true,
      fn: deferred => {
        const proof = {
          n: test.n,
          k: test.k,
          nonce: test.nonce,
          solution: inputs
        };
        equihash.verify(new Uint8Array(test.seed), proof, (err, verified) => {
          assert.ifError(err);
          assert(verified);
          deferred.resolve();
        });
      }
    });
    suite.add({
      name: 'verify ' + test.label + ' (sync)',
      defer: true,
      fn: deferred => {
        const proof = {
          n: test.n,
          k: test.k,
          nonce: test.nonce,
          solution: inputs
        };
        const verified = equihash.verifySync(new Uint8Array(test.seed), proof);
        assert(verified);
        deferred.resolve();
      }
    });
  });
});

// test solve
const minSamples = 10;
suite
  .add(_solveRandomInc({n: 90, k: 5, minSamples}))
  .add(_solveRandom({n: 90, k: 5, minSamples}))
  .add(_solveRandomInc({n: 96, k: 5, minSamples}))
  .add(_solveRandom({n: 96, k: 5, minSamples}))
  .add(_solveRandom({n: 64, k: 3, minSamples}))
  .add(_solveRandom({n: 128, k: 7, minSamples}))
  //.add(_solveRandom({n: 144, k: 5}))
  //.add(_solveRandom({n: 200, k: 9}))
  ;

suite
  .on('start', () => {
    console.log('Benchmarking...');
  })
  .on('cycle', event => {
    console.log(String(event.target));
    const s = event.target.stats;
    const d = event.target._data;
    console.log(`  min:${Math.min(...s.sample)} max:${Math.max(...s.sample)}`);
    console.log(`  deviation:${s.deviation} mean:${s.mean}`);
    console.log(`  moe:${s.moe} rme:${s.rme}% sem:${s.sem} var:${s.variance}`);
    progress({samples: s.sample, info: d, last: true});
  })
  .on('complete', () => {
    console.log('Done.');
  })
  .run({async: true});

function progress({samples, info, last=false}) {
  if(process.env.DATADIR) {
    if(last || (samples.length % 20 === 0)) {
      savedata(...arguments);
    }
  }
}
const filePrefix = (new Date).toISOString().replace(/[^0-9]/g,'');
const uniqf = new Map();
function savedata({samples, info, last=false}) {
  const idxname =
    `${filePrefix}-${info.n}-${info.k}-${info.tag}-${samples.length}`;
  const cur = uniqf.get(idxname) || 0;
  const next = cur + 1;
  uniqf.set(idxname, next);
  const lasttag = last ? '-last' : '';
  const filename =
    path.join(process.env.DATADIR, `${idxname}-${next}${lasttag}.csv`);
  if(fs.existsSync(filename)) {
    throw new Error('Conflicting data filename: ' + filename);
  }
  fs.writeFileSync(filename, samples.join('\n') + '\n');
  console.log('Wrote samples:', filename);
}
