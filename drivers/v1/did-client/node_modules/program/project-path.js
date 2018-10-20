var async = require("async");
var fs = require('fs');
var path = require('path');
var tilde = require("./tilde-expansion");
var config = require("./config");

function ProjectPath(pathSpec) {
	var splitSpec = pathSpec.split('/');
	if (splitSpec.length === 1) {
		this.pathKey = null;
		this.repo = splitSpec[0];
	} else if (splitSpec.length === 2) {
		this.pathKey = splitSpec[0];
		this.repo = splitSpec[1];
	} else {
		throw "Too many /-es in path specification";
	}
}

ProjectPath.prototype.resolve = function (callback) {
	var self = this;

	config.getConfig(function (config) {
		var unresolvedPaths = config.get("paths");
		var keys = [];
		if (self.pathKey) {
			keys.push(self.pathKey);
			if (!unresolvedPaths.hasOwnProperty(self.pathKey)) {
				callback("Unknown path key: " + self.pathKey);
				return;
			}
		} else {
			for (var key in unresolvedPaths) {
				if (unresolvedPaths.hasOwnProperty(key)) keys.push(key);
			}
		}

		async.map(keys, function (key, callback) {
			tilde(unresolvedPaths[key], function (resolvedPath) {
				callback(null, resolvedPath);
			});
		}, function (err, pathsList) {
			var paths = {};
			keys.forEach(function (key, index) { paths[key] = pathsList[index]; });
			async.filter(keys, function (key, callback) {
				fs.stat(path.join(paths[key], self.repo), function (err, stats) {
					if (err) callback(false);
					else callback(stats.isDirectory());
				});
			}, function (candidates) {
				var result = candidates.map(function (candidateKey) {
					return {
						pathKey: candidateKey,
						repo: self.repo,
						fullPath: path.join(paths[candidateKey], self.repo)
					};
				});

				var pathKey = self.pathKey ? self.pathKey : "default";
				var generatedPath = null;
				if (paths.hasOwnProperty(pathKey)) {
					generatedPath = path.join(paths[pathKey], self.repo);
				}

				callback(null, result, generatedPath);
			});
		});
	});
};

exports.ProjectPath = ProjectPath;
