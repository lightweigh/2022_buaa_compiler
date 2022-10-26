package middle.quartercode.function;

import frontend.grammar.funcDef.FuncType;
import middle.quartercode.operand.MiddleCode;

public class FuncDefCode implements MiddleCode {
    private String name;
    private FuncType funcType;

    public FuncDefCode(String name, FuncType funcType) {
        this.name = name;
        this.funcType = funcType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return funcType + " " + name + "()\n";
    }
}
