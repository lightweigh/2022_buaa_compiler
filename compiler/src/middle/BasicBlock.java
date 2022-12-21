package middle;

import backend.mipsCode.MipsCode;
import middle.quartercode.RetCode;
import middle.quartercode.branch.JumpCmp;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.Immediate;
import middle.quartercode.operand.primaryOpd.RetOpd;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

    private ArrayList<VarName> vars = new ArrayList<>();
    // private HashMap<>

    private static int blockCount = 0; // 计数，用于label命名唯一

    public enum BBType {
        GLOBAL, // 全局部分,非基本块
        FUNC,   // 函数体
        BRANCH, // 分支
        // BRANCHOR,
        // BRANCHAND,
        BRANCHGOTO, // 分支无条件跳转
        FOLLOWGOTO, // 无条件跳转后紧跟的代码, 为死代码
        LOOP,   // 循环
        BASIC   // 普通的 {}
    }

    private BBType bbType;

    public BasicBlock(String label, BBType bbType, int depth) {
        this.lable = label;
        if (bbType != BBType.FUNC) {
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
        /*if (!(middleCode instanceof JumpCmp ||
                middleCode instanceof RetOpd ||
                middleCode instanceof Immediate ||
                middleCode instanceof RetCode)) {
            vars.add(middleCode.getVarName());
        }*/
    }

    public void addUsedVars(VarName varName) {
        if (!vars.contains(varName)) {
            vars.add(varName);
        }
    }

    public ArrayList<VarName> getVars() {
        return vars;
    }

    public void addLocalVar(Operand operand, boolean isTmp) {
        VarName varName = operand.getVarName();
        if (isTmp) {
            tmpVars.put(varName.getLocalName(), varName);
        } else {
            symVars.put(varName.getLocalName(), varName);
        }
        addUsedVars(varName);
    }

    public HashMap<String, VarName> getSymVars() {
        return symVars;
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

    public void reassignMiddleCode(MiddleCode middleCode, VarName varName) {
        middleCode.rename(varName);
        vars.remove(middleCode.getVarName());
        vars.add(varName);
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
        if (this.bbType != BBType.BASIC) {
            out.write(this.lable + ":\n");
        }
        for (MiddleCode middleCode : middleCodes) {
            out.write(middleCode.toString());
        }
    }
}
