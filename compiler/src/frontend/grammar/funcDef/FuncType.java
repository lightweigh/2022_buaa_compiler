package frontend.grammar.funcDef;

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

    public boolean needRet() {
        return needRet;
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(funcType.toString());
        output.write("<FuncType>\n");
    }

    @Override
    public String toString() {
        return needRet ? "int" : "void";
    }
}
