package frontend.grammar.exp;

import java.io.BufferedWriter;
import java.io.IOException;

public interface Expression {


    public void print(BufferedWriter output) throws IOException;

}
