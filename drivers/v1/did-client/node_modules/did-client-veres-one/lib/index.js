/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
if(require('semver').gte(process.version, '8.0.0')) {
  module.exports = require('./did-client-veres-one');
} else {
  module.exports = require('../dist/node6/lib/did-client-veres-one');
}
