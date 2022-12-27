package backend.register;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Registers {
    public static Reg ZERO = new Reg("zero", 0);
    public static Reg AT = new Reg("at", 1);
    public static Reg V0 = new Reg("v0", 2);
    public static Reg V1 = new Reg("v1", 3);
    public static Reg A0 = new Reg("a0", 4);
    public static Reg A1 = new Reg("a1", 5);
    public static Reg A2 = new Reg("a2", 6);
    public static Reg A3 = new Reg("a3", 7);
    public static Reg T0 = new Reg("t0", 8);
    public static Reg T1 = new Reg("t1", 9);
    public static Reg T2 = new Reg("t2", 10);
    public static Reg T3 = new Reg("t3", 11);
    public static Reg T4 = new Reg("t4", 12);
    public static Reg T5 = new Reg("t5", 13);
    public static Reg T6 = new Reg("t6", 14);
    public static Reg T7 = new Reg("t7", 15);
    public static Reg S0 = new Reg("s0", 16);
    public static Reg S1 = new Reg("s1", 17);
    public static Reg S2 = new Reg("s2", 18);
    public static Reg S3 = new Reg("s3", 19);
    public static Reg S4 = new Reg("s4", 20);
    public static Reg S5 = new Reg("s5", 21);
    public static Reg S6 = new Reg("s6", 22);
    public static Reg S7 = new Reg("s7", 23);
    public static Reg T8 = new Reg("t8", 24);
    public static Reg T9 = new Reg("t9", 25);
    public static Reg K0 = new Reg("k0", 26);
    public static Reg K1 = new Reg("k1", 27);
    public static Reg GP = new Reg("gp", 28);
    public static Reg SP = new Reg("sp", 29);
    public static Reg FP = new Reg("fp", 30);
    public static Reg RA = new Reg("ra", 31);

    private ArrayList<Reg> regs = new ArrayList<>(Arrays.asList(ZERO, AT, V0, V1, A0, A1, A2, A3, T0, T1, T2, T3, T4, T5, T6, T7, S0, S1, S2, S3, S4, S5, S6, S7, T8, T9, K0, K1, GP, SP, FP, RA));
    private ArrayList<Reg> tempRegs = new ArrayList<>(Arrays.asList(T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, V1, A0, A1, A2, A3, S0, S1, S2, S3, S4, S5, S6, S7)); // todo 把v0怎么加进来呢？ ra也不分配，如果函数是叶子函数，我不会保存你的
    private ArrayList<Reg> argRegs = new ArrayList<>(Arrays.asList(Registers.A0, Registers.A1, Registers.A2, Registers.A3));// todo a0
    private ArrayList<Reg> generalRegs = new ArrayList<>(Arrays.asList(S0, S1, S2, S3, S4, S5, S6, S7));
    private int unmapNum;
    private HashSet<Reg> unmapReg;
    private int freeTgt = 0;   // 用于临时寄存器池满的时候，释放一个寄存器

    public Registers() {
        this.unmapNum = 32;
        this.unmapReg = new HashSet<>(regs);
        ZERO.setAlloced(true);
    }

    public void update() {
        unmapNum = 0;
        unmapReg.clear();
        for (Reg reg : regs) {
            if (!reg.isAlloced()) {
                unmapNum++;
                unmapReg.add(reg);
            }
        }
    }

    /*public void freeAllTmpReg() {
        for (Reg reg : tempRegs) {
            reg
        }
        freeTgt = 0;
    }*/

    public ArrayList<Reg> getTRegs() {
        return tempRegs;
    }

    public ArrayList<Reg> getARegs() {
        return argRegs;
    }

    public ArrayList<Reg> getTempRegs() {
        ArrayList<Reg> res = new ArrayList<>(tempRegs);
        res.addAll(argRegs);
        return res;
    }

    public Reg getOneTReg2Free() {
        Reg reg = tempRegs.get(freeTgt);
        freeTgt = (freeTgt + 1) % tempRegs.size();
        return reg;
    }

    // caller-saved
    public ArrayList<Reg> getUsedTmpRegs() {
        ArrayList<Reg> res = new ArrayList<>();
        // ArrayList<Reg> src = type.equals("tmp") ? tempRegs : generalRegs;
        for (Reg reg : tempRegs) {
            if (reg.isAlloced()) {
                res.add(reg);
            }
        }
        return res;
    }

    public void storeTReg() {
        for (Reg reg : tempRegs) {
            if (reg.isAlloced()) {

            }
        }
    }

    public Reg allocReg() {
        /*Reg reg = allocSReg();
        if (reg == null) {
            reg = allocTReg();
        }*/
        Reg reg = allocTReg();
        if (reg == null) {
            reg = allocAReg();
        }
        return reg;
    }

    public Reg allocTReg() {
        for (Reg reg : tempRegs) {
            if (!reg.isAlloced()) {
                return reg;
            }
        }
        return null;
    }

    public Reg allocAReg() {
        for (Reg reg : argRegs) {
            if (!reg.isAlloced()) {
                return reg;
            }
        }
        return null;
    }

    public Reg allocSReg() {
        for (Reg reg : generalRegs) {
            if (!reg.isAlloced()) {
                return reg;
            }
        }
        return null;
    }
}
