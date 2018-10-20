/*!
 * Copyright (c) 2017-2018 Digital Bazaar, Inc. All rights reserved.
 */
'use strict';

const suites = {
  EquihashProof2018: require('./EquihashProof2018')
};

module.exports = {
  suites,
  install: jsigs => {
    for(const suite in suites) {
      jsigs.suites[suite] = suites[suite];
    }
  }
};
