var verbs = require('./verbs');

exports.usage = "[verb]";
exports.shortHelp = "Display help about verbs, or this text";
exports.longHelp = "\
Without verb-argument: Displays global help\n\
\n\
With verb-argument: Displays help about verb\n\
";

function generateGlobalHelpString() {
	var usageWidth = 0;

	var lines = verbs.verbs.map(function (verbName) {
		var verb = verbs.req(verbName);
		var line = {
			"usage": verbName + " " + verb.usage,
			"short": verb.shortHelp
		};
		usageWidth = Math.max(usageWidth, line.usage.length);
		return line;
	});
	lines.sort(function (a, b) { return a.usage < b.usage ? -1 : 1; });

	function spaces(n) { return new Array(n+1).join(" "); }

	var formattedLines = lines.map(function (line) {
		return " " + line.usage + spaces(usageWidth - line.usage.length) + "  " + line.short;
	});

	return "Prog\n\nList of verbs:\n" + formattedLines.join('\n');
}

function generateVerbHelpString(verbName) {
	if (!verbs.has(verbName)) {
		return "Unknown verb: " + verbName + "\nTry: prog help";
	}

	var verb = verbs.req(verbName);

	return "Prog: " + verbName + "\n\nUsage: prog " + verb.usage + "\n\n" + verb.longHelp;
}

function handle(args) {
	if (args.length === 0) console.log(generateGlobalHelpString());
	else if (args.length === 1) console.log(generateVerbHelpString(args[0]));
	else console.log(generateVerbHelpString("help"));
}

function complete(args) {
	if (args.length === 1) {
		console.log(verbs.verbs.filter(function (candidate) {return candidate.indexOf(args[0]) === 0}).join('\n'));
	}
}

exports.handle = handle;
exports.complete = complete;
