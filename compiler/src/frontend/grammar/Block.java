package frontend.grammar;

import frontend.Lexer;
import frontend.grammar.stmt.ReturnStmt;
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
    private int retRow=-1;

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

    public Token getlBrace() {
        return lBrace;
    }

    public Token getrBrace() {
        return rBrace;
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public void addBlockItems(BlockItem blockItem) {
        blockItems.add(blockItem);
    }

    public boolean isStmt() {
        return isStmt;
    }

    public boolean hasRetval() {
        boolean hasRet=false;
        if (isStmt) {
            System.out.println("wrong using of hasRetval()!");
        }
        if (!blockItems.isEmpty()) {
            BlockItem blockItem = blockItems.get(blockItems.size()-1);
            if (!blockItem.isDecl() && blockItem.getStmt() instanceof ReturnStmt) {
                hasRet = ((ReturnStmt) blockItem.getStmt()).hasExp();
                retRow = ((ReturnStmt) blockItem.getStmt()).getRetRow();
            }
        }
        return hasRet;
    }

    public boolean rightRet(boolean needRet) {
        boolean rightRet = false;
        if (!blockItems.isEmpty()) {
            BlockItem blockItem = blockItems.get(blockItems.size()-1);
            if (!blockItem.isDecl() && blockItem.getStmt() instanceof ReturnStmt) {
                if (needRet || !((ReturnStmt) blockItem.getStmt()).hasExp()) {
                    rightRet = true;
                }
                retRow = ((ReturnStmt) blockItem.getStmt()).getRetRow();
            }
        }
        return rightRet;
    }

    public int getRetRow() {
        return retRow;
    }

    public int getRBraceRow() {
        return rBrace.getRow();
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
