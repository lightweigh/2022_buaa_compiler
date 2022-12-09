package middle.quartercode.branch;

import middle.BasicBlock;
import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class JumpCmp implements MiddleCode {
    private Operand cmpOp1;
    private Operand cmpOp2;

    public enum JumpType {
        BEQ("beq "),    // op1 == op2
        BEQZ("beqz "),   // op1 == 0

        BNE("bne "),    // op1 != op2
        BNEZ("bnez "),   // op1 != 0

        BGE("bge "),    // op1 >= op2
        BGEZ("bgez "),   // op1 >= 0
        BGT("bgt "),    // op1 >  op2
        BGTZ("bgtz "),   // op1 >  0

        BLE("ble "),    // op1 <= op2
        BLEZ("blez "),   // op1 <= 0
        BLT("blt "),    // op1 <  op2
        BLTZ("bltz "),   // op1 <  0

        GOTO("j ");      // op1 = 1 instance of Immediate

        String content;

        JumpType(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
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
                (cmpOp1 != null ? cmpOp1.getVarName().toString() + ", " : "") +
                (cmpOp2 != null ? cmpOp2.getVarName().toString() + ", " : "") +
                jumpTgtLabel + "\n";
    }
}
