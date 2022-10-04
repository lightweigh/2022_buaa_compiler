package frontend.parser;

import frontend.Lexer;
import frontend.Error;
import frontend.grammar.*;
import frontend.grammar.def.Variable;
import frontend.grammar.exp.*;
import frontend.grammar.exp.condExp.*;
import frontend.grammar.init.Init;
import frontend.grammar.decl.ConstDecl;
import frontend.grammar.decl.Decl;
import frontend.grammar.decl.VarDecl;
import frontend.grammar.def.ConstDef;
import frontend.grammar.def.VarDef;
import frontend.grammar.init.Vector;
import frontend.token.Ident;
import frontend.token.IntConst;
import frontend.token.Token;

import java.util.ArrayList;

public class Parser {

    public static Decl declParser() {
        if (Lexer.tokenList.peek(0).getRefType() == Token.Type.CONSTTK) {
            return constDeclParser();
        } else if (Lexer.tokenList.peek(0).getRefType() == Token.Type.INTTK) {
            return valDeclParser();
        } else {
            return null;
        }
    }

    public static FuncDef funcDefParser() {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncType funcType = new FuncType(Lexer.tokenList.poll());
        Ident ident = (Ident) Lexer.tokenList.poll();
        Token lParent = Lexer.tokenList.poll();
        FuncFParams funcFParams = null;
        if (!Lexer.tokenList.equalPeekType(0, Token.Type.RPARENT)) {
            funcFParams = funcFParamsParser();
        }
        Token rParent = Lexer.tokenList.poll();
        Block block = new Block(false);
        block.parser();
        return new FuncDef(funcType, ident, lParent, rParent, funcFParams, block);
    }

    private static FuncFParams funcFParamsParser() {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        ArrayList<Token> seperators = new ArrayList<>();
        FuncFParam funcFParam = funcFParamParser();
        funcFParams.add(funcFParam);

        while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            seperators.add(Lexer.tokenList.poll());
            funcFParams.add(funcFParamParser());
        }
        return new FuncFParams(funcFParams, seperators);
    }

    private static FuncFParam funcFParamParser() {
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        Token intTK = Lexer.tokenList.poll();
        Ident ident = (Ident) Lexer.tokenList.poll();
        int dimension = 0;
        ArrayList<Token> bracks = new ArrayList<>();
        ConstExp constExp = null;
        while (Lexer.tokenList.equalPeekType(0, Token.Type.LBRACK)) {
            dimension++;
            bracks.add(Lexer.tokenList.poll()); // '['
            if (Lexer.tokenList.peek(0).getRefType() != Token.Type.RBRACK) {
                constExp = (ConstExp) Parser.expressionParser("ConstExp");
            }
            bracks.add(Lexer.tokenList.poll()); // ']'
        }

        return new FuncFParam(intTK, ident, dimension, bracks, constExp);
    }

    private static Variable variableParser() {
        Ident ident = (Ident) Lexer.tokenList.poll();
        ArrayList<Token> bracks = new ArrayList<>();
        ArrayList<ConstExp> constExps = new ArrayList<>();
        int dimension = 0;
        while (Lexer.tokenList.equalPeekType(0, Token.Type.LBRACK)) {
            dimension++;
            bracks.add(Lexer.tokenList.poll());
            constExps.add((ConstExp) expressionParser("ConstExp"));
            Token rBrack = Error.errorDetect(']');
            bracks.add(rBrack);
        }
        return new Variable(ident, bracks, dimension, constExps);
    }

    private static Decl constDeclParser() {
        //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        Token constTK = Lexer.tokenList.poll();
        Token intTK = Lexer.tokenList.poll();
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        ArrayList<Token> seperators = new ArrayList<>();
        ConstDef constDef = constDefParser();
        constDefs.add(constDef);
        while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            seperators.add(Lexer.tokenList.poll());
            constDefs.add(constDefParser());
        }
        Token semicon = Error.errorDetect(';');
        return new ConstDecl(constTK, intTK, seperators, constDefs, semicon);
    }

    private static ConstDef constDefParser() {
        //  ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        assert Lexer.tokenList.peek(0).getRefType() != Token.Type.IDENFR;
        // TODO Error b
        // 函数名或者变量名在当前作用域下重复定义。
        // 报错行号为<Ident>所在行数。
        Variable variable = variableParser();
        return new ConstDef(variable, Lexer.tokenList.poll(),
                initParser(variable.getDimension(),"ConstExp"));
    }

    private static Vector vectorParser(String type) {
        boolean hasBrace = false;
        Token lBrace = null,rBrace=null;
        if (Lexer.tokenList.equalPeekType(0, Token.Type.LBRACE)) {
            lBrace = Lexer.tokenList.poll();
            hasBrace = true;
        }
        ArrayList<Expression> expressions = new ArrayList<>();
        ArrayList<Token> seperators = new ArrayList<>();
        if (Lexer.tokenList.equalPeekType(0, Token.Type.RBRACE)) {
            rBrace = Lexer.tokenList.poll();
            return new Vector(lBrace,expressions,seperators,rBrace,type);
        }
        expressions.add(expressionParser(type));    // wrong occurs
        while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            seperators.add(Lexer.tokenList.poll());
            expressions.add(expressionParser(type));
        }

        if (hasBrace) {
            rBrace = Lexer.tokenList.poll();
        }
        return new Vector(lBrace, expressions, seperators, rBrace, type);

    }

    private static Init initParser(int dimension, String type) {
        // Init     :=  Expression | '{' [Init {',' Init} ] '}'

        switch (dimension) {
            case 0:
                Expression scalar = expressionParser(type);
                return new Init(type.equals("ConstExp"), scalar);
            case 1:
                Vector vector = vectorParser(type);
                return new Init(type.equals("ConstExp"), vector);
            case 2:
                assert Lexer.tokenList.equalPeekType(0, Token.Type.LBRACE);
                Token lBrace = Lexer.tokenList.poll();
                ArrayList<Vector> vectors = new ArrayList<>();
                ArrayList<Token> seperators = new ArrayList<>();
                vectors.add(vectorParser(type));
                while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
                    seperators.add(Lexer.tokenList.poll());
                    vectors.add(vectorParser(type));
                }
                Token rBrace = Lexer.tokenList.poll();
                return new Init(type.equals("ConstExp"), lBrace, vectors, seperators, rBrace);
        }
        return null;
    }

    private static Decl valDeclParser() {
        //  VarDecl → BType VarDef { ',' VarDef } ';'
        Token intTK = Lexer.tokenList.poll();
        ArrayList<Token> seperators = new ArrayList<>();
        ArrayList<VarDef> varDefs = new ArrayList<>();
        varDefs.add(varDefParser());
        while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            seperators.add(Lexer.tokenList.poll());
            varDefs.add(varDefParser());
        }
        Token semicon = Error.errorDetect(';');
        return new VarDecl(intTK, varDefs, seperators, semicon);
    }

    private static VarDef varDefParser() {
        //  VarDef → Ident { '[' ConstExp ']' } |
        //  Ident { '[' ConstExp ']' } '=' InitVal
        Variable variable = variableParser();
        if (Lexer.tokenList.equalPeekType(0, Token.Type.ASSIGN)) {
            Token assign = Lexer.tokenList.poll();
            Init initVal = initParser(variable.getDimension(), "Exp");
            return new VarDef(variable, assign, initVal);
        } else {
            return new VarDef(variable);
        }
    }

    // parse ConstExp, Exp, AddExp
    public static Expression expressionParser(String type) {
        Token.Type tType;
        switch (type) {
            case "ConstExp":
                // ConstExp → AddExp
                ConstExp constExp = new ConstExp();
                constExp.setAddExp((AddExp) expressionParser("AddExp"));
                return constExp;
            case "Exp":
                // Exp → AddExp
                Exp exp = null;
                AddExp addExp = (AddExp) expressionParser("AddExp");
                if (addExp != null) {
                    exp = new Exp();
                    exp.setAddExp(addExp);
                }
                return exp;
            case "AddExp":
                // AddExp → MulExp | AddExp ('+' | '−') MulExp
                MulExp mulExp = (MulExp) expressionParser("MulExp");
                if (mulExp != null) {
                    ArrayList<Expression> expressions = new ArrayList<>();
                    ArrayList<Operator> operators = new ArrayList<>();
                    expressions.add(mulExp);

                    tType = Lexer.tokenList.peek(0).getRefType();
                    while (tType == Token.Type.PLUS || tType == Token.Type.MINU) {
                        operators.add(new Operator(Lexer.tokenList.poll()));// '+' | '-'
                        expressions.add(expressionParser("MulExp"));
                        tType = Lexer.tokenList.peek(0).getRefType();
                    }
                    return new AddExp(expressions, operators);
                }
                return null;
            case "MulExp":
                // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
                UnaryExp unaryExp = (UnaryExp) expressionParser("UnaryExp");
                if (unaryExp != null) {
                    ArrayList<Expression> expressions = new ArrayList<>();
                    ArrayList<Operator> operators = new ArrayList<>();
                    expressions.add(unaryExp);

                    tType = Lexer.tokenList.peek(0).getRefType();
                    while (tType == Token.Type.MULT || tType == Token.Type.DIV || tType == Token.Type.MOD) {
                        operators.add(new Operator(Lexer.tokenList.poll()));// '*' | '/' | '%'
                        expressions.add(expressionParser("UnaryExp"));
                        tType = Lexer.tokenList.peek(0).getRefType();
                    }
                    return new MulExp(expressions, operators);
                }
                return null;
            case "UnaryExp":
                // UnaryExp → PrimaryExp    0
                // → Ident '(' [FuncRParams] ')'    1
                // → UnaryOp UnaryExp   2
                tType = Lexer.tokenList.peek(0).getRefType();
                if (tType == Token.Type.PLUS || tType == Token.Type.MINU || tType == Token.Type.NOT) {
                    return new UnaryExp(new UnaryOp(Lexer.tokenList.poll()),(UnaryExp) expressionParser("UnaryExp"));
                } else if (tType == Token.Type.IDENFR &&
                        Lexer.tokenList.peek(1).getRefType() == Token.Type.LPARENT) {
                    return new UnaryExp(funcCallParser());
                } else {
                    PrimaryExp primaryExp = (PrimaryExp) expressionParser("PrimaryExp");
                    if (primaryExp!= null) {
                        return new UnaryExp(primaryExp);
                    }
                }
                return null;
            case "PrimaryExp":
                // PrimaryExp → '(' Exp ')'     0
                // → LVal   1
                // → Num 2
                tType = Lexer.tokenList.peek(0).getRefType();
                if (tType == Token.Type.LPARENT) {
                    return new PrimaryExp(Lexer.tokenList.poll(),
                            (Exp) expressionParser("Exp"), Lexer.tokenList.poll());
                } else if (tType == Token.Type.INTCON) {
                    return new PrimaryExp(new Num((IntConst) Lexer.tokenList.poll()));
                } else {
                    return new PrimaryExp(lValParser());
                }
        }
        return null;
    }

    public static CondExp condExpParser(String type) {
        switch (type) {
            case "Cond":
                // Cond → LOrExp
                return new Cond((LOrExp) condExpParser("LOrExp"));
            case "LOrExp":
                // LOrExp → LAndExp | LOrExp '||' LAndExp
                ArrayList<LAndExp> lAndExps = new ArrayList<>();
                ArrayList<Token> seperators= new ArrayList<>();
                lAndExps.add((LAndExp) condExpParser("LAndExp"));
                while (Lexer.tokenList.equalPeekType(0, Token.Type.OR)) {
                    seperators.add(Lexer.tokenList.poll());
                    lAndExps.add((LAndExp) condExpParser("LAndExp"));
                }
                return new LOrExp(lAndExps,seperators);
            case "LAndExp":
                // LAndExp → EqExp | LAndExp '&&' EqExp
                ArrayList<EqExp> eqExps = new ArrayList<>();
                seperators = new ArrayList<>();
                eqExps.add((EqExp) condExpParser("EqExp"));
                while (Lexer.tokenList.equalPeekType(0, Token.Type.AND)) {
                    seperators.add(Lexer.tokenList.poll());
                    eqExps.add((EqExp) condExpParser("EqExp"));
                }
                return new LAndExp(eqExps, seperators);
            case "EqExp":
                // EqExp → RelExp | EqExp ('==' | '!=') RelExp
                ArrayList<RelExp> relExps = new ArrayList<>();
                ArrayList<CondOp> condOps = new ArrayList<>();
                relExps.add((RelExp) condExpParser("RelExp"));
                Token.Type tType = Lexer.tokenList.peek(0).getRefType();
                while (tType == Token.Type.EQL || tType == Token.Type.NEQ) {
                    condOps.add(new CondOp(Lexer.tokenList.poll()));
                    relExps.add((RelExp) condExpParser("RelExp"));
                    tType = Lexer.tokenList.peek(0).getRefType();
                }
                return new EqExp(relExps, condOps);
            case "RelExp":
                // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
                ArrayList<CondExp> addExps = new ArrayList<>();
                condOps = new ArrayList<>();
                addExps.add((AddExp) expressionParser("AddExp"));
                tType = Lexer.tokenList.peek(0).getRefType();
                while (tType == Token.Type.GEQ || tType == Token.Type.LSS ||
                        tType == Token.Type.GRE || tType == Token.Type.LEQ) {
                    condOps.add(new CondOp(Lexer.tokenList.poll()));
                    addExps.add((AddExp) expressionParser("AddExp"));
                    tType = Lexer.tokenList.peek(0).getRefType();
                }
                return new RelExp(addExps, condOps);
        }
        return null;
    }

    private static FuncCall funcCallParser() {
        // FuncCall → Ident '(' [FuncRParams] ')'
        Ident ident = (Ident) Lexer.tokenList.poll();
        Token lParent = Lexer.tokenList.poll();
        FuncRParams funcRParams=null;
        if (!Lexer.tokenList.equalPeekType(0, Token.Type.RPARENT)) {
            funcRParams = funcRParamsParser();
        }
        Token rParent = Lexer.tokenList.poll();
        return new FuncCall(ident,lParent,funcRParams,rParent);
    }

    private static FuncRParams funcRParamsParser() {
        // FuncRParams → Exp { ',' Exp
        ArrayList<Exp> exps = new ArrayList<>();
        ArrayList<Token> seperators = new ArrayList<>();
        exps.add((Exp) expressionParser("Exp"));
        while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            seperators.add(Lexer.tokenList.poll());
            exps.add((Exp) expressionParser("Exp"));
        }
        return new FuncRParams(exps,seperators);
    }

    public static LVal lValParser() {
        // LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
        Ident ident = (Ident) Lexer.tokenList.poll();
        ArrayList<Token> bracks = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();
        int dimension = 0;
        while (Lexer.tokenList.equalPeekType(0, Token.Type.LBRACK)) {
            dimension++;
            bracks.add(Lexer.tokenList.poll());
            exps.add((Exp) expressionParser("Exp"));
            Token rBrack = Error.errorDetect(']');
            bracks.add(rBrack);
        }
        return new LVal(ident,bracks,dimension,exps);
    }

}
