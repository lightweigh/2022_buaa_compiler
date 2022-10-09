package frontend.grammar.exp;

import frontend.Lexer;
import frontend.grammar.FuncCall;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class UnaryExp implements Expression {
    //  UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    //  UnaryExp → PrimaryExp | FuncCall | UnaryOp UnaryExp
    private PrimaryExp primaryExp = null;
    private FuncCall funcCall = null;
    private UnaryOp unaryOp = null;
    private UnaryExp unaryExp = null;
    private int type;


    // 0 PrimaryExp
    public UnaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
        this.type = 0;
    }

    // 1 funcCall Ident '(' [FuncRParams] ')'
    public UnaryExp(FuncCall funcCall) {
        this.funcCall = funcCall;
        this.type = 1;
    }

    // 2 UnaryOp UnaryExp
    public UnaryExp(UnaryOp unaryOp, UnaryExp unaryExp) {
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
        this.type = 2;
    }

    public PrimaryExp getPrimaryExp() {
        return primaryExp;
    }

    public FuncCall getFuncCall() {
        return funcCall;
    }

    public UnaryOp getUnaryOp() {
        return unaryOp;
    }

    public UnaryExp getUnaryExp() {
        return unaryExp;
    }

    public int getType() {
        return type;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        switch (type) {
            case 0:
                primaryExp.print(output);
                break;
            case 1:
                funcCall.print(output);
                break;
            case 2:
                unaryOp.print(output);
                unaryExp.print(output);
                break;
        }
        output.write("<UnaryExp>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case 0:
                sb.append(primaryExp.toString());
                break;
            case 1:
                sb.append(funcCall.toString());
                break;
            case 2:
                sb.append(unaryOp.toString()).append(unaryExp.toString());
                break;
        }
        return sb.toString();
    }
}
