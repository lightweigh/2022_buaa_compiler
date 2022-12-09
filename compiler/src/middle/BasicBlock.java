package middle;

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

    private HashMap<String, BasicBlock> prevBlocks = new HashMap<>();
    private HashMap<String, BasicBlock> followBlocks = new HashMap<>();
    private BasicBlock directBb = null;
    private ArrayList<MiddleCode> middleCodes = new ArrayList<>();

    private HashMap<String, VarName> symVars = new HashMap<>();   // 局部变量
    private HashMap<String, VarName> tmpVars = new HashMap<>();   // 中间代码里的临时变量

    private static int blockCount = 0; // 计数，用于label命名唯一

    public enum BBType {
        GLOBAL, // 全局部分,非基本块
        FUNC,   // 函数体
        MAINFUNC, // main函数
        BRANCH, // 分支
        // BRANCHOR,
        // BRANCHAND,
        BRANCHGOTO, // 分支无条件跳转
        LOOP,   // 循环
        BASIC   // 普通的 {}
    }

    private BBType bbType;

    public BasicBlock(String label, BBType bbType, int depth) {
        this.lable = label;
        if (bbType != BBType.FUNC && bbType != BBType.MAINFUNC) {
            this.lable += blockCount++;
        }
        this.depth = depth;
        this.bbType = bbType;
    }

    public BasicBlock getDirectBb() {
        return directBb;
    }

    public BBType getBbType() {
        return bbType;
    }

    public void setDirectBb(BasicBlock directBb) {
        this.directBb = directBb;
        this.addFollowBlock(directBb);
        directBb.addPrevBlock(this);
    }

    private void addPrevBlock(BasicBlock bb) {
        prevBlocks.put(bb.getLable(), bb);
    }

    private void addFollowBlock(BasicBlock bb) {
        followBlocks.put(bb.getLable(), bb);
    }

    public void append(MiddleCode middleCode) {
        middleCodes.add(middleCode);
    }

    public void addLocalVar(Operand operand, boolean isTmp) {
        VarName varName = operand.getVarName();
        if (isTmp) {
            tmpVars.put(varName.getLocalName(), varName);
        } else {
            symVars.put(varName.getLocalName(), varName);
        }
    }

    public VarName getLocalVar(String localName) {
        if (tmpVars.containsKey(localName)) {
            return tmpVars.get(localName);
        }
        if (symVars.containsKey(localName)) {
            return symVars.get(localName);
        }
        return null;
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

    public HashMap<String, BasicBlock> getPrevBlocks() {
        return prevBlocks;
    }

    public HashMap<String, BasicBlock> getFollowBlocks() {
        return followBlocks;
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
        out.write(this.lable + ":\n");
        for (MiddleCode middleCode : middleCodes) {
            out.write(middleCode.toString());
        }
    }
}
