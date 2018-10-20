/**
 * rdf-canonize NQuads
 */

#ifndef RDF_CANONIZE_NQUADS_H_
#define RDF_CANONIZE_NQUADS_H_

#include "Quad.h"
#include <string>

namespace RdfCanonize {

struct NQuads {
  /**
   * Converts a QuadSet to N-Quads.
   *
   * @param quadset the set of RDF Quads to convert.
   *
   * @return the N-Quads string.
   */
  static std::string serialize(const QuadSet& quadset);

  /**
   * Converts an RDF quad to an N-Quad string (a single quad).
   *
   * @param quad the RDF quad to convert.
   *
   * @return the N-Quad string.
   */
  static std::string serializeQuad(const Quad& quad);
};

}

#endif // RDF_CANONIZE_NQUADS_H_
