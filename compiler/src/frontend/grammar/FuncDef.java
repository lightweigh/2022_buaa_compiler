package frontend.grammar;

import frontend.Lexer;
import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class FuncDef extends Component {
    //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncType funcType;
    private boolean needRet;
    private Ident ident;
    private Token lParent;
    private Token rParent;
    private FuncFParams funcFParams;
    private Block block;

    public FuncDef(FuncType funcType, Ident ident, Token lParent,
                   Token rParent, FuncFParams funcFParams, Block block) {
        this.funcType = funcType;
        this.needRet = funcType.isNeedRet();
        this.ident = ident;
        this.lParent = lParent;
        this.rParent = rParent;
        this.funcFParams = funcFParams;
        this.block = block;
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
}
