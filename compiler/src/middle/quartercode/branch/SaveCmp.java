package middle.quartercode.branch;

import middle.BasicBlock;
import middle.VarName;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

public class SaveCmp implements MiddleCode {
    private Operand t1; // 临时变量 // 如果是常量比较可以直接出结果的话，就是Immediate(1)
    private Operand cmpOp1;
    private Operand cmpOp2;

    public enum CmpType {
        SLT,    // t1 = 1 if op1 <  op2
        SLTI,   // t1 = 1 if op1 <  op2 and op2 instance of Immediate
        SLE,    // t1 = 1 if op1 <= op2
        SEG,    // t1 = 1 if op1 >= op2
        SGT,    // t1 = 1 if op1 >  op2
        SEQ,    // t1 = 1 if op1 == op2
        SNE,    // t1 = 1 if op1 != op2
    }

    private CmpType cmpType;

    public SaveCmp(Operand t1, Operand cmpOp1, Operand cmpOp2, CmpType cmpType) {
        this.t1 = t1;
        this.cmpOp1 = cmpOp1;
        this.cmpOp2 = cmpOp2;
        this.cmpType = cmpType;
    }

    @Override
    public CodeType getCodeType() {
        return CodeType.SAVECMP;
    }

    @Override
    public VarName getVarName() {
        return t1.getVarName();
    }

    @Override
    public void rename(VarName name) {

    }

    public Operand getT1() {
        return t1;
    }

    public Operand getCmpOp1() {
        return cmpOp1;
    }

    public Operand getCmpOp2() {
        return cmpOp2;
    }

    public CmpType getCmpType() {
        return cmpType;
    }

    @Override
    public boolean isGlobalVar() {
        return false;
    }

    @Override
    public String toString() {
        return cmpType + t1.getVarName().toString() +
                (cmpOp1 != null ? cmpOp1.getVarName().toString():"") +
                (cmpOp2 != null ? cmpOp2.getVarName().toString():"\n");
    }
}
