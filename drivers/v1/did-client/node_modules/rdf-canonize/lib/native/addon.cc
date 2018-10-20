/*********************************************************************
 * rdf-canonize addon for Node.js.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * BSD License
 * <https://github.com/digitalbazaar/rdf-canonize/blob/master/LICENSE>
 ********************************************************************/

#include <nan.h>
#include "addon.h"   // NOLINT(build/include)
#include "urdna2015.h"  // NOLINT(build/include)

using Nan::AsyncQueueWorker;
using Nan::AsyncWorker;
using Nan::Callback;
using Nan::GetFunction;
using Nan::HandleScope;
using Nan::MaybeLocal;
using Nan::New;
using Nan::Null;
using Nan::Set;
using Nan::To;
using Nan::Utf8String;
using v8::Array;
using v8::Function;
using v8::FunctionTemplate;
using v8::Handle;
using v8::Isolate;
using v8::Local;
using v8::Number;
using v8::Object;
using v8::String;
using v8::Value;

using namespace RdfCanonize;

class Urdna2015Worker : public AsyncWorker {
public:
  Urdna2015Worker(Urdna2015 urdna2015, Dataset* dataset, Callback* callback)
    : AsyncWorker(callback), urdna2015(urdna2015), dataset(dataset) {}
  ~Urdna2015Worker() {
    delete dataset;
  }

  // Executed inside the worker-thread.
  // It is not safe to access V8, or V8 data structures
  // here, so everything we need for input and output
  // should go on `this`.
  void Execute () {
    output = urdna2015.main(*dataset);
  }

  // Executed when the async work is complete
  // this function will be run inside the main event loop
  // so it is safe to use V8 again
  void HandleOKCallback () {
    HandleScope scope;
    Local<Value> argv[] = {
      Null(),
      New(output.c_str()).ToLocalChecked()
    };

    callback->Call(2, argv);
  }

private:
  Urdna2015 urdna2015;
  Dataset* dataset;
  std::string output;
};

static bool fillDataset(
  Dataset& dataset, const Handle<Array>& datasetArray);
static bool createTerm(
  Term*& term,
  const Handle<Object>& object,
  const Local<String>& termTypeKey,
  const Local<String>& valueKey,
  const Local<String>& datatypeKey,
  const Local<String>& languageKey);

NAN_METHOD(Main) {
  // ensure first argument is an object
  if(!info[0]->IsObject()) {
    Nan::ThrowTypeError("'options' must be an object");
    return;
  }
  // ensure second argument is a callback
  if(!info[1]->IsFunction()) {
    Nan::ThrowTypeError("'callback' must be a function");
    return;
  }

  Callback* callback = new Callback(info[1].As<Function>());
  Handle<Object> object = Handle<Object>::Cast(info[0]);
  /*
  Handle<Value> maxCallStackDepthValue =
    object->Get(New("maxCallStackDepth").ToLocalChecked());
  Handle<Value> maxTotalCallStackDepthValue =
    object->Get(New("maxTotalCallStackDepth").ToLocalChecked());
  */
  Handle<Array> datasetArray =
    Handle<Array>::Cast(object->Get(New("dataset").ToLocalChecked()));

  /*
  const unsigned maxCallStackDepth =
    To<unsigned>(maxCallStackDepthValue).FromJust();
  const unsigned maxTotalCallStackDepth =
    To<unsigned>(maxTotalCallStackDepthValue).FromJust();
  */

  //Urdna2015 urdna2015(maxCallStackDepth, maxTotalCallStackDepth);
  Urdna2015 urdna2015(0, 0);

  Dataset* dataset = new Dataset();
  if(!fillDataset(*dataset, datasetArray)) {
    delete dataset;
    // TODO: call `callback` with the error?
    return;
  }

  AsyncQueueWorker(new Urdna2015Worker(urdna2015, dataset, callback));
}

NAN_METHOD(MainSync) {
  // ensure first argument is an object
  if(!info[0]->IsObject()) {
    Nan::ThrowTypeError("'options' must be an object");
    return;
  }

  Handle<Object> object = Handle<Object>::Cast(info[0]);
  /*
  Handle<Value> maxCallStackDepthValue =
    object->Get(New("maxCallStackDepth").ToLocalChecked());
  Handle<Value> maxTotalCallStackDepthValue =
    object->Get(New("maxTotalCallStackDepth").ToLocalChecked());
  */
  Handle<Array> datasetArray =
    Handle<Array>::Cast(object->Get(New("dataset").ToLocalChecked()));

  /*
  const unsigned maxCallStackDepth =
    To<unsigned>(maxCallStackDepthValue).FromJust();
  const unsigned maxTotalCallStackDepth =
    To<unsigned>(maxTotalCallStackDepthValue).FromJust();
  */

  //Urdna2015 urdna2015(maxCallStackDepth, maxTotalCallStackDepth);
  Urdna2015 urdna2015(0, 0);

  Dataset dataset;
  if(!fillDataset(dataset, datasetArray)) {
    return;
  }

  std::string output = urdna2015.main(dataset);
  info.GetReturnValue().Set(New(output.c_str()).ToLocalChecked());
}

static bool fillDataset(Dataset& dataset, const Handle<Array>& datasetArray) {
  Local<String> subjectKey = New("subject").ToLocalChecked();
  Local<String> predicateKey = New("predicate").ToLocalChecked();
  Local<String> objectKey = New("object").ToLocalChecked();
  Local<String> graphKey = New("graph").ToLocalChecked();

  Local<String> termTypeKey = New("termType").ToLocalChecked();
  Local<String> valueKey = New("value").ToLocalChecked();
  Local<String> datatypeKey = New("datatype").ToLocalChecked();
  Local<String> languageKey = New("language").ToLocalChecked();

  // TODO: check for valid structure
  for(size_t di = 0; di < datasetArray->Length(); ++di) {
    Handle<Object> quad = Handle<Object>::Cast(datasetArray->Get(di));

    // TODO: ensure all keys are present and represent objects

    Handle<Object> subject =
      Handle<Object>::Cast(quad->Get(subjectKey));
    Handle<Object> predicate =
      Handle<Object>::Cast(quad->Get(predicateKey));
    Handle<Object> object =
      Handle<Object>::Cast(quad->Get(objectKey));
    Handle<Object> graph =
      Handle<Object>::Cast(quad->Get(graphKey));

    Quad* q = new Quad();

    if(!(
      createTerm(
        q->subject, subject,
        termTypeKey, valueKey, datatypeKey, languageKey) &&
      createTerm(
        q->predicate, predicate,
        termTypeKey, valueKey, datatypeKey, languageKey) &&
      createTerm(
        q->object, object,
        termTypeKey, valueKey, datatypeKey, languageKey) &&
      createTerm(
        q->graph, graph,
        termTypeKey, valueKey, datatypeKey, languageKey))) {
      delete q;
      return false;
    }

    // TODO: ensure q is valid (term types all valid for s, p, o, g, etc.)

    dataset.quads.push_back(q);
  }

  return true;
}

static bool createTerm(
  Term*& term,
  const Handle<Object>& object,
  const Local<String>& termTypeKey,
  const Local<String>& valueKey,
  const Local<String>& datatypeKey,
  const Local<String>& languageKey) {
  if(!(object->Has(termTypeKey) && object->Get(termTypeKey)->IsString())) {
    Nan::ThrowTypeError(
      "'termType' must be 'BlankNode', 'NamedNode', " \
      "'Literal', or 'DefaultGraph'.");
    return false;
  }

  Utf8String termType(object->Get(termTypeKey));

  if(strcmp(*termType, "BlankNode") == 0) {
    term = new BlankNode();
  } else if(strcmp(*termType, "NamedNode") == 0) {
    term = new NamedNode();
  } else if(strcmp(*termType, "Literal") == 0) {
    Literal* literal = new Literal();
    term = literal;
    if(object->Has(datatypeKey)) {
      Handle<Object> datatype =
        Handle<Object>::Cast(object->Get(datatypeKey));
      if(!datatype->IsObject() || datatype->IsNull()) {
        Nan::ThrowError(
          "'termType' must be 'BlankNode', 'NamedNode', " \
          "'Literal', or 'DefaultGraph'.");
        return false;
      }
      Term* dataTypeTerm;
      if(!createTerm(
        dataTypeTerm, datatype,
        termTypeKey, valueKey, datatypeKey, languageKey)) {
        return false;
      }
      if(dataTypeTerm->termType != TermType::NAMED_NODE) {
        Nan::ThrowError("datatype 'termType' must be 'NamedNode'.");
        delete dataTypeTerm;
        return false;
      }
      literal->datatype = (NamedNode*)dataTypeTerm;
    }
    if(object->Has(languageKey)) {
      literal->language = *Utf8String(object->Get(languageKey));
    }
  } else if(strcmp(*termType, "DefaultGraph") == 0) {
    term = new DefaultGraph();
  } else {
    Nan::ThrowError(
      "'termType' must be 'BlankNode', 'NamedNode', " \
      "'Literal', or 'DefaultGraph'.");
    return false;
  }

  term->value = *Utf8String(object->Get(valueKey));

  return true;
}

NAN_MODULE_INIT(InitAll) {
  Set(
    target, New<String>("main").ToLocalChecked(),
    GetFunction(New<FunctionTemplate>(Main)).ToLocalChecked());
  Set(
    target, New<String>("mainSync").ToLocalChecked(),
    GetFunction(New<FunctionTemplate>(MainSync)).ToLocalChecked());
}

NODE_MODULE(addon, InitAll)
