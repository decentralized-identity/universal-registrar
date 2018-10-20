/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
/* global should */
'use strict';

const expect = global.chai.expect;

describe('Veres One attachGrantProof', () => {
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

  it('should attach an ld-ocap grant proof to an operation', async () => {
    let {publicDidDocument: didDocument, privateDidDocument} =
      await didv1.generate({passphrase: null});

    const creator = didDocument.grantCapability[0].publicKey[0].id;
    const privateKeyPem = privateDidDocument.grantCapability[0].publicKey[0]
      .privateKey.privateKeyPem;

    didDocument = await didv1.attachGrantProof({
      didDocument,
      creator,
      privateKeyPem
    });

    expect(didDocument.proof).to.exist;
    expect(didDocument.proof.type).to.equal('RsaSignature2018');
    expect(didDocument.proof.proofPurpose).to.equal('grantCapability');
    expect(didDocument.proof.creator).to.equal(creator);
    expect(didDocument.proof.jws).to.exist;
  }).timeout(30000);

});
