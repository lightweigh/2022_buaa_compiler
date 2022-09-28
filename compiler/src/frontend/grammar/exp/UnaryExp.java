package frontend.grammar.exp;

import frontend.Lexer;
import frontend.grammar.FuncCall;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class UnaryExp implements Expression {
    //  UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
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
}
