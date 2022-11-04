package middle.quartercode.function;

import frontend.grammar.funcDef.FuncType;
import middle.VarName;
import middle.quartercode.operand.MiddleCode;

public class FuncDefCode implements MiddleCode {
    private VarName name;
    private FuncType funcType;

    public FuncDefCode(VarName name, FuncType funcType) {
        this.name = name;
        this.funcType = funcType;
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
        return CodeType.FUNCDEF;
    }

    @Override
    public String toString() {
        return funcType + " " + name + "()\n";
    }
}
