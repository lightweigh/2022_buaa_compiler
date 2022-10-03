package frontend.grammar.exp;

import frontend.Lexer;
import frontend.grammar.LVal;
import frontend.grammar.Num;
import frontend.token.IntConst;
import frontend.token.Token;

import javax.swing.text.LabelView;
import java.io.BufferedWriter;
import java.io.IOException;
import java.rmi.dgc.Lease;

public class PrimaryExp implements Expression {
    //  PrimaryExp → '(' Exp ')' | LVal | Num
    private Token lParent;
    private Exp exp=null;
    private Token rParent;
    private LVal lVal = null;
    private Num number = null;
    private int type;

    // 0 '(' Exp ')'
    public PrimaryExp(Token lParent, Exp exp, Token rParent) {
        this.lParent = lParent;
        this.exp = exp;
        this.rParent = rParent;
        this.type = 0;
    }

    // 1 LVal
    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
        this.type = 1;
    }

    // 2 Num
    public PrimaryExp(Num number) {
        this.number = number;
        this.type = 2;
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        switch (type) {
            case 0:
                output.write(lParent.toString());
                exp.print(output);
                output.write(rParent.toString());
                break;
            case 1:
                lVal.print(output);
                break;
            case 2:
                number.print(output);
                break;
        }
        output.write("<PrimaryExp>\n");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case 0:
                sb.append("(").append(exp.toString()).append(")");
                break;
            case 1:
                sb.append(lVal.toString());
                break;
            case 2:
                sb.append(number.toString());
                break;
        }
        return sb.toString();
    }
}
