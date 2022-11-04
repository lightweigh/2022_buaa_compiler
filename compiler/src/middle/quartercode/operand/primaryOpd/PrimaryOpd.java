package middle.quartercode.operand.primaryOpd;

import middle.VarName;
import middle.quartercode.operand.Operand;

public interface PrimaryOpd extends Operand {

    // t1;
    // Immediate
    // ret

    /*private Immediate immediate;
    private boolean isImmediate = false;
    private boolean isRet = false;

    //
    public PrimaryOpd(String name, Operand idx) {
        this.name = name;
        this.idx = idx;
    }

    public PrimaryOpd(Immediate immediate) {
        this.immediate = immediate;
        this.isImmediate = true;
    }

    // ret
    public PrimaryOpd() {
        this.isRet = true;
    }

    public boolean isRet(String name) {
        return isRet;
    }*/

    VarName getVarName();

    /*@Override
    public String toString() {
        if (isRet) {
            return "RET";
        } else if (isImmediate) {
            return immediate.toString();
        }
        return name + (idx != null ? "[" + idx.getLocalName() + "]" : "");
    }*/
}
