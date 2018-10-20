/**
 * rdf-canonize IdentifierIssuer
 */

#ifndef RDF_CANONIZE_IDENTIFIER_ISSUER_H_
#define RDF_CANONIZE_IDENTIFIER_ISSUER_H_

#include <map>
#include <string>
#include <vector>

namespace RdfCanonize {

typedef std::string NodeIdentifier;
typedef std::vector<NodeIdentifier> NodeIdentifierList;
typedef std::map<NodeIdentifier, NodeIdentifier> NodeIdentifierMap;

struct IdentifierIssuer {
  std::string prefix;
  unsigned counter;
  NodeIdentifierMap existing;
  NodeIdentifierList ordered;

  IdentifierIssuer(const std::string& prefix = "_:b") :
    prefix(prefix), counter(0) {};
  NodeIdentifier getNew();
  NodeIdentifier getNew(const NodeIdentifier& old);
  bool hasOld(const NodeIdentifier& old);
};

// used to simplify memory management for IdentifierIssuers since their
// allocation and reuse is part of a complex recursive algorithm
struct IdentifierIssuerPool {
  std::vector<IdentifierIssuer*> issuers;

  ~IdentifierIssuerPool() {
    for(IdentifierIssuer* issuer : issuers) {
      delete issuer;
    }
  }

  IdentifierIssuer* create(const std::string& prefix = "_:b") {
    IdentifierIssuer* issuer = new IdentifierIssuer(prefix);
    issuers.push_back(issuer);
    return issuer;
  }

  IdentifierIssuer* copy(const IdentifierIssuer* toCopy) {
    IdentifierIssuer* issuer = new IdentifierIssuer(*toCopy);
    issuers.push_back(issuer);
    return issuer;
  }
};

} // namespace RdfCanonize

#endif // RDF_CANONIZE_IDENTIFIER_ISSUER_H_
