{
  "targets": [
    {
      "target_name": "khovratovich",
      "sources": [
        "lib/khovratovich/addon.cc",
        "lib/khovratovich/pow.cc",
        "lib/khovratovich/blake/blake2b.cpp"
      ],
      "include_dirs": ["<!(node -e \"require('nan')\")"],
      "cflags": [
        #"-m64",
        #"-maes",
        #"-mavx",
        "-Wno-maybe-uninitialized",
        "-msse2",
        "-std=c++11"
      ]
    }
  ]
}
