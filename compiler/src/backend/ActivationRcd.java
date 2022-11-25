package backend;

import backend.register.Reg;
import frontend.symbol.VarSymbol;
import middle.VarName;
import middle.quartercode.array.ArrayDef;
import middle.quartercode.operand.Operand;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivationRcd {
    private ActivationRcd parent;   // 应该是所在BasicBlock的父bb里的AR
    private int addrBase;   // 基地址  $fp


    private HashMap<VarName, Reg> varMap2Reg = new HashMap<>();
    private HashMap<String, VarName> regMap2Var = new HashMap<>();

    private ArrayList<String> blockLabels = new ArrayList<>(); // 按照顺序代表block的由深到浅
    // private HashMap<String, HashMap<String, String>> localVars = new HashMap<>();   // 第一个key是label，第二个是装所有当前block的name, name
    private HashMap<Integer, HashMap<String, VarName>> varsOnMem = new HashMap<>();
    private HashMap<String, Integer> offsets = new HashMap<>();
    private int curOffset = 0;
    private int varOccupiedSpace = 0;

    // 动态栈
    public ActivationRcd(ActivationRcd parent/*, int addrBase*//*, ActivationRcd arInBasicBlock*/) {
        this.parent = parent;
    }

    public ActivationRcd() {
    }

    /*public boolean containsLocalVar(VarName name) {
        boolean res = false;
        if (localVars.get(name.getLabel()).containsKey(name.getLocalName())) {
            res = true;
        } else {
            for (String label : blockLabels) {
                if (localVars.get(label).containsKey(name.getLocalName())) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }*/

    public Address getAddr(VarName name) {
        if (varsOnMem.get(name.getDepth()).containsKey(name.toString())) {
            return new Address(offsets.get(name.toString()), true);
        }
        return null;
    }

    public int getVarOccupiedSpace() {
        return varOccupiedSpace;
    }

    public int getCapacity() {
        return curOffset;
    }

    public void varMap2Reg(VarName varName, Reg reg) {
        varMap2Reg.put(varName, reg);
        regMap2Var.put(reg.getName(), varName);
        reg.setAlloced(true);
        reg.setVarName(varName);
    }

    public Reg getVarMapReg(VarName varName) {
        return varMap2Reg.getOrDefault(varName, null);
    }

    public void varUnmapReg(VarName varName) {
        Reg reg = varMap2Reg.get(varName);
        reg.setVarName(null);
        reg.setAlloced(false);
        varMap2Reg.remove(varName);
        regMap2Var.remove(reg.getName());
    }

    public VarName regUnmapVar(Reg reg) {
        VarName varName = regMap2Var.get(reg.getName());
        reg.setVarName(null);
        reg.setAlloced(false);
        varMap2Reg.remove(varName);
        regMap2Var.remove(reg.getName());
        return varName;
    }

    public void removeVarOnMem(int depth) {
        // todo capacity 和 sp 怎么办
        assert false;   // 先别执行啊
        for (String name : varsOnMem.get(depth).keySet()) {
            offsets.remove(name);
        }
    }

    public void arrSetToMem(VarName name, int size) {
        if (!varsOnMem.containsKey(name.getDepth())) {
            varsOnMem.put(name.getDepth(), new HashMap<>());
        }
        varsOnMem.get(name.getDepth()).put(name.toString(), name);
        curOffset = curOffset + size * 4;
        offsets.put(name.toString(), curOffset);    // 数组名指向空间底部, 向上增长
        varOccupiedSpace = varOccupiedSpace + size * 4;
    }

    public void varSetToMem(VarName name) {
        if (!varsOnMem.containsKey(name.getDepth())) {
            varsOnMem.put(name.getDepth(), new HashMap<>());
        }
        varsOnMem.get(name.getDepth()).put(name.toString(), name);
        offsets.put(name.toString(), curOffset);
        curOffset += 4;
        varOccupiedSpace += 4;
    }

    public void regsMapToMem(int offset) {
        // 存到 depth = 0 的位置去啊，多好
        // 有没有一种可能，腾出空间就好。
        /*if (!varsOnMem.containsKey(0)) {
            varsOnMem.put(0, new HashMap<>());
        }
        varsOnMem.get(0).put(reg.getName(), new VarName(reg.getName(), 0));
        offsets.put(reg.getName(), curOffset);*/
        curOffset += offset;
    }

    public void regsUnMapToMem(int offset) {
        curOffset -= offset;
    }

    public boolean isAddressed(VarName name) {
        /*// 这个name是局部变量名和所在基本块的label的合成
        // 同样地也要判断 当前block下还是前面的block里面的
        boolean isAddressed = false;
        if (offsets.containsKey(name.toString())) {
            isAddressed = true;
        } else {
            for (String label : blockLabels) {
                if (offsets.containsKey(name.getLocalName() + label)) {
                    isAddressed = true;
                    break;
                }
            }
        }
        return isAddressed;*/
        return offsets.containsKey(name.toString());
    }
}
