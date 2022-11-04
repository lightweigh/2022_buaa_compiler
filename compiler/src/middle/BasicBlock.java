package middle;

import backend.ActivationRcd;
import backend.register.Reg;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BasicBlock {
    private String lable;
    private int depth;

    // private boolean isFunc;
    private boolean hasFuncCall = false;

    private HashMap<String, BasicBlock> bbIn = new HashMap<>();
    private HashMap<String, BasicBlock> bbOut = new HashMap<>();
    private BasicBlock directBb = null;
    private ArrayList<MiddleCode> middleCodes = new ArrayList<>();

    private static int  blockCount = 0; // 计数，用于label命名唯一

    public BasicBlock(String label, boolean isFuncDef, int depth) {
        this.lable = label;
        if (!isFuncDef) {
            this.lable += blockCount++;
        }
        this.depth = depth;
    }

    public void addBlockIn(BasicBlock bb) {
        bbIn.put(bb.getLable(),bb);
    }

    public void addBlockOut(BasicBlock bb) {
        bbOut.put(bb.getLable(), bb);
    }

    public BasicBlock getDirectBb() {
        return directBb;
    }

    public void setDirectBb(BasicBlock directBb) {
        this.directBb = directBb;
    }

    public void append(MiddleCode middleCode) {
        middleCodes.add(middleCode);
    }

    public void remove(MiddleCode middleCode) {
        middleCodes.remove(middleCode);
    }

    public String getLable() {
        return lable;
    }

    public ArrayList<MiddleCode> getMiddleCodes() {
        return middleCodes;
    }

    public HashMap<String, BasicBlock> getBbIn() {
        return bbIn;
    }

    public HashMap<String, BasicBlock> getBbOut() {
        return bbOut;
    }

    // main 不会调用这个方法
    public boolean hasFuncCall() {
        return hasFuncCall;
    }

    public void setHasFuncCall(boolean hasFuncCall) {
        this.hasFuncCall = hasFuncCall;
    }

    public int getDepth() {
        return depth;
    }

    public void output(BufferedWriter out) throws IOException {
        for (MiddleCode middleCode : middleCodes) {
            out.write(middleCode.toString());
        }
    }
}
