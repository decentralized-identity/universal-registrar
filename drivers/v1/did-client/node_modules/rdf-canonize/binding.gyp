{
  "targets": [
    {
      "target_name": "urdna2015",
      "sources": [
        "lib/native/IdentifierIssuer.cc",
        "lib/native/MessageDigest.cc",
        "lib/native/NQuads.cc",
        "lib/native/addon.cc",
        "lib/native/urdna2015.cc"
      ],
      "include_dirs": ["<!(node -e \"require('nan')\")"],
      "cflags": [
        "-Wno-maybe-uninitialized",
        "-std=c++11"
      ],
      "variables": {
        "node_shared_openssl%": "true"
      },
      "conditions": [
        ['node_shared_openssl=="false"', {
          "include_dirs": [
            "<(node_root_dir)/include/node/openssl"
          ]
        }]
      ]
    }
  ]
}
