package backend;

import backend.mipsCode.Label2Jump;
import backend.mipsCode.MipsCode;
import backend.mipsCode.global.Data;
import backend.mipsCode.global.GlobalStr;
import backend.mipsCode.global.GlobalVar;
import backend.mipsCode.global.Text;
import backend.mipsCode.instruction.*;
import backend.register.Reg;
import backend.register.Registers;
import middle.BasicBlock;
import middle.FuncDefBlock;
import middle.VarName;
import middle.quartercode.*;
import middle.quartercode.array.ArrayDef;
import middle.quartercode.array.ArrayLoad;
import middle.quartercode.array.ArrayStore;
import middle.quartercode.array.GlobalArray;
import middle.quartercode.function.FParaCode;
import middle.quartercode.function.FuncCallCode;
import middle.quartercode.function.RParaCode;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.Immediate;
import middle.quartercode.operand.primaryOpd.LValOpd;
import middle.quartercode.operand.primaryOpd.RetOpd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class CodeGen {
    private HashMap<String, ConstStrCode> constStrs;
    private HashMap<VarName, MiddleCode> globalVars = new HashMap<>();  // globalAR
    private BasicBlock globalBlock;
    private BasicBlock curBlock;
    private HashMap<String, FuncDefBlock> funcDefBbMap;
    private ArrayList<FuncDefBlock> funcDefBlocks;
    private Stack<ActivationRcd> activationRcdStack;
    private ActivationRcd curAR = null;
    private ArrayList<MipsCode> mipsCodes;
    private Registers registers;

    public CodeGen(HashMap<String, ConstStrCode> constStrs, BasicBlock globalBlock, HashMap<String, FuncDefBlock> funcDefBbMap, ArrayList<FuncDefBlock> funcDefBlocks) {
        this.constStrs = constStrs;
        this.globalBlock = globalBlock;
        this.funcDefBbMap = funcDefBbMap;
        this.funcDefBlocks = funcDefBlocks;
        this.activationRcdStack = new Stack<>();
        this.mipsCodes = new ArrayList<>();
        this.registers = new Registers();
    }

    public void mipsGen() {
        mipsCodes.add(new Data());
        globalGen();

        mipsCodes.add(new Text());
        mipsCodes.add(new J("main"));

        for (FuncDefBlock funcDefBlock : funcDefBlocks) {
            funcGen(funcDefBlock);
        }
    }

    private void globalGen() {
        // 字符串
        for (String content : constStrs.keySet()) {
            // System.out.println(name + constStrs.get(name).getContent());
            mipsCodes.add(new GlobalStr(content, constStrs.get(content).getVarName().toString()));
        }
        // 全局变量、数组
        for (MiddleCode middleCode : globalBlock.getMiddleCodes()) {
            globalVars.put(middleCode.getVarName(), middleCode);
            if (middleCode instanceof GlobalArray) {
                mipsCodes.add(new GlobalVar(middleCode.getVarName(), 1, ((GlobalArray) middleCode).getValues()));
            } else {
                // assert middleCode instanceof ConstVar;
                ConstVar constVar = (ConstVar) middleCode;
                if (!constVar.isConst()) {  //如果是const可以不用管
                    ArrayList<Integer> values = null;
                    if (constVar.isInit()) {
                        values = new ArrayList<>();
                        values.add(constVar.getOperandImm());
                    }
                    mipsCodes.add(new GlobalVar(constVar.getVarName(), 0, values));
                }
            }
        }
    }

    // 一直到curBlock为下一个函数体的基本块
    private void funcGen(FuncDefBlock funcDefBlock) {
        // 函数体
        // 一些初始化

        curBlock = funcDefBlock.getStartBb();
        boolean isMainFunc = curBlock.getLable().equals("main");
        // callee saved
        ArrayList<Reg> generalRegsUsed = funcDefBlock.getGeneralRegsUsed();

        String label_funcEnd = curBlock.getLable() + "_ret_"; // todo
        while (funcDefBbMap.containsKey(label_funcEnd)) {
            // 别和函数名冲了
            label_funcEnd += "0";
        }

        curAR = new ActivationRcd(curAR);
        activationRcdStack.push(curAR);

        /*HashMap<Integer, Integer> blockCapacity = new HashMap<>(); // depth -> start capacity
        blockCapacity.put(curBlock.getDepth(), curAR.getCapacity());*/

        Iterator<MiddleCode> middleCodeIter = curBlock.getMiddleCodes().iterator();

        MiddleCode middleCode = middleCodeIter.next();  // 这个就是FuncDefCode
        mipsCodes.add(new Label2Jump(middleCode.getVarName().toString()));

        // 记录当前保存函数局部变量的栈底(fp)
        mipsCodes.add(new Move(Registers.FP, Registers.SP));    // todo curSp 和 Register.SP 应该是相等的吧

        // FuncDefCode
        if (!isMainFunc) {
            // 栈上保存的参数
            int i;
            for (i = 1; i < curBlock.getMiddleCodes().size(); i++) {
                if (curBlock.getMiddleCodes().get(i) instanceof FParaCode) {
                    middleCode = middleCodeIter.next();
                    if (i <= 4) {
                        // 前四个参数在寄存器里
                        Reg reg = i == 1 ? Registers.A0 : i == 2 ? Registers.A1 : i == 3 ? Registers.A2 : Registers.A3;
                        curAR.varMap2Reg(middleCode.getVarName(), reg);
                    } else {
                        // 保存在栈上， 就指定这个位置是它的就好
                        curAR.varSetToMem(middleCode.getVarName());
                    }
                } else {
                    break;
                }
            }
            if (i > 5) {
                // i处不是参数
                // 栈上有参数，“压栈”
                mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate((i - 5) * 4)));
            }

            // callee save
            regsStore(generalRegsUsed);

            // ra
            if (funcDefBlock.isRaNeedSave()) {
                mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
                mipsCodes.add(new Store(Registers.RA, Registers.SP, new Immediate(0)));
                curAR.regsMapToMem(4);
            }
        }

        do {
            /*if (!blockCapacity.containsKey(curBlock.getDepth())) {
                blockCapacity.put(curBlock.getDepth(), curAR.getCapacity());
            }*/

            while (middleCodeIter.hasNext()) {
                middleCode = middleCodeIter.next();
                switch (middleCode.getCodeType()) {
                    case ARRAY_DEF:
                        curAR.arrSetToMem(middleCode.getVarName(), ((ArrayDef) middleCode).getSize());
                        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(((ArrayDef) middleCode).getSize() * 4)));
                        break;
                    case ARRAY_LOAD:
                        // dst = array[idx]
                        ArrayLoad arrayLoad = (ArrayLoad) middleCode;
                        Operand dst = arrayLoad.getDst();
                        LValOpd lValOpd = (LValOpd) arrayLoad.getPrimaryOpd();
                        /*VarName array = lValOpd.getVarName();
                        Operand idx = lValOpd.getIdx();

                        if (dst.isGlobalVar()) {
                            // label = array[idx]
                            // store
                            if (array.isGlobalVar()) {
                                if (idx.isGlobalVar()) {
                                    mipsCodes.add(new Load(Registers.AT, idx.toString(), new Immediate(0)));
                                    mipsCodes.add(new Load(Registers.AT, array.toString(), Registers.AT));
                                    mipsCodes.add(new Store(Registers.AT, dst.toString(), new Immediate(0)));
                                } else {
                                    mipsCodes.add(new Load(Registers.AT, array.toString(), getOrAllocReg4Var(idx.getVarName(), true)));
                                    mipsCodes.add(new Store(Registers.AT, dst.toString(), new Immediate(0)));
                                }
                            } else {
                                // todo 不写了，代码生成二再说, 下面所有数组load都
                                *//*if (idx.isGlobalVar()) {
                                    mipsCodes.add(new Load(Registers.AT, idx.toString(), new Immediate(0)));
                                    mipsCodes.add(new Load(Registers.AT, array.toString(), Registers.AT));
                                    mipsCodes.add(new Store(Registers.AT, dst.toString(), new Immediate(0)));
                                } else {
                                    mipsCodes.add(new Load(Registers.AT, array.toString(), getOrAllocReg4Var(idx.getVarName(), true)));
                                    mipsCodes.add(new Store(Registers.AT, dst.toString(), new Immediate(0)));
                                }*//*
                            }
                        }*/
                        Reg dstReg = getOrAllocReg4Var(dst.getVarName(), false);


                        if (lValOpd.isGlobalVar()) {
                            // 全局数组
                            String label = lValOpd.getVarName().toString();
                            if (lValOpd.getIdx() instanceof Immediate) {
                                ((Immediate) lValOpd.getIdx()).procValue("*", new Immediate(4));
                                mipsCodes.add(new Load(dstReg, label, (Immediate) lValOpd.getIdx()));
                            } else {
                                Reg offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true);
                                mipsCodes.add(new Sll(Registers.AT, offset, 2));
                                mipsCodes.add(new Load(dstReg, label, Registers.AT));
                            }
                        } else {
                            Reg base = getOrAllocReg4Var(lValOpd.getVarName(), true);
                            if (lValOpd.getIdx() instanceof Immediate) {
                                ((Immediate) lValOpd.getIdx()).procValue("*", new Immediate(4));
                                mipsCodes.add(new Load(dstReg, base, (Immediate) lValOpd.getIdx()));
                            } else {
                                Reg offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true);
                                mipsCodes.add(new Sll(Registers.AT, offset, 2));
                                mipsCodes.add(new Add(Registers.AT, Registers.AT, base));
                                mipsCodes.add(new Load(dstReg, Registers.AT, null));
                            }
                        }
                        break;
                    case ARRAY_STORE:
                        // array[idx] = src
                        ArrayStore arrayStore = (ArrayStore) middleCode;

                        Operand src = arrayStore.getSrc();
                        Reg srcReg = getOrAllocReg4Var(src.getVarName(), true);

                        lValOpd = (LValOpd) arrayStore.getPrimaryOpd();

                        if (lValOpd.isGlobalVar()) {
                            String label = lValOpd.getVarName().toString();
                            if (lValOpd.getIdx() instanceof Immediate) {
                                ((Immediate) lValOpd.getIdx()).procValue("*", new Immediate(4));
                                mipsCodes.add(new Store(srcReg, label, (Immediate) lValOpd.getIdx()));
                            } else {
                                Reg offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true);
                                mipsCodes.add(new Sll(Registers.AT, offset, 2));
                                mipsCodes.add(new Store(srcReg, label, Registers.AT));
                            }
                        } else {
                            Reg base = getOrAllocReg4Var(lValOpd.getVarName(), true);
                            if (lValOpd.getIdx() instanceof Immediate) {
                                ((Immediate) lValOpd.getIdx()).procValue("*", new Immediate(4));
                                mipsCodes.add(new Store(srcReg, base, (Immediate) lValOpd.getIdx()));
                            } else {
                                Reg offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true);
                                mipsCodes.add(new Sll(Registers.AT, offset, 2));
                                mipsCodes.add(new Add(Registers.AT, Registers.AT, base));
                                mipsCodes.add(new Store(srcReg, Registers.AT, null));
                            }
                        }
                        break;
                    case ASSIGN:
                        AssignCode assignCode = (AssignCode) middleCode;
                        dstReg = getOrAllocReg4Var(assignCode.getVarName(), false);
                        if (assignCode.getOperand() instanceof Immediate) {
                            mipsCodes.add(new Li(dstReg, (Immediate) assignCode.getOperand()));
                        } else if (assignCode.getOperand() instanceof RetOpd) {
                            // todo 考虑函数调用之前有用v0存临时，函数调用之后会覆盖掉这个返回值，用getReg
                            mipsCodes.add(new Move(dstReg, Registers.V0));
                        } else {
                            assert false;
                        }
                        break;
                    case BINARY:
                        BinaryCode binaryCode = (BinaryCode) middleCode;
                        if (binaryCode.getSrc1() instanceof Immediate) {
                            Immediate immediate = (Immediate) binaryCode.getSrc1();
                            srcReg = Registers.AT;
                            mipsCodes.add(new Li(srcReg, immediate));
                        } else if (binaryCode.getSrc1() instanceof RetOpd) {
                            srcReg = Registers.V0;
                        } else {
                            srcReg = getOrAllocReg4Var(binaryCode.getSrc1().getVarName(), true);
                        }
                        Immediate immediate = null;
                        Reg srcReg2 = null;
                        boolean src2IsImm = false;
                        if (binaryCode.getSrc2() instanceof Immediate) {
                            immediate = (Immediate) binaryCode.getSrc2();
                            src2IsImm = true;
                        } else if (binaryCode.getSrc2() instanceof RetOpd) {
                            srcReg2 = Registers.V0;
                        } else {
                            srcReg2 = getOrAllocReg4Var(binaryCode.getSrc2().getVarName(), true);
                        }
                        dstReg = getOrAllocReg4Var(binaryCode.getVarName(), false);
                        switch (binaryCode.getOp()) {
                            case ADD:
                                if (src2IsImm) {
                                    mipsCodes.add(new Add(dstReg, srcReg, immediate));
                                } else {
                                    mipsCodes.add(new Add(dstReg, srcReg, srcReg2));
                                }
                                break;
                            case SUB:
                                if (src2IsImm) {
                                    mipsCodes.add(new Sub(dstReg, srcReg, immediate));
                                } else {
                                    mipsCodes.add(new Sub(dstReg, srcReg, srcReg2));
                                }
                                break;
                            case MUL:
                                // todo mfhi ?
                                if (src2IsImm) {
                                    mipsCodes.add(new Mul(dstReg, srcReg, immediate));
                                } else {
                                    mipsCodes.add(new Mul(dstReg, srcReg, srcReg2));
                                }
                                break;
                            case DIV:
                                // todo mfhi ?
                                if (src2IsImm) {
                                    mipsCodes.add(new Div(dstReg, srcReg, immediate));
                                } else {
                                    mipsCodes.add(new Div(dstReg, srcReg, srcReg2));
                                }
                                break;
                            case MOD:
                                if (src2IsImm) {
                                    srcReg2 = Registers.AT;
                                    mipsCodes.add(new Li(srcReg2, immediate));
                                }
                                mipsCodes.add(new Div(srcReg, srcReg2));
                                mipsCodes.add(new Mfhi(dstReg));
                                break;
                        }
                        break;
                    case CONSTVAR:
                        ConstVar constVar = (ConstVar) middleCode;
                        if (constVar.isInit()) {
                            dstReg = getOrAllocReg4Var(constVar.getVarName(), false);
                            if (constVar.getOperand() instanceof Immediate) {
                                mipsCodes.add(new Li(dstReg, (Immediate) constVar.getOperand()));
                            } else if (constVar.getOperand() instanceof RetOpd) {
                                mipsCodes.add(new Move(dstReg, Registers.V0));
                            } else {
                                srcReg = getOrAllocReg4Var(constVar.getOperand().getVarName(), true);
                                mipsCodes.add(new Move(dstReg, srcReg));
                            }
                        }
                        break;
                    case PRINT:
                        PutOut putOut = (PutOut) middleCode;
                        if (putOut.getOperand() instanceof ConstStrCode) {
                            mipsCodes.add(new La(getReg(Registers.A0), putOut.getOperand().getVarName().toString()));
                            mipsCodes.add(new Li(Registers.V0, new Immediate(4)));
                        } else {
                            // int值
                            if (putOut.getOperand() instanceof Immediate) {
                                mipsCodes.add(new Li(getReg(Registers.A0), (Immediate) putOut.getOperand()));
                            } else if (putOut.getOperand() instanceof RetOpd) {
                                mipsCodes.add(new Move(Registers.A0, Registers.V0));
                            } else {
                                srcReg = getOrAllocReg4Var(putOut.getOperand().getVarName(), true);
                                if (srcReg != Registers.A0) {
                                    // System.out.println("maybe optimization!");
                                    mipsCodes.add(new Move(getReg(Registers.A0),
                                            getOrAllocReg4Var(putOut.getOperand().getVarName(), true)));
                                }
                            }
                            mipsCodes.add(new Li(Registers.V0, new Immediate(1)));
                        }
                        mipsCodes.add(new Syscall());
                        break;
                    case SCANF:
                        ReadIn readIn = (ReadIn) middleCode;
                        mipsCodes.add(new Li(Registers.V0, new Immediate(5)));
                        mipsCodes.add(new Syscall());
                        mipsCodes.add(new Move(getOrAllocReg4Var(readIn.getVarName(), false), Registers.V0));
                        break;
                    case RET:
                        // main 的话，不用管返回值啥的吧
                        if (!isMainFunc) {
                            RetCode retCode = (RetCode) middleCode;
                            if (retCode.hasRetValue()) {
                                if (retCode.getOperand() instanceof Immediate) {
                                    mipsCodes.add(new Li(Registers.V0, (Immediate) retCode.getOperand()));
                                } else {
                                    mipsCodes.add(new Move(Registers.V0, getOrAllocReg4Var(middleCode.getVarName(), true)));
                                }
                            }
                        }
                        if (middleCodeIter.hasNext()) {
                            // 函数返回，跳转到最后的标签 todo
                            mipsCodes.add(new J(label_funcEnd));
                        }
                        break;
                    case UNARY:
                        UnaryCode unaryCode = (UnaryCode) middleCode;
                        srcReg = getOrAllocReg4Var(unaryCode.getSrc().getVarName(), true);
                        dstReg = getOrAllocReg4Var(unaryCode.getVarName(), false);
                        switch (unaryCode.getOp().getName()) {
                            case "+":
                                // System.out.println("maybe optimization at UNARY!");
                                mipsCodes.add(new Move(dstReg, srcReg));
                                break;
                            case "-":
                                mipsCodes.add(new Neg(dstReg, srcReg));
                                break;
                        }
                        break;
                    case RPARA:
                        // 交给 FunCall处理
                        break;
                    case FUNCCALL:
                        FuncCallCode funcCallCode = (FuncCallCode) middleCode;
                        // curAR = new ActivationRcd(curAR, curSp);
                        // 先保存 fp
                        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
                        mipsCodes.add(new Store(Registers.FP, Registers.SP, new Immediate(0)));
                        curAR.regsMapToMem(4);

                        // push参数
                        ArrayList<Operand> rParaCodes = funcCallCode.getrParaCodes();
                        for (int i = 0; i < rParaCodes.size(); i++) {
                            // 这一步是把所有没有分配到寄存器的变量，都分配到位
                            if (!(((RParaCode) rParaCodes.get(i)).getOperand() instanceof Immediate ||
                                    ((RParaCode) rParaCodes.get(i)).getOperand() instanceof RetOpd)) {
                                getOrAllocReg4Var(rParaCodes.get(i).getVarName(), true);
                            }
                        }
                        getReg(Registers.A0);
                        getReg(Registers.A1);
                        getReg(Registers.A2);
                        getReg(Registers.A3);

                        // caller saved                 // push 的时候也会用到寄存器，最好保护
                        ArrayList<Reg> tRegsUsed = registers.getUsedTRegs();
                        regsStore(tRegsUsed);

                        for (int i = 0; i < rParaCodes.size(); i++) {
                            if (i < 4) {
                                dstReg = i == 0 ? Registers.A0 : i == 1 ? Registers.A1 : i == 2 ? Registers.A2 : Registers.A3;

                                if (((RParaCode) rParaCodes.get(i)).getOperand() instanceof Immediate) {
                                    mipsCodes.add(new Li(dstReg, (Immediate) ((RParaCode) rParaCodes.get(i)).getOperand()));
                                } else if (((RParaCode) rParaCodes.get(i)).getOperand() instanceof RetOpd) {
                                    mipsCodes.add(new Move(dstReg, Registers.V0));
                                } else {
                                    srcReg = getOrAllocReg4Var(rParaCodes.get(i).getVarName(), true);
                                    mipsCodes.add(new Move(dstReg, srcReg));    // 需要get一下？
                                }
                            }

                            if (i >= 4) {
                                // 放在栈上，但是栈空间先不给，保持sp
                                if (((RParaCode) rParaCodes.get(i)).getOperand() instanceof Immediate) {
                                    mipsCodes.add(new Li(Registers.AT, (Immediate) ((RParaCode) rParaCodes.get(i)).getOperand()));
                                    srcReg = Registers.AT;
                                } else if (((RParaCode) rParaCodes.get(i)).getOperand() instanceof RetOpd) {
                                    srcReg = Registers.V0;
                                } else {
                                    srcReg = getOrAllocReg4Var(rParaCodes.get(i).getVarName(), true);
                                }
                                mipsCodes.add(new Store(srcReg, Registers.SP, new Immediate(-4 * (i - 3))));
                            }
                        }

                        mipsCodes.add(new Jal(funcCallCode.getVarName().toString()));

                        // 函数返回之后
                        mipsCodes.add(new Move(Registers.SP, Registers.FP));    // reset 栈顶
                        regsLoad(tRegsUsed);
                        mipsCodes.add(new Load(Registers.FP, Registers.SP, new Immediate(0)));
                        curAR.regsUnMapToMem(4);    // fp 空间的释放
                        mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(4)));

                        break;
                }
            }
            /*// curBlock 和 nextBlock 层数相同的话? 那应该是不同的Block
            if (curBlock.getDirectBb() != null) {
                if (curBlock.getDepth() >= curBlock.getDirectBb().getDepth()) {
                    // 弹栈
                    int prevCapacity = blockCapacity.get(curBlock.getDepth());
                    mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(curAR.getCapacity() - prevCapacity)));
                    blockCapacity.remove(curBlock.getDepth());
                    // AR记录的变量也要删掉？
                    curAR.removeVarOnMem(curBlock.getLable());
                }
            }*/
            curBlock = curBlock.getDirectBb();
            if (curBlock != null) {
                middleCodeIter = curBlock.getMiddleCodes().iterator();
            }
        } while (curBlock != null);

        // main函数中间也允许return的
        mipsCodes.add(new Label2Jump(label_funcEnd));


        // 函数退出
        if (!isMainFunc) {
            freeAllTReg();  // 切换到另外的函数块中去了，只需要把isAlloced设置为 false

            // 释放局部变量的栈空间
            if (curAR.getVarOccupiedSpace() != 0) {
                mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(curAR.getVarOccupiedSpace())));
            }

            // 先弹ra 如果有
            if (funcDefBlock.isRaNeedSave()) {
                mipsCodes.add(new Load(getReg(Registers.RA), Registers.SP, new Immediate(0)));
                curAR.regsUnMapToMem(4); // ra 空间的释放
                mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(4)));
            }
            // callee saved
            regsLoad(generalRegsUsed);
            mipsCodes.add(new Jr(Registers.RA));
        } else {
            mipsCodes.add(new Li(Registers.V0, new Immediate(10)));
            mipsCodes.add(new Syscall());
        }

    }

    private void regsStore(ArrayList<Reg> src) {
        if (src.isEmpty()) {
            return;
        }
        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(src.size() * 4)));
        int offset = 0;
        for (Reg reg : src) {
            mipsCodes.add(new Store(reg, Registers.SP, new Immediate(offset)));
            // curAR.regUnmapVar(reg);    // 只需要把reg置为未分配即可，不要删除reg和varName的映射关系（因为映射关系存在于AR中，函数调用之后AR改变了
            reg.setAlloced(false);
            offset += 4;
        }
        curAR.regsMapToMem(offset); // 分配空间
    }

    private void regsLoad(ArrayList<Reg> dst) {
        if (dst.isEmpty()) {
            return;
        }
        int offset = 0;
        for (Reg reg : dst) {
            mipsCodes.add(new Load(reg, Registers.SP, new Immediate(offset)));
            reg.setAlloced(true);
            // curAR.varUnmapReg();
            offset += 4;
        }
        // 空间收回
        curAR.regsUnMapToMem(offset);
        mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(offset)));
    }

    // todo 没有重新绑定, 以及标记为绑定状态
    // 用于 A0,V0 即选即用的
    private Reg getReg(Reg reg) {
        if (reg.isAlloced()) {
            Reg newReg = registers.allocReg();
            VarName storeVar = curAR.regUnmapVar(reg);
            // 把 storeVar 移到另外的寄存器或者内存中
            if (newReg != null) {
                mipsCodes.add(new Move(newReg, reg));
                curAR.varMap2Reg(storeVar, newReg);
            } else {
                storeBack(reg, storeVar);
            }
            reg.setAlloced(false);
        }
        return reg;
    }

    private Reg getOrAllocReg4Var(VarName name, boolean needLoad) {
        Reg reg = curAR.getVarMapReg(name);
        if (reg == null) {
            reg = allocReg2Var(name, needLoad);
        }
        return reg;
    }

    private Reg allocReg2Var(VarName name, boolean needLoad) {
        Reg reg = registers.allocReg();
        if (reg == null) {
            reg = registers.freeOneTReg();
            VarName storeVar = curAR.regUnmapVar(reg);
            // 把 storeVar 给 store 回去
            storeBack(reg, storeVar);
        }
        if (needLoad) {
            if (name.isGlobalVar()) {
                mipsCodes.add(new Load(reg, name.toString(), new Immediate(0)));
            } else {
                mipsCodes.add(new Load(reg, curAR.getAddr(name)));
            }
        }
        curAR.varMap2Reg(name, reg);
        return reg;
    }

    private void storeBack(Reg reg, VarName storeVar) {
        // 把 storeVar 给 store 回去
        if (storeVar.isGlobalVar()) {
            // 对于全局变量也是不够了就放回去
            mipsCodes.add(new Store(reg, storeVar.toString(), new Immediate(0)));
        } else {
            if (!curAR.isAddressed(storeVar)) {
                curAR.varSetToMem(storeVar);
                mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
            }
            Address addr = curAR.getAddr(storeVar);
            mipsCodes.add(new Store(reg, addr));
        }
    }

    public void freeAllTReg() {
        // 如果有映射到全局变量的寄存器，要存回
        for (Reg reg : registers.getTempRegs()) {
            if (reg.isAlloced()) {
                reg.setAlloced(false);
                VarName storeVar = curAR.regUnmapVar(reg);
                // 把 storeVar 给 store 回去
                if (storeVar.isGlobalVar()) {
                    mipsCodes.add(new Store(reg, storeVar.toString(), new Immediate(0)));
                }/* else {
                    if (!curAR.isAddressed(storeVar)) {
                        curAR.varSetToMem(storeVar);
                        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
                    }
                    Address addr = curAR.getAddr(storeVar);
                    mipsCodes.add(new Store(reg, addr));
                }*/
            }
        }
    }

    public ArrayList<MipsCode> getMipsCodes() {
        return mipsCodes;
    }
}
