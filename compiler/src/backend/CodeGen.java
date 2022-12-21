package backend;

import backend.mipsCode.Annotation;
import backend.mipsCode.Label2Jump;
import backend.mipsCode.MipsCode;
import backend.mipsCode.global.Data;
import backend.mipsCode.global.GlobalStr;
import backend.mipsCode.global.GlobalVar;
import backend.mipsCode.global.Text;
import backend.mipsCode.instruction.*;
import backend.mipsCode.instruction.branch.Cmp;
import backend.mipsCode.instruction.branch.Jump;
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
import middle.quartercode.branch.JumpCmp;
import middle.quartercode.branch.SaveCmp;
import middle.quartercode.function.FParaCode;
import middle.quartercode.function.FuncCallCode;
import middle.quartercode.function.RParaCode;
import middle.quartercode.operand.MiddleCode;
import middle.quartercode.operand.Operand;
import middle.quartercode.operand.primaryOpd.Immediate;
import middle.quartercode.operand.primaryOpd.LValOpd;
import middle.quartercode.operand.primaryOpd.RetOpd;

import java.util.*;

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

    private ArrayList<GlobalArray> globalArrays = new ArrayList<>();

    private boolean prevCodeIsGoto = false;

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
        for (GlobalArray globalArray : globalArrays) {
            int offset = 0;
            for (int value : globalArray.getValues()) {
                mipsCodes.add(new Li(Registers.V0, new Immediate(value)));
                mipsCodes.add(new Store(Registers.V0, globalArray.getVarName().toString(), new Immediate(offset)));
                offset += 4;
            }
        }
        mipsCodes.add(new J("main"));

        for (FuncDefBlock funcDefBlock : funcDefBlocks) {
            funcGen(funcDefBlock);
        }
    }

    private void globalGen() {
        // 全局变量、数组
        for (MiddleCode middleCode : globalBlock.getMiddleCodes()) {
            globalVars.put(middleCode.getVarName(), middleCode);
            if (middleCode instanceof GlobalArray) {
                mipsCodes.add(new GlobalVar(middleCode.getVarName(), 1, ((GlobalArray) middleCode).getSize(), ((GlobalArray) middleCode).getValues()));
                if (!((GlobalArray) middleCode).getValues().isEmpty()) {
                    // 需要在text字段初始化
                    globalArrays.add((GlobalArray) middleCode);
                }
            } else {
                // assert middleCode instanceof ConstVar;
                ConstVar constVar = (ConstVar) middleCode;
                if (!constVar.isConst()) {  //如果是const可以不用管
                    ArrayList<Integer> values = null;
                    if (constVar.isInit()) {
                        values = new ArrayList<>();
                        // todo 不会有 int a = b + c这样的吗
                        values.add(constVar.getOperandImm());
                    }
                    mipsCodes.add(new GlobalVar(constVar.getVarName(), 0, 0, values));
                }
            }
        }
        // 字符串
        for (String content : constStrs.keySet()) {
            // System.out.println(name + constStrs.get(name).getContent());
            mipsCodes.add(new GlobalStr(content, constStrs.get(content).getVarName().toString()));
        }
    }

    // 一直到curBlock为下一个函数体的基本块
    private void funcGen(FuncDefBlock funcDefBlock) {
        // 函数体
        // 一些初始化

        curBlock = funcDefBlock.getStartBb();
        boolean isMainFunc = curBlock.getLable().equals("main");
        // todo callee saved
        ArrayList<Reg> generalRegsUsed = isMainFunc ? new ArrayList<>() : funcDefBlock.getGeneralRegsUsed();

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
                curAR.varSetToMem(new VarName("31RA", 0));
                // curAR.regsMapToMem(4);
            }
        }

        do {
            /*if (!blockCapacity.containsKey(curBlock.getDepth())) {
                blockCapacity.put(curBlock.getDepth(), curAR.getCapacity());
            }*/
            if (curBlock.getBbType() != BasicBlock.BBType.BASIC &&
                    curBlock.getBbType() != BasicBlock.BBType.FUNC) {
                // System.out.println(curBlock.getLable());
                mipsCodes.add(new Label2Jump(curBlock.getLable()));
            }
            while (middleCodeIter.hasNext()) {
                middleCode = middleCodeIter.next();
                switch (middleCode.getCodeType()) {
                    case SAVECMP:
                        SaveCmp saveCmp = (SaveCmp) middleCode;
                        Reg dst = getOrAllocReg4Var(saveCmp.getVarName(), false, null, null);
                        Reg op1;
                        if (saveCmp.getCmpOp1() instanceof Immediate) {
                            // todo 寄存器分配
                            op1 = getOneTReg(dst, null);
                            mipsCodes.add(new Li(op1, (Immediate) saveCmp.getCmpOp1()));
                        } else if (saveCmp.getCmpOp1() instanceof RetOpd) {
                            op1 = Registers.V0;
                        } else {
                            assert !(saveCmp.getCmpOp1() instanceof LValOpd && ((LValOpd) saveCmp.getCmpOp1()).isArray());
                            op1 = getOrAllocReg4Var(saveCmp.getCmpOp1().getVarName(), true, dst, null);
                        }
                        // Reg op1 = getOrAllocReg4Var(saveCmp.getCmpOp1().getVarName(), true, dst, null);
                        Reg op2 = null;
                        Immediate op2Imm = null;
                        if (saveCmp.getCmpOp2() instanceof Immediate) {
                            op2Imm = (Immediate) saveCmp.getCmpOp2();
                        } else if (saveCmp.getCmpOp2() instanceof RetOpd) {
                            assert !(saveCmp.getCmpOp2() instanceof RetOpd);
                            op2 = Registers.V0;
                        } else {
                            assert !(saveCmp.getCmpOp2() instanceof LValOpd && ((LValOpd) saveCmp.getCmpOp2()).isArray());
                            op2 = getOrAllocReg4Var(saveCmp.getCmpOp2().getVarName(), true, dst, op1);
                        }
                        mipsCodes.add(new Cmp(saveCmp.getCmpType(), dst, op1, op2, op2Imm));
                        break;
                    case JUMPCMP:
                        storeVarsInCurBlock();
                        JumpCmp jumpCmp = (JumpCmp) middleCode;
                        // op1 = jumpCmp.getCmpOp1() == null ? null : getOrAllocReg4Var(jumpCmp.getCmpOp1().getVarName(), true, null, null);
                        if (jumpCmp.getCmpOp1() == null) {
                            op1 = null;
                        } else {
                            if (jumpCmp.getCmpOp1() instanceof RetOpd) {
                                op1 = Registers.V0;
                            } else {
                                assert !(jumpCmp.getCmpOp1() instanceof Immediate);
                                op1 = getOrAllocReg4Var(jumpCmp.getCmpOp1().getVarName(), true, null, null);
                                // op1 = getOrTmpDesignateReg(jumpCmp.getCmpOp1().getVarName(), null, null);
                            }
                        }
                        // op2 = (jumpCmp.getCmpOp2() == null || jumpCmp.getCmpOp2() instanceof Immediate) ? null :
                        //         getOrAllocReg4Var(jumpCmp.getCmpOp2().getVarName(), true, op1, null);
                        if (jumpCmp.getCmpOp2() == null || jumpCmp.getCmpOp2() instanceof Immediate) {
                            op2 = null;
                        } else {
                            if (jumpCmp.getCmpOp2() instanceof RetOpd) {
                                assert !(jumpCmp.getCmpOp1() instanceof RetOpd);
                                op2 = Registers.V0;
                            } else {
                                op2 = getOrAllocReg4Var(jumpCmp.getCmpOp2().getVarName(), true, op1, null);
                                // op2 = getOrTmpDesignateReg(jumpCmp.getCmpOp2().getVarName(), op1, null);
                            }
                        }
                        op2Imm = (jumpCmp.getCmpOp2() == null || op2 != null) ? null : (Immediate) jumpCmp.getCmpOp2();
                        mipsCodes.add(new Jump(jumpCmp.getJumpType(), op1, op2, op2Imm, jumpCmp.getJumpTgtLabel()));
                        if (op1 != null && op1 != Registers.V0) {
                            curAR.regUnmapVar(op1);
                        }
                        if (op2 != null && op2 != Registers.V0) {
                            curAR.regUnmapVar(op2);
                        }
                        break;
                    case ARRAY_DEF:
                        curAR.arrSetToMem(middleCode.getVarName(), ((ArrayDef) middleCode).getSize());
                        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(((ArrayDef) middleCode).getSize() * 4)));
                        break;
                    case ARRAY_LOAD:
                        // dst = array[idx]
                        ArrayLoad arrayLoad = (ArrayLoad) middleCode;
                        Reg dstReg = getOrAllocReg4Var(arrayLoad.getDst().getVarName(), false, null, null);
                        loadOrStoreArray(dstReg, (LValOpd) arrayLoad.getPrimaryOpd(), true);
                        break;
                    case ARRAY_STORE:
                        // array[idx] = src
                        ArrayStore arrayStore = (ArrayStore) middleCode;
                        Reg srcReg;
                        if (arrayStore.getSrc() instanceof Immediate) {
                            srcReg = Registers.V0;
                            mipsCodes.add(new Li(srcReg, (Immediate) arrayStore.getSrc()));
                        } else if (arrayStore.getSrc() instanceof RetOpd) {
                            srcReg = Registers.V0;
                        } else {
                            srcReg = getOrAllocReg4Var(arrayStore.getSrc().getVarName(), true, null, null);
                        }
                        loadOrStoreArray(srcReg, (LValOpd) arrayStore.getPrimaryOpd(), false);
                        break;
                    case ASSIGN:
                        AssignCode assignCode = (AssignCode) middleCode;
                        dstReg = getOrAllocReg4Var(assignCode.getVarName(), false, null, null);
                        if (assignCode.getOperand() instanceof Immediate) {
                            mipsCodes.add(new Li(dstReg, (Immediate) assignCode.getOperand()));
                        } else if (assignCode.getOperand() instanceof RetOpd) {
                            mipsCodes.add(new Move(dstReg, Registers.V0));
                        } else if (assignCode.getOperand() instanceof LValOpd && ((LValOpd) assignCode.getOperand()).isArray()) {
                            loadOrStoreArray(dstReg, (LValOpd) assignCode.getOperand(), true);
                        } else {
                            srcReg = getOrAllocReg4Var(assignCode.getOperand().getVarName(), true, dstReg, null);
                            mipsCodes.add(new Move(dstReg, srcReg));
                        }
                        break;
                    case BINARY:
                        BinaryCode binaryCode = (BinaryCode) middleCode;
                        Operand src1 = binaryCode.getSrc1();
                        Operand src2 = binaryCode.getSrc2();
                        Reg immReg = null;
                        if (src1 instanceof Immediate) {
                            Immediate immediate = (Immediate) src1;
                            immReg = getOrAllocReg4Var(src1.getVarName(), false, null, null);
                            srcReg = immReg;    // 不能是AT啊
                            mipsCodes.add(new Li(srcReg, immediate));
                        } else if (src1 instanceof RetOpd) {
                            assert !(src2 instanceof RetOpd);
                            srcReg = Registers.V0;
                        } /*else if (src1 instanceof LValOpd && ((LValOpd) src1).isArray()) {
                            assert false;
                        } */ else {
                            assert !(src1 instanceof LValOpd && ((LValOpd) src1).isArray());
                            srcReg = getOrAllocReg4Var(binaryCode.getSrc1().getVarName(), true, null, null);
                        }
                        Immediate immediate = null;
                        Reg srcReg2 = null;
                        boolean src2IsImm = false;
                        if (binaryCode.getSrc2() instanceof Immediate) {
                            immediate = (Immediate) binaryCode.getSrc2();
                            src2IsImm = true;
                        } else if (binaryCode.getSrc2() instanceof RetOpd) {
                            assert !(binaryCode.getSrc1() instanceof RetOpd);
                            srcReg2 = Registers.V0;
                        } else {
                            assert !(src2 instanceof LValOpd && ((LValOpd) src2).isArray());
                            srcReg2 = getOrAllocReg4Var(binaryCode.getSrc2().getVarName(), true, srcReg, null);
                        }
                        dstReg = getOrAllocReg4Var(binaryCode.getVarName(), false, null, null);
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
                        if (immReg != null) {
                            curAR.regUnmapVar(immReg);
                        }
                        break;
                    case CONSTVAR:
                        ConstVar constVar = (ConstVar) middleCode;
                        if (!constVar.isConst() && constVar.isInit()) {
                            dstReg = getOrAllocReg4Var(constVar.getVarName(), false, null, null);
                            if (constVar.getOperand() instanceof Immediate) {
                                mipsCodes.add(new Li(dstReg, (Immediate) constVar.getOperand()));
                            } else if (constVar.getOperand() instanceof RetOpd) {
                                mipsCodes.add(new Move(dstReg, Registers.V0));
                            } else if (constVar.getOperand() instanceof LValOpd && ((LValOpd) constVar.getOperand()).isArray()) {
                                loadOrStoreArray(dstReg, (LValOpd) constVar.getOperand(), true);
                            } else {
                                srcReg = getOrAllocReg4Var(constVar.getOperand().getVarName(), true, dstReg, null);
                                mipsCodes.add(new Move(dstReg, srcReg));
                            }
                        }
                        break;
                    case PRINT:
                        PutOut putOut = (PutOut) middleCode;
                        Operand operand = putOut.getOperand();
                        if (operand instanceof ConstStrCode) {
                            mipsCodes.add(new La(getReg(Registers.A0), operand.getVarName().toString()));
                            mipsCodes.add(new Li(Registers.V0, new Immediate(4)));
                        } else {
                            if (operand instanceof Immediate) {
                                mipsCodes.add(new Li(getReg(Registers.A0), (Immediate) operand));
                            } else if (operand instanceof RetOpd) {
                                mipsCodes.add(new Move(getReg(Registers.A0), Registers.V0));
                            } else if (operand instanceof LValOpd && ((LValOpd) operand).isArray()) {
                                loadOrStoreArray(getReg(Registers.A0), (LValOpd) operand, true);
                            } else {
                                srcReg = getOrAllocReg4Var(putOut.getOperand().getVarName(), true, null, null);
                                if (srcReg != Registers.A0) {
                                    // System.out.println("maybe optimization!");
                                    mipsCodes.add(new Move(getReg(Registers.A0), srcReg));
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
                        mipsCodes.add(new Move(getOrAllocReg4Var(readIn.getVarName(), false, null, null), Registers.V0));
                        break;
                    case RET:
                        // main 的话，不用管返回值啥的吧
                        if (!isMainFunc) {
                            RetCode retCode = (RetCode) middleCode;
                            if (retCode.hasRetValue()) {
                                if (retCode.getOperand() instanceof Immediate) {
                                    mipsCodes.add(new Li(Registers.V0, (Immediate) retCode.getOperand()));
                                } else if (retCode.getOperand() instanceof RetOpd) {
                                    // do nothing
                                } else if (retCode.getOperand() instanceof LValOpd && ((LValOpd) retCode.getOperand()).isArray()) {
                                    loadOrStoreArray(Registers.V0, (LValOpd) retCode.getOperand(), true);
                                } else {
                                    Reg reg = curAR.getVarMapReg(middleCode.getVarName());
                                    if (reg == null) {
                                        loadValue2Reg(middleCode.getVarName(), Registers.V0);
                                    } else {
                                        mipsCodes.add(new Move(Registers.V0, getOrAllocReg4Var(middleCode.getVarName(), true, null, null)));
                                    }
                                }
                            }
                        }
                        if (middleCodeIter.hasNext() || curBlock.getDirectBb() != null) {
                            // 函数返回，跳转到最后的标签 todo
                            storeVarsInCurBlock();
                            mipsCodes.add(new J(label_funcEnd));
                        }
                        break;
                    case UNARY:
                        UnaryCode unaryCode = (UnaryCode) middleCode;
                        if (unaryCode.getSrc() instanceof RetOpd) {
                            srcReg = Registers.V0;
                        } else {
                            // Immediate 应该是不会有的
                            srcReg = getOrAllocReg4Var(unaryCode.getSrc().getVarName(), true, null, null);
                        }
                        dstReg = getOrAllocReg4Var(unaryCode.getVarName(), false, srcReg, null);
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
                    case GLOBAL_ARRAY:
                        break;
                    case VARSTORE:
                        break;
                    case FPARA:
                        break;
                    case FUNCCALL:
                        FuncCallCode funcCallCode = (FuncCallCode) middleCode;
                        // 把全局变量存回
                        storeGlobalBackBeforeFuncCall();
                        // curAR = new ActivationRcd(curAR, curSp);
                        // 先保存 fp
                        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
                        mipsCodes.add(new Store(Registers.FP, Registers.SP, new Immediate(0)));
                        curAR.regsMapToMem(4);

                        // push参数
                        ArrayList<Operand> rParaCodes = funcCallCode.getrParaCodes();
                        /*for (int i = 0; i < rParaCodes.size(); i++) {
                            // 这一步是把所有没有分配到寄存器的变量，都分配到位，主要是针对于caller-saved
                            if (!(((RParaCode) rParaCodes.get(i)).getOperand() instanceof Immediate ||
                                    ((RParaCode) rParaCodes.get(i)).getOperand() instanceof RetOpd)) {
                                getOrAllocReg4Var(rParaCodes.get(i).getVarName(), true);
                            }
                        }*/

                        // argReg 都不分配了、
                        // clearArgRegs(); // todo this one!
                        /*getReg(Registers.A0)
                        getReg(Registers.A1);
                        getReg(Registers.A2);
                        getReg(Registers.A3);*/



                        for (int i = 0; i < rParaCodes.size(); i++) {
                            RParaCode rParaCode = (RParaCode) rParaCodes.get(i);
                            // 用于在需要push参数时的辅助寄存器, 参数push完之后释放
                            Reg toolReg = Registers.V0;
                            // toolReg.setAlloced(true);
                            dstReg = i == 0 ? Registers.A0 : i == 1 ? Registers.A1 : i == 2 ? Registers.A2 : i == 3 ? Registers.A3 : toolReg;
                            getReg(dstReg);
                            dstReg.setAlloced(true);
                            operand = rParaCode.getOperand();
                            if (rParaCode.isAddr()) {
                                // 传地址
                                assert operand instanceof LValOpd;
                                // 获得基地址
                                Reg tmp;
                                if ((tmp = curAR.getVarMapReg(operand.getVarName())) != null) {
                                    if (i >= 4 && !((LValOpd) operand).isArray()) { // 如果是 array, 就转到有偏移的情况里面再store了
                                        mipsCodes.add(new Store(tmp, Registers.SP, new Immediate(-4 * (i - 3))));
                                    } else {
                                        mipsCodes.add(new Move(dstReg, tmp));
                                    }
                                } else {
                                    loadValueOrAddr(operand.getVarName(), dstReg);
                                    // loadAddr2Reg(operand.getVarName(), dstReg);
                                    if (i >= 4 && !((LValOpd) operand).isArray()) {
                                        mipsCodes.add(new Store(dstReg, Registers.SP, new Immediate(-4 * (i - 3))));
                                    }
                                }
                                // 如果有偏移
                                if (((LValOpd) operand).isArray()) {
                                    // addr = name + idx * colNum * 4
                                    int colNum = operand.getVarName().getColNum();
                                    Operand offset = ((LValOpd) operand).getIdx();
                                    if (offset instanceof Immediate) {
                                        offset = ((Immediate) offset).procValue("*", 4 * colNum);
                                        mipsCodes.add(new Add(dstReg, dstReg, (Immediate) offset));
                                    } else if (offset instanceof RetOpd) {
                                        // push aa@1[RET]
                                        mipsCodes.add(new Mul(Registers.V0, Registers.V0, new Immediate(colNum)));
                                        mipsCodes.add(new Sll(Registers.V0, Registers.V0, 2));
                                        mipsCodes.add(new Add(dstReg, dstReg, Registers.V0));
                                    } else if (offset instanceof LValOpd) {
                                        assert !((LValOpd) offset).isArray();
                                        Reg off = getOrAllocReg4Var(offset.getVarName(), true, dstReg, null);
                                        mipsCodes.add(new Mul(off, off, new Immediate(colNum)));
                                        mipsCodes.add(new Sll(off, off, 2));
                                        mipsCodes.add(new Add(dstReg, dstReg, off));
                                    } else {
                                        assert offset instanceof MiddleCode;
                                        Reg off = getOrAllocReg4Var(offset.getVarName(), true, dstReg, null);
                                        mipsCodes.add(new Mul(off, off, new Immediate(colNum)));
                                        mipsCodes.add(new Sll(off, off, 2));
                                        mipsCodes.add(new Add(dstReg, dstReg, off));
                                    }
                                    if (i >= 4) {
                                        mipsCodes.add(new Store(dstReg, Registers.SP, new Immediate(-4 * (i - 3))));
                                    }
                                }
                            } else {
                                // 传数值
                                if (operand instanceof Immediate) {
                                    mipsCodes.add(new Li(dstReg, (Immediate) ((RParaCode) rParaCodes.get(i)).getOperand()));
                                    if (i >= 4) {
                                        mipsCodes.add(new Store(dstReg, Registers.SP, new Immediate(-4 * (i - 3))));
                                    }
                                } else if (operand instanceof RetOpd) {
                                    if (i >= 4) {
                                        mipsCodes.add(new Store(Registers.V0, Registers.SP, new Immediate(-4 * (i - 3))));
                                    } else {
                                        mipsCodes.add(new Move(dstReg, Registers.V0));
                                    }
                                } else if (operand instanceof LValOpd && ((LValOpd) operand).isArray()) {
                                    assert operand.getVarName().isArray();
                                    loadOrStoreArray(dstReg, (LValOpd) operand, true);
                                    if (i >= 4) {
                                        mipsCodes.add(new Store(dstReg, Registers.SP, new Immediate(-4 * (i - 3))));
                                    }
                                } else {
                                    // srcReg = getOrTmpDesignateReg(rParaCodes.get(i).getVarName(), dstReg, null);
                                    srcReg = getOrAllocReg4Var(rParaCodes.get(i).getVarName(),true, dstReg, null);
                                    if (i >= 4) {
                                        mipsCodes.add(new Store(srcReg, Registers.SP, new Immediate(-4 * (i - 3))));
                                    } else {
                                        mipsCodes.add(new Move(dstReg, srcReg));    // 需要get一下？
                                    }
                                }
                            }
                            dstReg.setAlloced(false);
                            // toolReg.setAlloced(false);
                        }
                        // caller saved                 // push 的时候也会用到寄存器，最好保护
                        ArrayList<Reg> tRegsUsed = registers.getUsedTmpRegs();
                        regsStore(tRegsUsed);
                        mipsCodes.add(new Jal(funcCallCode.getVarName().toString()));

                        // 函数返回之后
                        mipsCodes.add(new Move(Registers.SP, Registers.FP));    // reset 栈顶
                        regsLoad(tRegsUsed);
                        mipsCodes.add(new Load(Registers.FP, Registers.SP, new Immediate(0)));
                        curAR.regsUnMapToMem(4);    // fp 空间的释放
                        mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(4)));

                        break;
                    case FUNCDEF:
                        break;
                    case CONSTSTR:
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
            // 跨越基本块, 将局部变量存回内存
            // if ((reg = curAR.getVarMapReg(middleCode.getVarName())) != null) {
            //     storeBack(reg, middleCode.getVarName());
            //     curAR.regUnmapVar(reg);
            // }
            // todo
            storeVarsInCurBlock();

            curBlock = curBlock.getDirectBb();
            while (curBlock != null && curBlock.getBbType() == BasicBlock.BBType.FOLLOWGOTO) {
                curBlock = curBlock.getDirectBb();
            }
            if (curBlock != null) {
                middleCodeIter = curBlock.getMiddleCodes().iterator();
            }
        } while (curBlock != null);

        // main函数中间也允许return的
        mipsCodes.add(new Label2Jump(label_funcEnd));


        // 函数退出
        if (!isMainFunc) {
            freeAllTmpReg();  // 切换到另外的函数块中去了，只需要把isAlloced设置为 false

            // 先弹ra 如果有
            if (funcDefBlock.isRaNeedSave()) {
                mipsCodes.add(new Load(getReg(Registers.RA), curAR.getAddr(new VarName("31RA", 0))));
                // curAR.regsUnMapToMem(4); // ra 空间的释放
                // mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(4)));
            }
            // callee saved
            regsLoad(generalRegsUsed);
            // 释放局部变量的栈空间
            if (curAR.getVarOccupiedSpace() != 0) {
                mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(curAR.getVarOccupiedSpace())));
            }
            mipsCodes.add(new Jr(Registers.RA));
        } else {
            mipsCodes.add(new Li(Registers.V0, new Immediate(10)));
            mipsCodes.add(new Syscall());
        }

    }

    public void storeVarsInCurBlock() {
        mipsCodes.add(new Annotation("switch basic block. store back vars that are used"));
        for (VarName symVar : curBlock.getVars()) {
            Reg reg = curAR.getVarMapReg(symVar);
            if (reg != null) {
                /*if (!symVar.isArray() || symVar.isPtr()) {
                    // reg里面存的是fp-offset或者数组形参的值(数组形参不可变), symVar 对应的地址是 fp-offset, 里面是具体数值symVar[0]
                    storeBack(reg, symVar);
                }*/
                storeBack(reg, symVar);
                curAR.regUnmapVar(reg);
            }
        }
        mipsCodes.add(new Annotation("vars store back done!"));
    }

    private void loadOrStoreArray(Reg reg, LValOpd lValOpd, boolean isLoad) {
        if (lValOpd.isGlobalVar()) {
            // 全局数组
            String label = lValOpd.getVarName().toString();
            if (lValOpd.getIdx() instanceof Immediate) {
                Immediate offset = ((Immediate) lValOpd.getIdx()).procValue("*", 4);
                if (isLoad) {
                    mipsCodes.add(new Load(reg, label, offset));
                } else {
                    mipsCodes.add(new Store(reg, label, offset));
                }
            } else {
                Reg offset;
                if (lValOpd.getIdx() instanceof RetOpd) {
                    offset = Registers.V0;
                } else {
                    offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true, null, null);
                }
                mipsCodes.add(new Sll(Registers.AT, offset, 2));
                if (isLoad) {
                    mipsCodes.add(new Load(reg, label, Registers.AT));
                } else {
                    mipsCodes.add(new Store(reg, label, Registers.AT));
                }
            }
        } else {
            Reg base = getOrAllocReg4Var(lValOpd.getVarName(), true, reg, null);
            if (lValOpd.getIdx() instanceof Immediate) {
                Immediate offset = ((Immediate) lValOpd.getIdx()).procValue("*", 4);
                if (isLoad) {
                    mipsCodes.add(new Load(reg, base, offset));
                } else {
                    mipsCodes.add(new Store(reg, base, offset));
                }
            } else {
                Reg offset;
                if (lValOpd.getIdx() instanceof RetOpd) {
                    offset = Registers.V0;
                } else {
                    offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true, null, null);
                }
                mipsCodes.add(new Sll(Registers.AT, offset, 2));
                mipsCodes.add(new Add(Registers.AT, base, Registers.AT));
                if (isLoad) {
                    mipsCodes.add(new Load(reg, Registers.AT, null));
                } else {
                    mipsCodes.add(new Store(reg, Registers.AT, null));   // todo 考虑换一个store
                }
            }
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
            // reg.setAlloced(false);   2022 c testfile21
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

    public void clearArgRegs() {
        ArrayList<Reg> args = new ArrayList<>(Arrays.asList(Registers.A0, Registers.A1, Registers.A2, Registers.A3));
        for (Reg reg : args) {
            if (reg.isAlloced()) {
                Reg newReg = registers.allocTReg();
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
        }
    }

    // todo 没有重新绑定, 以及标记为绑定状态
    // 用于 A0,V0 即选即用的
    private Reg getReg(Reg reg) {
        if (reg.isAlloced()) {
            Reg newReg = registers.allocTReg();
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

    // temporary alloc to
    private Reg getOrTmpDesignateReg(VarName name, Reg alloc1, Reg alloc2) {
        Reg reg = curAR.getVarMapReg(name);
        if (reg == null) {
            reg = allocReg2Var(name, true, false, alloc1, alloc2);
        }
        return reg;
    }

    /**
     * @param name
     * @param needLoad
     * @return Reg, 保存 变量值 or 数组地址
     */
    // todo modify this, alloc1-2 是指和当前需要分配寄存器的变量需要同时使用的
    private Reg getOrAllocReg4Var(VarName name, boolean needLoad, Reg alloc1, Reg alloc2) {
        Reg reg = curAR.getVarMapReg(name);
        if (reg == null) {
            reg = allocReg2Var(name, needLoad, true, alloc1, alloc2);
        }
        return reg;
    }

    private Reg allocReg2Var(VarName name, boolean needLoad, boolean needMap, Reg alloc1, Reg alloc2) {
        Reg reg = getOneTReg(alloc1, alloc2);
        if (needLoad) {
            loadValueOrAddr(name, reg);
        }
        if (needMap) {
            curAR.varMap2Reg(name, reg);
        }
        return reg;
    }

    private Reg getOneTReg(Reg alloc1, Reg alloc2) {
        Reg reg = registers.allocTReg();
        if (reg == null) {
            reg = registers.getOneTReg2Free();
            while (reg == alloc1 || reg == alloc2) {
                reg = registers.getOneTReg2Free();
            }
            VarName storeVar = curAR.regUnmapVar(reg);
            // 把 storeVar 给 store 回去
            storeBack(reg, storeVar);
        }
        return reg;
    }

    private void loadValueOrAddr(VarName name, Reg reg) {
        if (!name.isArray() || name.isPtr()) {
            loadValue2Reg(name, reg);
        } else {
            loadAddr2Reg(name, reg);
        }
    }

    /**
     * 全局/局部 变量值
     *
     * @param varName
     * @param dstReg
     */
    private void loadValue2Reg(VarName varName, Reg dstReg) {
        if (varName.isGlobalVar()) {
            mipsCodes.add(new Load(dstReg, varName.toString(), new Immediate(0)));
        } else {
            mipsCodes.add(new Load(dstReg, curAR.getAddr(varName)));
        }
    }

    /**
     * 全局/局部 数组基地址
     *
     * @param varName
     * @param dstReg
     */
    private void loadAddr2Reg(VarName varName, Reg dstReg) {
        if (varName.isGlobalVar()) {
            mipsCodes.add(new La(dstReg, varName.toString()));
        } else {
            assert curAR.getAddr(varName).isRelative();
            mipsCodes.add(new Sub(dstReg, Registers.FP, new Immediate(curAR.getAddr(varName).getAddr())));
        }
    }

    private void storeGlobalBackBeforeFuncCall() {
        for (Reg reg : registers.getTempRegs()) {
            if (reg.isAlloced() && reg.getVarName()!= null && reg.getVarName().isGlobalVar()) {
                storeBack(reg, reg.getVarName());
                curAR.regUnmapVar(reg);
            }
        }
    }

    private void storeBack(Reg reg, VarName storeVar) {
        // 把 storeVar 给 store 回去
        if (storeVar.isGlobalVar()) {
            // 对于全局变量也是不够了就放回去
            // todo 改为如果更改了，就放回去?
            mipsCodes.add(new Store(reg, storeVar.toString(), new Immediate(0)));
        } else if (!storeVar.isArray() ||   // 如果是数组的话，里面存的是 fp-offset，不能存回，也不用存回
                storeVar.isPtr() && !curAR.isAddressed(storeVar)) { // 如果是指针，也就是函数的参数，可能在内存中没有分配地址，这个时候需要存回一次
            if (!curAR.isAddressed(storeVar)) {
                curAR.varSetToMem(storeVar);
                mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
            }
            Address addr = curAR.getAddr(storeVar);
            mipsCodes.add(new Store(reg, addr));
        }
    }

    public void freeAllTmpReg() {
        // 如果有映射到全局变量的寄存器，要存回
        for (Reg reg : registers.getTempRegs()) {
            if (reg.isAlloced()) {
                VarName storeVar = curAR.regUnmapVar(reg);
                // 把 storeVar 给 store 回去
                if (storeVar.isGlobalVar()) {
                    mipsCodes.add(new Store(reg, storeVar.toString(), new Immediate(0)));
                    // storeVar.setDirty(true);     俩都不是同一个对象
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
