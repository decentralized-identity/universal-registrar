#!/usr/bin/env node

var verbs = require("./verbs");

var args = process.argv.slice(2);

if (args.length < 1) {
	console.log("Usage: prog <verb> [...]");
	console.log("Try: prog help");
	process.exit(1);
}

var verb = args[0];

try {
	verbs.req(verb).handle(args.slice(1));
}
catch (e) {
	console.log(e);
	process.exit(1);
}
