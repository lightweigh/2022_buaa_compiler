package middle.quartercode.function;

import middle.quartercode.operand.MiddleCode;

public class FParaCode implements MiddleCode {
    private String name;
    private boolean isAddr;

    public FParaCode(String name, boolean isAddr) {
        this.name = name;
        this.isAddr = isAddr;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "para int"+(isAddr ? "* ": " ") + name + "\n";

    }
}
