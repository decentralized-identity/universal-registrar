HMAC-SHA-512-256 for TweetNacl.js
=================================

Implementation of <http://nacl.cr.yp.to/auth.html>
(requires [TweetNaCl.js](https://github.com/dchest/tweetnacl-js)).

Written in 2014 by Dmitry Chestnykh. Public domain.

[![Build Status](https://travis-ci.org/dchest/tweetnacl-auth-js.svg?branch=master)
](https://travis-ci.org/dchest/tweetnacl-auth-js)


Installation
------------

Via NPM:

    $ npm install tweetnacl-auth

Via Bower:

    $ bower install tweetnacl-auth


or just download `nacl-auth.js` or `nacl-auth.min.js` and include it after
TweetNaCl.js:

```html
<script src="nacl.min.js"></script>
<script src="nacl-auth.min.js"></script>
```

If using a CommonJS environment, such as Node.js, you can import it into `nacl`
namespace:

```javascript
var nacl = require('tweetnacl');
nacl.auth = require('tweetnacl-auth');
```


Usage
-----


### nacl.auth(message, key)

Authenticates the given message with the secret key.
(In other words, returns HMAC-SHA-512-256 of the message under the key.)


### nacl.auth.full(message, key)

Returns HMAC-SHA-512 (without truncation) of the message under the key

### nacl.auth.authLength = 32

Length of authenticator returned by `nacl.auth`.

### nacl.auth.authFullLength = 64

Length of authenticator returned by `nacl.auth.full`.

### nacl.auth.keyLength = 32

Length of key for `nacl.auth` and `nacl.auth.full` (key length is currently not
enforced).
