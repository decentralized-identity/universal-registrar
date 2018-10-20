const expandTilde = require('expand-tilde');

// The tilde-expansion package in npm broke. Expand-tilde
// is similar but offers a slightly different API. Polyfill:

module.exports = function (path, callback) {
    callback(expandTilde(path));
};
