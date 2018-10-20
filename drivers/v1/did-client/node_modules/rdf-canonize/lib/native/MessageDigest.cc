/*********************************************************************
 * rdf-canonize MessageDigest for Node.js.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * BSD License
 * <https://github.com/digitalbazaar/rdf-canonize/blob/master/LICENSE>
 ********************************************************************/

#include "MessageDigest.h"
#include <cstring>
#include <vector>

using namespace std;
using namespace RdfCanonize;

static string _bytesToHex(vector<unsigned char> &bytes, const size_t length);

MessageDigest::MessageDigest(const char* algorithm) : hashFn(NULL) {
  EVP_MD_CTX_init(&context);

  if(strcmp(algorithm, "sha256") == 0) {
    hashFn = EVP_sha256();
  } else {
    // TODO: throw error -- should never happen
  }

  if(hashFn != NULL) {
    // initialize the message digest context (NULL uses the default engine)
    EVP_DigestInit_ex(&context, hashFn, NULL);
  }
}

MessageDigest::~MessageDigest() {
  EVP_MD_CTX_cleanup(&context);
}

void MessageDigest::update(const char& c) {
  if(hashFn != NULL) {
    EVP_DigestUpdate(&context, &c, 1);
  }
}

void MessageDigest::update(const string& msg) {
  if(hashFn != NULL) {
    EVP_DigestUpdate(&context, msg.c_str(), msg.size());
  }
}

string MessageDigest::digest() {
  if(hashFn == NULL) {
    return "error";
  }

  // get hash
  const unsigned maxLength = EVP_MD_size(hashFn);
  unsigned length = maxLength;
  vector<unsigned char> hash(maxLength);
  EVP_DigestFinal_ex(&context, hash.data(), &length);

  // TODO: return bytes instead of hex
  // convert hash to hexadecimal
  return _bytesToHex(hash, length);
}

// initialize hexadecimal characters strings for fast lookups
static const char* HEX_CHARS = "0123456789abcdef";

static string _bytesToHex(vector<unsigned char> &bytes, const size_t length) {
  string hex;
  hex.reserve(length * 2);
  for(size_t i = 0; i < length; ++i) {
    // hexadecimal uses 2 digits, each with 16 values (or 4 bits):
    // convert the top 4 bits
    hex.push_back(HEX_CHARS[(bytes[i] >> 4)]);
    // convert the bottom 4 bits
    hex.push_back(HEX_CHARS[(bytes[i] & 0x0f)]);
  }

  return hex;
}
