package frontend.grammar.decl.def.init;

import frontend.grammar.exp.Expression;
import frontend.token.Token;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Init {
    //  ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    //  InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'

    // Init     :=  Expression | '{' [Init {',' Init} ] '}'

    private boolean isConst;
    private int dimension;
    private Expression scalar = null;  // 0 维
    private Vector vector = null;      // 1 维
    private ArrayList<Vector> vectors = null;  // 2 维
    private Token lBrace = null;
    private ArrayList<Token> seperators = null;
    private Token rBrace = null;


    public int getDimension() {
        return dimension;
    }


    // 0 维
    public Init(boolean isConst, Expression scalar) {
        this.isConst = isConst;
        this.dimension = 0;
        this.scalar = scalar;
    }

    // 1 维
    public Init(boolean isConst, Vector vector) {
        this.isConst = isConst;
        this.dimension = 1;
        this.vector = vector;
    }

    // 2 维
    public Init(boolean isConst, Token lBrace, ArrayList<Vector> vectors, ArrayList<Token> seperators, Token rBrace) {
        this.isConst = isConst;
        this.dimension = 2;
        this.lBrace = lBrace;
        this.vectors = vectors;
        this.seperators = seperators;
        this.rBrace = rBrace;
    }

    public void print(BufferedWriter output) throws IOException {
        switch (dimension) {
            case 0:
                // scalar
                scalar.print(output);
                break;
            case 1:
                vector.print(output);
                break;
            case 2:
                output.write(lBrace.toString());
                Iterator<Token> iter = seperators.iterator();
                for (Vector vector : vectors) {
                    vector.print(output);
                    if (iter.hasNext()) {
                        Token comma = iter.next();
                        output.write(comma.toString());
                    }
                }
                output.write(rBrace.toString());
                break;
        }
        if (dimension != 1) {
            if (isConst) {
                output.write("<ConstInitVal>\n");
            } else {
                output.write("<InitVal>\n");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (dimension) {
            case 0:
                // scalar
                sb.append(scalar.toString());
                break;
            case 1:
                sb.append(vector.toString());
                break;
            case 2:
                sb.append("{");
                Iterator<Token> iter = seperators.iterator();
                for (Vector vector : vectors) {
                    sb.append(vector.toString());
                    if (iter.hasNext()) {
                        Token comma = iter.next();
                        sb.append(", ");
                    }
                }
                sb.append("}");
                break;
        }
        return sb.toString();
    }
}
