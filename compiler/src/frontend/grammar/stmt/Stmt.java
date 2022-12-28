package frontend.grammar.stmt;

import backend.mipsCode.instruction.Add;
import backend.mipsCode.instruction.Mul;
import frontend.Error;
import frontend.Lexer;
import frontend.grammar.Block;
import frontend.grammar.Component;
import frontend.grammar.LVal;
import frontend.grammar.Num;
import frontend.grammar.exp.*;
import frontend.parser.Parser;
import frontend.token.Ident;
import frontend.token.IntConst;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Stmt implements Component {

    public static Stmt stmtParser() {
        Token.Type type = Lexer.tokenList.peek(0).getRefType();
        switch (type) {
            case LBRACE:
                // Stmt → Block
                Block block = new Block(true);
                block.parser();
                return block;
            case IFTK:
                // Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                IfStmt ifStmt = new IfStmt();
                ifStmt.parser();
                return ifStmt;
            case WHILETK:
                //  'while' '(' Cond ')' Stmt
                WhileStmt whileStmt = new WhileStmt();
                whileStmt.parser();
                return whileStmt;
            case BREAKTK:
            case CONTINUETK:
                // 'break' ';' | 'continue' ';'
                BreakOrContinueStmt breakOrContinueStmt = new BreakOrContinueStmt();
                breakOrContinueStmt.parser();
                return breakOrContinueStmt;
            case RETURNTK:
                //  'return' [Exp] ';'
                ReturnStmt returnStmt = new ReturnStmt();
                returnStmt.parser();
                return returnStmt;
            case PRINTFTK:
                // 'printf''('FormatString{','Exp}')'';'
                PrintfStmt printfStmt = new PrintfStmt();
                printfStmt.parser();
                Error.errorDetect(printfStmt, "printfStmt");
                return printfStmt;
            default:
                // Stmt → LVal '=' Exp ';'
                // Stmt → Exp ';'
                int curPos = Lexer.tokenList.getPos();
                if (Lexer.tokenList.equalPeekType(0, Token.Type.IDENFR) &&
                        !Lexer.tokenList.equalPeekType(1, Token.Type.LPARENT)) {
                    // 可能是 LVal '=' Exp  也可能是 LVal, 还可能是 [Exp];  多往前看几步
                    LVal lVal = Parser.lValParser();
                    if (Lexer.tokenList.equalPeekType(0, Token.Type.ASSIGN)) {
                        LvalStmt lvalStmt = new LvalStmt();
                        lvalStmt.parser(lVal);
                        return lvalStmt;
                    } else if (Lexer.tokenList.equalPeekType(0, Token.Type.PLUS) &&
                            Lexer.tokenList.equalPeekType(1, Token.Type.PLUS) &&
                            Lexer.tokenList.equalPeekType(2, Token.Type.SEMICN)) {
                        Token plus1 = Lexer.tokenList.poll();
                        Token plus2 = Lexer.tokenList.poll();
                        Token semicon = Lexer.tokenList.poll();
                        Exp exp = genExpFromLVal(lVal, true);
                        return new LvalStmt(lVal, exp);
                    } else if (Lexer.tokenList.equalPeekType(0, Token.Type.MINU) &&
                            Lexer.tokenList.equalPeekType(1, Token.Type.MINU) &&
                            Lexer.tokenList.equalPeekType(2, Token.Type.SEMICN)) {
                        Token minus1 = Lexer.tokenList.poll();
                        Token minus2 = Lexer.tokenList.poll();
                        Token semicon = Lexer.tokenList.poll();
                        Exp exp = genExpFromLVal(lVal, false);
                        return new LvalStmt(lVal, exp);
                    }
                }
                Lexer.tokenList.setPos(curPos);
                ExpStmt expStmt = new ExpStmt();
                expStmt.parser();
                return expStmt;

        }
    }

    private static Exp genExpFromLVal(LVal lval, boolean isIncrease) {
        PrimaryExp p = new PrimaryExp(lval);
        PrimaryExp numP = new PrimaryExp(new Num(new IntConst(Token.Type.INTCON, -1, "1")));
        UnaryExp u = new UnaryExp(p);
        UnaryExp numU = new UnaryExp(numP);
        MulExp m = new MulExp(new ArrayList<>(Collections.singletonList(u)), new ArrayList<>());
        MulExp numM = new MulExp(new ArrayList<>(Collections.singletonList(numU)), new ArrayList<>());
        AddExp a = new AddExp(new ArrayList<>(Arrays.asList(m, numM)),
                new ArrayList<>(Collections.singletonList(isIncrease ?
                        new Operator(new Token(Token.Type.PLUS, -1, "+")) :
                        new Operator(new Token(Token.Type.MINU, -1, "-")))));
        return new Exp(a);
    }

    public void print(BufferedWriter output) throws IOException {
        output.write("<Stmt>\n");
    }
}
