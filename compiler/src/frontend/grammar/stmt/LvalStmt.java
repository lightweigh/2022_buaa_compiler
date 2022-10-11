package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.LVal;
import frontend.grammar.exp.Exp;
import frontend.parser.Parser;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class LvalStmt extends Stmt {
    // Stmt → LVal '=' Exp ';'
    // Stmt → LVal '=' 'getint''('')'';'
    private LVal lVal = null;
    private Token assign;
    private Exp exp = null;
    private Token semicon=null;

    private boolean isGetInt=false;
    private Token getInt=null;
    private Token lParent;
    private Token rParent;

    public LVal getlVal() {
        return lVal;
    }

    public Token getAssign() {
        return assign;
    }

    public Exp getExp() {
        return exp;
    }

    public Token getSemicon() {
        return semicon;
    }

    public boolean isGetInt() {
        return isGetInt;
    }

    public Token getGetInt() {
        return getInt;
    }

    public Token getlParent() {
        return lParent;
    }

    public Token getrParent() {
        return rParent;
    }

    public void parser(LVal lVal) {
        // lVal = Parser.lValParser();
        this.lVal = lVal;
        assign = Lexer.tokenList.poll();
        if (Lexer.tokenList.equalPeekType(0, Token.Type.GETINTTK)) {
            getInt = Lexer.tokenList.poll();
            lParent = Lexer.tokenList.poll();
            rParent = Error.errorDetect(Token.Type.RPARENT);
            isGetInt = true;
        } else {
            exp = (Exp) Parser.expressionParser("Exp");
        }
        semicon = Error.errorDetect(Token.Type.SEMICN);
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        lVal.print(output);
        output.write(assign.toString());
        if (isGetInt) {
            output.write(getInt.toString());
            output.write(lParent.toString());
            output.write(rParent.toString());
        } else if (exp != null) {
            exp.print(output);
        }
        output.write(semicon.toString());
        super.print(output);
    }
}
