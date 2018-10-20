/**
 * Code by Dmitry Khovratovich, 2016
 * CC0 license
 */

#ifndef __POW
#define __POW

#include <stdint.h>

#include <vector>
#include <cstdio>

const int MAX_N = 32; //Max length of n in bytes, should not exceed 32
const int LIST_LENGTH = 5;
const unsigned FORK_MULTIPLIER = 3; //Maximum collision factor

/* The block used to initialize the PoW search
 * @v actual values
 */

/*
static void printhex(const char *title, const unsigned int *buf, size_t buf_len)
{
    size_t i = 0;
    fprintf(stdout, "%s length: %i\n", title, buf_len);
    for(i = 0; i < buf_len; ++i)
    fprintf(stdout, "0x%08x%s", buf[i],
             ( i + 1 ) % 4 == 0 ? "\r\n" : " " );
}
*/

/* Different nonces for PoW search
 * @v actual values
 */
typedef std::vector<uint8_t> Personal;
typedef std::vector<uint8_t> Seed;
typedef std::vector<uint8_t> Nonce;
typedef uint32_t Input;
typedef std::vector<uint32_t> Solution;

/* Actual proof of work */
struct Proof {
    unsigned n;
    unsigned k;
    Personal personal;
    Seed seed;
    Nonce nonce;
    Solution solution;
    Proof(unsigned n_v, unsigned k_v, Personal personal_v, Seed I_v, Nonce V_v, Solution solution_v):
        n(n_v), k(k_v), personal(personal_v), seed(I_v), nonce(V_v), solution(solution_v) {};
    Proof(const Proof &p):
        n(p.n), k(p.k), personal(p.personal), seed(p.seed), nonce(p.nonce), solution(p.solution) {};
    Proof():
        n(0), k(0), personal(Personal(0)), seed(Seed(0)), nonce(Nonce(0)), solution(Solution(0)) {};
    bool Test();

    Proof& operator=(const Proof &p) {
        n = p.n;
        k = p.k;
        personal = p.personal;
        seed = p.seed;
        nonce = p.nonce;
        solution = p.solution;
        return *this;
    }
};

class Tuple {
public:
    std::vector<uint32_t> blocks;
    Input reference;
    Tuple(unsigned i) { blocks.resize(i); }
    Tuple& operator=(const Tuple &r) {
        blocks = r.blocks;
        reference = r.reference;
        return *this;
    }
};

class Fork {
public:
    Input ref1, ref2;
    Fork() {};
    Fork(Input r1, Input r2) : ref1(r1), ref2(r2) {};
};

/* Algorithm class for creating proof
 * Assumes that n/(k+1) <= 32
 */
class Equihash {
    std::vector<std::vector<Tuple>> tupleList;
    std::vector<unsigned> filledList;
    std::vector<Proof> solutions;
    std::vector<std::vector<Fork>> forks;
    unsigned n;
    unsigned k;
    Personal personal;
    Seed seed;
    Nonce nonce;
    uint32_t maxNonces;

    void FillMemory(uint32_t length);      //fill with hash
    void InitializeMemory(); //allocate memory
    void ResolveCollisions(bool store);
    bool HasDistinctIndicies(Solution &solution);
    void OrderSolution(Solution &solution);
    void IncrementNonce();
    std::vector<Input> ResolveTree(Fork fork);
    std::vector<Input> ResolveTreeByLevel(Fork fork, unsigned level);
public:
    /*
       Initializes memory.
       */
    Equihash(unsigned n_in, unsigned k_in, Personal personal, Seed s, Nonce nonce, uint32_t maxNonces):
        n(n_in), k(k_in), personal(personal), seed(s), nonce(nonce), maxNonces(maxNonces) {};
    Equihash(const Equihash &eh):
        n(eh.n), k(eh.k), personal(eh.personal), seed(eh.seed), nonce(eh.nonce), maxNonces(eh.maxNonces) {};
    ~Equihash() {};
    Proof FindProof();
    void PrintTuples(FILE* fp);
};

#endif //define __POW
