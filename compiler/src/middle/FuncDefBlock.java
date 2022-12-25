package middle;

import backend.register.Reg;
import frontend.symbol.Symbol;
import middle.quartercode.ConstVar;
import middle.quartercode.array.ArrayDef;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
    // private ArrayList<VarName> allVars = new ArrayList<>(); // 所有的临时变量记录下来
    private LinkedHashMap<VarName, Integer> allVars = new LinkedHashMap<>(); // 所有的变量记录下来 -> varName , size

    public FuncDefBlock(String lable, BasicBlock startBb) {
        this.lable = lable;
        this.lastBb = startBb;
        this.basicBlocks = new ArrayList<>();
        basicBlocks.add(startBb);
    }

    public void funcBlockProcess() {
        BasicBlock bb = getStartBb();
        while (bb != null) {
            for (MiddleCode middleCode : bb.getMiddleCodes()) {
                if (!middleCode.getVarName().isGlobalVar()) {   // 全局变量没必要加进来了
                    // SaveCmp. JumpCmp
                    switch (middleCode.getCodeType()) {
                        case SAVECMP:
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                allVars.put(middleCode.getVarName(), 1);
                            }
                            break;
                        case JUMPCMP:
                            break;
                        case ARRAY_DEF:
                            allVars.put(middleCode.getVarName(), ((ArrayDef) middleCode).getSize());
                            break;
                        case ARRAY_LOAD:
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                allVars.put(middleCode.getVarName(), 1);
                            }
                            break;
                        case ARRAY_STORE:
                            break;
                        case ASSIGN:
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                allVars.put(middleCode.getVarName(), 1);
                            }
                            break;
                        case BINARY:
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                allVars.put(middleCode.getVarName(), 1);
                            }
                            break;
                        case CONSTVAR:
                            if (!((ConstVar) middleCode).isConst()) {
                                // assert !allVars.containsKey(middleCode.getVarName());
                                if (!allVars.containsKey(middleCode.getVarName())) {
                                    // 如果varName相同, 意味着 名字 和 blockDepth 都相同, 那么它们生命周期一定不同
                                    allVars.put(middleCode.getVarName(), 1);
                                } else {
                                    System.out.println("same var declare");
                                }
                            }
                            break;
                        case PRINT:
                            break;
                        case SCANF:
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                System.out.println("scanf item not defined");
                                allVars.put(middleCode.getVarName(), 1);
                            }
                            break;
                        case RET:
                            break;
                        case UNARY:
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                allVars.put(middleCode.getVarName(), 1);
                            }
                            break;
                        case RPARA:
                        case GLOBAL_ARRAY:
                            break;
                        case FPARA:
                            // 函数参数的空间是事先分配好的，这里记录下函数参数的名字，后续不能再分配空间了！
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                allVars.put(middleCode.getVarName(), 0);
                            }
                            break;
                        case ARRAY_BASE:
                            if (!allVars.containsKey(middleCode.getVarName())) {
                                allVars.put(middleCode.getVarName(), 1);
                            }
                            break;
                        case FUNCCALL:
                            break;
                        case FUNCDEF:
                            break;
                        case CONSTSTR:
                            break;
                        default:
                            break;
                    }
                }
            }
            bb = bb.getDirectBb();
        }
    }

    public LinkedHashMap<VarName, Integer> getAllVars() {
        for (VarName varName : allVars.keySet()) {
            System.out.println(varName);
        }
        return allVars;
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
