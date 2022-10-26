package middle.quartercode.function;

import middle.quartercode.operand.MiddleCode;

public class FuncCallCode implements MiddleCode {
    private String name;

    public FuncCallCode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "call " + name + "\n";
    }
}
