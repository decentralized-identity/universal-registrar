/*
 * Copyright (c) 2018 Veres One Project. All rights reserved.
 */
const config = require('bedrock').config;
const path = require('path');

// mocha tests
config.mocha.tests.push(path.join(__dirname, 'mocha'));

// server info
//config.server.port = 23443;
//config.server.httpPort = 22080;
//config.server.domain = 'bedrock.localhost';
