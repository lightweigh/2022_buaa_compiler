package middle;

import middle.quartercode.operand.MiddleCode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BasicBlock {
    private String lable;
    private HashMap<String, BasicBlock> bbIn = new HashMap<>();
    private HashMap<String, BasicBlock> bbOut = new HashMap<>();
    private BasicBlock directBb = null;
    private ArrayList<MiddleCode> middleCodes = new ArrayList<>();
    private HashMap<String, MiddleCode> name2MiddleCodes = new HashMap<>();

    // 活动记录
    // 局部变量
    private int capacity = 0;

    private int blockCount = 0; // 计数，用于label命名唯一

    public BasicBlock(String label) {
        this.lable = label + blockCount++ + ":";
    }

    public void addBlockIn(BasicBlock bb) {
        bbIn.put(bb.getLable(),bb);
    }

    public void addBlockOut(BasicBlock bb) {
        bbOut.put(bb.getLable(), bb);
    }

    public void setDirectBb(BasicBlock directBb) {
        this.directBb = directBb;
    }

    public void append(MiddleCode middleCode) {
        middleCodes.add(middleCode);
        // name2MiddleCodes.put(middleCode.getName(), middleCode);
    }

    public String getLable() {
        return lable;
    }

    public HashMap<String, BasicBlock> getBbIn() {
        return bbIn;
    }

    public HashMap<String, BasicBlock> getBbOut() {
        return bbOut;
    }

    public void output(BufferedWriter out) throws IOException {
        for (MiddleCode middleCode : middleCodes) {
            out.write(middleCode.toString());
        }
    }
}
