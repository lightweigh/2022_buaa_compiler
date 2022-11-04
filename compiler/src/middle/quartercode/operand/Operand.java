package middle.quartercode.operand;

import middle.VarName;

public interface Operand {
    VarName getVarName();

    void rename(VarName name);

    boolean isGlobalVar();
}
