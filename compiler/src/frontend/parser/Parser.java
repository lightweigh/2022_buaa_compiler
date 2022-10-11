package frontend.parser;

import frontend.Lexer;
import frontend.Error;
import frontend.grammar.*;
import frontend.grammar.decl.def.Def;
import frontend.grammar.decl.def.Variable;
import frontend.grammar.exp.*;
import frontend.grammar.exp.condExp.*;
import frontend.grammar.funcDef.FuncDef;
import frontend.grammar.funcDef.FuncFParam;
import frontend.grammar.funcDef.FuncFParams;
import frontend.grammar.funcDef.FuncType;
import frontend.grammar.decl.def.init.Init;
import frontend.grammar.decl.Decl;
import frontend.grammar.decl.def.init.Vector;
import frontend.token.Ident;
import frontend.token.IntConst;
import frontend.token.Token;

import java.util.ArrayList;

public class Parser {

    public static Decl declParser() {
        //  Decl → {'const'} BType Def { ',' Def } ';'
        Token constTK = null;
        if (Lexer.tokenList.peek(0).getRefType() == Token.Type.CONSTTK) {
            constTK = Lexer.tokenList.poll();
        }
        Token intTK = Lexer.tokenList.poll();
        ArrayList<Def> defs = new ArrayList<>();
        ArrayList<Token> seperators = new ArrayList<>();
        defs.add(defParser(constTK != null));
        while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            seperators.add(Lexer.tokenList.poll());
            defs.add(defParser(constTK != null));
        }
        Token semicon = Error.errorDetect(Token.Type.SEMICN);
        return new Decl(constTK, intTK, seperators, defs, semicon);
    }

    public static Def defParser(boolean isConst) {
        //  Def → Variable [ '=' InitVal ]
        Variable variable = variableParser();
        String type = isConst ? "ConstExp" : "Exp";
        Token assign = null;
        Init init = null;
        if (Lexer.tokenList.equalPeekType(0, Token.Type.ASSIGN)) {
            assign = Lexer.tokenList.poll();
            init = initParser(variable.getDimension(), type);
        }
        return new Def(variable, assign, init, isConst);
    }

    public static FuncDef funcDefParser() {
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncType funcType = new FuncType(Lexer.tokenList.poll());
        Ident ident = (Ident) Lexer.tokenList.poll();
        Token lParent = Lexer.tokenList.poll();
        FuncFParams funcFParams = funcFParamsParser();
        Token rParent = Error.errorDetect(Token.Type.RPARENT);
        Block block = new Block(false);
        block.parser();
        return new FuncDef(funcType, ident, lParent, rParent, funcFParams, block);
    }

    private static FuncFParams funcFParamsParser() {
        // FuncFParams → FuncFParam { ',' FuncFParam }
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        ArrayList<Token> seperators = new ArrayList<>();
        if (Lexer.tokenList.equalPeekType(0, Token.Type.INTTK)) {
            FuncFParam funcFParam = funcFParamParser();
            funcFParams.add(funcFParam);

            while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
                seperators.add(Lexer.tokenList.poll());
                funcFParams.add(funcFParamParser());
            }
        }
        return new FuncFParams(funcFParams, seperators);
    }

    private static FuncFParam funcFParamParser() {
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        Token intTK = Lexer.tokenList.poll();
        Ident ident = (Ident) Lexer.tokenList.poll();
        int type = 0;
        ArrayList<Token> bracks = new ArrayList<>();
        ConstExp constExp = null;
        while (Lexer.tokenList.equalPeekType(0, Token.Type.LBRACK)) {
            type++;
            bracks.add(Lexer.tokenList.poll()); // '['
            constExp = (ConstExp) Parser.expressionParser("ConstExp");
            Token rBrack = Error.errorDetect(Token.Type.RBRACK);
            if (rBrack != null) {
                bracks.add(rBrack); // ']'
            }
        }

        return new FuncFParam(intTK, ident, type, bracks, constExp);
    }

    private static Variable variableParser() {
        // Variable → Ident { '[' ConstExp ']' }
        Ident ident = (Ident) Lexer.tokenList.poll();
        ArrayList<Token> bracks = new ArrayList<>();
        ArrayList<ConstExp> constExps = new ArrayList<>();
        int dimension = 0;
        while (Lexer.tokenList.equalPeekType(0, Token.Type.LBRACK)) {
            dimension++;
            bracks.add(Lexer.tokenList.poll());
            constExps.add((ConstExp) expressionParser("ConstExp"));
            Token rBrack = Error.errorDetect(Token.Type.RBRACK);
            if (rBrack != null) {
                bracks.add(rBrack);
            }
        }
        return new Variable(ident, bracks, dimension, constExps);
    }

    private static Vector vectorParser(String type) {
        // Vector := '{'Expression {',' Expression}'}'  | Expression
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

    // parse ConstExp, Exp, AddExp
    public static Expression expressionParser(String type) {
        Token.Type tType;
        switch (type) {
            case "ConstExp":
                // ConstExp → AddExp
                AddExp addExp = (AddExp) expressionParser("AddExp");
                if (addExp != null) {
                    return new ConstExp(addExp);
                }
                return null;
            case "Exp":
                // Exp → AddExp
                addExp = (AddExp) expressionParser("AddExp");
                if (addExp != null) {
                    return new Exp(addExp);
                }
                return null;
            case "AddExp":
                // 判断FIRST集
                tType = Lexer.tokenList.peek(0).getRefType();
                if (tType != Token.Type.IDENFR && tType != Token.Type.LPARENT
                        && tType != Token.Type.INTCON && tType != Token.Type.PLUS
                        && tType != Token.Type.MINU && tType != Token.Type.NOT) {
                    return null;
                }
                // AddExp → MulExp | AddExp ('+' | '−') MulExp
                MulExp mulExp = (MulExp) expressionParser("MulExp");
                // if (mulExp != null) {
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
                // }
            case "MulExp":
                // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
                UnaryExp unaryExp = (UnaryExp) expressionParser("UnaryExp");
                if (unaryExp != null) {
                    expressions = new ArrayList<>();
                    operators = new ArrayList<>();
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
                // → FuncCall    1
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
                            (Exp) expressionParser("Exp"), Error.errorDetect(Token.Type.RPARENT));
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
        funcRParams = funcRParamsParser();
        Token rParent = Error.errorDetect(Token.Type.RPARENT);
        return new FuncCall(ident,lParent,funcRParams,rParent);
    }

    private static FuncRParams funcRParamsParser() {
        // FuncRParams → Exp { ',' Exp
        ArrayList<Exp> exps = new ArrayList<>();
        ArrayList<Token> seperators = new ArrayList<>();
        Exp exp = (Exp) expressionParser("Exp");
        if (exp != null) {
            exps.add(exp);
            while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
                seperators.add(Lexer.tokenList.poll());
                exps.add((Exp) expressionParser("Exp"));
            }
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
            Token rBrack = Error.errorDetect(Token.Type.RBRACK);
            if (rBrack != null) {
                bracks.add(rBrack);
            }
        }
        return new LVal(ident,bracks,dimension,exps);
    }

}
