package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;

public class VarStore implements MiddleCode {
    private VarName varName;

    public VarStore(VarName varName) {
        this.varName = varName;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.VARSTORE;
    }

    @Override
    public VarName getVarName() {
        return varName;
    }

    @Override
    public void rename(VarName name) {

    }

    @Override
    public boolean isGlobalVar() {
        return false;
    }

    @Override
    public String toString() {
        return "varStore " + varName + "\n";
    }
}
