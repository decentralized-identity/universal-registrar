exports.usage = "[incomplete argument list]";
exports.shortHelp = "Supply bash completion";
exports.longHelp = "\
Reads the supplied incomplete argument list and proposes completions in a\n\
format compatible with bash completion.\n\
\n\
To enable bash completion add the following to your ~/.profile and start a new shell:\n\
\n\
. prog.sh\n\
";

function completeCore(args) {
	if (args.length < 2) {
		console.log("prog");
		return;
	}

	var verbs = require("./verbs");
	var verbName = args[1];
	if (args.length === 2) {
		console.log(verbs.verbs.filter(function (candidate) {return candidate.indexOf(verbName) === 0}).join('\n'));
	} else {
		if (verbs.has(verbName)) {
			var verb = verbs.req(verbName);
			if (verb.complete) verb.complete(args.slice(2));
		}
	}
}

exports.handle = function (args) {
	completeCore(args.map(function (arg) { return arg.slice(1); }));
};

exports.complete = function (args) {
	completeCore(args);
};
