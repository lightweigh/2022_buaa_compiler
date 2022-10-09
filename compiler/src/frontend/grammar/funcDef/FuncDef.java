package frontend.grammar.funcDef;

import frontend.grammar.Block;
import frontend.grammar.Component;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncDef implements Component {
    //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncType funcType;
    private boolean needRet;
    private Ident ident;
    private Token lParent;
    private Token rParent;
    private FuncFParams funcFParams;
    private Block block;
    private boolean hasRet;
    private int retRow;

    public FuncDef(FuncType funcType, Ident ident, Token lParent,
                   Token rParent, FuncFParams funcFParams, Block block) {
        this.funcType = funcType;
        this.needRet = funcType.isNeedRet();
        this.ident = ident;
        this.lParent = lParent;
        this.rParent = rParent;
        this.funcFParams = funcFParams;
        this.block = block;
        this.hasRet = block.hasRetval();
        this.retRow = block.getRetRow();
    }

    public boolean isNeedRet() {
        return needRet;
    }

    public Ident getIdent() {
        return ident;
    }

    public Token getlParent() {
        return lParent;
    }

    public Token getrParent() {
        return rParent;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isHasRet() {
        return hasRet;
    }

    public boolean needRet() {
        return needRet;
    }

    public boolean hasRet() {
        return hasRet;
    }

    public int getRetRow() {
        return retRow;
    }

    public int getRBraceRow() {
        return block.getRBraceRow();
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public int getNameRow() {
        return ident.getRow();
    }

    public String getName() {
        return ident.getContent();
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams.getFuncFParams();
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        funcType.print(output);
        output.write(ident.toString());
        output.write(lParent.toString());
        if (funcFParams != null) {
            funcFParams.print(output);
        }
        output.write(rParent.toString());
        block.print(output);
        output.write("<FuncDef>\n");
    }

    @Override
    public String toString() {
        // FuncType Ident '(' [FuncFParams] ')' Block
        StringBuilder sb = new StringBuilder();
        sb.append(funcType.toString()).append(" ").append(ident.getContent()).append("(");
        if (funcFParams != null) {
            sb.append(funcFParams.toString());
        }
        sb.append(")");
        return sb.toString();
    }
}
