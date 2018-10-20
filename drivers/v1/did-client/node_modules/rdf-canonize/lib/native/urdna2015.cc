/*********************************************************************
 * rdf-canonize urdna2015 for Node.js.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * BSD License
 * <https://github.com/digitalbazaar/rdf-canonize/blob/master/LICENSE>
 ********************************************************************/

#include "urdna2015.h"
#include "MessageDigest.h"
#include "NQuads.h"
#include <algorithm>
#include <cstring>
#include <map>
#include <sstream>

using namespace std;
using namespace RdfCanonize;

static const char* POSITIONS = "sog";

static void getSortedHashes(
  HashToBlankNodeMap& hashToBlankNodeMap, vector<Hash>& hashes);

//static void printDataset(const Dataset& dataset);
//static void printTerm(const Term& term);

// TODO: rename "component" to "term" everywhere?

string Urdna2015::main(const Dataset& dataset) {
  // printDataset(dataset);

  // 4.4) Normalization Algorithm

  // 1) Create the normalization state.

  // Note: Optimize by generating non-normalized blank node map concurrently.
  map<const NodeIdentifier, bool> nonNormalized;

  // 2) For every quad in input dataset:
  for(Quad* q : dataset.quads) {
    // 2.1) For each blank node that occurs in the quad, add a reference
    // to the quad using the blank node identifier in the blank node to
    // quads map, creating a new entry if necessary.
    for(Term* term : {q->subject, q->predicate, q->object, q->graph}) {
      if(term->termType != TermType::BLANK_NODE) {
        continue;
      }
      string id = term->value;
      if(blankNodeInfo.find(id) != blankNodeInfo.end()) {
        blankNodeInfo[id].quads.push_back(q);
      } else {
        nonNormalized[id] = true;
        // TODO: consider using `new`
        BlankNodeInfo bni;
        bni.quads.push_back(q);
        blankNodeInfo[id] = bni;
      }
    }
  }

  // 3) Create a list of non-normalized blank node identifiers
  // non-normalized identifiers and populate it using the keys from the
  // blank node to quads map.
  // Note: We use a map here and it was generated during step 2.

  // 4) Initialize `simple`, a boolean flag, to true.
  bool simple = true;

  // 5) While `simple` is true, issue canonical identifiers for blank nodes:
  while(simple) {
    // 5.1) Set `simple` to false.
    simple = false;

    // 5.2) Clear hash to blank nodes map.
    hashToBlankNodes.clear();

    // 5.3) For each blank node identifier identifier in non-normalized
    // identifiers:
    for(auto& kv : nonNormalized) {
      // 5.3.1) Create a hash, hash, according to the Hash First Degree
      // Quads algorithm.
      Hash hash = hashFirstDegreeQuads(kv.first);

      // 5.3.2) Add hash and identifier to hash to blank nodes map,
      // creating a new entry if necessary.
      if(hashToBlankNodes.find(hash) != hashToBlankNodes.end()) {
        hashToBlankNodes[hash].push_back(kv.first);
      } else {
        NodeIdentifierList nil;
        nil.push_back(kv.first);
        hashToBlankNodes[hash] = nil;
      }
    }

    // 5.4) For each hash to identifier list mapping in hash to blank
    // nodes map, lexicographically-sorted by hash:
    vector<string> hashes;
    getSortedHashes(hashToBlankNodes, hashes);
    for(Hash& hash : hashes) {
      // 5.4.1) If the length of identifier list is greater than 1,
      // continue to the next mapping.
      NodeIdentifierList& idList = hashToBlankNodes[hash];
      if(idList.size() > 1) {
        continue;
      }

      // 5.4.2) Use the Issue Identifier algorithm, passing canonical
      // issuer and the single blank node identifier in identifier
      // list, identifier, to issue a canonical replacement identifier
      // for identifier.
      NodeIdentifier id = idList[0];
      canonicalIssuer.getNew(id);

      // 5.4.3) Remove identifier from non-normalized identifiers.
      nonNormalized.erase(id);

      // 5.4.4) Remove hash from the hash to blank nodes map.
      hashToBlankNodes.erase(hash);

      // 5.4.5) Set simple to true.
      simple = true;
    }
  }

  // 6) For each hash to identifier list mapping in hash to blank nodes map,
  // lexicographically-sorted by hash:
  vector<Hash> hashes;
  getSortedHashes(hashToBlankNodes, hashes);
  for(Hash& hash : hashes) {
    // 6.1) Create hash path list where each item will be a result of
    // running the Hash N-Degree Quads algorithm.
    HashPathList hashPathList;

    // 6.2) For each blank node identifier identifier in identifier list:
    NodeIdentifierList& idList = hashToBlankNodes[hash];
    for(NodeIdentifier id : idList) {
      // 6.2.1) If a canonical identifier has already been issued for
      // identifier, continue to the next identifier.
      if(canonicalIssuer.hasOld(id)) {
        continue;
      }

      // 6.2.2) Create temporary issuer, an identifier issuer
      // initialized with the prefix _:b.
      IdentifierIssuer* issuer = issuerPool.create("_:b");

      // 6.2.3) Use the Issue Identifier algorithm, passing temporary
      // issuer and identifier, to issue a new temporary blank node
      // identifier for identifier.
      issuer->getNew(id);

      // 6.2.4) Run the Hash N-Degree Quads algorithm, passing
      // temporary issuer, and append the result to the hash path list.
      hashPathList.push_back(hashNDegreeQuads(id, issuer));
    }

    // 6.3) For each result in the hash path list,
    // lexicographically-sorted by the hash in result:
    sort(
      hashPathList.begin(), hashPathList.end(),
      // TODO: use string operator fn instead of lambda?
      [] (const HashPath& a, const HashPath& b) {
        return a.first < b.first;
      });
    for(HashPath& hashPath : hashPathList) {
      // 6.3.1) For each blank node identifier, existing identifier,
      // that was issued a temporary identifier by identifier issuer
      // in result, issue a canonical identifier, in the same order,
      // using the Issue Identifier algorithm, passing canonical
      // issuer and existing identifier.
      for(auto& id : hashPath.second->ordered) {
        canonicalIssuer.getNew(id);
      }
    }
  }

  /* Note: At this point all blank nodes in the set of RDF quads have been
  assigned canonical identifiers, which have been stored in the canonical
  issuer. Here each quad is updated by assigning each of its blank nodes
  its new identifier. */

  // 7) For each quad `quad` in input dataset:
  for(Quad* q : dataset.quads) {
    // 7.1) Create a copy `quad copy` of quad and replace any existing
    // blank node identifiers using the canonical identifiers
    // previously issued by canonical issuer.
    // Note: We optimize away the copy here.
    for(Term* term : {q->subject, q->predicate, q->object, q->graph}) {
      if(term->termType == TermType::BLANK_NODE &&
        term->value.find(canonicalIssuer.prefix) != 0) {
        term->value = canonicalIssuer.getNew(term->value);
      }
    }
    // 7.2) Add quad copy to the normalized dataset.
    // Note: the copy is optimized away.
  }

  // `serialize` will serialize, sort, and join the quads
  // 8) Return the normalized dataset.
  return NQuads::serialize(dataset.quads);
}

// 4.6) Hash First Degree Quads
Hash Urdna2015::hashFirstDegreeQuads(NodeIdentifier id) {
  // return cached hash
  BlankNodeInfo& info = blankNodeInfo[id];
  if(info.hash.size() != 0) {
    return info.hash;
  }

  // 1) Initialize nquads to an empty list. It will be used to store quads in
  // N-Quads format.
  vector<string> nquads;

  // 2) Get the list of quads `quads` associated with the reference blank node
  // identifier in the blank node to quads map.
  QuadSet& quads = info.quads;

  // 3) For each quad `quad` in `quads`:
  for(Quad* q : quads) {
    // 3.1) Serialize the quad in N-Quads format with the following special
    // rule:

    // 3.1.1) If any component in quad is an blank node, then serialize it
    // using a special identifier as follows:
    Quad copy;
    // 3.1.2) If the blank node's existing blank node identifier matches
    // the reference blank node identifier then use the blank node
    // identifier _:a, otherwise, use the blank node identifier _:z.
    copy.subject = modifyFirstDegreeComponent(id, *(q->subject));
    copy.predicate = modifyFirstDegreeComponent(id, *(q->predicate));
    copy.object = modifyFirstDegreeComponent(id, *(q->object));
    copy.graph = modifyFirstDegreeComponent(id, *(q->graph));
    nquads.push_back(NQuads::serializeQuad(copy));
  }

  // 4) Sort nquads in lexicographical order.
  sort(nquads.begin(), nquads.end());

  // 5) Return the hash that results from passing the sorted, joined nquads
  // through the hash algorithm.
  MessageDigest md(hashAlgorithm);
  for(string& nquad : nquads) {
    md.update(nquad);
  }
  // TODO: represent as byte buffer instead to cut memory usage in half
  info.hash = md.digest();
  return info.hash;
}

// 4.7) Hash Related Blank Node
Hash Urdna2015::hashRelatedBlankNode(
  NodeIdentifier related, const Quad& quad,
  IdentifierIssuer& issuer, char position) {
  // 1) Set the identifier to use for related, preferring first the canonical
  // identifier for related if issued, second the identifier issued by issuer
  // if issued, and last, if necessary, the result of the Hash First Degree
  // Quads algorithm, passing related.
  NodeIdentifier id;
  if(canonicalIssuer.hasOld(related)) {
    id = canonicalIssuer.getNew(related);
  } else if(issuer.hasOld(related)) {
    id = issuer.getNew(related);
  } else {
    id = hashFirstDegreeQuads(related);
  }

  // 2) Initialize a string input to the value of position.
  // Note: We use a hash object instead.
  MessageDigest md(hashAlgorithm);
  md.update(position);

  // 3) If position is not g, append <, the value of the predicate in quad,
  // and > to input.
  if(position != 'g') {
    md.update(getRelatedPredicate(quad));
  }

  // 4) Append identifier to input.
  md.update(id);

  // 5) Return the hash that results from passing input through the hash
  // algorithm.
  // TODO: represent as byte buffer instead to cut memory usage in half
  return md.digest();
}

// 4.8) Hash N-Degree Quads
HashPath Urdna2015::hashNDegreeQuads(
  NodeIdentifier id, IdentifierIssuer*& issuer) {
  // 1) Create a hash to related blank nodes map for storing hashes that
  // identify related blank nodes.
  // Note: 2) and 3) handled within `createHashToRelated`
  MessageDigest md(hashAlgorithm);
  // TODO: consider using heap
  HashToBlankNodeMap hashToRelated = createHashToRelated(id, issuer);

  // 4) Create an empty string, data to hash.
  // Note: We created a hash object `md` above instead.

  // 5) For each related hash to blank node list mapping in hash to related
  // blank nodes map, sorted lexicographically by related hash:
  vector<string> hashes;
  getSortedHashes(hashToRelated, hashes);
  for(Hash hash : hashes) {
    // 5.1) Append the related hash to the data to hash.
    md.update(hash);

    // 5.2) Create a string chosen path.
    string chosenPath;

    // 5.3) Create an unset chosen issuer variable.
    IdentifierIssuer* chosenIssuer = NULL;

    // 5.4) For each permutation of blank node list:
    // TODO: would it be safe to use a reference here (`NodeIdentifierList&`)
    // and avoid the copy?
    NodeIdentifierList permutation = hashToRelated[hash];
    sort(permutation.begin(), permutation.end());
    do {
      // 5.4.1) Create a copy of issuer, issuer copy.
      IdentifierIssuer* issuerCopy = issuerPool.copy(issuer);

      // 5.4.2) Create a string path.
      // TODO: optimize with ostringstream?
      string path;

      // 5.4.3) Create a recursion list, to store blank node identifiers
      // that must be recursively processed by this algorithm.
      NodeIdentifierList recursionList;

      // 5.4.4) For each related in permutation:
      bool nextPermutation = false;
      for(NodeIdentifier related : permutation) {
        // 5.4.4.1) If a canonical identifier has been issued for
        // related, append it to path.
        if(canonicalIssuer.hasOld(related)) {
          path.append(canonicalIssuer.getNew(related));
        } else {
          // 5.4.4.2) Otherwise:
          // 5.4.4.2.1) If issuer copy has not issued an identifier for
          // related, append related to recursion list.
          if(!issuerCopy->hasOld(related)) {
            recursionList.push_back(related);
          }
          // 5.4.4.2.2) Use the Issue Identifier algorithm, passing
          // issuer copy and related and append the result to path.
          path.append(issuerCopy->getNew(related));
        }

        // 5.4.4.3) If chosen path is not empty and the length of path
        // is greater than or equal to the length of chosen path and
        // path is lexicographically greater than chosen path, then
        // skip to the next permutation.
        if(chosenPath.size() != 0 &&
          path.size() >= chosenPath.size() && path > chosenPath) {
          nextPermutation = true;
          break;
        }
      }

      if(nextPermutation) {
        continue;
      }

      // 5.4.5) For each related in recursion list:
      for(NodeIdentifier related : recursionList) {
        // 5.4.5.1) Set result to the result of recursively executing
        // the Hash N-Degree Quads algorithm, passing related for
        // identifier and issuer copy for path identifier issuer.
        HashPath result = hashNDegreeQuads(related, issuerCopy);

        // 5.4.5.2) Use the Issue Identifier algorithm, passing issuer
        // copy and related and append the result to path.
        path.append(issuerCopy->getNew(related));

        // 5.4.5.3) Append <, the hash in result, and > to path.
        path.append(1, '<').append(result.first).append(1, '>');

        // 5.4.5.4) Set issuer copy to the identifier issuer in
        // result.
        issuerCopy = result.second;

        // 5.4.5.5) If chosen path is not empty and the length of path
        // is greater than or equal to the length of chosen path and
        // path is lexicographically greater than chosen path, then
        // skip to the next permutation.
        if(chosenPath.size() != 0 &&
          path.size() >= chosenPath.size() && path > chosenPath) {
          nextPermutation = true;
          break;
        }
      }

      if(nextPermutation) {
        continue;
      }

      // 5.4.6) If chosen path is empty or path is lexicographically
      // less than chosen path, set chosen path to path and chosen
      // issuer to issuer copy.
      if(chosenPath.size() == 0 || path < chosenPath) {
        chosenPath = path;
        chosenIssuer = issuerCopy;
      }
    } while(next_permutation(permutation.begin(), permutation.end()));

    // 5.5) Append chosen path to data to hash.
    md.update(chosenPath);

    // 5.6) Replace issuer, by reference, with chosen issuer.
    issuer = chosenIssuer;
  }

  // 6) Return issuer and the hash that results from passing data to hash
  // through the hash algorithm.
  return HashPath(md.digest(), issuer);
}

// helper for modifying component during Hash First Degree Quads
Term* Urdna2015::modifyFirstDegreeComponent(
  NodeIdentifier id, Term& component) {
  Term* copy = component.clone();
  if(component.termType == TermType::BLANK_NODE) {
    copy->value = (component.value == id ? "_:a" : "_:z");
  }
  return copy;
}

// helper for getting a related predicate
string Urdna2015::getRelatedPredicate(const Quad& quad) {
  ostringstream predicate;
  predicate << "<" << quad.predicate->value << ">";
  return predicate.str();
}

// helper for creating hash to related blank nodes map
HashToBlankNodeMap Urdna2015::createHashToRelated(
  NodeIdentifier id, IdentifierIssuer*& issuer) {
  // 1) Create a hash to related blank nodes map for storing hashes that
  // identify related blank nodes.
  // TODO: consider using heap
  HashToBlankNodeMap hashToRelated;

  // 2) Get a reference, quads, to the list of quads in the blank node to
  // quads map for the key identifier.
  const QuadSet& quads = blankNodeInfo[id].quads;

  // 3) For each quad in quads:
  for(const Quad* q : quads) {
    // 3.1) For each component in quad, if component is the subject, object,
    // and graph name and it is a blank node that is not identified by
    // identifier:
    unsigned counter = -1;
    for(const Term* component : {q->subject, q->object, q->graph}) {
      counter++;
      if(!(component->termType == TermType::BLANK_NODE &&
        component->value != id)) {
        continue;
      }
      // 3.1.1) Set hash to the result of the Hash Related Blank Node
      // algorithm, passing the blank node identifier for component as
      // related, quad, path identifier issuer as issuer, and position as
      // either s, o, or g based on whether component is a subject, object,
      // graph name, respectively.
      string related = component->value;
      char position = POSITIONS[counter];
      Hash hash = hashRelatedBlankNode(related, *q, *issuer, position);

      // 3.1.2) Add a mapping of hash to the blank node identifier for
      // component to hash to related blank nodes map, adding an entry as
      // necessary.
      if(hashToRelated.find(hash) != hashToRelated.end()) {
        hashToRelated[hash].push_back(related);
      } else {
        NodeIdentifierList nil;
        nil.push_back(related);
        hashToRelated[hash] = nil;
      }
    }
  }

  return hashToRelated;
}

static void getSortedHashes(
  HashToBlankNodeMap& hashToBlankNodeMap, vector<Hash>& hashes) {
  for(auto& kv : hashToBlankNodeMap) {
    hashes.push_back(kv.first);
  }
  sort(hashes.begin(), hashes.end());
}
/*
static void printDataset(const Dataset& dataset) {
  QuadSet::const_iterator dit = dataset.quads.begin();
  printf("dataset:\n");
  while(dit != dataset.quads.end()) {
    Quad& q = **dit;
    printf("  quad:\n");
    printf("    subject:\n");
    printTerm(*(q.subject));
    printf("    predicate:\n");
    printTerm(*(q.predicate));
    printf("    object:\n");
    printTerm(*(q.object));
    printf("    graph:\n");
    printTerm(*(q.graph));
    dit++;
  }
}

static void printTerm(const Term& term) {
  string termType;
  switch(term.termType) {
    case TermType::BLANK_NODE:
      termType = "BlankNode";
      break;
    case TermType::NAMED_NODE:
      termType = "NamedNode";
      break;
    case TermType::LITERAL:
      termType = "Literal";
      break;
    case TermType::DEFAULT_GRAPH:
      termType = "DefaultGraph";
      break;
  }

  printf("      termType: %s\n", termType.c_str());
  if(term.termType != TermType::DEFAULT_GRAPH) {
    printf("      value: %s\n", term.value.c_str());
  }
  if(term.termType == TermType::LITERAL) {
    Term* datatype = ((Literal&)term).datatype;
    string& language = ((Literal&)term).language;
    if(datatype != NULL) {
      printf("      datatype: \n");
      printTerm(*datatype);
    } else if(language.size() != 0) {
      printf("      language: %s\n", language.c_str());
    }
  }
}
*/
