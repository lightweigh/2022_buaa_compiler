package frontend.grammar;

import frontend.Lexer;
import frontend.grammar.decl.Decl;
import frontend.grammar.stmt.*;
import frontend.parser.Parser;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class BlockItem {
    //  BlockItem → Decl | Stmt
    private Decl decl = null;
    private Stmt stmt = null;

    public BlockItem() {
    }

    public BlockItem(Stmt stmt) {
        this.stmt = stmt;
    }

    public void parser() {
        Token.Type type = Lexer.tokenList.peek(0).getRefType();
        if (type == Token.Type.CONSTTK || type == Token.Type.INTTK) {
            decl = Parser.declParser();
        } else {
            stmt = Stmt.stmtParser();
        }
    }

    public boolean isDecl() {
        return decl != null;
    }

    public Decl getDecl() {
        return decl;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public void print(BufferedWriter output) throws IOException {
        if (decl != null) {
            decl.print(output);
        } else {
            // stmt
            stmt.print(output);
        }
    }

    @Override
    public String toString() {
        if (decl != null) {
            return decl.toString();
        } else {
            return stmt.toString();
        }
    }
}
