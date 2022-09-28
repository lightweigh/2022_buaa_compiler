package frontend.grammar.stmt;

import frontend.Error;
import frontend.Lexer;
import frontend.grammar.exp.Exp;
import frontend.grammar.exp.condExp.LAndExp;
import frontend.parser.Parser;
import frontend.token.FormatString;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class PrintfStmt extends Stmt {
    // 'printf''('FormatString{','Exp}')'';'
    private Token printf;
    private Token lParent;
    private FormatString formatString;
    private ArrayList<Token> seperators = new ArrayList<>();
    private ArrayList<Exp> exps = new ArrayList<>();
    private Token rParent;
    private Token semicon;

    public void parser() {
        printf = Lexer.tokenList.poll();
        lParent = Lexer.tokenList.poll();
        formatString = (FormatString) Lexer.tokenList.poll();
        while (Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            seperators.add(Lexer.tokenList.poll());
            Exp exp = (Exp) Parser.expressionParser("Exp");
            exps.add(exp);
        }
        rParent = Lexer.tokenList.poll();
        /*if (!Lexer.tokenList.equalPeekType(0, Token.Type.COMMA)) {
            Error.errorDetect(';');
        } else {
            semicon = Lexer.tokenList.poll();
        }*/
        semicon = Error.errorDetect(';');
    }

    @Override
    public void print(BufferedWriter output) throws IOException {
        output.write(printf.toString());
        output.write(lParent.toString());
        output.write(formatString.toString());
        if (!seperators.isEmpty()) {
            Iterator<Token> iter = seperators.iterator();
            for (Exp exp : exps) {
                assert iter.hasNext();
                Token comma = iter.next();
                output.write(comma.toString());
                exp.print(output);
            }
        }
        output.write(rParent.toString());
        output.write(semicon.toString());
        super.print(output);
    }
}
