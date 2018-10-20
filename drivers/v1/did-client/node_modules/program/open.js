var fs = require('fs');
var path = require('path');
var tilde = require("./tilde-expansion");
var config = require('./config');
var ProjectPath = require('./project-path').ProjectPath;

exports.usage = "<repo>";
exports.shortHelp = "Changes working directory to <progDir>/<repo>";
exports.longHelp = "\
Aliases: open, go\n\
\n\
Changes working directory to <progDir>/<repo>\n\
";

function handle(args) {
	if (args.length !== 1) throw "Wrong arguments (Try: prog help open)";
	var projectPath = new ProjectPath(args[0]);

	var targetCwdFile = process.env["PROG_TARGET_CWD_FILE"];
	if (!targetCwdFile) {
		process.stderr.write("ERROR: Cannot change working directory\n");
		process.stderr.write("Try adding the following to your ~/.profile and start a new shell:\n");
		process.stderr.write(". prog.sh\n");
		process.exit(1);
	}

	projectPath.resolve(function (err, resolvedPaths) {
		if (err) {
			process.stderr.write(err + "\n");
			process.exit(1);
		}

		if (resolvedPaths.length === 0) {
			process.stderr.write("Not found\n");
			process.exit(1);
		} else if (resolvedPaths.length > 1) {
			process.stderr.write("Ambiguous project. Candidates:\n");
			resolvedPaths.forEach(function (candidate) {
				process.stderr.write(" * " + candidate.pathKey + "/" + candidate.repo + "\n");
			});
			process.exit(1);
		}

		fs.writeFileSync(targetCwdFile, resolvedPaths[0].fullPath);
	});
}

function complete(args) {
	if (args.length !== 1) return;

	var candidate = new ProjectPath(args[0]);

	function startsWith(prefix) {
		return function (x) {
			return x.indexOf(prefix) === 0;
		};
	}

	// TODO: Refactor to be asynchronous. Stating lots of files sequentially
	// is a typical bottle neck.
	function is_dir(dir, filename) {
		return fs.statSync(path.join(dir, filename)).isDirectory();
	}

	config.getConfig(function (config) {
		if (candidate.pathKey === null) {
			var pathKeys = config.keys("paths");
			pathKeys.filter(startsWith(candidate.repo)).forEach(function (x) {
				console.log(x + "/");
			});

			pathKeys.forEach(function (pathKey) {
				tilde(config.get("paths", pathKey), function (progDir) {
					fs.readdir(progDir, function (err, files) {
						if (err) return;
						console.log(files.filter(startsWith(candidate.repo)).filter(is_dir.bind(null, progDir)).join('\n'));
					});
				});
			});
		} else {
			if (!config.has("paths", candidate.pathKey)) return;

			tilde(config.get("paths", candidate.pathKey), function (progDir) {
				fs.readdir(progDir, function (err, files) {
					if (err) return;
					files.filter(startsWith(candidate.repo)).filter(is_dir.bind(null, progDir)).forEach(function (x) {
						console.log(candidate.pathKey + "/" + x);
					});
				});
			});
		}
	});
}

exports.handle = handle;
exports.complete = complete;
