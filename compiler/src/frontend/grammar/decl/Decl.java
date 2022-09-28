package frontend.grammar.decl;

import frontend.grammar.Component;

import java.io.BufferedWriter;
import java.io.IOException;

public class Decl extends Component {

    public void print(BufferedWriter output) throws IOException {
        System.out.println("Decl print. Wrong!");
    }
    /*public static Decl addDecl() {
        if (Lexer.tokenList.peek(0).getRefType() == Token.Type.CONSTTK) {
            return constParser();
        } else if (Lexer.tokenList.peek(0).getRefType() == Token.Type.INTTK) {
            return valParser();
        } else {
            return null;
        }
    }*/
}
