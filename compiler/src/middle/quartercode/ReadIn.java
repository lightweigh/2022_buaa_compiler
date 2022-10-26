package middle.quartercode;

import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class ReadIn implements MiddleCode {
    private String name=null;
    private Operand operand;

    public ReadIn(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "scanf " + name + "\n";
    }
}
