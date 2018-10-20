/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
'use strict';

process.env['NODE_PATH'] = '../node_modules';
const bedrock = require('bedrock');
require('../lib');

require('bedrock-test');
bedrock.start();
