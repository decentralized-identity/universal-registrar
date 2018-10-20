/*!
 * Copyright (c) 2017-2018 Digital Bazaar, Inc. All rights reserved.
 */
'use strict';

const equihashSigs = require('equihash-signature');
const jsigs = require('jsonld-signatures');
equihashSigs.install(jsigs);

const testDoc = {
  "@context": "https://w3id.org/security/v2",
  "id": "foo:something",
  "proof": {
    "type": "EquihashProof2018",
    "created": "2018-03-02T18:32:27Z",
    "equihashParameterK": 3,
    "equihashParameterN": 64,
    "nonce": "AgAAAA==",
    "proofValue": "AAAyywABiRYAAc2aAAH73AAAW7AAAdvuAADG7wABOu0="
  }
};

describe('Equihash Signature API', () => {
  it('should sign', done => {
    jsigs.sign({
      '@context': 'https://w3id.org/security/v2',
      id: 'foo:something'
    }, {
      algorithm: 'EquihashProof2018',
      parameters: {
        n: 64,
        k: 3
      }
    }, (err, result) => {
      // FIXME: is this assertion accurate?
      result.proof.proofValue.should.not.equal('');
      done();
    });
  });

  it('should verify', done => {
    jsigs.verify(testDoc, (err, result) => {
      result.verified.should.equal(true);
      done();
    });
  });

  it('should fail to verify', done => {
    const badTestDoc = JSON.parse(JSON.stringify(testDoc));
    badTestDoc.proof.proofValue =
      'BAAyywABiRYAAc2aAAH73AAAW7AAAdvuAADG7wABOu0=';
    jsigs.verify(badTestDoc, (err, result) => {
      result.verified.should.equal(false);
      done();
    });
  });
});
