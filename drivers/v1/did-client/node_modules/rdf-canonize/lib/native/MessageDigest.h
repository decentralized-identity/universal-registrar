/**
 * rdf-canonize MessageDigest
 */

#ifndef RDF_CANONIZE_MESSAGE_DIGEST_H_
#define RDF_CANONIZE_MESSAGE_DIGEST_H_

#include <string>
#include <openssl/evp.h>

namespace RdfCanonize {

struct MessageDigest {
  EVP_MD_CTX context;
  const EVP_MD* hashFn;

  MessageDigest(const char* algorithm);
  ~MessageDigest();
  void update(const char& c);
  void update(const std::string& msg);
  std::string digest();
};

} // namespace RdfCanonize

#endif // RDF_CANONIZE_MESSAGE_DIGEST_H_
