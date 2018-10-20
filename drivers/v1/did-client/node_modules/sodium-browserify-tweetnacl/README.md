# sodium-browserify-tweetnacl

wraps javascript port of [tweetnacl](https://github.com/dchest/tweetnacl-js)
with the api of [chloride](https://github.com/dominictarr/chloride)
as an alternative to [sodium-browserify](https://github.com/dominictarr/sodium-browserify)
that has a much much smaller file size.

There are some chloride apis that are not implemented in tweetnacl.

* crypto_sign_ed25519_pk_to_curve25519
* crypto_sign_ed25519_sk_to_curve25519
* crypto_auth
* crypto_auth_verify

## License

MIT

