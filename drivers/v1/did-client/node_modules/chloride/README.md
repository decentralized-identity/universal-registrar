# chloride

Chloride is a Cryptography Library (Cl) for javascript enviroments.

## about

Chloride decends from Dan J. Bernstein's [NaCl](http://nacl.cr.yp.to/) library ("Networking And Cryptography Library",
not to be confused with the other NaCl, google's Native Client)
djb wrote nacl, but did not maintain it, some ideas in the library (in particular the networking part)
wasn't really fully baked, and the best parts where taken and maintained as [libsodium](http://libsodium.org)
(although "Na" represents the element sodium, so they took the wrong part of the acronym)

chloride is a compatibility layer that gives you bindings to [libsodium](https://npm.im/sodium) when used in node.js,
and either the [libsodium-wrappers](https://npm.im/libsodium-wrappers) which is libsodium compiled to javascript via [emscripten](http://kripken.github.io/emscripten-site/)
if performance is important but code size isn't.
Or, if you are not doing many crypto operations, it uses
[tweetnacl](https://www.npmjs.com/package/tweetnacl), which is a handwritten
port, and 1/10 the size of libsodium-wrappers.

## support

I have wrapped and tested enough functions for my crypto modules to work.

  * [private-box](https://github.com/auditdrivencrypto/private-box) write a shortish message to multiple recipients.
  * [pull-box-stream](https://github.com/dominictarr/pull-box-stream) encrypt (every byte in) a one-way stream.
  * [secret-handshake](https://github.com/dominictarr/secret-handshake) create an authenticated private channel to a public key.

This is probably everything you need, nacl doesn't have a very large api, so this is probably everything.

## do i need performance or code size?

nacl was written with performance in mind, unfortunately a lot of that is lost when you compile it to javascript.
However, chloride still has the fastest _javascript_ elliptic curve signature that I am aware of.
(and asymmetric crypto is much slower than symmetric, so this is always the weak point)

If you are only doing a symmetric ciphers (crypto_box) or a signature or two,
then performance is probably not a problem. If you are veryify _many_ signatures,
performance _may_ be a problem. Bear in mind that an asymetric operation
(sign, verify, scalarmult, keygen) are usually 50 times slower than a symmetric operation,
say a hash.

See [sodiumperf](https://github.com/dominictarr/sodiumperf) performance comparisons.

to run chloride in performance mode, load via

``` js
var chloride = require('chloride')
```
to run in low size mode

``` js
var chloride = require('chloride/small')
```

This only applies to enviroments that only support javascript.
If you are running this on the server and could compile sodium,
then you have the same fast crypto either way.

## License

MIT
