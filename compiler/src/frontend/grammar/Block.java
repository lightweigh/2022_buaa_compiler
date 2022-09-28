package frontend.grammar;

import frontend.Lexer;
import frontend.grammar.stmt.Stmt;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Block extends Stmt {
    //  Block → '{' { BlockItem } '}'
    private Token lBrace;
    private Token rBrace;
    private ArrayList<BlockItem> blockItems = new ArrayList<>();
    private boolean isStmt; //

    public Block(boolean isStmt) {
        this.isStmt = isStmt;
    }

    public void parser() {
        lBrace = Lexer.tokenList.poll();
        while (Lexer.tokenList.peek(0).getRefType() != Token.Type.RBRACE) {
            BlockItem blockItem = new BlockItem();
            blockItem.parser();
            blockItems.add(blockItem);
        }
        rBrace = Lexer.tokenList.poll();
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(lBrace.toString());
        for (BlockItem blockItem : blockItems) {
            blockItem.print(output);
        }
        output.write(rBrace.toString());
        output.write("<Block>\n");
        if (isStmt) {
            super.print(output);
        }
    }
}
