#include <map>
#include <string>

#include <llvm/IR/CFG.h>
#include <llvm/IR/InstVisitor.h>
#include <llvm/IRReader/IRReader.h>
#include <llvm/Support/SourceMgr.h>
#include <llvm/Support/raw_ostream.h>
#include <z3++.h>

using namespace llvm;
using namespace z3;
using namespace std;

namespace {

// Get unique name of a LLVM node. Applicable to BasicBlock and Instruction.
std::string getName(const Value &Node) {
  if (!Node.getName().empty())
    return Node.getName().str();

  std::string Str;
  raw_string_ostream OS(Str);

  Node.printAsOperand(OS, false);
  return OS.str();
}

// Check
void checkAndReport(z3::solver &solver, const GetElementPtrInst &gep) {
  std::string name = getName(gep);
  std::cout << "Checking with assertions:" << std::endl
            << solver.assertions() << std::endl;
  if (solver.check() == z3::sat)
    std::cout << "GEP " << name << " is potentially out of bound." << std::endl
              << "Model causing out of bound:" << std::endl
              << solver.get_model() << std::endl;
  else
    std::cout << "GEP " << name << " is safe." << std::endl;
}
} // namespace

// ONLY MODIFY THIS CLASS FOR PART 1 & 2!
class Z3Walker : public InstVisitor<Z3Walker> {
private:
  std::map<std::string, std::map<std::string,z3::expr>> predicate_map;
  z3::context ctx;
  z3::solver solver;

public:
  Z3Walker() : ctx(), solver(ctx) {}

  // Not using InstVisitor::visit due to their sequential order.
  // We want topological order on the Call Graph and CFG.
  void visitModule(Module &M) {
    for(auto &f : M.getFunctionList()){
      visitFunction(f);
    }
  }

  /* traverse controlgraph
   * forward: from entry Block start dfs
   * when visit each Block
   * check first whether its precessors all visited
   * if, go ahead
   * record: conditions come from each z3::expr
  */
  
  void visitFunction(Function &F) {
    cout <<"Function "<<F.getName().str()<<endl;
    //reset
    solver.reset();
    predicate_map.clear();

    visitBasicBlock(F.getEntryBlock());
  }

  void visitBasicBlock(BasicBlock &B) {
    //ensure all predecessors have been visited
    if(predicate_map[getName(B)].size() != pred_size(&B)){
      return;
    }
    cout << "BasicBlock " << B.getName().str() <<endl;
    for(auto &inst : B.getInstList()){
      this->visit(inst);
    }
  }

  z3::expr valueToZ3(llvm::Value *value) {
    auto name = value->getName().str().c_str();
    int bit_width = value->getType()->getIntegerBitWidth();
    if(auto ci = dyn_cast<llvm::ConstantInt>(value)) {
      return ctx.bv_val(ci->getValue().getSExtValue(), bit_width);
    }else{
      return ctx.bv_const(name, bit_width);
    }
  }

  void visitZExtInst(ZExtInst &I) {
    auto src = valueToZ3(I.getOperand(0));
    auto expr = zext(src, getExtSize(I));
    solver.add(varToZ3(I) == expr);
  }

  void visitSExtInst(SExtInst &I) {
    auto src = valueToZ3(I.getOperand(0));
    auto expr = sext(src, getExtSize(I));
    solver.add(varToZ3(I) == expr);
  }

  static z3::expr binOpToZ3(Instruction::BinaryOps const &op, z3::expr const &p1, z3::expr const &p2) {
    switch(op) {
      case Instruction::BinaryOps::Add:   return p1 + p2;
      case Instruction::BinaryOps::Sub:   return p1 - p2;
      case Instruction::BinaryOps::Mul:   return p1 * p2;
      case Instruction::BinaryOps::Shl:   return shl(p1, p2);
      case Instruction::BinaryOps::LShr:  return lshr(p1, p2);
      case Instruction::BinaryOps::AShr:  return ashr(p1, p2);
      case Instruction::BinaryOps::And:   return p1 & p2;
      case Instruction::BinaryOps::Or:    return p1 | p2;
      case Instruction::BinaryOps::Xor:   return p1 ^ p2;
      default: throw std::runtime_error("Not supported binary operator");
    }
  }

  void visitBinaryOperator(BinaryOperator &I) {
    auto p1 = valueToZ3(I.getOperand(0));
    auto p2 = valueToZ3(I.getOperand(1));
    auto expr = binOpToZ3(I.getOpcode(), p1, p2);
    solver.add(varToZ3(I) == expr);
  }

  void visitICmp(ICmpInst &I) {
    auto p1 = valueToZ3(I.getOperand(0));
    auto p2 = valueToZ3(I.getOperand(1));
    auto cond = predicateToZ3(I.getPredicate(), p1, p2);
    auto expr = ite(cond, ctx.bv_val(1, 1), ctx.bv_val(0, 1));
    solver.add(varToZ3(I) == expr);
  }

  static unsigned getExtSize(CastInst const& I) {
    auto dst_sz = I.getDestTy()->getIntegerBitWidth();
    auto src_sz = I.getSrcTy()->getIntegerBitWidth();
    return dst_sz - src_sz;
  }

  z3::expr varToZ3(llvm::Instruction const &I) {
    auto name = I.getName().str().c_str();
    auto size = I.getType()->getIntegerBitWidth();
    return ctx.bv_const(name, size);
  }

  static z3::expr predicateToZ3(CmpInst::Predicate const &p, z3::expr const &p1, z3::expr const &p2) {
    switch(p) {
      case CmpInst::Predicate::ICMP_EQ:  return p1 == p2;
      case CmpInst::Predicate::ICMP_NE:  return p1 != p2;
      case CmpInst::Predicate::ICMP_ULT: return p1 <  p2;
      case CmpInst::Predicate::ICMP_ULE: return p1 <= p2;
      case CmpInst::Predicate::ICMP_UGT: return p1 >  p2;
      case CmpInst::Predicate::ICMP_UGE: return p1 >= p2;
      case CmpInst::Predicate::ICMP_SLT: return p1 <  p2;
      case CmpInst::Predicate::ICMP_SLE: return p1 <= p2;
      case CmpInst::Predicate::ICMP_SGT: return p1 >  p2;
      case CmpInst::Predicate::ICMP_SGE: return p1 >= p2;
      default: throw std::runtime_error("Not supported predicate");
    }
  }

  z3::expr merge_predicate(BasicBlock const& B) {
    if(predicate_map[getName(B)].empty()) {
      return ctx.bool_val(true);  // then && ...
    }
    auto e = ctx.bool_val(false);
    for(auto const& pair: predicate_map[getName(B)]) {
      e = e || pair.second;
    }
    return e.simplify();
  }

  void visitBranchInst(BranchInst &I) {
    auto bb = getName(*I.getParent());
    auto predicate = merge_predicate(*I.getParent());

    if(I.isConditional()) {
      auto cmp = valueToZ3(I.getOperand(0));
      auto &tb = *I.getSuccessor(0);
      auto &fb = *I.getSuccessor(1);
      predicate_map[getName(tb)].insert(std::make_pair(bb, predicate && (cmp == 1)));
      visitBasicBlock(tb);
      predicate_map[getName(fb)].insert(std::make_pair(bb, predicate && (cmp == 0)));
      visitBasicBlock(fb);
    } else {
      assert(bb != "entry");
      auto &b = *I.getSuccessor(0);
      predicate_map[getName(b)].insert(std::make_pair(bb, predicate));
      visitBasicBlock(b);
    }
  }

  void visitPHINode(PHINode &I) {
    auto dst = varToZ3(I);

    for(auto bb: I.blocks()) {
      auto const& pred = predicate_map[getName(*I.getParent())].find(getName(*bb))->second;
      auto src = valueToZ3(I.getIncomingValueForBlock(bb));
      auto e = z3::implies(pred, dst == src);
      solver.add(e);
    }
  }

  // Call checkAndReport here.
  void visitGetElementPtrInst(GetElementPtrInst &I) {
    // only check 'inbounds' && type as [N x i32]
    if(!I.isInBounds())
        return;
    if(!I.getSourceElementType()->getArrayElementType()->isIntegerTy(32))
        return;
    solver.push();
    {
      auto pred = merge_predicate(*I.getParent());
      solver.add(pred);

      auto len = (int)I.getSourceElementType()->getArrayNumElements();
      auto idx = valueToZ3(I.getOperand(2));
      auto out_of_bound = idx < 0 || idx >= len;
      solver.add(out_of_bound);

      checkAndReport(solver, I);
    }
    solver.pop();
  }
};

int main(int argc, char const *argv[]) {
  if (argc < 2) {
    errs() << "Usage: " << argv[0] << " <IR file>\n";
    return 1;
  }

  LLVMContext llvmctx;

  // Parse the input LLVM IR file into a module.
  SMDiagnostic Err;
  auto module = parseIRFile(argv[1], Err, llvmctx);
  if (!module) {
    Err.print(argv[0], errs());
    return 1;
  }

  Z3Walker().visitModule(*module);

  return 0;
}



