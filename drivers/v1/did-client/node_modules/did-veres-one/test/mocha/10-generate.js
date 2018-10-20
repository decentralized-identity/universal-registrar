/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
/* global should */
'use strict';

const expect = global.chai.expect;

describe('Veres One generate DIDs', () => {
  const didv1 = require('../../lib');

  it('should generate protected nym-based DID Document', async () => {
    const nymOptions = {
      passphrase: 'foobar'
    };
    const didDocument = await didv1.generate(nymOptions);

    expect(didDocument.publicDidDocument.id).to.match(
      /^did\:v1\:test\:nym\:.*/);
    expect(
      didDocument.publicDidDocument.authentication[0].publicKey[0].publicKeyPem)
      .to.have.string('-----BEGIN PUBLIC KEY-----');
    expect(
      didDocument.privateDidDocument.authentication[0].publicKey[0]
        .privateKey.privateKeyPem)
      .to.have.string('-----BEGIN ENCRYPTED PRIVATE KEY-----');
  }).timeout(30000);

  it('should generate unprotected nym-based DID Document', async () => {
    const nymOptions = {
      passphrase: null
    };
    const didDocument = await didv1.generate(nymOptions);

    expect(didDocument.publicDidDocument.id).to.match(
      /^did\:v1\:test\:nym\:.*/);
    expect(
      didDocument.publicDidDocument.authentication[0].publicKey[0].publicKeyPem)
      .to.have.string('-----BEGIN PUBLIC KEY-----');
    expect(
      didDocument.privateDidDocument.authentication[0].publicKey[0]
        .privateKey.privateKeyPem)
      .to.have.string('-----BEGIN RSA PRIVATE KEY-----');
  }).timeout(30000);

  it('should generate uuid-based DID Document', async () => {
    const uuidOptions = {
      didType: 'uuid'
    };
    const didDocument = await didv1.generate(uuidOptions);

    expect(didDocument.publicDidDocument.id).to.match(
      /^did\:v1\:test\:uuid\:.*/);
  });

});
