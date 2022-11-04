package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;

public class ConstStrCode implements MiddleCode {
    private VarName name;
    private String content;

    public ConstStrCode(VarName name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public VarName getVarName() {
        return name;
    }

    @Override
    public void rename(VarName name) {

    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.CONSTSTR;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "const str " + name + " = " + "\"" + content + "\"\n";
    }
}
