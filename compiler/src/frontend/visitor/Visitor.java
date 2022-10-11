package frontend.visitor;

import frontend.Error;
import frontend.grammar.*;
import frontend.grammar.decl.Decl;
import frontend.grammar.decl.def.Def;
import frontend.grammar.decl.def.Variable;
import frontend.grammar.exp.*;
import frontend.grammar.funcDef.FuncDef;
import frontend.grammar.funcDef.FuncFParam;
import frontend.grammar.stmt.*;
import frontend.symbol.*;

import java.util.ArrayList;
import java.util.Iterator;

public class Visitor {
    private SymTable curSymTable;

    public Visitor() {
        this.curSymTable = new SymTable(null);
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
        SymTable newBlock = new SymTable(curSymTable);
        for (FuncFParam funcFParam : funcDef.getFuncFParams()) {
            // 函数参数的 名字重定义 是怎样的呢？ todo 名字重定义
            // 函数参数在当前域下能不能重命名？
            VarSymbol fParamSymbol = new VarSymbol(funcFParam.getIdent().getContent(), false, funcFParam.getType());
            if (newBlock.hasSymbol(funcFParam.getIdent().getContent())) {
                Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                        funcFParam.getIdent().getRow()));
            } else {
                newBlock.addSymbol(fParamSymbol);
            }
            funcSymbol.addFParam(new FParamSymbol(funcFParam.getIdent().getContent(),
                    funcFParam.getType()));
        }
        visitBlock(funcDef.getBlock(), newBlock, false);
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
        if (stmt instanceof Block) {
            SymTable newTable = new SymTable(curSymTable);
            visitBlock((Block) stmt, newTable, isInwhile);
        } else if (stmt instanceof ExpStmt && ((ExpStmt) stmt).getExp() != null) {
            visitAddExp(((ExpStmt) stmt).getExp().getAddExp());
        } else if (stmt instanceof LvalStmt) {
            visitLVal(((LvalStmt) stmt).getlVal(), true);
            if (!((LvalStmt) stmt).isGetInt()) {
                visitAddExp(((LvalStmt) stmt).getExp().getAddExp());
            }
        } else if (stmt instanceof WhileStmt) {
            visitStmt(((WhileStmt) stmt).getStmt(), true);
        } else if (stmt instanceof BreakOrContinueStmt) {
            if (!isInwhile) {
                Error.errorTable.add(new Error(Error.ErrorType.WRONG_BREAK_CONTINUE, ((BreakOrContinueStmt) stmt).getRow()));
            }
        } else if (stmt instanceof IfStmt) {
            visitStmt(((IfStmt) stmt).getStmt(),isInwhile);
            if (((IfStmt) stmt).hasElse()) {
                visitStmt(((IfStmt) stmt).getElseStmt(),isInwhile);
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
        visitVariable(def.getVariable(), isConst);
        /*if (def.getInit() != null) {

        }*/
    }

    private void visitVariable(Variable variable, boolean isConst) {
        // Variable → Ident { '[' ConstExp ']' }
        if (curSymTable.hasSymbol(variable.getIdent().getContent())) {
            Error.errorTable.add(new Error(Error.ErrorType.NAME_REDEF,
                    variable.getIdent().getRow()));
        } else {
            curSymTable.addSymbol(new VarSymbol(variable.getIdent().getContent(),
                    isConst, variable.getDimension()));
        }
    }
}
