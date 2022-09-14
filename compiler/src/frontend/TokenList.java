package frontend;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TokenList {
    private List<Token> tokens = new LinkedList<>();

    public void addToken(Token token) {
        tokens.add(token);
    }

    public void print() {
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter("output.txt"));
            for (Token token : tokens) {
                output.write(token.toString()+"\n");
//                System.out.println(token);
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
