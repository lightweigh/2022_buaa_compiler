package middle.quartercode.function;

import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

import java.util.ArrayList;

public class FuncCallCode implements MiddleCode {
    private VarName name;
    private ArrayList<Operand> rParaCodes;

    public FuncCallCode(VarName name, ArrayList<Operand> rParaCodes) {
        this.name = name;
        this.rParaCodes = rParaCodes;
    }

    @Override
    public VarName getVarName() {
        // 别用这个
        return name;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public void rename(VarName name) {

    }

    public ArrayList<Operand> getrParaCodes() {
        return rParaCodes;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.FUNCCALL;
    }

    @Override
    public String toString() {
        return "call " + name.toString() + "\n";
    }
}
