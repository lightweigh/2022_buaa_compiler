package frontend.grammar;

import frontend.Error;
import frontend.Lexer;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class MainFuncDef implements Component{
    //  MainFuncDef → 'int' 'main' '(' ')' Block
    private Token intTK;
    private Token mainTK;
    private Token lParent;
    private Token rParent;
    private Block block = new Block(false);
    private boolean hasRet;
    private int retRow;

    public void parser() {
        intTK = Lexer.tokenList.poll();
        mainTK = Lexer.tokenList.poll();
        lParent = Lexer.tokenList.poll();
        rParent = Error.errorDetect(Token.Type.RPARENT);
        block.parser();
        hasRet = block.rightRet(true);
        retRow = block.getRetRow();
    }

    public int getRBraceRow() {
        return block.getRBraceRow();
    }

    public boolean hasRet() {
        return hasRet;
    }

    public int getRetRow() {
        return retRow;
    }

    public Token getIntTK() {
        return intTK;
    }

    public Token getMainTK() {
        return mainTK;
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

    public void print(BufferedWriter output) throws IOException {
        output.write(intTK.toString());
        output.write(mainTK.toString());
        output.write(lParent.toString());
        output.write(rParent.toString());
        block.print(output);
        output.write("<MainFuncDef>\n");
    }
}
