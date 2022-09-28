package frontend.grammar.init;

import frontend.grammar.exp.Exp;
import frontend.grammar.exp.Expression;

import java.io.BufferedWriter;
// delete
public class InitVal extends Init {
    public InitVal(boolean isConst, Expression scalar) {
        super(isConst, scalar);
    }
    //  InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'



    public void print(BufferedWriter output) {

    }
}
