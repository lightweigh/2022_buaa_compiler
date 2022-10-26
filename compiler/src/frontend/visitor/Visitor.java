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
import middle.MiddleTn;
import middle.quartercode.*;
import middle.quartercode.array.ArrayAssign;
import middle.quartercode.array.ArrayDef;
import middle.quartercode.array.ArrayStore;
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
    private MiddleTn middleTn;
    private HashMap<String, MiddleCode> constStr;
    private int constStringCnt = 0;

    public Visitor() {
        this.curSymTable = new SymTable(null);
        this.globalSymTable = curSymTable;
        this.curBBlock = new BasicBlock("global_block_");
        this.globalBlock = curBBlock;
        this.middleTn = new MiddleTn();
        this.constStr = new HashMap<>();
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
        // 如果有 int main = 1;   那么？ todo think
        //  MainFuncDef → 'int' 'main' '(' ')' Block
        if (curSymTable.hasSymbol("main")) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                    mainFuncDef.getMainTK().getRow()));
        }
        SymTable newTable = new SymTable(curSymTable);
        curBBlock.append(new FuncDefCode("main", new FuncType(mainFuncDef.getIntTK())));
        visitBlock(mainFuncDef.getBlock(), newTable, false);
    }

    private void visitFuncDef(FuncDef funcDef) {
        //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncSymbol funcSymbol = new FuncSymbol(funcDef.getFuncType(), funcDef.getName());
        // 函数名  ->  符号表
        if (curSymTable.hasSymbol(funcSymbol.getSymName())) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF, funcDef.getNameRow()));
        } else {
            curSymTable.addSymbol(funcSymbol);
        }
        curBBlock.append(new FuncDefCode(funcDef.getName(), funcDef.getFuncType())); // todo 我想不要这个东西，我的BasicBlock还没整呢
        SymTable newSymTable = new SymTable(curSymTable);
        for (FuncFParam funcFParam : funcDef.getFuncFParams()) {
            // todo modified
            int type = funcFParam.getType();
            int colNum = 1;
            if (type == 2) {
                ArrayList<Operand> operands = analyseExpression(funcFParam.getConstExp());
                colNum = ((Immediate) operands.get(0)).getValue();
            }
            FParamSymbol fParamSymbol = new FParamSymbol(funcFParam.getIdent().getContent(), funcFParam.getType(), colNum);
            if (newSymTable.hasSymbol(funcFParam.getIdent().getContent())) {
                Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                        funcFParam.getIdent().getRow()));
            } else {
                newSymTable.addSymbol(fParamSymbol);
            }
            funcSymbol.addFParam(new FParamSymbol(funcFParam.getIdent().getContent(),
                    funcFParam.getType(),colNum));
            curBBlock.append(new FParaCode(funcFParam.getIdent().getContent(), funcFParam.getType() != 0));
        }
        visitBlock(funcDef.getBlock(), newSymTable, false);
    }

    private void visitBlock(Block block, SymTable newTable, boolean isInwhile) {
        //  Block → '{' { BlockItem } '}'
        SymTable prevTable = curSymTable;
        curSymTable = newTable;
        for (BlockItem blockItem : block.getBlockItems()) {
            if (blockItem.isDecl()) {
                visitDecl(blockItem.getDecl());
            } else {
                visitStmt(blockItem.getStmt(), isInwhile);
            }
        }
        curSymTable = prevTable;
    }

    private void visitStmt(Stmt stmt, boolean isInwhile) {
        middleTn.clear();
        if (stmt instanceof Block) {
            SymTable newTable = new SymTable(curSymTable);
            visitBlock((Block) stmt, newTable, isInwhile);
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
                    curBBlock.append(new AssignCode(lastOfLVal.getName(), lastOfExp));
                } else {
                    curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, lastOfExp));
                }
            } else {
                MiddleCode t1 = new ReadIn(middleTn.genTemporyName());
                curBBlock.append(t1);
                ArrayList<Operand> lValOps = analyseLVal(lVal);
                Operand lastOfLVal = getLast(lValOps);
                assert lastOfLVal instanceof LValOpd;
                lValOps.remove(lastOfLVal);
                for (Operand operand : lValOps) {
                    curBBlock.append((MiddleCode) operand);
                }
                if (((LValOpd) lastOfLVal).getIdx() == null) {
                    curBBlock.append(new AssignCode(lastOfLVal.getName(), t1));
                } else {
                    curBBlock.append(new ArrayStore((PrimaryOpd) lastOfLVal, t1));
                }
            }
        } else if (stmt instanceof WhileStmt) {
            visitStmt(((WhileStmt) stmt).getStmt(), true);
        } else if (stmt instanceof BreakOrContinueStmt) {
            if (!isInwhile) {
                Error.errorTable.add(new Error(Error.ErrorType.WRONG_BREAK_CONTINUE, ((BreakOrContinueStmt) stmt).getRow()));
            }
        } else if (stmt instanceof IfStmt) {
            visitStmt(((IfStmt) stmt).getStmt(), isInwhile);
            if (((IfStmt) stmt).hasElse()) {
                visitStmt(((IfStmt) stmt).getElseStmt(), isInwhile);
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
                        str = new ConstStrCode("str_" + constStringCnt++, sub);
                        constStr.put(sub, str);
                    }
                    str = (ConstStrCode) constStr.get(sub);
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
                    str = new ConstStrCode("str_" + constStringCnt++, sub);
                    constStr.put(sub, str);
                }
                str = (ConstStrCode) constStr.get(sub);
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
                operands.add(new RetCode(lastOne));
                for (Operand operand : operands) {
                    curBBlock.append((MiddleCode) operand);
                }
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
            if (((VarSymbol) lvalSym).isConst() && isLeft) {
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
            UnaryOp unaryOp = unaryExp.getUnaryOp();
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
            // 如果没有被初始化,那么也不需要给它构造四元式.吗?
            // middleTn 初始化? todo
            middleTn.clear();
            if (def.getInit() == null) {
                curBBlock.append(new ConstVar(isConst, def.getVariable().getIdent(),null));
            }
            if (def.getInit() != null) {
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
                        curBBlock.append(new ConstVar(isConst, def.getVariable().getIdent(), lastOne));
                        break;
                    case 1:
                        curBBlock.append(new ArrayDef(isConst, def.getVariable().getIdent(), size));
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
                            }
                            curBBlock.append(new ArrayAssign(def.getVariable().getIdent(), i, lastOne));
                        }
                        break;
                    case 2:
                        curBBlock.append(new ArrayDef(isConst, def.getVariable().getIdent(), size));
                        int cnt = 0;
                        for (Vector vector : init.getVectors()) {
                            expressions = vector.getExpressions();
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
                                }
                                curBBlock.append(new ArrayAssign(def.getVariable().getIdent(), cnt++, lastOne));
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
            } else {
                operands.addAll(operands2);
                if (rightOne instanceof PrimaryOpd) {
                    operands.remove(rightOne);
                }
                operands.add(new BinaryCode(middleTn.genTemporyName(), leftOne, rightOne, op.getOp()));
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
            } else {
                operands.addAll(operands2);
                if (rightOne instanceof PrimaryOpd) {
                    operands.remove(rightOne);
                }
                operands.add(new BinaryCode(middleTn.genTemporyName(), leftOne, rightOne, op.getOp()));
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
                FuncRParams funcRParams = unaryExp.getFuncCall().getFuncRParams();
                for (Exp exp : funcRParams.getExps()) {
                    operands.addAll(analyseAddExp(exp.getAddExp()));
                    Operand lastOne = getLast(operands);
                    if (lastOne instanceof PrimaryOpd) {
                        operands.remove(lastOne);
                    }
                    operands.add(new RParaCode(lastOne));
                }
                // todo 这个FuncCallCode 的属性应该是要改一改的
                String funcName = unaryExp.getFuncCall().getIdent().getContent();
                operands.add(new FuncCallCode(funcName));
                assert getSymbol(funcName) instanceof FuncSymbol;
                FuncSymbol funcSymbol = (FuncSymbol) getSymbol(funcName);
                assert funcSymbol != null;
                if (funcSymbol.isInt()) {
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
                        operands.add(new UnaryCode(middleTn.genTemporyName(), lastOne, MiddleCode.Op.SUB));
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
                    operands.add(new LValOpd(lVal.getIdent().getContent(), null));
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
                        operands.add(new LValOpd(lVal.getIdent().getContent(), lastOne));
                    }
                } else {
                    if (lastOne instanceof PrimaryOpd) {
                        operands1.remove(lastOne);
                    }
                    operands.addAll(operands1);
                    // 没有检查会不会数组越界
                    operands.add(new LValOpd(lVal.getIdent().getContent(), lastOne));
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
                        operands.add(new LValOpd(lVal.getIdent().getContent(), x));
                    }
                } else if (x instanceof Immediate) {
                    ((Immediate) x).procValue("*", colNum);
                    operands.addAll(operands1);
                    operands.addAll(operands2);
                    Operand t1 = new BinaryCode(middleTn.genTemporyName(), x, y, "+");
                    operands.add(t1);
                    operands.add(new LValOpd(lVal.getIdent().getContent(), t1));
                } else {
                    operands.addAll(operands1);
                    operands.addAll(operands2);
                    Operand t1 = new BinaryCode(middleTn.genTemporyName(), x, colNum, "*");
                    Operand t2 = new BinaryCode(middleTn.genTemporyName(), t1, y, "+");
                    operands.add(t1);
                    operands.add(t2);
                    operands.add(new LValOpd(lVal.getIdent().getContent(), t2));
                }
                break;
        }
        return operands;
    }

    public BasicBlock getGlobalBlock() {
        return globalBlock;
    }

    public HashMap<String, MiddleCode> getConstStr() {
        return constStr;
    }
}
