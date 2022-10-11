import frontend.Error;
import frontend.Lexer;
import frontend.TokenList;
import frontend.parser.CompUnitParser;
import frontend.visitor.Visitor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) {
        Lexer lexer = new Lexer("testfile.txt");
        TokenList tokenList = lexer.GenTokenList();
        // tokenList.print();
        CompUnitParser compUnitParser = new CompUnitParser();
        compUnitParser.parser();
        Visitor visitor = new Visitor();
        visitor.visitCompUnit(compUnitParser.getCompUnit());
        /*try {
            BufferedWriter output = new BufferedWriter(new FileWriter("output.txt"));
            compUnitParser.print(output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("error.txt"));
            while (!Error.errorTable.isEmpty()) {
                Error error = Error.errorTable.poll();
                output.write(error.toString());
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
