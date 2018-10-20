# sodium-browserify

A polyfil between the apis of [node-sodium](https://github.com/paixaop/node-sodium/)
and [libsodium-wrappers](https://github.com/jedisct1/libsodium.js), heir to [crypto-browserify](https://github.com/crypto-browserify/crypto-browserify)

Mainly, this wraps libsodium-wrappers to make it work with buffers,
and pass the same tests as it does in node, and in the browser.

Tests are generated from sodium, with values stored in JSON so that they can be run in the browser.
## License

MIT
