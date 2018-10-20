/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
/* global should */
'use strict';

const expect = global.chai.expect;

describe('Veres One wrap DIDs', () => {
  const didv1 = require('../../lib');

  it('should wrap a nym-based DID Document in an operation', async () => {
    const {publicDidDocument: didDocument} = await didv1.generate(
      {passphrase: null});

    const operation = didv1.wrap({didDocument});
    expect(operation.type).to.equal('CreateWebLedgerRecord');
    expect(operation.record.id).to.match(/^did\:v1\:test\:nym\:.*/);
    expect(operation.record.authentication[0].publicKey[0].publicKeyPem)
      .to.have.string('-----BEGIN PUBLIC KEY-----');
  }).timeout(30000);

  it('should wrap a uuid-based DID Document in an operation', async () => {
    const {publicDidDocument: didDocument} = await didv1.generate(
      {didType: 'uuid'});
    const operation = didv1.wrap({didDocument});
    expect(operation.type).to.equal('CreateWebLedgerRecord');
    expect(operation.record.id).to.match(/^did\:v1\:test\:uuid\:.*/);
  });

});
