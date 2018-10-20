var fs = require('fs');
var tilde = require("./tilde-expansion");
var config = require("./config");
var ProjectPath = require('./project-path').ProjectPath;

exports.usage = "[<progDir>/]<repo>";
exports.shortHelp = "Creates a new project in [<progDir>/]<repo>";
exports.longHelp = "\
Creates a new project in [<progDir>/]<repo>\n\
";

function handle(args) {
	if (args.length !== 1) throw "Wrong arguments (Try: prog help new)";

	var projectPath = new ProjectPath(args[0]);

	projectPath.resolve(function (err, result, generatedPath) {
		if (err) {
			console.log(err);
			process.exit(1);
		}

		if (result.length !== 0) {
			console.log("Project already exists:");
			result.forEach(function (path) {
				console.log(" * " + path.pathKey + "/" + path.repo);
			});
			return;
		}

		console.log("Creating new project at", generatedPath);

		fs.mkdir(generatedPath, function (err) {
			if (err) {
				process.stderr.write("Failed making directory " + generatedPath);
				process.stderr.write(err);
				process.exit(1);
			}

			var targetCwdFile = process.env["PROG_TARGET_CWD_FILE"];
			if (!targetCwdFile) {
				process.stderr.write("ERROR: Cannot change working directory\n");
				process.stderr.write("Try adding the following to your ~/.profile and start a new shell:\n");
				process.stderr.write(". prog.sh\n");
				process.exit(1);
			}

			fs.writeFile(targetCwdFile, generatedPath);
		});
	});
}

function complete(args) {
	return;
}

exports.handle = handle;
exports.complete = complete;
