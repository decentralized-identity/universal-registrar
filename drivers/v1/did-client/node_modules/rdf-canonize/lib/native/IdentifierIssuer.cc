/*********************************************************************
 * rdf-canonize IdentifierIssuer for Node.js.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * BSD License
 * <https://github.com/digitalbazaar/rdf-canonize/blob/master/LICENSE>
 ********************************************************************/

#include "IdentifierIssuer.h"

using namespace std;
using namespace RdfCanonize;

NodeIdentifier IdentifierIssuer::getNew() {
  // get next identifier
  NodeIdentifier id = prefix;
  id.append(to_string(counter));
  counter++;
  return id;
}

NodeIdentifier IdentifierIssuer::getNew(const NodeIdentifier& old) {
  // return already issued identifier
  NodeIdentifierMap::iterator i = existing.find(old);
  if(i != existing.end()) {
    return i->second;
  }

  // get next identifier
  NodeIdentifier id = getNew();

  // save mapping and order
  existing[old] = id;
  ordered.push_back(old);

  return id;
}

bool IdentifierIssuer::hasOld(const NodeIdentifier& old) {
  return existing.find(old) != existing.end();
}
