package frontend.grammar;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class FuncType {
    private Token funcType;
    private boolean needRet;

    public FuncType(Token funcType) {
        this.funcType = funcType;
        needRet = funcType.getRefType() == Token.Type.INTTK;
    }

    public boolean isNeedRet() {
        return needRet;
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(funcType.toString());
        output.write("<FuncType>\n");
    }
}
