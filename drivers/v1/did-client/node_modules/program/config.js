var fs = require("fs");
var ini = require("ini");
var tilde = require("./tilde-expansion");

var configFile = "~/.prog";
var cachedConfig;

function Config(data) {
	this.data = data;
}

Config.prototype.has = function (sectionName, key) {
	if (this.data.hasOwnProperty(sectionName)) {
		var section = this.data[sectionName];
		if (section.hasOwnProperty(key)) return true;
	}
	return false;
};

Config.prototype.get = function (sectionName, key) {
	if (this.data.hasOwnProperty(sectionName)) {
		var section = this.data[sectionName];
		if (key === undefined) return section;
		if (section.hasOwnProperty(key)) return section[key];
	}
	throw new Error("Missing configuration: [" + sectionName + "] "+key+"=... in " + configFile);
};

Config.prototype.keys = function (sectionName) {
	if (!this.data.hasOwnProperty(sectionName)) return [];
	var section = this.data[sectionName];
	return Object.keys(section);
};

function getConfig(callback) {
	if (cachedConfig) {
		callback(cachedConfig);
		return;
	}

	tilde(configFile, function (expandedConfigFile) {
		fs.readFile(expandedConfigFile, 'utf8', function (err, data) {
			if (err) {
				if (err.code === "ENOENT") {
					cachedConfig = new Config({});
					console.log("No config file found at " + expandedConfigFile);
					console.log("Assuming default settings\n");
				} else {
					throw e;
				}
			} else {
				cachedConfig = new Config(ini.parse(data));
			}
			callback(cachedConfig);
		});
	});
}

exports.getConfig = getConfig;
