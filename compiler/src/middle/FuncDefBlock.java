package middle;

import backend.register.Reg;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FuncDefBlock {
    private String lable;

    // 第一个key是 varName.toString() ，第二个是对应的 varName
    // private HashMap<String, VarName> localVars = new HashMap<>();
    // depth -> (name, name@depth)
    private HashMap<Integer, HashMap<String, VarName>> localVars = new HashMap<>();

    private ArrayList<Reg> generalRegsUsed = null;
    private BasicBlock startBb;
    private ArrayList<BasicBlock> restBb;

    private boolean raNeedSave = false;

    public FuncDefBlock(String lable, BasicBlock startBb) {
        this.lable = lable;
        this.startBb = startBb;
        this.restBb = new ArrayList<>();
    }

    public void addBb(BasicBlock bb) {
        restBb.add(bb);
    }

    public String getLable() {
        return lable;
    }

    public boolean isRaNeedSave() {
        if (startBb.hasFuncCall()) {
            return true;
        }
        for (BasicBlock bb : restBb) {
            if (bb.hasFuncCall()) {
                return true;
            }
        }
        return false;
    }

    public BasicBlock getStartBb() {
        return startBb;
    }

    public ArrayList<BasicBlock> getRestBb() {
        return restBb;
    }

    public ArrayList<Reg> getGeneralRegsUsed() {
        if (generalRegsUsed == null) {
            generalRegsUsed = new ArrayList<>();
            // todo 全局寄存器分配
        }
        return generalRegsUsed;
    }

    public void addLocalVar(Operand operand) {
        VarName varName = operand.getVarName();
        if (!localVars.containsKey(varName.getDepth())) {
            localVars.put(varName.getDepth(), new HashMap<>());
        }
        localVars.get(varName.getDepth()).put(varName.getLocalName(), varName);
    }

    public VarName getLocalVar(String localName, int curDepth) {
        if (localVars.containsKey(curDepth) &&
                localVars.get(curDepth).containsKey(localName)) {
            return localVars.get(curDepth).get(localName);
        } else {
            for (int depth = curDepth - 1; depth > 0; depth--) {
                if (localVars.containsKey(depth) &&
                        localVars.get(depth).containsKey(localName)) {
                    return localVars.get(depth).get(localName);
                }
            }
        }
        return null;
    }

    public void output(BufferedWriter out) throws IOException {
        startBb.output(out);
        for (BasicBlock bb : restBb) {
            bb.output(out);
        }
    }
}
