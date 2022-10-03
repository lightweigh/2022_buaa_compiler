package frontend.grammar;

import frontend.token.IntConst;

import java.io.BufferedWriter;
import java.io.IOException;

public class Num {
    private IntConst intConst;

    public Num(IntConst intConst) {
        this.intConst = intConst;
    }

    public void print(BufferedWriter output) throws IOException {
        output.write(intConst.toString());
        output.write("<Number>\n");
    }

    @Override
    public String toString() {
        return  intConst.getContent();
    }
}
