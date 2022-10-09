package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.Block;
import frontend.grammar.Component;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

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
                int i = 0;
                while (Lexer.tokenList.peek(i).getRefType() != Token.Type.SEMICN) {
                    if (Lexer.tokenList.peek(i).getRefType() == Token.Type.ASSIGN) {
                        LvalStmt lvalStmt = new LvalStmt();
                        lvalStmt.parser();
                        return lvalStmt;
                    }
                    i++;
                }
                ExpStmt expStmt = new ExpStmt();
                expStmt.parser();
                return expStmt;

        }
    }

    public void print(BufferedWriter output) throws IOException {
        output.write("<Stmt>\n");
    }
}
