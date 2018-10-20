/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
/* global should */
'use strict';

const expect = global.chai.expect;

describe('Veres One attachInvocationProof', () => {
  const didv1 = require('../../lib');

  // FIXME: determine how to simplify/move this code out of test
  const jsonld = didv1.use('jsonld');
  const documentLoader = jsonld.documentLoader;
  jsonld.documentLoader = async url => {
    if(url in didv1.contexts) {
      return {
        contextUrl: null,
        documentUrl: url,
        document: didv1.contexts[url]
      };
    }
    return documentLoader(url);
  };
  const jsigs = require('jsonld-signatures');
  jsigs.use('jsonld', jsonld);
  didv1.use('jsonld-signatures', jsigs);

  it('should attach an ld-ocap invocation proof to an operation', async () => {
    const {publicDidDocument: didDocument, privateDidDocument} =
      await didv1.generate({passphrase: null});

    let operation = didv1.wrap({didDocument});
    const creator = didDocument.invokeCapability[0].publicKey[0].id;
    const privateKeyPem = privateDidDocument.invokeCapability[0].publicKey[0]
      .privateKey.privateKeyPem;

    operation = await didv1.attachInvocationProof({
      operation,
      capability: didDocument.id,
      capabilityAction: operation.type,
      creator,
      privateKeyPem
    });

    expect(operation.type).to.equal('CreateWebLedgerRecord');
    expect(operation.record.id).to.match(/^did\:v1\:test\:nym\:.*/);
    expect(operation.record.authentication[0].publicKey[0].publicKeyPem)
      .to.have.string('-----BEGIN PUBLIC KEY-----');
    expect(operation.proof).to.exist;
    expect(operation.proof.type).to.equal('RsaSignature2018');
    expect(operation.proof.capabilityAction).to.equal(operation.type);
    expect(operation.proof.proofPurpose).to.equal('invokeCapability');
    expect(operation.proof.creator).to.equal(creator);
    expect(operation.proof.jws).to.exist;
  }).timeout(30000);

});
