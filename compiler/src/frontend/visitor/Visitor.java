package frontend.visitor;

import frontend.Error;
import frontend.grammar.*;
import frontend.grammar.decl.Decl;
import frontend.grammar.decl.def.Def;
import frontend.grammar.decl.def.Variable;
import frontend.grammar.decl.def.init.Init;
import frontend.grammar.decl.def.init.Vector;
import frontend.grammar.exp.*;
import frontend.grammar.exp.condExp.*;
import frontend.grammar.funcDef.FuncDef;
import frontend.grammar.funcDef.FuncFParam;
import frontend.grammar.funcDef.FuncType;
import frontend.grammar.stmt.*;
import frontend.symbol.*;
import frontend.token.Token;
import middle.BasicBlock;
import middle.FuncDefBlock;
import middle.MiddleTn;
import middle.VarName;
import middle.quartercode.*;
import middle.quartercode.array.*;
import middle.quartercode.branch.JumpCmp;
import middle.quartercode.branch.SaveCmp;
import middle.quartercode.function.FParaCode;
import middle.quartercode.function.FuncCallCode;
import middle.quartercode.function.FuncDefCode;
import middle.quartercode.function.RParaCode;
import middle.quartercode.operand.primaryOpd.LValOpd;
import middle.quartercode.operand.primaryOpd.Immediate;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.PrimaryOpd;
import middle.quartercode.operand.primaryOpd.RetOpd;

import java.util.*;

public class Visitor {
    private SymTable curSymTable;
    private SymTable globalSymTable;

    private BasicBlock curBBlock;
    private BasicBlock globalBlock;

    private HashMap<String, FuncDefBlock> funcDefBBlocksMap;
    private ArrayList<FuncDefBlock> funcDefBlocks;
    private FuncDefBlock curFuncDefBb;
    private MiddleTn middleTn;
    private HashMap<String, ConstStrCode> constStr;
    private HashMap<VarName, MiddleCode> globalVars;
    private HashMap<String, VarName> globalName2VarName;
    private int constStringCnt = 0;

    // private int blockDepth = 0;

    // private HashSet<VarName> varInWhileCond = new HashSet<>();

    public Visitor() {
        this.curSymTable = new SymTable(null);
        this.globalSymTable = curSymTable;

        this.curBBlock = new BasicBlock("GLOBAL_", BasicBlock.BBType.GLOBAL, 0);
        this.globalBlock = curBBlock;

        this.globalVars = new HashMap<>();
        this.globalName2VarName = new HashMap<>();
        this.middleTn = new MiddleTn();
        this.constStr = new HashMap<>();
        this.funcDefBBlocksMap = new HashMap<>();
        this.funcDefBlocks = new ArrayList<>();
    }

    public void visitCompUnit(CompUnit compUnit) {
        //  CompUnit → {ConstDecl | VarDecl} {FuncDef} MainFuncDef
        for (Decl decl : compUnit.getDecls()) {
            visitDecl(decl);
        }
        for (FuncDef funcDef : compUnit.getFuncDefs()) {
            visitFuncDef(funcDef);
        }
        visitMainFuncDef(compUnit.getMainFuncDef());
    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
        // blockDepth = 1;
        // curBBlock.setDirectBb(newBb);
        // curBBlock = new BasicBlock("main", BasicBlock.BBType.FUNC, blockDepth);
        curBBlock = new BasicBlock("main", BasicBlock.BBType.FUNC, 1);
        FuncDefBlock funcDefBlock = new FuncDefBlock("main", curBBlock);
        funcDefBBlocksMap.put(funcDefBlock.getLable(), funcDefBlock);
        funcDefBlocks.add(funcDefBlock);
        curFuncDefBb = funcDefBlock;

        // 如果有 int main = 1;   那么？ todo think
        //  MainFuncDef → 'int' 'main' '(' ')' Block
        if (curSymTable.hasSymbol("main")) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                    mainFuncDef.getMainTK().getRow()));
        }
        SymTable newTable = new SymTable(curSymTable);
        curBBlock.append(new FuncDefCode(new VarName("main", 0), new FuncType(mainFuncDef.getIntTK())));
        visitBlock(mainFuncDef.getBlock(), newTable, null, null);
    }

    private void visitFuncDef(FuncDef funcDef) {
        // blockDepth = 1;
        // curBBlock.setDirectBb(newBb);
        curBBlock = new BasicBlock(funcDef.getName(), BasicBlock.BBType.FUNC, 1);
        FuncDefBlock funcDefBlock = new FuncDefBlock(funcDef.getName(), curBBlock);
        funcDefBBlocksMap.put(funcDefBlock.getLable(), funcDefBlock);
        funcDefBlocks.add(funcDefBlock);
        curFuncDefBb = funcDefBlock;

        //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncSymbol funcSymbol = new FuncSymbol(funcDef.getFuncType(), funcDef.getName());
        // 函数名  ->  符号表
        if (curSymTable.hasSymbol(funcSymbol.getSymName())) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF, funcDef.getNameRow()));
        } else {
            curSymTable.addSymbol(funcSymbol);
        }
        curBBlock.append(new FuncDefCode(new VarName(funcDef.getName(), 0), funcDef.getFuncType())); // todo 我想不要这个东西，我的BasicBlock还没整呢
        SymTable newSymTable = new SymTable(curSymTable);
        for (FuncFParam funcFParam : funcDef.getFuncFParams()) {
            // todo modified 别改，出错了qwq
            // 如果函数的调用，实参是调用函数的参数就出bug
            int type = funcFParam.getType();
            int colNum = type == 0 ? -1 : 0;
            if (type == 2) {
                ArrayList<Operand> operands = analyseExpression(funcFParam.getConstExp());
                assert operands.size() == 1;
                colNum = ((Immediate) operands.get(0)).getValue();
            }
            VarSymbol varSymbol = new VarSymbol(funcFParam.getIdent().getContent(), funcFParam.getType(), colNum, 1);
            if (newSymTable.hasSymbol(funcFParam.getIdent().getContent())) {
                Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                        funcFParam.getIdent().getRow()));
            } else {
                newSymTable.addSymbol(varSymbol);
            }
            funcSymbol.addFParam(new FParamSymbol(funcFParam.getIdent().getContent(),
                    funcFParam.getType(), colNum, curBBlock.getDepth()));
            // middle part
            // FParaCode fParaCode = new FParaCode(new VarName(funcFParam.getIdent().getContent(), curBBlock.getDepth(), -1));//colNum));//new Immediate(colNum)));
            FParaCode fParaCode = new FParaCode(new VarName(funcFParam.getIdent().getContent(), curBBlock.getDepth(), colNum, funcFParam.getType() != 0),   //todo array check
                    funcFParam.getType() != 0, new Immediate(colNum));
            curBBlock.append(fParaCode);
            curFuncDefBb.addLocalVar(fParaCode, false);
        }
        visitBlock(funcDef.getBlock(), newSymTable, null, null);
    }

    private void visitBlock(Block block, SymTable newTable, String loopBegin, String loopEnd) {
        //  Block → '{' { BlockItem } '}'
        middleTn.clear();
        curSymTable = newTable;
        for (BlockItem blockItem : block.getBlockItems()) {
            if (blockItem.isDecl()) {
                visitDecl(blockItem.getDecl());
            } else {
                if (blockItem.getStmt() instanceof Block) {
                    createNewCurBlock("BASIC_", 1, BasicBlock.BBType.BASIC);
                }
                visitStmt(blockItem.getStmt(), loopBegin, loopEnd);
                if (blockItem.getStmt() instanceof Block) {
                    createNewCurBlock("BASIC_NEXT_", -1, BasicBlock.BBType.BASIC);
                }
            }
        }
        curSymTable = curSymTable.getParent();
    }

    private void visitStmt(Stmt stmt, String loopBeginLabel, String loopEndLabel) {
        // middleTn.clear();
        if (stmt instanceof Block) {
            SymTable newTable = new SymTable(curSymTable);
            visitBlock((Block) stmt, newTable, loopBeginLabel, loopEndLabel);
            // block出来之后要新建一个bb

        } else if (stmt instanceof ExpStmt && ((ExpStmt) stmt).getExp() != null) {
            AddExp addExp = ((ExpStmt) stmt).getExp().getAddExp();
            visitAddExp((addExp));
            // middle part
            ArrayList<Operand> operands = analyseAddExp(addExp);
            // todo check primaryOpd
            if (getLast(operands) instanceof PrimaryOpd) {
                // 如果是 单一的 应该 没什么用吧。
                System.out.println("visitor:visitStmt:" + getLast(operands));
                operands.remove(getLast(operands));
            }
            for (Operand operand : operands) {
                curBBlock.append((MiddleCode) operand);
            }

        } else if (stmt instanceof LvalStmt) {
            LVal lVal = ((LvalStmt) stmt).getlVal();
            visitLVal(lVal, true);
            if (!((LvalStmt) stmt).isGetInt()) {
                AddExp addExp = ((LvalStmt) stmt).getExp().getAddExp();
                visitAddExp(addExp);
                // middle part
                ArrayList<Operand> expOps = analyseAddExp(addExp);
                ArrayList<Operand> lValOps = analyseLVal(lVal);

                // todo check primaryOpd
                Operand lastOfExp = getLast(expOps);
                if (lastOfExp instanceof PrimaryOpd) {
                    expOps.remove(lastOfExp);
                }
                for (Operand operand : expOps) {
                    curBBlock.append((MiddleCode) operand);
                }

                Operand lastOfLVal = getLast(lValOps);
                assert lastOfLVal instanceof LValOpd;
                lValOps.remove(lastOfLVal);
                for (Operand operand : lValOps) {
                    curBBlock.append((MiddleCode) operand);
                }

                if (((LValOpd) lastOfLVal).getIdx() == null) {
                    if (lastOfExp instanceof LValOpd && ((LValOpd) lastOfExp).getIdx() != null) {
                        // b = a[t0];
                        curBBlock.append(new ArrayLoad((PrimaryOpd) lastOfExp, lastOfLVal));
                    } else if (lastOfExp instanceof PrimaryOpd/*不会是数组*/) {
                        curBBlock.append(new AssignCode(lastOfLVal.getVarName(), lastOfExp));
                    } else {
                        // 删除 b = t0这样的式子，直接rename t0
                        curBBlock.reassignMiddleCode((MiddleCode) lastOfExp, lastOfLVal.getVarName());
                    }
                } else {
                    if (lastOfExp instanceof LValOpd && ((LValOpd) lastOfExp).getIdx() != null) {
                        // b[t1] = a[t0];
                        // ->
                        // t2 = a[t0]
                        // b[t1] = t2
                        LValOpd dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                        curFuncDefBb.addLocalVar(dst, true);
                        MiddleCode t2 = new ArrayLoad((PrimaryOpd) lastOfExp, dst);
                        curBBlock.append(t2);
                        curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, t2));
                    } else {
                        curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, lastOfExp));
                    }
                }
            } else {
                MiddleCode t1 = new ReadIn(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                curFuncDefBb.addLocalVar(t1, true);
                curBBlock.append(t1);
                ArrayList<Operand> lValOps = analyseLVal(lVal);
                Operand lastOfLVal = getLast(lValOps);
                assert lastOfLVal instanceof LValOpd;
                lValOps.remove(lastOfLVal);
                for (Operand operand : lValOps) {
                    curBBlock.append((MiddleCode) operand);
                }
                if (((LValOpd) lastOfLVal).getIdx() == null) {
                    // curBBlock.append(new AssignCode(lastOfLVal.getLocalName(), t1)); todo check
                    // t1.rename(lastOfLVal.getVarName());
                    curBBlock.reassignMiddleCode(t1, lastOfLVal.getVarName());
                } else {
                    curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, t1));
                }
            }
        } else if (stmt instanceof WhileStmt) {
            createNewCurBlock("LOOP_", 1, BasicBlock.BBType.LOOP);
            BasicBlock loopBegin = curBBlock;
            BasicBlock whileStmtBlock = new BasicBlock("WHILE_STMT_", BasicBlock.BBType.BRANCH, curBBlock.getDepth());
            BasicBlock loopEndStmt = new BasicBlock("LOOP_END_", BasicBlock.BBType.BRANCH, curBBlock.getDepth() - 1);
            // varInWhileCond = new HashSet<>();
            analyseLOrExp(((WhileStmt) stmt).getCond().getlOrExp(), whileStmtBlock, null, loopEndStmt);
            // HashSet<VarName> varStores = varInWhileCond;
            // 分析 whileStmt
            switch2DirectNextBlock(whileStmtBlock);
            visitStmt(((WhileStmt) stmt).getStmt(), loopBegin.getLable(), loopEndStmt.getLable());

            // for (VarName varName : varStores) {
            //     curBBlock.append(new VarStore(varName));
            // }
            curBBlock.append(new JumpCmp(null, null, JumpCmp.JumpType.GOTO, loopBegin.getLable()));
            // while 结束
            switch2DirectNextBlock(loopEndStmt);
        } else if (stmt instanceof BreakOrContinueStmt) {
            // todo 这儿也有一个基本块
            if (loopBeginLabel == null) {
                Error.errorTable.add(new Error(Error.ErrorType.WRONG_BREAK_CONTINUE, ((BreakOrContinueStmt) stmt).getRow()));
            } else {
                String jumpLabel = ((BreakOrContinueStmt) stmt).isBreak() ? loopEndLabel : loopBeginLabel;
                curBBlock.append(new JumpCmp(null, null, JumpCmp.JumpType.GOTO, jumpLabel));
                createNewCurBlock("FOLLOW_GOTO_", 0, BasicBlock.BBType.FOLLOWGOTO);
            }
        } else if (stmt instanceof IfStmt) {
            // todo 这儿也有一个基本块
            createNewCurBlock("IF_", 1, BasicBlock.BBType.BRANCH);
            // analyseLOrExp(LOrExp lOrExp, BasicBlock ifStmtBlock, BasicBlock elseStmtBlock, BasicBlock endIfBlock)
            BasicBlock ifStmtBlock = new BasicBlock("IF_STMT_", BasicBlock.BBType.BRANCH, curBBlock.getDepth());
            BasicBlock elseStmtBlock = null;
            if (((IfStmt) stmt).hasElse()) {
                elseStmtBlock = new BasicBlock("ELSE_", BasicBlock.BBType.BRANCH, curBBlock.getDepth());    //todo if 和 else 的基本块深度相同,考虑"相同"变量我是怎么找的
            }
            BasicBlock endIfBlock = new BasicBlock("END_IF_", BasicBlock.BBType.BRANCH, curBBlock.getDepth() - 1);

            analyseLOrExp(((IfStmt) stmt).getCond().getlOrExp(), ifStmtBlock, elseStmtBlock, endIfBlock);
            switch2DirectNextBlock(ifStmtBlock);    // 之前在IF_ 下
            visitStmt(((IfStmt) stmt).getStmt(), loopBeginLabel, loopEndLabel);
            if (((IfStmt) stmt).hasElse()) {
                curBBlock.append(new JumpCmp(null, null, JumpCmp.JumpType.GOTO, endIfBlock.getLable()));
                switch2DirectNextBlock(elseStmtBlock);
                visitStmt(((IfStmt) stmt).getElseStmt(), loopBeginLabel, loopEndLabel);
            }
            switch2DirectNextBlock(endIfBlock);
        } else if (stmt instanceof PrintfStmt) {
            PrintfStmt printfStmt = (PrintfStmt) stmt;
            String inner = printfStmt.getFormatString().getInner();
            ArrayList<Integer> formatChars = printfStmt.getFormatString().getFormatChars();
            Iterator<Exp> expIter = printfStmt.getExps().iterator();
            ArrayList<Operand> subBB = new ArrayList<>();
            ArrayList<Operand> printStrings = new ArrayList<>();
            ArrayList<Operand> printExps = new ArrayList<>();
            int start = 0;
            for (int end : formatChars) {
                if (start != end) {
                    ConstStrCode str;
                    String sub = inner.substring(start, end);
                    if (!constStr.containsKey(sub)) {
                        str = new ConstStrCode(new VarName("str_" + constStringCnt++, 0), sub);
                        constStr.put(sub, str);
                    }
                    str = constStr.get(sub);
                    printStrings.add(new PutOut(str));
                }
                start = end + 2;
            }
            if (start < inner.length()) {
                ConstStrCode str;
                String sub = inner.substring(start);
                if (!constStr.containsKey(sub)) {
                    str = new ConstStrCode(new VarName("str_" + constStringCnt++, 0), sub);
                    constStr.put(sub, str);
                }
                str = constStr.get(sub);
                printStrings.add(new PutOut(str));
            }

            for (int i = printfStmt.getExpNum() - 1; i >= 0; i--) {
                ArrayList<Operand> operands = analyseExpression(printfStmt.getExps().get(i));
                Operand lastOne = getLast(operands);
                if (lastOne instanceof PrimaryOpd) {
                    operands.remove(lastOne);
                    if (lastOne instanceof RetOpd) {
                        lastOne = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), lastOne);
                        operands.add(lastOne);
                    }
                }
                subBB.addAll(operands);
                printExps.add(0, new PutOut(lastOne));
            }
            Iterator<Operand> iterator = printExps.iterator();
            if (!formatChars.isEmpty()) {
                if (formatChars.get(0) == 0) {
                    subBB.add(iterator.next());
                }
                for (Operand str : printStrings) {
                    subBB.add(str);
                    if (iterator.hasNext()) {
                        subBB.add(iterator.next());
                    }
                }
            } else {
                assert printStrings.size() == 1;
                subBB.addAll(printStrings);
            }
            /*assert expIter.hasNext();
            Exp exp = expIter.next();
            ArrayList<Operand> operands = analyseExpression(exp);
            Operand lastOne = getLast(operands);
            if (lastOne instanceof PrimaryOpd) {
                operands.remove(lastOne);
                if (lastOne instanceof RetOpd) {
                    lastOne = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), lastOne);
                    operands.add(lastOne);
                }
            }
            subBB.addAll(operands);
            subBB.add(new PutOut(lastOne));*/
            for (Operand operand : subBB) {
                curBBlock.append((MiddleCode) operand);
            }
        } else if (stmt instanceof ReturnStmt) {
            if (((ReturnStmt) stmt).hasExp()) {
                Exp exp = ((ReturnStmt) stmt).getExp();
                ArrayList<Operand> operands = analyseAddExp(exp.getAddExp());
                Operand lastOne = getLast(operands);
                if (lastOne instanceof PrimaryOpd) {
                    operands.remove(lastOne);
                }
                for (Operand operand : operands) {
                    curBBlock.append((MiddleCode) operand);
                }
                curBBlock.append(new RetCode(lastOne));
            } else {
                curBBlock.append(new RetCode(null));
            }
            // 切换基本块
            createNewCurBlock("AFTER_RET_", 0, BasicBlock.BBType.BASIC);
        }
    }

    private void createNewCurBlock(String bbLabel, int step, BasicBlock.BBType bbType) {
        BasicBlock newBb = new BasicBlock(bbLabel, bbType, curBBlock.getDepth() + step);
        switch2DirectNextBlock(newBb);
    }

    private void switch2DirectNextBlock(BasicBlock directNext) {
        curBBlock.setDirectBb(directNext);
        curBBlock = directNext;
        curFuncDefBb.addBb(curBBlock);
    }

    private void analyseCond(Cond cond) {
        LOrExp lOrExp = cond.getlOrExp();

    }

    private void analyseLOrExp(LOrExp lOrExp, BasicBlock ifStmtBlock, BasicBlock elseStmtBlock, BasicBlock endIfBlock) {
        //  LOrExp → LAndExp | LOrExp '||' LAndExp

        Iterator<LAndExp> lAndExpIter = lOrExp.getlAndExps().iterator();
        while (lAndExpIter.hasNext()) {
            LAndExp lAndExp = lAndExpIter.next();
            BasicBlock nextCondOrBlock = lAndExpIter.hasNext() ?    // hasOr ?
                    new BasicBlock("COND_OR_", BasicBlock.BBType.BRANCH, curBBlock.getDepth()) :
                    null;

            analyseLAndExp(lAndExp, nextCondOrBlock, ifStmtBlock, elseStmtBlock, endIfBlock);
            // if (nextCondOrBlock != null) {
            //     // 循环继续
            //     createNewCurBlock("JUMP_", 0, BasicBlock.BBType.BRANCHGOTO);
            //     curBBlock.append(new JumpCmp(null, null, JumpCmp.JumpType.GOTO, ifStmtBlock.getLable()));
            //     switch2DirectNextBlock(nextCondOrBlock);
            // }
        }
    }

    private void analyseLAndExp(LAndExp lAndExp, BasicBlock nextCondOrBlock, BasicBlock ifStmtBlock, BasicBlock elseStmtBlock, BasicBlock endIfBlock) {
        //   LAndExp → EqExp | LAndExp '&&' EqExp
        Iterator<EqExp> eqExpIter = lAndExp.getEqExps().iterator();
        while (eqExpIter.hasNext()) {
            EqExp eqExp = eqExpIter.next();
            BasicBlock nextCondAndBlock = eqExpIter.hasNext() ?
                    new BasicBlock("COND_AND_", BasicBlock.BBType.BRANCH, curBBlock.getDepth()) :
                    null;

            BasicBlock unSatisfiedJump = nextCondOrBlock != null ? nextCondOrBlock :
                    elseStmtBlock != null ? elseStmtBlock :
                            endIfBlock;
            analyseEqExp(eqExp, unSatisfiedJump);
            if (nextCondAndBlock != null) {
                // 循环继续
                switch2DirectNextBlock(nextCondAndBlock);
            }
        }
        // LAndCond 条件满足, 执行 ifStmt
        if (nextCondOrBlock != null) {
            // Cond未解析完, 需要跳转
            createNewCurBlock("JUMP_", 0, BasicBlock.BBType.BRANCHGOTO);
            curBBlock.append(new JumpCmp(null, null, JumpCmp.JumpType.GOTO, ifStmtBlock.getLable()));
            switch2DirectNextBlock(nextCondOrBlock);
        }
    }

    private void analyseEqExp(EqExp eqExp, BasicBlock jump) {
        //  EqExp → RelExp | EqExp ('==' | '!=') RelExp

        ArrayList<RelExp> relExps = eqExp.getRelExps();
        Iterator<RelExp> relExpIter = relExps.iterator();
        Iterator<CondOp> condOpIter = eqExp.getCondOps().iterator();
        CondOp condOp;
        RelExp relExp = relExpIter.next();

        ArrayList<Operand> operands1 = new ArrayList<>(analyseRelExp(relExp));
        Operand left = getLast(operands1), right;
        if (left instanceof PrimaryOpd) {
            operands1.remove(left);
            if (left instanceof LValOpd && ((LValOpd) left).isArray()) {
                left = new ArrayLoad((PrimaryOpd) left, new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth())));
                operands1.add(left);
                curFuncDefBb.addLocalVar(left, true);
            }
        }

        if (!relExpIter.hasNext()) {
            if (left instanceof PrimaryOpd) {
                if (left instanceof Immediate) {
                    assert operands1.size() == 0;
                    if (((Immediate) left).getValue() == 0) {
                        operands1.add(new JumpCmp(null, null, JumpCmp.JumpType.GOTO, jump.getLable()));
                    }
                } else if (left instanceof LValOpd && ((LValOpd) left).isArray()) {
                    assert false;
                    /*MiddleCode middleCode = new ArrayLoad((PrimaryOpd) left, new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth())));
                    operands1.add(middleCode);
                    curFuncDefBb.addLocalVar(middleCode, true);
                    operands1.add(new JumpCmp(middleCode, null, JumpCmp.JumpType.BEQZ, jump.getLable()));*/
                } else {
                    // left可能是RetOpd
                    operands1.add(new JumpCmp(left, null, JumpCmp.JumpType.BEQZ, jump.getLable()));
                }
            } else if (!(left instanceof SaveCmp)) {
                // 其他middleCode
                operands1.add(new JumpCmp(left, null, JumpCmp.JumpType.BEQZ, jump.getLable()));
            } else {
                // left instanceof SaveCmp
                operands1.remove(left);
                SaveCmp saveCmp = (SaveCmp) left;
                JumpCmp.JumpType jumpType;
                switch (((SaveCmp) left).getCmpType()) {
                    case SLT:
                    case SLTI:
                        jumpType = JumpCmp.JumpType.BGE;
                        break;
                    case SLE:
                        jumpType = JumpCmp.JumpType.BGT;
                        break;
                    case SEG:
                        jumpType = JumpCmp.JumpType.BLT;
                        break;
                    case SGT:
                        jumpType = JumpCmp.JumpType.BLE;
                        break;
                    case SEQ:
                        jumpType = JumpCmp.JumpType.BNE;
                        break;
                    case SNE:
                        jumpType = JumpCmp.JumpType.BEQ;
                        break;
                    default:
                        jumpType = null;
                        break;
                }
                operands1.add(new JumpCmp(saveCmp.getCmpOp1(), saveCmp.getCmpOp2(), jumpType, jump.getLable()));
            }
        }/* else if (left instanceof Immediate) {
            left = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()),left);
            curFuncDefBb.addLocalVar(left, true);
        }*/

        for (Operand operand : operands1) {
            curBBlock.append((MiddleCode) operand);
        }
        ArrayList<Operand> operands2;
        while (relExpIter.hasNext()) {
            // gen right
            relExp = relExpIter.next();
            operands2 = new ArrayList<>(analyseRelExp(relExp));
            right = getLast(operands2);
            if (right instanceof PrimaryOpd) {
                operands2.remove(right);
                if (right instanceof LValOpd && ((LValOpd) right).isArray()) {
                    right = new ArrayLoad((PrimaryOpd) right, new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth())));
                    operands2.add(right);
                    curFuncDefBb.addLocalVar(right, true);
                }
            }
            if (left instanceof RetOpd && right instanceof RetOpd) {
                left = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), left);
                curBBlock.append((MiddleCode) left);
                curFuncDefBb.addLocalVar(left, true);
            }

            for (Operand operand : operands2) {
                curBBlock.append((MiddleCode) operand);
            }
            // get condOp
            condOp = condOpIter.next();
            Token.Type type = condOp.getCondOp().getRefType();

            // JumpCmp
            if (left instanceof Immediate && right instanceof Immediate) {
                // 直接计算结果
                boolean lEqR = ((Immediate) left).getValue() == ((Immediate) right).getValue();
                boolean condFalse = false;
                if (type == Token.Type.EQL && !lEqR || type == Token.Type.NEQ && lEqR) {
                    condFalse = true;
                }
                if (!condOpIter.hasNext()) {
                    if (condFalse) {
                        // 条件不满足, 跳转
                        curBBlock.append(new JumpCmp(null, null, JumpCmp.JumpType.GOTO, jump.getLable()));
                    }
                } else {
                    left = new Immediate(condFalse ? 0 : 1);
                }
            } else {
                if (left instanceof Immediate) {
                    Operand tmp = left;
                    left = right;
                    right = tmp;
                }

                if (condOpIter.hasNext()) {
                    // SaveCmp
                    Operand t1 = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                    curFuncDefBb.addLocalVar(t1, true);

                    SaveCmp.CmpType cmpType = type == Token.Type.EQL ? SaveCmp.CmpType.SEQ : SaveCmp.CmpType.SNE;
                    t1 = new SaveCmp(t1, left, right, cmpType);
                    curBBlock.append((MiddleCode) t1);
                    left = t1;
                } else {
                    JumpCmp.JumpType jumpType = type == Token.Type.EQL ? JumpCmp.JumpType.BNE : JumpCmp.JumpType.BEQ;
                    curBBlock.append(new JumpCmp(left, right, jumpType, jump.getLable()));
                }
            }
        }
    }

    private ArrayList<Operand> analyseRelExp(RelExp relExp) {
        // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        ArrayList<Operand> operands = new ArrayList<>();

        // assert relExp.getCondExps().size() > 1;
        Iterator<CondExp> condExpIter = relExp.getCondExps().iterator();
        Iterator<CondOp> condOpIter = relExp.getCondOps().iterator();
        ArrayList<Operand> operands1 = analyseAddExp((AddExp) condExpIter.next());
        Operand left = getLast(operands1);
        // System.out.println(left);
        if (left instanceof PrimaryOpd && condOpIter.hasNext()) {
            operands1.remove(left);
        }
        operands.addAll(operands1);

        ArrayList<Operand> operands2;
        Operand right;
        while (condOpIter.hasNext()) {
            // gen right
            operands2 = analyseAddExp((AddExp) condExpIter.next());
            right = getLast(operands2);
            if (right instanceof PrimaryOpd) {
                operands2.remove(right);
                if (left instanceof RetOpd && right instanceof RetOpd) {
                    left = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), left);
                    operands.add(left);
                    curFuncDefBb.addLocalVar(left, true);
                }
            }
            operands.addAll(operands2);
            // get op
            CondOp condOp = condOpIter.next();
            Operand t1;

            if (left instanceof Immediate && right instanceof Immediate) {
                // 直接计算结果
                switch (condOp.getCondOp().getRefType()) {
                    case LEQ:
                        t1 = new Immediate(((Immediate) left).getValue() <= ((Immediate) right).getValue() ? 1 : 0);
                        break;
                    case LSS:
                        t1 = new Immediate(((Immediate) left).getValue() < ((Immediate) right).getValue() ? 1 : 0);
                        break;
                    case GEQ:
                        t1 = new Immediate(((Immediate) left).getValue() >= ((Immediate) right).getValue() ? 1 : 0);
                        break;
                    case GRE:
                        t1 = new Immediate(((Immediate) left).getValue() > ((Immediate) right).getValue() ? 1 : 0);
                        break;
                    default:
                        t1 = null;
                        break;
                }
                if (!condExpIter.hasNext()) {
                    operands.add(t1);
                }
            } else {
                boolean rightIsImm = false;
                boolean swapped = false;
                if (left instanceof Immediate) {
                    Operand tmp = left;
                    left = right;
                    right = tmp;
                    swapped = true;
                }
                if (right instanceof Immediate) {
                    rightIsImm = true;
                }
                // SaveCmp
                t1 = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                curFuncDefBb.addLocalVar(t1, true);

                SaveCmp.CmpType cmpType;
                switch (condOp.getCondOp().getRefType()) {
                    case LEQ:   // <=
                        cmpType = swapped ? SaveCmp.CmpType.SEG : SaveCmp.CmpType.SLE;
                        break;
                    case GEQ:   // >=
                        cmpType = swapped ? SaveCmp.CmpType.SLE : SaveCmp.CmpType.SEG;
                        break;
                    case LSS:   // <
                        cmpType = rightIsImm ? (swapped ? SaveCmp.CmpType.SGT : SaveCmp.CmpType.SLTI) : SaveCmp.CmpType.SLT;
                        break;
                    case GRE:   // >
                        cmpType = swapped ? SaveCmp.CmpType.SLTI : SaveCmp.CmpType.SGT;
                        break;
                    default:
                        cmpType = null;
                        break;
                }
                operands.add(new SaveCmp(t1, left, right, cmpType));
            }
            // left = SaveCmp:t1
            left = t1;
        }
        return operands;
    }

    private void visitLVal(LVal lval, boolean isLeft) {
        //  NAME_UNDEF('c'),  MODIFY_CONST('h')
        Symbol lvalSym = getSymbol(lval.getIdent().getContent());
        if (lvalSym == null) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_UNDEF,
                    lval.getIdent().getRow()));
        } else {
            // todo 除了可能是varSymbol以外，还有可能是函数参数。
            if (lvalSym instanceof VarSymbol && ((VarSymbol) lvalSym).isConst() && isLeft) {
                Error.errorTable.add(new Error(Error.ErrorType.MODIFY_CONST,
                        lval.getIdent().getRow()));
            }
        }
    }

    private void visitAddExp(AddExp addExp) {
        // constExp, Exp, AddExp 直接来啦
        //  AddExp → MulExp | AddExp ('+' | '−') MulExp
        for (Expression exp : addExp.getExpressions()) {
            MulExp mulExp = (MulExp) exp;
            visitMulExp(mulExp);
        }
    }

    private void visitMulExp(MulExp mulExp) {
        for (Expression exp : mulExp.getExpressions()) {
            UnaryExp unaryExp = (UnaryExp) exp;
            visitUnaryExp(unaryExp);
        }
    }

    private void visitUnaryExp(UnaryExp unaryExp) {
        //  UnaryExp → PrimaryExp | FuncCall | UnaryOp UnaryExp
        if (unaryExp.getType() == 0) {
            // PrimaryExp → '(' Exp ')' | LVal | Num
            PrimaryExp primaryExp = unaryExp.getPrimaryExp();
            if (primaryExp.getType() == 0) {
                visitAddExp(primaryExp.getExp().getAddExp());
            } else if (primaryExp.getType() == 1) {
                visitLVal(primaryExp.getlVal(), false);
            } else {
                // Num
            }
        } else if (unaryExp.getType() == 1) {
            visitFuncCall(unaryExp.getFuncCall());
        } else {
            visitUnaryExp(unaryExp.getUnaryExp());
        }
    }

    private void visitFuncCall(FuncCall funcCall) {
        // WRONG_PARA_NUM('d'), WRONG_PARA_TYPE('e')
        Symbol symbol = getSymbol(funcCall.getIdent().getContent());
        if (symbol == null) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_UNDEF, funcCall.getIdent().getRow()));
        } else {
            if (symbol instanceof FuncSymbol) {
                if (((FuncSymbol) symbol).getfParams().size() != funcCall.getFuncRParams().getExps().size()) {
                    Error.errorTable.add(new Error(Error.ErrorType.WRONG_PARA_NUM,
                            funcCall.getIdent().getRow()));
                } else {
                    Iterator<Symbol> fParamSymIter = ((FuncSymbol) symbol).getfParams().iterator();
                    Iterator<Exp> rParamExpIter = funcCall.getFuncRParams().getExps().iterator();
                    while (fParamSymIter.hasNext() && rParamExpIter.hasNext()) {
                        FParamSymbol fParamSym = (FParamSymbol) fParamSymIter.next();
                        Exp rParamExp = rParamExpIter.next();
                        int rParamType = -1;
                        UnaryExp rParamUnary = funcCall.getFuncRParams().getRParamUnaryExp(rParamExp);
                        //  UnaryExp → PrimaryExp | FuncCall | UnaryOp UnaryExp
                        if (rParamUnary == null) {
                            rParamType = 0;
                        } else {
                            while (rParamUnary.getType() == 2) {
                                rParamUnary = rParamUnary.getUnaryExp();
                            }
                            if (rParamUnary.getType() == 0) {
                                PrimaryExp primaryExp = rParamUnary.getPrimaryExp();
                                //  PrimaryExp → '(' Exp ')' | LVal | Num
                                assert primaryExp.getType() != 0;
                                if (primaryExp.getType() == 1) {
                                    LVal lVal = primaryExp.getlVal();
                                    VarSymbol varSymbol = (VarSymbol) getSymbol(lVal.getIdent().getContent());
                                    if (varSymbol == null) {
                                        Error.errorTable.add(new Error(Error.ErrorType.NAME_UNDEF,
                                                lVal.getIdent().getRow())); //
                                    } else {
                                        rParamType = getRParamType(varSymbol, lVal);
                                    }
                                } else if (primaryExp.getType() == 2) {
                                    rParamType = 0;
                                }
                            } else if (rParamUnary.getType() == 1) {
                                // funcCall 对应函数返回类型    // todo call void ?
                                FuncCall rFuncCall = rParamUnary.getFuncCall();
                                Symbol funcSymbol = getSymbol(rFuncCall.getIdent().getContent());
                                if (!(funcSymbol instanceof FuncSymbol)) {
                                    Error.errorTable.add(new Error(Error.ErrorType.NAME_UNDEF,
                                            rFuncCall.getIdent().getRow()));
                                } else if (((FuncSymbol) funcSymbol).isInt()) {
                                    rParamType = 0;
                                }
                            }
                        }
                        if (fParamSym.getType() != rParamType) {
                            Error.errorTable.add(new Error(Error.ErrorType.WRONG_PARA_TYPE,
                                    funcCall.getIdent().getRow()));
                        }
                    }
                }
            } else {
                // void a(){}
                // main 中
                // int a = 1;
                //     a();
                Error.errorTable.add(new Error(Error.ErrorType.NAME_UNDEF,
                        funcCall.getIdent().getRow()));
            }
        }
    }

    private int getRParamType(VarSymbol varSymbol, LVal rParam) {
        int rParamDim = rParam.getDimension();
        int symDim = varSymbol.getDimension();
        assert symDim >= rParamDim;
        if (symDim == 2 && rParamDim == 1) {
            rParam.setAddrNotValue(true);
        }
        return symDim - rParamDim;
    }

    private Symbol getSymbol(String symName) {
        SymTable nowSymTable = curSymTable;
        while (nowSymTable != null) {
            if (nowSymTable.hasSymbol(symName)) {
                return nowSymTable.getSymbol(symName);
            }
            nowSymTable = nowSymTable.getParent();
        }
        return null;
    }

    private void visitDecl(Decl decl) {
        //  Decl → {'const'} BType Def { ',' Def } ';'

        boolean isConst = decl.isConst();
        ArrayList<Def> defs = decl.getDefs();
        for (Def def : defs) {
            visitDef(def, isConst);
        }
    }

    private boolean isGlobalVisit() {
        return curFuncDefBb == null;
    }

    private void visitDef(Def def, boolean isConst) {
        //  ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        //  VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
        //  Def → Variable [ '=' InitVal ]
        Variable variable = def.getVariable();
        ArrayList<ConstExp> constExps = variable.getConstExps();
        int size = 1;
        int colNum = 1;
        ArrayList<Integer> values = new ArrayList<>();
        // Variable → Ident { '[' ConstExp ']' }
        if (curSymTable.hasSymbol(variable.getIdent().getContent())) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                    variable.getIdent().getRow()));
        } else {
            Init init = def.getInit();
            // todo 没有考虑 a[2] = {} 类似的情况
            // 有init的时候会用到的
            ArrayList<Operand> operands;
            Operand lastOne;

            if (constExps.isEmpty()) {
                // scalar
                ConstVar constVar;
                if (init == null) {
                    constVar = new ConstVar(new VarName(variable.getIdent().getContent(), curBBlock.getDepth()), isConst, null);
                } else {
                    operands = analyseExpression(init.getScalar());
                    lastOne = getLast(operands);
                    if (lastOne instanceof PrimaryOpd) {
                        operands.remove(lastOne);
                    }
                    for (Operand operand : operands) {
                        curBBlock.append((MiddleCode) operand);
                    }
                    if (lastOne instanceof Immediate) {
                        values.add(((Immediate) lastOne).getValue());
                    } // 当全局变量的定义 int a = b + c + 231; 类似时要用到 b c 的初始值
                    constVar = new ConstVar(new VarName(variable.getIdent().getContent(), curBBlock.getDepth()), isConst, lastOne);
                }
                // 全局变量存放在global block里面，局部变量存放在函数block里面
                curBBlock.append(constVar);
                if (!isGlobalVisit()) {
                    curFuncDefBb.addLocalVar(constVar, false);
                } else {
                    globalName2VarName.put(def.getVariable().getIdent().getContent(), constVar.getVarName());
                    globalVars.put(constVar.getVarName(), constVar);
                }
            } else {
                // array

                // 一维
                assert analyseAddExp(constExps.get(0).getAddExp()).size() == 1;
                Immediate immediate = (Immediate) analyseAddExp(constExps.get(0).getAddExp()).get(0);
                size = immediate.getValue();
                colNum = size;
                // 二维
                if (constExps.size() == 2) {
                    assert analyseAddExp(constExps.get(1).getAddExp()).size() == 1;
                    immediate = (Immediate) analyseAddExp(constExps.get(1).getAddExp()).get(0);
                    colNum = immediate.getValue();
                    size *= colNum;
                }

                // 无所谓有没有初始化
                if (curSymTable == globalSymTable) {
                    GlobalArray globalArray = new GlobalArray(new VarName(def.getVariable().getIdent().getContent(), 0, constExps.size() == 2 ? colNum : 0, false), size, values);
                    globalName2VarName.put(def.getVariable().getIdent().getContent(), globalArray.getVarName());
                    globalVars.put(globalArray.getVarName(), globalArray);
                    curBBlock.append(globalArray);
                } else {
                    ArrayDef arrayDef = new ArrayDef(new VarName(variable.getIdent().getContent(), curBBlock.getDepth(), constExps.size() == 2 ? colNum : 0, false), isConst, size);
                    curBBlock.append(arrayDef);
                    curFuncDefBb.addLocalVar(arrayDef, false);    // 局部数组加进来
                }
                VarSymbol arraySymbol = new VarSymbol(variable.getIdent().getContent(),
                        isConst, variable.getDimension(), size, colNum, values, curBBlock.getDepth());
                if (init != null) {
                    int cnt = 0;
                    if (init.getDimension() == 1) {
                        genArrayInit(isConst, init.getVector(), values, arraySymbol, cnt);
                    } else {
                        for (Vector vector : init.getVectors()) {
                            genArrayInit(isConst, vector, values, arraySymbol, cnt);
                            cnt += vector.getExpressions().size();
                        }
                    }
                }
            }
        }

        curSymTable.addSymbol(new VarSymbol(variable.getIdent().getContent(),
                isConst, variable.getDimension(), size, colNum, values, curBBlock.getDepth()));
    }

    private void genArrayInit(boolean isConst, Vector vector, ArrayList<Integer> values, Symbol arraySymbol, int cnt) {
        ArrayList<Operand> operands;
        Operand lastOne;
        ArrayList<Expression> expressions = vector.getExpressions();
        for (Expression expression : expressions) {
            operands = analyseExpression(expression);
            lastOne = getLast(operands);
            if (isGlobalVisit()) {
                // 全局数组,直接获得立即数
                assert operands.size() == 1;
                values.add(((Immediate) lastOne).getValue());
            } else {
                if (isConst) {
                    // 局部常数组
                    assert operands.size() == 1;
                    values.add(((Immediate) lastOne).getValue());
                }
                if (lastOne instanceof PrimaryOpd) {
                    operands.remove(lastOne);
                }
                for (Operand operand : operands) {
                    curBBlock.append((MiddleCode) operand);
                }
                if (lastOne instanceof Immediate) {
                    // dst[i] = 1
                    lastOne = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), lastOne); // li t1, 1
                    curFuncDefBb.addLocalVar(lastOne, true);
                    curBBlock.append((MiddleCode) lastOne);
                } else if (lastOne instanceof LValOpd && lValOpdIsArrayEle(lastOne)) {
                    // 把数组的值赋给它 ->
                    // t2 = src[t0]
                    // dst[i] = t2
                    PrimaryOpd dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                    lastOne = new ArrayLoad((PrimaryOpd) lastOne, dst);
                    curFuncDefBb.addLocalVar(dst, true);
                    curBBlock.append((MiddleCode) lastOne);
                }
                VarName arrayName = getVarName(arraySymbol);
                arrayName.addOrSubRef(1);
                PrimaryOpd arrayDst = new LValOpd(arrayName, new Immediate(cnt++));
                curBBlock.append(new ArrayStore(arrayDst, lastOne));
                curBBlock.addUsedVars(arrayName);
            }
        }
    }

    private Operand getLast(ArrayList<Operand> operands) {
        return operands.get(operands.size() - 1);
    }

    private boolean lValOpdIsArrayEle(Operand operand) {
        assert operand instanceof LValOpd;
        return ((LValOpd) operand).getIdx() != null;
    }

    private ArrayList<Operand> analyseExpression(Expression expression) {
        if (expression instanceof ConstExp) {
            return analyseAddExp(((ConstExp) expression).getAddExp());
        }
        return analyseAddExp(((Exp) expression).getAddExp());
    }

    private ArrayList<Operand> analyseAddExp(AddExp addExp) {
        Iterator<Expression> expIter = addExp.getExpIter();
        Iterator<Operator> opIter = addExp.getOpIter();
        MulExp mulExp = (MulExp) expIter.next();

        ArrayList<Operand> operands1 = analyseMulExp(mulExp);
        ArrayList<Operand> operands = new ArrayList<>(operands1);
        Operand leftOne;
        Operand rightOne;
        ArrayList<Operand> operands2;
        while (expIter.hasNext()) {
            leftOne = getLast(operands);
            if (leftOne instanceof PrimaryOpd) {
                operands.remove(leftOne);
            }
            Operator op = opIter.next();
            mulExp = (MulExp) expIter.next();
            operands2 = analyseMulExp(mulExp);
            rightOne = getLast(operands2);
            if (leftOne instanceof Immediate && rightOne instanceof Immediate) {
                // 如果是Immediate 的话，那么operands1、2 里面应该只能有一个元素。
                assert operands1.size() == 1 && operands2.size() == 1;
                leftOne = ((Immediate) leftOne).procValue(op.getOp(), ((Immediate) rightOne).getValue());
                operands.add(leftOne);
            } else if (leftOne instanceof RetOpd && rightOne instanceof RetOpd) {
                leftOne = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne);
                operands.add(leftOne);
                curFuncDefBb.addLocalVar(leftOne, true);
                operands.addAll(operands2);
                operands.remove(rightOne);
                BinaryCode b = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                operands.add(b);
                curFuncDefBb.addLocalVar(b, true);
            } else {
                if (leftOne instanceof LValOpd && lValOpdIsArrayEle(leftOne)) {
                    Operand dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), null);
                    curFuncDefBb.addLocalVar(dst, true);
                    leftOne = new ArrayLoad((PrimaryOpd) leftOne, dst);
                    operands.add(leftOne);
                }

                operands.addAll(operands2);
                if (rightOne instanceof PrimaryOpd) {
                    operands.remove(rightOne);
                }
                if (rightOne instanceof LValOpd && lValOpdIsArrayEle(rightOne)) {
                    Operand dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), null);
                    curFuncDefBb.addLocalVar(dst, true);
                    rightOne = new ArrayLoad((PrimaryOpd) rightOne, dst);
                    operands.add(rightOne);
                }

                BinaryCode b = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                operands.add(b);
                curFuncDefBb.addLocalVar(b, true);
            }
        }
        return operands;
    }

    private ArrayList<Operand> analyseMulExp(MulExp mulExp) {

        Iterator<Expression> expIter = mulExp.getExpIter();
        Iterator<Operator> opIter = mulExp.getOpIter();
        UnaryExp unaryExp = (UnaryExp) expIter.next();

        ArrayList<Operand> operands1 = analyseUnaryExp(unaryExp);
        ArrayList<Operand> operands = new ArrayList<>(operands1);
        Operand leftOne;
        Operand rightOne;
        ArrayList<Operand> operands2;
        while (expIter.hasNext()) {
            leftOne = getLast(operands);
            if (leftOne instanceof PrimaryOpd) {
                operands.remove(leftOne);
            }
            Operator op = opIter.next();
            unaryExp = (UnaryExp) expIter.next();
            operands2 = analyseUnaryExp(unaryExp);
            rightOne = getLast(operands2);
            if (leftOne instanceof Immediate && rightOne instanceof Immediate) {
                // 如果是Immediate 的话，那么operands1、2 里面应该只能有一个元素。
                leftOne = ((Immediate) leftOne).procValue(op.getOp(), ((Immediate) rightOne).getValue());
                operands.add(leftOne);
            } else if (leftOne instanceof RetOpd && rightOne instanceof RetOpd) {
                leftOne = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne);
                operands.add(leftOne);
                curFuncDefBb.addLocalVar(leftOne, true);
                operands.addAll(operands2);
                operands.remove(rightOne);
                BinaryCode b = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                operands.add(b);
                curFuncDefBb.addLocalVar(b, true);
            } else {
                if (leftOne instanceof LValOpd && lValOpdIsArrayEle(leftOne)) {
                    Operand dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), null);
                    curFuncDefBb.addLocalVar(dst, true);
                    leftOne = new ArrayLoad((PrimaryOpd) leftOne, dst);
                    operands.add(leftOne);
                }

                operands.addAll(operands2);
                if (rightOne instanceof PrimaryOpd) {
                    operands.remove(rightOne);
                }
                if (rightOne instanceof LValOpd && lValOpdIsArrayEle(rightOne)) {
                    Operand dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), null);
                    curFuncDefBb.addLocalVar(dst, true);
                    rightOne = new ArrayLoad((PrimaryOpd) rightOne, dst);
                    operands.add(rightOne);
                }

                BinaryCode dst = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                curFuncDefBb.addLocalVar(dst, true);
                operands.add(dst);
            }
        }
        return operands;
    }

    // 可能有 PrimaryOpd 也可能没有
    private ArrayList<Operand> analyseUnaryExp(UnaryExp unaryExp) {
        ArrayList<Operand> operands = new ArrayList<>();
        switch (unaryExp.getType()) {
            case 0:
                operands.addAll(analysePrimaryExp(unaryExp.getPrimaryExp()));
                break;
            case 1:
                // FuncCall ->  Ident '(' [FuncRParams] ')'
                curBBlock.setHasFuncCall(true); // todo check
                ArrayList<Operand> rParaCodes = new ArrayList<>();
                FuncCall funcCall = unaryExp.getFuncCall();
                FuncRParams funcRParams = funcCall.getFuncRParams();
                // ArrayList<Symbol> fParamSyms = ((FuncSymbol) Objects.requireNonNull(getSymbol(funcCall.getIdent().getContent()))).getfParams();
                Iterator<Symbol> fParamSymIter = ((FuncSymbol) Objects.requireNonNull(getSymbol(funcCall.getIdent().getContent()))).getfParams().iterator();
                for (Exp exp : funcRParams.getExps()) {
                    operands.addAll(analyseAddExp(exp.getAddExp()));
                    Operand lastOne = getLast(operands);
                    if (lastOne instanceof PrimaryOpd) {
                        operands.remove(lastOne);
                    }
                    FParamSymbol fParamSymbol = (FParamSymbol) fParamSymIter.next();
                    rParaCodes.add(new RParaCode(lastOne, fParamSymbol.getType() > 0)); // 最后把参数一起push
                }
                operands.addAll(rParaCodes);
                // todo 这个FuncCallCode 的属性应该是要改一改的

                String funcName = unaryExp.getFuncCall().getIdent().getContent();
                operands.add(new FuncCallCode(new VarName(funcName, 0), rParaCodes));
                FuncSymbol funcSymbol = (FuncSymbol) getSymbol(funcName);
                if (funcSymbol != null && funcSymbol.isInt()) {
                    // 错误处理之后不能处理这个null，还是要特判一下
                    operands.add(new RetOpd());
                }
                break;
            case 2:
                boolean isNeg = unaryExp.getUnaryOp().getOp().equals("-");
                boolean isNot = unaryExp.getUnaryOp().getOp().equals("!");

                UnaryExp subUnary = unaryExp.getUnaryExp();
                while (isNeg && subUnary.getType() == 2) {
                    // 如果 isNeg，意味着后面一定是+/- 而不会是 !
                    // 异或一下
                    isNeg = isNeg ^ subUnary.getUnaryOp().getOp().equals("-");
                    subUnary = subUnary.getUnaryExp();
                }

                ArrayList<Operand> operands1 = analyseUnaryExp(subUnary); // recurrence
                Operand lastOne = getLast(operands1);

                // UnaryExp -> Num | UnaryOp UnaryExp
                if (lastOne instanceof Immediate) {
                    assert operands1.size() == 1;   // 应该只有这一个Immediate
                    if (isNeg) {
                        lastOne = ((Immediate) lastOne).procValue(true);
                    } else if (isNot) {
                        lastOne = new Immediate(((Immediate) lastOne).getValue() == 0 ? 1 : 0);
                    }
                    operands.add(lastOne);
                } else {
                    // 其他 PrimaryOpd 情况或者 exp
                    operands.addAll(operands1);
                    if (isNeg) {
                        if (lastOne instanceof PrimaryOpd) {
                            operands.remove(lastOne);
                        }
                        UnaryCode unaryCode = new UnaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), lastOne, MiddleCode.Op.SUB);
                        operands.add(unaryCode);
                        curFuncDefBb.addLocalVar(unaryCode, true);
                    } else if (isNot) {
                        operands.remove(lastOne);
                        LValOpd lValOpd = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                        SaveCmp saveCmp = new SaveCmp(lValOpd, lastOne, new Immediate(0), SaveCmp.CmpType.SEQ);
                        operands.add(saveCmp);
                        curFuncDefBb.addLocalVar(lValOpd, true);
                    }
                }
                break;
        }
        return operands;
    }

    private ArrayList<Operand> analysePrimaryExp(PrimaryExp primaryExp) {
        ArrayList<Operand> operands = new ArrayList<>();
        switch (primaryExp.getType()) {
            case 0:
                // (Exp)
                operands.addAll(analyseAddExp(primaryExp.getExp().getAddExp()));
                break;
            case 1:
                // LVal
                operands.addAll(analyseLVal(primaryExp.getlVal()));
                break;
            case 2:
                // Num
                operands.add(new Immediate(primaryExp.getNumber().getValueOfNum()));
                break;
        }
        return operands;
    }

    // 最后一个元素是一个PrimaryOpd:LVal的体现，不是MiddleCode， 根据LVal在左边还是右边灵活使用。
    private ArrayList<Operand> analyseLVal(LVal lVal) {
        ArrayList<Operand> operands = new ArrayList<>();
        Symbol symbol = getSymbol(lVal.getIdent().getContent());
        // System.out.println(lVal.getIdent().getContent());
        switch (lVal.getDimension()) {
            case 0:
                // size = 1
                if (isGlobalVisit()) {
                    // 1, a
                    assert symbol instanceof VarSymbol;
                    operands.add(new Immediate(((VarSymbol) symbol).getValue(0)));
                } else {
                    if (symbol instanceof VarSymbol && ((VarSymbol) symbol).isConst() &&
                            ((VarSymbol) symbol).getDimension() == 0/*lval可能是数组名，传参时出现*/) {
                        operands.add(new Immediate(((VarSymbol) symbol).getValue(0)));
                    } else {
                        /*lval可能是数组名，传参时出现,((VarSymbol) symbol).getDimension() != 0*/
                        VarName varName = getVarName(symbol);
                        varName.addOrSubRef(1);
                        operands.add(new LValOpd(varName, null));
                        curBBlock.addUsedVars(varName);
                        // varInWhileCond.add(getVarName(symbol));
                    }
                }
                break;
            case 1:
                Exp exp = lVal.getExps().get(0);
                ArrayList<Operand> operands1 = analyseExpression(exp);
                Operand lastOne = getLast(operands1);
                if (lastOne instanceof Immediate) {
                    // 索引是常数
                    assert operands1.size() == 1;
                    if (isGlobalVisit()) {
                        // a[2] // 全局数组的索引肯定是常数
                        assert symbol instanceof VarSymbol;
                        operands.add(new Immediate(((VarSymbol) symbol).getValue(((Immediate) lastOne).getValue())));
                    } else {
                        if (symbol instanceof VarSymbol && ((VarSymbol) symbol).isConst()
                                && !lVal.isAddrNotValue()/*非二维常量数组传一维参数*/) {
                            operands.add(new Immediate(((VarSymbol) symbol).getValue(((Immediate) lastOne).getValue())));
                        } else {
                            VarName varName = getVarName(symbol);
                            varName.addOrSubRef(1);
                            operands.add(new LValOpd(varName, lastOne));
                            curBBlock.addUsedVars(varName);
                        }
                    }
                } else {
                    operands.addAll(operands1);
                    if (lastOne instanceof PrimaryOpd) {
                        operands.remove(lastOne);
                        if (lastOne instanceof LValOpd && ((LValOpd) lastOne).isArray()) {
                            lastOne = new ArrayLoad((PrimaryOpd) lastOne, new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth())));
                            assert !isGlobalVisit();
                            curFuncDefBb.addLocalVar(lastOne, true);
                            operands.add(lastOne);
                        }
                    }
                    // 没有检查会不会数组越界
                    VarName varName = getVarName(symbol);
                    varName.addOrSubRef(1);
                    operands.add(new LValOpd(varName, lastOne));
                    curBBlock.addUsedVars(varName);
                }
                break;
            case 2:
                // var[1][2]
                operands1 = analyseExpression(lVal.getExps().get(0));
                ArrayList<Operand> operands2 = analyseExpression(lVal.getExps().get(1));
                assert symbol instanceof VarSymbol;
                int colNum = ((VarSymbol) symbol).getColNum();
                /*// 左值只会是这两种之一
                if (symbol instanceof VarSymbol) {
                    colNum = new Immediate(((VarSymbol) symbol).getColNum());
                } else if (symbol instanceof FParamSymbol) {
                    assert false;   // 应该不是，我是把参数当成varSymbol加入到符号表。当成FParamSymbol是加入了函数符号表
                    colNum = new Immediate(((FParamSymbol) symbol).getColNum());
                }*/
                Operand x = getLast(operands1);
                Operand y = getLast(operands2);

                if (x instanceof PrimaryOpd) {
                    operands1.remove(x);
                }
                if (y instanceof PrimaryOpd) {
                    operands2.remove(y);
                }

                if (x instanceof Immediate && y instanceof Immediate) {
                    // a[1][2]  a[n][colNum]
                    Immediate idx = ((Immediate) x).procValue("*", colNum).procValue("+", ((Immediate) y).getValue());
                    // idx =  idx.procValue("+", (Immediate) y);
                    if (((VarSymbol) symbol).isConst() || isGlobalVisit()) {
                        operands.add(new Immediate(((VarSymbol) symbol).getValue(idx.getValue())));
                    } else {
                        VarName varName = getVarName(symbol);
                        varName.addOrSubRef(1);
                        operands.add(new LValOpd(varName, idx));
                        curBBlock.addUsedVars(varName);
                    }
                } else if (x instanceof Immediate) {
                    x = ((Immediate) x).procValue("*", colNum);
                    operands.addAll(operands1);
                    operands.addAll(operands2);
                    Operand t1 = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), x, y, "+");
                    operands.add(t1);
                    curFuncDefBb.addLocalVar(t1, true);
                    VarName varName = getVarName(symbol);
                    varName.addOrSubRef(1);
                    operands.add(new LValOpd(varName, t1));
                    curBBlock.addUsedVars(varName);
                } else {
                    operands.addAll(operands1);
                    operands.addAll(operands2);
                    Operand t1 = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), x, new Immediate(colNum), "*");
                    Operand t2 = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), t1, y, "+");
                    operands.add(t1);
                    operands.add(t2);
                    curFuncDefBb.addLocalVar(t1, true);
                    curFuncDefBb.addLocalVar(t2, true);
                    VarName varName = getVarName(symbol);
                    varName.addOrSubRef(1);
                    operands.add(new LValOpd(getVarName(symbol), t2));
                    curBBlock.addUsedVars(varName);
                }
                break;
        }
        return operands;
    }

    public BasicBlock getGlobalBlock() {
        return globalBlock;
    }

    public HashMap<String, ConstStrCode> getConstStr() {
        return constStr;
    }

    public ArrayList<FuncDefBlock> getFuncDefBlocks() {
        return funcDefBlocks;
    }

    public HashMap<String, FuncDefBlock> getFuncDefBBlocksMap() {
        return funcDefBBlocksMap;
    }

    public VarName getVarName(Symbol symbol) {
        // symbol  与 VarName 是一一对应关系
        VarName res;
        if (curFuncDefBb != null && (res = curFuncDefBb.getLocalVar(symbol)) != null) {
            return res;
        }
        return globalName2VarName.get(symbol.getSymName());
    }
}
