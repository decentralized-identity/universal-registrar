/*********************************************************************
 * Equihash for Node.js.
 * Node.js addon.
 *
 * Copyright (c) 2017 Digital Bazaar, Inc.
 *
 * MIT License
 * <https://github.com/digitalbazaar/equihash/blob/master/LICENSE>
 ********************************************************************/

#include <arpa/inet.h>
#include <nan.h>
#include "addon.h"   // NOLINT(build/include)
#include "pow.h"  // NOLINT(build/include)

using Nan::AsyncQueueWorker;
using Nan::AsyncWorker;
using Nan::Callback;
using Nan::GetFunction;
using Nan::HandleScope;
using Nan::New;
using Nan::Null;
using Nan::Set;
using Nan::To;
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

class EquihashSolveWorker : public AsyncWorker {
public:
    EquihashSolveWorker(Equihash equihash, Callback *callback)
        : AsyncWorker(callback), equihash(equihash), proof() {}
    ~EquihashSolveWorker() {}

    // Executed inside the worker-thread.
    // It is not safe to access V8, or V8 data structures
    // here, so everything we need for input and output
    // should go on `this`.
    void Execute () {
        proof = equihash.FindProof();
        //printhex("solution", &solution[0], solution.size());
    }

    // Executed when the async work is complete
    // this function will be run inside the main event loop
    // so it is safe to use V8 again
    void HandleOKCallback () {
        HandleScope scope;
        Local<Object> obj = New<Object>();

        Local<Array> solutionValue = New<Array>(proof.solution.size());
        for (size_t i = 0; i < proof.solution.size(); ++i) {
            Set(solutionValue, i, New<Number>(proof.solution[i]));
        }

        //printhex("solution COPY", &solution[0], solution.size());

        obj->Set(New("n").ToLocalChecked(), New(proof.n));
        obj->Set(New("k").ToLocalChecked(), New(proof.k));
        obj->Set(New("personal").ToLocalChecked(),
            Nan::CopyBuffer(
                (const char *)proof.personal.data(), proof.personal.size())
                .ToLocalChecked());
        // TODO: add option to include seed in proof
        //obj->Set(New("seed").ToLocalChecked(),
        //    Nan::CopyBuffer(
        //        (const char *)proof.seed.data(), proof.seed.size())
        //        .ToLocalChecked());
        obj->Set(New("nonce").ToLocalChecked(),
            Nan::CopyBuffer(
                (const char *)proof.nonce.data(), proof.nonce.size())
                .ToLocalChecked());
        obj->Set(New("solution").ToLocalChecked(), solutionValue);

        Local<Value> argv[] = {
            Null(),
            obj
        };

        callback->Call(2, argv);
    }

private:
    Equihash equihash;
    Proof proof;
};

class EquihashVerifyWorker : public AsyncWorker {
public:
    EquihashVerifyWorker(Proof proof, Callback *callback)
        : AsyncWorker(callback), proof(proof) {}
    ~EquihashVerifyWorker() {}

    // Executed inside the worker-thread.
    // It is not safe to access V8, or V8 data structures
    // here, so everything we need for input and output
    // should go on `this`.
    void Execute () {
        verified = proof.Test();
    }

    // Executed when the async work is complete
    // this function will be run inside the main event loop
    // so it is safe to use V8 again
    void HandleOKCallback () {
        HandleScope scope;

        Local<Value> argv[] = {
            Null(),
            New(verified)
        };

        callback->Call(2, argv);
    }

private:
    Proof proof;
    bool verified;
};

NAN_METHOD(Solve) {
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

    Callback *callback = new Callback(info[1].As<Function>());
    Handle<Object> object = Handle<Object>::Cast(info[0]);
    Handle<Value> nValue = object->Get(New("n").ToLocalChecked());
    Handle<Value> kValue = object->Get(New("k").ToLocalChecked());
    Handle<Value> personalValue = object->Get(New("personal").ToLocalChecked());
    Handle<Value> seedValue = object->Get(New("seed").ToLocalChecked());
    Handle<Value> nonceValue = object->Get(New("nonce").ToLocalChecked());
    Handle<Value> maxNoncesValue =
        object->Get(New("maxNonces").ToLocalChecked());

    const unsigned n = To<uint32_t>(nValue).FromJust();
    const unsigned k = To<uint32_t>(kValue).FromJust();
    uint8_t* personalBuffer = (uint8_t*)node::Buffer::Data(personalValue);
    size_t personalBufferLength = node::Buffer::Length(personalValue);
    size_t seedBufferLength = node::Buffer::Length(seedValue);
    uint8_t* seedBuffer = (uint8_t*)node::Buffer::Data(seedValue);
    size_t nonceBufferLength = node::Buffer::Length(nonceValue);
    uint8_t* nonceBuffer = (uint8_t*)node::Buffer::Data(nonceValue);
    const uint32_t maxNonces = To<uint32_t>(maxNoncesValue).FromJust();

    Equihash equihash(
            n, k,
            Personal(personalBuffer, personalBuffer + personalBufferLength),
            Seed(seedBuffer, seedBuffer + seedBufferLength),
            Nonce(nonceBuffer, nonceBuffer + nonceBufferLength),
            maxNonces);

    //printhex("seed", seedBuffer, bufferLength);

    AsyncQueueWorker(new EquihashSolveWorker(equihash, callback));
}

NAN_METHOD(Verify) {
    // ensure first argument is an object
    if(!info[0]->IsObject()) {
        Nan::ThrowTypeError("'options' must be an object");
        return;
    }

    Callback *callback = new Callback(info[1].As<Function>());
    // unbundle all data needed to check the proof
    Handle<Object> object = Handle<Object>::Cast(info[0]);
    Handle<Value> nValue = object->Get(New("n").ToLocalChecked());
    Handle<Value> kValue = object->Get(New("k").ToLocalChecked());
    Handle<Value> personalValue = object->Get(New("personal").ToLocalChecked());
    Handle<Value> seedValue = object->Get(New("seed").ToLocalChecked());
    Handle<Value> nonceValue = object->Get(New("nonce").ToLocalChecked());
    Handle<Array> solutionArray = Handle<Array>::Cast(object->Get(New("solution").ToLocalChecked()));

    const unsigned n = To<uint32_t>(nValue).FromJust();
    const unsigned k = To<uint32_t>(kValue).FromJust();
    uint8_t* personalBuffer = (uint8_t*)node::Buffer::Data(personalValue);
    size_t personalBufferLength = node::Buffer::Length(personalValue);
    uint8_t* nonceBuffer = (uint8_t*)node::Buffer::Data(nonceValue);
    size_t nonceBufferLength = node::Buffer::Length(nonceValue);
    uint8_t* seedBuffer = (uint8_t*)node::Buffer::Data(seedValue);
    size_t seedBufferLength = node::Buffer::Length(seedValue);

    //printhex("seed", seedBuffer, seedBufferLength);
    //printhex("input", inputBuffer, inputBufferLength);

    // initialize the proof object
    Personal personal(personalBuffer, personalBuffer + personalBufferLength);
    Seed seed(seedBuffer, seedBuffer + seedBufferLength);
    Nonce nonce(nonceBuffer, nonceBuffer + nonceBufferLength);
    Solution solution(solutionArray->Length());
    for(size_t i = 0; i < solution.size(); ++i) {
        solution[i] = solutionArray->Get(i)->NumberValue();
    }
    Proof proof(n, k, personal, seed, nonce, solution);

    AsyncQueueWorker(new EquihashVerifyWorker(proof, callback));
}

NAN_METHOD(VerifySync) {
    // ensure first argument is an object
    if(!info[0]->IsObject()) {
        Nan::ThrowTypeError("'options' must be an object");
        return;
    }

    // unbundle all data needed to check the proof
    Handle<Object> object = Handle<Object>::Cast(info[0]);
    Handle<Value> nValue = object->Get(New("n").ToLocalChecked());
    Handle<Value> kValue = object->Get(New("k").ToLocalChecked());
    Handle<Value> personalValue = object->Get(New("personal").ToLocalChecked());
    Handle<Value> seedValue = object->Get(New("seed").ToLocalChecked());
    Handle<Value> nonceValue = object->Get(New("nonce").ToLocalChecked());
    Handle<Array> solutionArray = Handle<Array>::Cast(object->Get(New("solution").ToLocalChecked()));

    const unsigned n = To<uint32_t>(nValue).FromJust();
    const unsigned k = To<uint32_t>(kValue).FromJust();
    uint8_t* personalBuffer = (uint8_t*)node::Buffer::Data(personalValue);
    size_t personalBufferLength = node::Buffer::Length(personalValue);
    uint8_t* nonceBuffer = (uint8_t*)node::Buffer::Data(nonceValue);
    size_t nonceBufferLength = node::Buffer::Length(nonceValue);
    uint8_t* seedBuffer = (uint8_t*)node::Buffer::Data(seedValue);
    size_t seedBufferLength = node::Buffer::Length(seedValue);

    // initialize the proof object
    Personal personal(personalBuffer, personalBuffer + personalBufferLength);
    Seed seed(seedBuffer, seedBuffer + seedBufferLength);
    Nonce nonce(nonceBuffer, nonceBuffer + nonceBufferLength);
    Solution solution(solutionArray->Length());
    for(size_t i = 0; i < solution.size(); ++i) {
        solution[i] = solutionArray->Get(i)->NumberValue();
    }
    Proof proof(n, k, personal, seed, nonce, solution);

    // check proof and and return result
    info.GetReturnValue().Set(proof.Test());
}

NAN_MODULE_INIT(InitAll) {
    Set(target, New<String>("solve").ToLocalChecked(),
            GetFunction(New<FunctionTemplate>(Solve)).ToLocalChecked());
    Set(target, New<String>("verify").ToLocalChecked(),
            GetFunction(New<FunctionTemplate>(Verify)).ToLocalChecked());
    Set(target, New<String>("verifySync").ToLocalChecked(),
            GetFunction(New<FunctionTemplate>(VerifySync)).ToLocalChecked());
}

NODE_MODULE(addon, InitAll)
