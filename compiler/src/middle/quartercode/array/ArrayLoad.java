package middle.quartercode.array;

import middle.quartercode.operand.MiddleCode;

public class ArrayLoad implements MiddleCode {
    private String name;
    private String arrayName;
    private String idx;

    public ArrayLoad(String name, String arrayName, String idx) {
        this.name = name;
        this.arrayName = arrayName;
        this.idx = idx;
    }

    @Override
    public String getName() {
        return name;
    }
}
