package frontend.grammar;

import frontend.token.Ident;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FuncCall {
    // FuncCall → Ident '(' [FuncRParams] ')'
    private Ident ident;
    private Token lParent;
    private Token rParent;
    private FuncRParams funcRParams;

    public FuncCall(Ident ident, Token lParent, FuncRParams funcRParams, Token rParent) {
        this.ident = ident;
        this.lParent = lParent;
        this.rParent = rParent;
        this.funcRParams = funcRParams;
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(ident.toString());
        output.write(lParent.toString());
        if (funcRParams != null) {
            funcRParams.print(output);
        }
        output.write(rParent.toString());
    }
}
