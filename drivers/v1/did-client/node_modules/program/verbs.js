var verbs = [
	"clone",
	"complete",
	"help",
	"new",
	"open"
];

var aliases = {
	"go": "open"
};

function has(verb) {
	return aliases.hasOwnProperty(verb) || (verbs.indexOf(verb) !== -1);
}

function req(verb) {
	if (aliases.hasOwnProperty(verb)) verb = aliases[verb];
	if (verbs.indexOf(verb) === -1) throw "Verb not found: " + verb;
	return require("./" + verb);
}

exports.verbs = verbs;
exports.has = has;
exports.req = req;
