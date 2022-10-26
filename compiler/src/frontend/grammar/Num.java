package frontend.grammar;

import frontend.token.IntConst;

import java.io.BufferedWriter;
import java.io.IOException;

public class Num {
    private IntConst intConst;

    public Num(IntConst intConst) {
        this.intConst = intConst;
    }

    public int getValueOfNum() {
        return Integer.parseInt(intConst.getContent());
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
