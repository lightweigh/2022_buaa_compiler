package frontend.visitor;

import frontend.Error;
import frontend.grammar.*;
import frontend.grammar.decl.Decl;
import frontend.grammar.decl.def.Def;
import frontend.grammar.decl.def.Variable;
import frontend.grammar.decl.def.init.Init;
import frontend.grammar.decl.def.init.Vector;
import frontend.grammar.exp.*;
import frontend.grammar.funcDef.FuncDef;
import frontend.grammar.funcDef.FuncFParam;
import frontend.grammar.funcDef.FuncType;
import frontend.grammar.stmt.*;
import frontend.symbol.*;
import middle.BasicBlock;
import middle.FuncDefBlock;
import middle.MiddleTn;
import middle.VarName;
import middle.quartercode.*;
import middle.quartercode.array.*;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
    private int constStringCnt = 0;

    private int blockDepth = 0;

    public Visitor() {
        this.curSymTable = new SymTable(null);
        this.globalSymTable = curSymTable;

        this.curBBlock = new BasicBlock("global_", false, blockDepth);
        this.globalBlock = curBBlock;

        this.globalVars = new HashMap<>();
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
        blockDepth = 1;
        // curBBlock.setDirectBb(newBb);
        curBBlock = new BasicBlock("main", true, blockDepth);
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
        visitBlock(mainFuncDef.getBlock(), newTable, false);
    }

    private void visitFuncDef(FuncDef funcDef) {
        blockDepth = 1;
        // curBBlock.setDirectBb(newBb);
        curBBlock = new BasicBlock(funcDef.getName(), true, blockDepth);
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
        int argNum = 0;
        for (FuncFParam funcFParam : funcDef.getFuncFParams()) {
            // todo modified 别改，出错了qwq
            // 如果函数的调用，实参是调用函数的参数就出bug
            int type = funcFParam.getType();
            int colNum = 1;
            if (type == 2) {
                ArrayList<Operand> operands = analyseExpression(funcFParam.getConstExp());
                colNum = ((Immediate) operands.get(0)).getValue();
            }
            VarSymbol varSymbol = new VarSymbol(funcFParam.getIdent().getContent(), funcFParam.getType());
            if (newSymTable.hasSymbol(funcFParam.getIdent().getContent())) {
                Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                        funcFParam.getIdent().getRow()));
            } else {
                newSymTable.addSymbol(varSymbol);
            }
            funcSymbol.addFParam(new FParamSymbol(funcFParam.getIdent().getContent(),
                    funcFParam.getType(), colNum));

            FParaCode fParaCode = new FParaCode(new VarName(funcFParam.getIdent().getContent(), curBBlock.getDepth()),
                    funcFParam.getType() != 0, argNum++);
            curBBlock.append(fParaCode);
            curFuncDefBb.addLocalVar(fParaCode);
        }
        visitBlock(funcDef.getBlock(), newSymTable, false);
    }

    private void visitBlock(Block block, SymTable newTable, boolean isInwhile) {
        //  Block → '{' { BlockItem } '}'
        middleTn.clear();
        SymTable prevTable = curSymTable;
        curSymTable = newTable;
        for (BlockItem blockItem : block.getBlockItems()) {
            if (blockItem.isDecl()) {
                visitDecl(blockItem.getDecl());
            } else {
                String bbLabel = null;
                if (blockItem.getStmt() instanceof Block) {
                    // 只担心和函数名重名, 这样就好啦
                    bbLabel = "int_";
                }
                visitStmt(blockItem.getStmt(), isInwhile, bbLabel);
            }
        }
        curSymTable = prevTable;
    }

    private void visitStmt(Stmt stmt, boolean isInwhile, String bbLabel) {
        // middleTn.clear();
        if (stmt instanceof Block) {
            if (bbLabel != null) {
                // 新建一个basicBlock
                blockDepth++;
                BasicBlock newBb = new BasicBlock(bbLabel, false, blockDepth);
                curBBlock.setDirectBb(newBb);
                curBBlock = newBb;
                curFuncDefBb.addBb(curBBlock);
            }
            SymTable newTable = new SymTable(curSymTable);
            visitBlock((Block) stmt, newTable, isInwhile);
            if (bbLabel != null) {
                blockDepth--;
                // block出来之后要新建一个bb
                BasicBlock newBb = new BasicBlock(bbLabel + "next_", false, blockDepth);
                curBBlock.setDirectBb(newBb);
                curBBlock = newBb;
                curFuncDefBb.addBb(curBBlock);
            }

        } else if (stmt instanceof ExpStmt && ((ExpStmt) stmt).getExp() != null) {
            AddExp addExp = ((ExpStmt) stmt).getExp().getAddExp();
            visitAddExp((addExp));
            // middle code part
            ArrayList<Operand> operands = analyseAddExp(addExp);
            // todo check primaryOpd
            if (getLast(operands) instanceof PrimaryOpd) {
                // 如果是 单一的 应该 没什么用吧。
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
                        // curBBlock.append(new AssignCode(lastOfLVal.getLocalName(), lastOfExp));
                        lastOfExp.rename(lastOfLVal.getVarName());
                        // System.out.println(lastOfExp);   todo check 到底要不要再加这个
                        // curBBlock.append((MiddleCode) lastOfExp);
                    }
                } else {
                    if (lastOfExp instanceof LValOpd && ((LValOpd) lastOfExp).getIdx() != null) {
                        // b[t1] = a[t0];
                        // ->
                        // t2 = a[t0]
                        // b[t1] = t2
                        LValOpd dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                        curFuncDefBb.addLocalVar(dst);
                        MiddleCode t2 = new ArrayLoad((PrimaryOpd) lastOfExp, dst);
                        curBBlock.append(t2);
                        curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, t2));
                    } else {
                        curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, lastOfExp));
                    }
                }
            } else {
                MiddleCode t1 = new ReadIn(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                curFuncDefBb.addLocalVar(t1);
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
                    t1.rename(lastOfLVal.getVarName());    // 本来就append进去了，不用再append
                    // curBBlock.append(t1);
                } else {
                    curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, t1));
                }
            }
        } else if (stmt instanceof WhileStmt) {
            blockDepth++;
            BasicBlock newBb = new BasicBlock("while_", false, blockDepth);
            curBBlock.setDirectBb(newBb);
            curBBlock = newBb;
            curFuncDefBb.addBb(curBBlock);
            visitStmt(((WhileStmt) stmt).getStmt(), true, null);
            // 分析完之后，退出while基本块 todo 然后紧接着新的基本块
            blockDepth--;
        } else if (stmt instanceof BreakOrContinueStmt) {
            // todo 这儿也有一个基本块
            if (!isInwhile) {
                Error.errorTable.add(new Error(Error.ErrorType.WRONG_BREAK_CONTINUE, ((BreakOrContinueStmt) stmt).getRow()));
            }
        } else if (stmt instanceof IfStmt) {
            // todo 这儿也有一个基本块
            visitStmt(((IfStmt) stmt).getStmt(), isInwhile, null);
            if (((IfStmt) stmt).hasElse()) {
                visitStmt(((IfStmt) stmt).getElseStmt(), isInwhile, null);
            }
        } else if (stmt instanceof PrintfStmt) {
            PrintfStmt printfStmt = (PrintfStmt) stmt;
            String inner = printfStmt.getFormatString().getInner();
            ArrayList<Integer> formatChars = printfStmt.getFormatString().getFormatChars();
            Iterator<Exp> expIter = printfStmt.getExps().iterator();
            ArrayList<Operand> subBB = new ArrayList<>();
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
                    subBB.add(new PutOut(str));
                }
                assert expIter.hasNext();
                Exp exp = expIter.next();
                ArrayList<Operand> operands = analyseExpression(exp);
                Operand lastOne = getLast(operands);
                if (lastOne instanceof PrimaryOpd) {
                    operands.remove(lastOne);
                }
                subBB.addAll(operands);
                subBB.add(new PutOut(lastOne));
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
                subBB.add(new PutOut(str));
            }
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
        }
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
                        UnaryExp rParam = funcCall.getFuncRParams().getRParamUnaryExp(rParamExp);
                        //  UnaryExp → PrimaryExp | FuncCall | UnaryOp UnaryExp
                        if (rParam == null) {
                            rParamType = 0;
                        } else {
                            while (rParam.getType() == 2) {
                                rParam = rParam.getUnaryExp();
                            }
                            if (rParam.getType() == 0) {
                                PrimaryExp primaryExp = rParam.getPrimaryExp();
                                //  PrimaryExp → '(' Exp ')' | LVal | Num
                                if (primaryExp.getType() == 1) {
                                    LVal lVal = primaryExp.getlVal();
                                    VarSymbol varSymbol = (VarSymbol) getSymbol(lVal.getIdent().getContent());
                                    if (varSymbol == null) {
                                        Error.errorTable.add(new Error(Error.ErrorType.NAME_UNDEF,
                                                lVal.getIdent().getRow())); //
                                    } else {
                                        rParamType = getFParamType(varSymbol, lVal);
                                    }
                                } else if (primaryExp.getType() == 2) {
                                    Num num = primaryExp.getNumber();
                                    rParamType = 0;
                                }
                            } else if (rParam.getType() == 1) {
                                // funcCall 对应函数返回类型    // todo call void ?
                                FuncCall rFuncCall = rParam.getFuncCall();
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

    private int getFParamType(VarSymbol varSymbol, LVal fParam) {
        int fParamDim = fParam.getDimension();
        int symDim = varSymbol.getDimension();
        assert symDim >= fParamDim;
        return symDim - fParamDim;
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
            if (constExps.size() >= 1) {
                Immediate immediate = (Immediate) analyseAddExp(constExps.get(0).getAddExp()).get(0);
                size = immediate.getValue();
                colNum = size;
            }
            if (constExps.size() == 2) {
                Immediate immediate = (Immediate) analyseAddExp(constExps.get(1).getAddExp()).get(0);
                colNum = immediate.getValue();
                size *= colNum;
            }

            if (def.getInit() == null) {
                ConstVar constVar = new ConstVar(new VarName(def.getVariable().getIdent().getContent(), curBBlock.getDepth()), isConst, null);
                curBBlock.append(constVar);
                if (curFuncDefBb != null) {
                    curFuncDefBb.addLocalVar(constVar);
                } else {
                    globalVars.put(constVar.getVarName(), constVar);
                }
            } else {
                Init init = def.getInit();
                // todo 没有考虑 a[2] = {} 类似的情况
                ArrayList<Operand> operands;
                switch (init.getDimension()) {
                    case 0:
                        // scalar
                        operands = analyseExpression(init.getScalar());
                        Operand lastOne = getLast(operands);
                        if (lastOne instanceof PrimaryOpd) {
                            operands.remove(lastOne);
                        }
                        for (Operand operand : operands) {
                            curBBlock.append((MiddleCode) operand);
                        }
                        if (lastOne instanceof Immediate) {
                            values.add(((Immediate) lastOne).getValue());
                        }
                        ConstVar constVar = new ConstVar(new VarName(def.getVariable().getIdent().getContent(), curBBlock.getDepth()), isConst, lastOne);
                        // 全局变量存放在block里面，局部变量存放在函数block里面
                        curBBlock.append(constVar);
                        if (curFuncDefBb != null) {
                            curFuncDefBb.addLocalVar(constVar);
                        } else {
                            globalVars.put(constVar.getVarName(), constVar);
                        }
                        break;
                    case 1:
                        if (curSymTable == globalSymTable) {
                            // 全局数组的初始化不一样噢
                            assert curBBlock.getDepth() == 0;
                            curBBlock.append(new GlobalArray(new VarName(def.getVariable().getIdent().getContent(), curBBlock.getDepth()), size, values));
                        } else {
                            ArrayDef arrayDef = new ArrayDef(new VarName(def.getVariable().getIdent().getContent(), curBBlock.getDepth()), isConst, size);
                            curBBlock.append(arrayDef);
                            curFuncDefBb.addLocalVar(arrayDef);    // 局部数组加进来

                            ArrayList<Expression> expressions = init.getVector().getExpressions();
                            for (int i = 0; i < expressions.size(); i++) {
                                operands = analyseExpression(expressions.get(i));
                                lastOne = getLast(operands);
                                if (lastOne instanceof PrimaryOpd) {
                                    operands.remove(lastOne);
                                }
                                for (Operand operand : operands) {
                                    curBBlock.append((MiddleCode) operand);
                                }
                                if (lastOne instanceof Immediate) {
                                    values.add(((Immediate) lastOne).getValue());
                                } else if (lastOne instanceof LValOpd && ((LValOpd) lastOne).getIdx() != null) {
                                    // 把数组的值赋给它
                                    // ->
                                    // t2 = src[t0]
                                    // dst[i] = t2
                                    PrimaryOpd dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                                    MiddleCode t2 = new ArrayLoad((PrimaryOpd) lastOne, dst);
                                    curFuncDefBb.addLocalVar(dst);
                                    curBBlock.append(t2);

                                    PrimaryOpd primaryOpd = new LValOpd(getVarName(def.getVariable().getIdent().getContent()), new Immediate(i));
                                    curBBlock.append(new ArrayStore(primaryOpd, t2));
                                } else {
                                    PrimaryOpd primaryOpd = new LValOpd(getVarName(def.getVariable().getIdent().getContent()), new Immediate(i));
                                    curBBlock.append(new ArrayStore(primaryOpd, lastOne));
                                }
                            }
                        }
                        break;
                    case 2:
                        if (curSymTable == globalSymTable) {
                            // 全局数组的初始化不一样噢
                            GlobalArray globalArray = new GlobalArray(new VarName(def.getVariable().getIdent().getContent(), 0), size, values);
                            globalVars.put(globalArray.getVarName(), globalArray);
                        } else {
                            ArrayDef arrayDef = new ArrayDef(new VarName(def.getVariable().getIdent().getContent(), curBBlock.getDepth()), isConst, size);
                            curBBlock.append(arrayDef);
                            curFuncDefBb.addLocalVar(arrayDef);
                            int cnt = 0;
                            for (Vector vector : init.getVectors()) {
                                ArrayList<Expression> expressions = vector.getExpressions();
                                for (Expression expression1 : expressions) {
                                    operands = analyseExpression(expression1);
                                    lastOne = getLast(operands);
                                    if (lastOne instanceof PrimaryOpd) {
                                        operands.remove(lastOne);
                                    }
                                    for (Operand operand : operands) {
                                        curBBlock.append((MiddleCode) operand);
                                    }
                                    if (lastOne instanceof Immediate) {
                                        values.add(((Immediate) lastOne).getValue());
                                    } else if (lastOne instanceof LValOpd && ((LValOpd) lastOne).getIdx() != null) {
                                        // 把数组的值赋给它
                                        // ->
                                        // t2 = src[t0]
                                        // dst[i] = t2
                                        PrimaryOpd dst = new LValOpd(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()));
                                        MiddleCode t2 = new ArrayLoad((PrimaryOpd) lastOne, dst);
                                        curFuncDefBb.addLocalVar(dst);
                                        curBBlock.append(t2);
                                        PrimaryOpd primaryOpd = new LValOpd(getVarName(def.getVariable().getIdent().getContent()), new Immediate(cnt++));
                                        curBBlock.append(new ArrayStore(primaryOpd, t2));
                                    } else {
                                        PrimaryOpd primaryOpd = new LValOpd(getVarName(def.getVariable().getIdent().getContent()), new Immediate(cnt++));
                                        curBBlock.append(new ArrayStore(primaryOpd, lastOne));
                                    }
                                }
                            }
                        }
                        break;
                }
            }
            curSymTable.addSymbol(new VarSymbol(variable.getIdent().getContent(),
                    isConst, variable.getDimension(), size, colNum, values));
        }
    }

    private Operand getLast(ArrayList<Operand> operands) {
        return operands.get(operands.size() - 1);
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
                ((Immediate) leftOne).procValue(op.getOp(), (Immediate) rightOne);
                operands.add(leftOne);
            } else if (leftOne instanceof RetOpd && rightOne instanceof RetOpd){
                leftOne = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne);
                operands.add(leftOne);
                curFuncDefBb.addLocalVar(leftOne);
                operands.addAll(operands2);
                operands.remove(rightOne);
                BinaryCode b = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                operands.add(b);
                curFuncDefBb.addLocalVar(b);
            } else {
                // todo 应该还有存在数组的情况?
                operands.addAll(operands2);
                if (rightOne instanceof PrimaryOpd) {
                    operands.remove(rightOne);
                }
                BinaryCode b = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                operands.add(b);
                curFuncDefBb.addLocalVar(b);
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
                ((Immediate) leftOne).procValue(op.getOp(), (Immediate) rightOne);
                operands.add(leftOne);
            } else if (leftOne instanceof RetOpd && rightOne instanceof RetOpd){
                leftOne = new AssignCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne);
                operands.add(leftOne);
                curFuncDefBb.addLocalVar(leftOne);
                operands.addAll(operands2);
                operands.remove(rightOne);
                BinaryCode b = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                operands.add(b);
                curFuncDefBb.addLocalVar(b);
            }
            else {
                operands.addAll(operands2);
                if (rightOne instanceof PrimaryOpd) {
                    operands.remove(rightOne);
                }
                BinaryCode dst = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), leftOne, rightOne, op.getOp());
                curFuncDefBb.addLocalVar(dst);
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
                // 末尾可能有 PrimaryOpd
                // FuncCall ->  Ident '(' [FuncRParams] ')'
                curBBlock.setHasFuncCall(true); // todo check
                ArrayList<Operand> rParaCodes = new ArrayList<>();
                FuncRParams funcRParams = unaryExp.getFuncCall().getFuncRParams();
                for (Exp exp : funcRParams.getExps()) {
                    operands.addAll(analyseAddExp(exp.getAddExp()));
                    Operand lastOne = getLast(operands);
                    if (lastOne instanceof PrimaryOpd) {
                        operands.remove(lastOne);
                    }
                    // operands.add(new RParaCode(lastOne));
                    rParaCodes.add(new RParaCode(lastOne)); // 最后把参数一起push
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
                UnaryExp subUnary = unaryExp.getUnaryExp();
                while (subUnary.getType() == 2) {
                    // 异或一下
                    isNeg = isNeg ^ subUnary.getUnaryOp().getOp().equals("-");
                    subUnary = subUnary.getUnaryExp();
                }

                ArrayList<Operand> operands1 = analyseUnaryExp(subUnary); // recurrence
                Operand lastOne = getLast(operands1);

                // UnaryExp -> Num | UnaryOp UnaryExp
                if (lastOne instanceof Immediate) {
                    assert operands1.size() == 1;   // 应该只有这一个Immediate
                    ((Immediate) lastOne).procValue(isNeg);
                    operands.add(lastOne);
                } else {
                    // 其他 PrimaryOpd 情况或者 exp
                    operands.addAll(operands1);
                    if (isNeg) {
                        operands.remove(lastOne);
                        UnaryCode unaryCode = new UnaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), lastOne, MiddleCode.Op.SUB);
                        operands.add(unaryCode);
                        curFuncDefBb.addLocalVar(unaryCode);
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
                // 这里在等号右边，Operand，相当于数组读取。
                // z = a[x][y]
                LVal lVal = primaryExp.getlVal();
                operands.addAll(analyseLVal(lVal));
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
        switch (lVal.getDimension()) {
            case 0:
                // size = 1
                Symbol symbol = getSymbol(lVal.getIdent().getContent());
                // System.out.println(lVal.getIdent().getContent());
                if (symbol instanceof VarSymbol && ((VarSymbol) symbol).isConst()) {
                    operands.add(new Immediate(((VarSymbol) symbol).getValue(0)));
                } else {
                    operands.add(new LValOpd(getVarName(lVal.getIdent().getContent()), null));
                }
                break;
            case 1:
                Exp exp = lVal.getExps().get(0);
                ArrayList<Operand> operands1 = analyseExpression(exp);
                Operand lastOne = getLast(operands1);
                if (lastOne instanceof Immediate) {
                    // 索引是常数
                    symbol = getSymbol(lVal.getIdent().getContent());
                    if (symbol instanceof VarSymbol && ((VarSymbol) symbol).isConst()) {
                        operands.add(new Immediate(((VarSymbol) symbol).getValue(((Immediate) lastOne).getValue())));
                    } else {
                        operands.add(new LValOpd(getVarName(lVal.getIdent().getContent()), lastOne));
                    }
                } else {
                    if (lastOne instanceof PrimaryOpd) {
                        operands1.remove(lastOne);
                    }
                    operands.addAll(operands1);
                    // 没有检查会不会数组越界
                    operands.add(new LValOpd(getVarName(lVal.getIdent().getContent()), lastOne));
                }
                break;
            case 2:
                operands1 = analyseExpression(lVal.getExps().get(0));
                ArrayList<Operand> operands2 = analyseExpression(lVal.getExps().get(1));
                symbol = getSymbol(lVal.getIdent().getContent());
                Immediate colNum = null;
                // 左值只会是这两种之一
                if (symbol instanceof VarSymbol) {
                    colNum = new Immediate(((VarSymbol) symbol).getColNum());
                } else if (symbol instanceof FParamSymbol) {
                    colNum = new Immediate(((FParamSymbol) symbol).getColNum());
                }
                Operand x = getLast(operands1);
                Operand y = getLast(operands2);

                if (x instanceof PrimaryOpd) {
                    operands1.remove(x);
                }
                if (y instanceof PrimaryOpd) {
                    operands2.remove(y);
                }

                if (x instanceof Immediate && y instanceof Immediate) {
                    ((Immediate) x).procValue("+", (Immediate) y);
                    if (symbol instanceof VarSymbol && ((VarSymbol) symbol).isConst()) {
                        operands.add(new Immediate(((VarSymbol) symbol).getValue(((Immediate) x).getValue())));
                    } else {
                        operands.add(new LValOpd(getVarName(lVal.getIdent().getContent()), x));
                    }
                } else if (x instanceof Immediate) {
                    ((Immediate) x).procValue("*", colNum);
                    operands.addAll(operands1);
                    operands.addAll(operands2);
                    Operand t1 = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), x, y, "+");
                    operands.add(t1);
                    curFuncDefBb.addLocalVar(t1);
                    operands.add(new LValOpd(getVarName(lVal.getIdent().getContent()), t1));
                } else {
                    operands.addAll(operands1);
                    operands.addAll(operands2);
                    Operand t1 = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), x, colNum, "*");
                    Operand t2 = new BinaryCode(new VarName(middleTn.genTemporyName(), curBBlock.getDepth()), t1, y, "+");
                    operands.add(t1);
                    operands.add(t2);
                    curFuncDefBb.addLocalVar(t1);
                    curFuncDefBb.addLocalVar(t2);
                    operands.add(new LValOpd(getVarName(lVal.getIdent().getContent()), t2));
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

    public VarName getVarName(String name) {
        VarName res;
        if ((res = curFuncDefBb.getLocalVar(name, curBBlock.getDepth())) != null) {
            return res;
        }
        return new VarName(name, 0);
    }
}
