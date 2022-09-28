package frontend.grammar.init;

import frontend.grammar.exp.ConstExp;
import frontend.grammar.exp.Expression;
import frontend.token.IntConst;

import java.io.BufferedWriter;

// delete
public class ConstInitVal extends Init {
    public ConstInitVal(boolean isConst, Vector vector) {
        super(isConst, vector);
    }
    //  ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'

    // public ConstInitVal(int dimension) {
    //     super(dimension);
    // }


    public void print(BufferedWriter output) {

    }

}
