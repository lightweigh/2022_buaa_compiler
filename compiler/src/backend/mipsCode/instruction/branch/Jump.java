package backend.mipsCode.instruction.branch;

import backend.mipsCode.instruction.Instruction;
import backend.register.Reg;
import middle.quartercode.branch.JumpCmp;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Jump implements Instruction {
    private JumpCmp.JumpType jumpType;
    private Reg cmpOp1;
    private Reg cmpOp2;
    private Immediate cmpOpImm2;
    private String jumpLabel;

    public Jump(JumpCmp.JumpType jumpType, Reg cmpOp1, Reg cmpOp2, Immediate cmpOpImm2, String jumpLabel) {
        this.jumpType = jumpType;
        this.cmpOp1 = cmpOp1;
        this.cmpOp2 = cmpOp2;
        this.cmpOpImm2 = cmpOpImm2;
        this.jumpLabel = jumpLabel;
    }

    @Override
    public String toString() {
        if (cmpOp1 == null) {
            return jumpType + jumpLabel + "\n";
        }
        if (cmpOp2 == null) {
            return jumpType + cmpOp1.getName() + ", " +
                    (cmpOpImm2 != null ? cmpOpImm2.getValue() + ", " : "") +
                    jumpLabel;
        }
        return jumpType + cmpOp1.getName() + ", " +
                cmpOp2.getName() + ", " +
                jumpLabel;
    }
}
