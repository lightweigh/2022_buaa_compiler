package frontend.grammar;

import frontend.Lexer;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;

public class MainFuncDef extends Component{
    //  MainFuncDef → 'int' 'main' '(' ')' Block
    private Token intTK;
    private Token mainTK;
    private Token lParent;
    private Token rParent;
    private Block block = new Block(false);

    public void parser() {
        intTK = Lexer.tokenList.poll();
        mainTK = Lexer.tokenList.poll();
        lParent = Lexer.tokenList.poll();
        rParent = Lexer.tokenList.poll();
        block.parser();
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(intTK.toString());
        output.write(mainTK.toString());
        output.write(lParent.toString());
        output.write(rParent.toString());
        block.print(output);
        output.write("<MainFuncDef>\n");
    }
}
