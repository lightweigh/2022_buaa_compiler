package middle.quartercode;

import middle.quartercode.operand.MiddleCode;

public class ConstStrCode implements MiddleCode {
    private String name;
    private String content;

    public ConstStrCode(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "const str " + name + " = " + "\"" + content + "\"\n";
    }
}
