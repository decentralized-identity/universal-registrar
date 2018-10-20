/**
 * rdf-canonize Quad
 */

#ifndef RDF_CANONIZE_QUAD_H_
#define RDF_CANONIZE_QUAD_H_

#include <vector>
#include <string>

namespace RdfCanonize {

typedef enum {
  BLANK_NODE,
  NAMED_NODE,
  LITERAL,
  DEFAULT_GRAPH
} TermType;

struct Term {
  TermType termType;
  std::string value;

  Term(const TermType& termType, const std::string& value = "") :
    termType(termType), value(value) {};
  virtual ~Term() {};
  virtual Term* clone() const {
    return new Term(termType, value);
  }
};

struct BlankNode : public Term {
  BlankNode(const std::string& value = "") :
    Term(TermType::BLANK_NODE, value) {};
  virtual ~BlankNode() {};
  virtual Term* clone() const {
    return new BlankNode(value);
  }
};

struct NamedNode : public Term {
  NamedNode(const std::string& value = "") :
    Term(TermType::NAMED_NODE, value) {};
  virtual ~NamedNode() {};
  virtual Term* clone() const {
    return new NamedNode(value);
  }
};

struct Literal : public Term {
  std::string language;
  NamedNode* datatype;

  Literal(const std::string& value = "") :
    Term(TermType::LITERAL, value), datatype(NULL) {};
  virtual ~Literal() {
    if(datatype != NULL) {
      delete datatype;
    }
  }
  virtual Term* clone() const {
    Literal* literal = new Literal(value);
    literal->language = language;
    if(datatype != NULL) {
      literal->datatype = (NamedNode*)datatype->clone();
    }
    return literal;
  }
};

struct DefaultGraph : public Term {
  DefaultGraph() : Term(TermType::DEFAULT_GRAPH) {};
  virtual Term* clone() const {
    return new DefaultGraph();
  }
};

struct Quad {
  Term* subject;
  Term* predicate;
  Term* object;
  Term* graph;

  Quad() : subject(NULL), predicate(NULL), object(NULL), graph(NULL) {};
  Quad& operator=(const Quad& toCopy) {
    delete subject;
    delete predicate;
    delete object;
    delete graph;

    subject = toCopy.subject->clone();
    predicate = toCopy.predicate->clone();
    object = toCopy.object->clone();
    graph = toCopy.graph->clone();

    return *this;
  }
  ~Quad() {
    if(subject != NULL) {
      delete subject;
    }
    if(predicate != NULL) {
      delete predicate;
    }
    if(object != NULL) {
      delete object;
    }
    if(graph != NULL) {
      delete graph;
    }
  }
};

typedef std::vector<Quad*> QuadSet;

struct Dataset {
  QuadSet quads;

  ~Dataset() {
    for(Quad* quad : quads) {
      delete quad;
    }
  }
};

}

#endif // RDF_CANONIZE_QUAD_H_
