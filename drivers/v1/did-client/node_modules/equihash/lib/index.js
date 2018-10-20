/**
 * Equihash for Node.js.
 * Main entry point.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * MIT License
 * <https://github.com/digitalbazaar/equihash/blob/master/LICENSE>
 */
function engine(name=engine.default) {
  return require('./' + name);
}
engine.default = 'khovratovich';

function _guessEngine(options) {
  // khovratovich specified
  if(options.algorithm === 'khovratovich') {
    return engine('khovratovich');
  }
  // unknown, try default
  return engine();
}

module.exports = {
  engine,
  solve(seed, options, callback) {
    return _guessEngine(options).solve(...arguments);
  },
  verify(seed, proof, callback) {
    return _guessEngine(proof).verify(...arguments);
  },
  verifySync(seed, proof) {
    return _guessEngine(proof).verifySync(...arguments);
  }
};
