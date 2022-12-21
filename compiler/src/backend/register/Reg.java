package backend.register;

import middle.VarName;

public class Reg {
    private String name;
    private int num;
    private boolean isAlloced;
    private VarName varName;
    private boolean isLocked;

    public Reg(String name, int num) {
        this.name = "$" + name;
        this.num = num;
        this.isAlloced = false;
        this.varName = null;
        this.isLocked = false;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    public boolean isAlloced() {
        return isAlloced;
    }

    public VarName getVarName() {
        return varName;
    }

    public void setAlloced(boolean alloced) {
        isAlloced = alloced;
    }

    public void setVarName(VarName varName) {
        this.varName = varName;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override
    public String toString() {
        return name + " " + (varName != null ? "->" + varName.toString() : "");
    }
}
