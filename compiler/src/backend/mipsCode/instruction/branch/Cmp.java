package backend.mipsCode.instruction.branch;

import backend.mipsCode.instruction.Instruction;
import backend.register.Reg;
import middle.quartercode.branch.SaveCmp;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.Immediate;

public class Cmp implements Instruction {
    private SaveCmp.CmpType cmpType;
    private Reg dst;
    private Reg cmpOp1;
    private Reg cmpOp2;
    private Immediate cmpOpImm;

    public Cmp(SaveCmp.CmpType cmpType, Reg dst, Reg cmpOp1, Reg cmpOp2, Immediate cmpOpImm) {
        this.cmpType = cmpType;
        this.dst = dst;
        this.cmpOp1 = cmpOp1;
        this.cmpOp2 = cmpOp2;
        this.cmpOpImm = cmpOpImm;
    }

    @Override
    public String toString() {
        return cmpType + dst.getName() + ", " + cmpOp1.getName() + ", " +
                (cmpOp2 != null ? cmpOp2.getName() : cmpOpImm.toString());
    }
}
