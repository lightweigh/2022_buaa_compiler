package frontend;

import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TokenList {
    private List<Token> tokens = new LinkedList<>();
    private int pos=0;

    public void addToken(Token token) {
        tokens.add(token);
    }

    public Token peek(int forward) {
        if (pos >= tokens.size()) {
            return null;
        }
        return tokens.get(pos+forward);
    }

    public Token poll() {
        if (pos >= tokens.size()) {
            return null;
        }
        Token token = tokens.get(pos);
        pos++;
        return token;
    }

    public void skip(int steps) {
        pos += steps;
    }

    public boolean equalPeekType(int forward,Token.Type type) {
        return tokens.get(pos+forward).getRefType() == type;
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
