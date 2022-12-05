package middle.quartercode.branch;

import middle.BasicBlock;
import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class JumpCmp implements MiddleCode {
    private Operand cmpOp1;
    private Operand cmpOp2;

    public enum JumpType {
        BEQ,    // op1 == op2
        BEQZ,   // op1 == 0

        BNE,    // op1 != op2
        BNEZ,   // op1 != 0

        BGE,    // op1 >= op2
        BGEZ,   // op1 >= 0
        BGT,    // op1 >  op2
        BGTZ,   // op1 >  0

        BLE,    // op1 <= op2
        BLEZ,   // op1 <= 0
        BLT,    // op1 <  op2
        BLTZ,   // op1 <  0

        GOTO,      // op1 = 1 instance of Immediate
    }

    private JumpType jumpType;
    private String jumpTgtLabel;

    public JumpCmp(Operand cmpOp1, Operand cmpOp2, JumpType jumpType, String jumpTgtLabel) {
        this.cmpOp1 = cmpOp1;
        this.cmpOp2 = cmpOp2;
        this.jumpType = jumpType;
        this.jumpTgtLabel = jumpTgtLabel;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.JUMPCMP;
    }

    @Override
    public VarName getVarName() {
        assert false;
        return null;
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
        return jumpType +
                (cmpOp1 != null ? cmpOp1.getVarName().toString() : "") +
                (cmpOp2 != null ? cmpOp2.getVarName().toString() : "") +
                jumpTgtLabel;
    }
}
