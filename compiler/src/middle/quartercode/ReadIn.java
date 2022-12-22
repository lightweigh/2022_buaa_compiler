package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.LValOpd;

public class ReadIn implements MiddleCode {
    private LValOpd name = null;

    public ReadIn(LValOpd name) {
        this.name = name;
    }

    @Override
    public VarName getVarName() {
        return name.getVarName();
    }

    @Override
    public void rename(VarName name) {
        this.name.rename(name);
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.SCANF;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public String toString() {
        return "scanf " + name + "\n";
    }
}
