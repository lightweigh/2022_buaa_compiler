package middle.quartercode;

import middle.VarName;
import middle.quartercode.operand.primaryOpd.Immediate;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class ConstVar implements MiddleCode {
    // int i;					# var int i
    // int i = 1;				# var int i = 1
    // const int i = 10;		# const int i = 10

    // 常量和变量哈
    private VarName varName;
    private boolean isConst;
    private Operand operand;

    public ConstVar(VarName varName, boolean isConst, Operand operand) {

        this.isConst = isConst;
        this.operand = operand;
        this.varName = varName;
    }

    public boolean isInit() {
        return operand != null;
    }

    public Integer getOperandImm() {
        // 只在常量初始化时调用此方法
        assert operand instanceof Immediate;
        return Integer.parseInt(operand.getVarName().toString());
    }

    public Operand getOperand() {
        return operand;
    }

    public boolean isConst() {
        return isConst;
    }

    @Override
    public String toString() {
        return (isConst ? "const int " : "var int ") +
                varName +
                (operand != null ? " = " + operand.getVarName() : "") + "\n";
    }

    @Override
    public VarName getVarName() {
        return varName;
    }

    @Override
    public void rename(VarName name) {
        this.varName = name;
    }

    @Override
    public boolean isGlobalVar() {
        return getVarName().getDepth() == 0;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.CONSTVAR;
    }
}
