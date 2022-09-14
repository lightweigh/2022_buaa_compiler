import frontend.Lexer;
import frontend.TokenList;

public class Compiler {
    public static void main(String[] args) {
        Lexer lexer = new Lexer("testfile.txt");
        TokenList tokenList = lexer.GenTokenList();
        tokenList.print();
    }
}
