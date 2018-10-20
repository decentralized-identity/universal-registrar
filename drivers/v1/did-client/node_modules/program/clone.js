var fs = require('fs');
var path = require('path');
var spawn = require('child_process').spawn;
var tilde = require('./tilde-expansion');
var config = require("./config");

exports.usage = "[<user>/]<repos>";
exports.shortHelp = "Clone bitbucket repo into local prog dir";
exports.longHelp = "\
Will clone the bitbucket repository named <repos> owned by <user>. If <user>\n\
is not specified, it defaults to the current user configured.\n\
";

function handle(args) {
	if (args.length !== 1) throw "Wrong arguments (Try: prog help clone)";

	config.getConfig(function (config) {
		var arg = args[0].split('/');
		var user, repo;
		if (arg.length === 1) {
			user = config.get("bitbucket", "user");
			repo = arg[0];
		} else if (arg.length === 2) {
			user = arg[0];
			repo = arg[1];
		} else {
			throw "Too many '/'-es in repo spec";
		}

		var progDirConfig = config.get("paths", "default");

		tilde(progDirConfig, function (progDir) {
			var url = "ssh://hg@bitbucket.org/" + user + "/" + repo;

			var hg = spawn('hg', ['clone', url], { cwd: progDir });

			hg.stdout.pipe(process.stdout);
			hg.stderr.pipe(process.stderr);

			hg.on('exit', function (code) {
				console.log('hg process exited with code ' + code);
			});

			var targetCwdFile = process.env["PROG_TARGET_CWD_FILE"];
			if (targetCwdFile) fs.writeFile(targetCwdFile, path.join(progDir, repo));
		});
	});
}

exports.handle = handle;
