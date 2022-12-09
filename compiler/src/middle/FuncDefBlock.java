package middle;

import backend.register.Reg;
import frontend.symbol.Symbol;
import middle.quartercode.operand.Operand;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FuncDefBlock {
    private String lable;

    // 第一个key是 varName.toString() ，第二个是对应的 varName
    // private HashMap<String, VarName> varTable = new HashMap<>();
    // depth -> (name, name@depth)
    private int curDepth = 0;
    private BasicBlock curBblock = null;    // 栈顶部分
    private HashMap<Integer, HashMap<String, VarName>> varTable = new HashMap<>();  // 栈式符号表
    private HashMap<Integer, HashMap<String, VarName>> symVars = new HashMap<>();   // 高级语言里的变量
    private HashMap<Integer, HashMap<String, VarName>> tmpVars = new HashMap<>();   // 中间代码里的临时变量

    private ArrayList<Reg> generalRegsUsed = null;
    private BasicBlock lastBb;  // 也是编译程序所处的当前的基本块
    private ArrayList<BasicBlock> basicBlocks;

    private boolean raNeedSave = false;

    public FuncDefBlock(String lable, BasicBlock startBb) {
        this.lable = lable;
        this.lastBb = startBb;
        this.basicBlocks = new ArrayList<>();
        basicBlocks.add(startBb);
    }

    public void addBb(BasicBlock bb) {
        lastBb = bb;
        basicBlocks.add(bb);
    }

    public String getLable() {
        return lable;
    }

    public boolean isRaNeedSave() {
        for (BasicBlock bb : basicBlocks) {
            if (bb.hasFuncCall()) {
                return true;
            }
        }
        return false;
    }

    public BasicBlock getStartBb() {
        return basicBlocks.get(0);
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ArrayList<Reg> getGeneralRegsUsed() {
        if (generalRegsUsed == null) {
            generalRegsUsed = new ArrayList<>();
            // todo 全局寄存器分配
        }
        return generalRegsUsed;
    }

    public void addLocalVar(Operand operand, boolean isTmp) {
        VarName varName = operand.getVarName();
        lastBb.addLocalVar(operand, isTmp);
        /*if (curDepth >= varName.getDepth()) {
            for (int i = varName.getDepth();i <= curDepth; i++) {
                varTable.remove(i);
            }
        }
        curDepth = varName.getDepth();
        varTable.put(varName.getDepth(), new HashMap<>());
        varTable.get(varName.getDepth()).put(varName.getLocalName(), varName);*/
    }

    public VarName getLocalVar(Symbol symbol) {
        VarName res;
        if ((res = lastBb.getLocalVar(symbol.getSymName())) != null) {
            assert lastBb.getDepth() == symbol.getDepth();
            return res;
        }
        for (int i = basicBlocks.size() - 2; i >= 0; i--) {
            if (basicBlocks.get(i).getDepth() == symbol.getDepth() &&
                    (res = basicBlocks.get(i).getLocalVar(symbol.getSymName())) != null) {
                return res;
            }
        }
        /*if (varTable.containsKey(curDepth) &&
                varTable.get(curDepth).containsKey(localName)) {
            assert this.curDepth == curDepth;
            return varTable.get(curDepth).get(localName);
        } else {
            for (int depth = curDepth - 1; depth > 0; depth--) {
                assert varTable.containsKey(depth);
                if (varTable.get(depth).containsKey(localName)) {
                    return varTable.get(depth).get(localName);
                }
            }
        }*/
        return null;
    }

    public void output(BufferedWriter out) throws IOException {
        for (BasicBlock bb : basicBlocks) {
            bb.output(out);
        }
    }
}
