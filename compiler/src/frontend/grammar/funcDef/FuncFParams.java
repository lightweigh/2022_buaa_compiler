package frontend.grammar.funcDef;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class FuncFParams {
    //  FuncFParams → FuncFParam { ',' FuncFParam }
    private ArrayList<FuncFParam> funcFParams;
    private ArrayList<Token> seperators;
    private int num = 0;

    public FuncFParams(ArrayList<FuncFParam> funcFParams, ArrayList<Token> seperators) {
        this.funcFParams = funcFParams;
        this.seperators = seperators;
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public void print(BufferedWriter output) throws IOException {
        Iterator<Token> iter = seperators.iterator();
        for (FuncFParam funcFParam : funcFParams) {
            funcFParam.print(output);
            if (iter.hasNext()) {
                Token comma = iter.next();
                output.write(comma.toString());
            }
        }
        output.write("<FuncFParams>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Token> iter = seperators.iterator();
        for (FuncFParam funcFParam : funcFParams) {
            sb.append(funcFParam.toString());
            if (iter.hasNext()) {
                iter.next();
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
