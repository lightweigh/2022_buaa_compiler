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
import middle.quartercode.array.*;
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
        funcDefBlock.funcBlockProcess();

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
                    }
                    // 所有的参数都有地址空间
                    curAR.varSetToMem(middleCode.getVarName());
                } else {
                    break;
                }
            }
            /*if (i > 5) {
                // i处不是参数
                // 栈上有参数，“压栈”
                mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate((i - 5) * 4)));
            }*/
            // 对全体参数，“压栈”
            mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate((i - 1) * 4)));

            // ra
            if (funcDefBlock.isRaNeedSave()) {
                mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
                mipsCodes.add(new Store(Registers.RA, Registers.SP, new Immediate(0)));
                curAR.varSetToMem(new VarName("31RA", 0));
                // curAR.regsMapToMem(4);
            }

            // callee save
            regsStore(generalRegsUsed, null);
        }
        // LinkedHashMap<VarName, Integer> varNameSize = funcDefBlock.getAllVars();
        int size = 0;
        for (Map.Entry<VarName, Integer> nameSize : funcDefBlock.getAllVars().entrySet()) {
            // System.out.println(nameSize.getKey().toString() + nameSize.getValue());
            if (nameSize.getValue() != 0) { // 为0的是参数, 别映射
                curAR.varSetToMem(nameSize.getKey(), nameSize.getValue());
                size += nameSize.getValue();
            }
        }
        if (size != 0) {
            mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(size * 4)));
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
                        Reg op1;
                        if (saveCmp.getCmpOp1() instanceof Immediate) {
                            op1 = allocReg4Imm(null);
                            mipsCodes.add(new Li(op1, (Immediate) saveCmp.getCmpOp1()));
                        } else {
                            assert !(saveCmp.getCmpOp1() instanceof LValOpd && ((LValOpd) saveCmp.getCmpOp1()).isArray());
                            op1 = getOrAllocReg4Var(saveCmp.getCmpOp1().getVarName(), true, null);
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
                            op2 = getOrAllocReg4Var(saveCmp.getCmpOp2().getVarName(), true, new ArrayList<>(Collections.singletonList(op1)));
                        }
                        ArrayList<Reg> mutexed = new ArrayList<>();
                        mutexed.add(op1);
                        if (op2 != null) {
                            mutexed.add(op2);
                        }
                        Reg dst = getOrAllocReg4Var(saveCmp.getVarName(), false, mutexed);
                        mipsCodes.add(new Cmp(saveCmp.getCmpType(), dst, op1, op2, op2Imm));
                        if (saveCmp.getCmpOp1() instanceof Immediate) {
                            op1.setAlloced(false);
                        }
                        break;
                    case JUMPCMP:
                        JumpCmp jumpCmp = (JumpCmp) middleCode;
                        // op1 = jumpCmp.getCmpOp1() == null ? null : getOrAllocReg4Var(jumpCmp.getCmpOp1().getVarName(), true, null, null);
                        if (jumpCmp.getCmpOp1() == null) {
                            op1 = null;
                        } else {
                            if (jumpCmp.getCmpOp1() instanceof RetOpd) {
                                op1 = Registers.V0;
                            } else {
                                assert !(jumpCmp.getCmpOp1() instanceof Immediate);
                                op1 = getOrAllocReg4Var(jumpCmp.getCmpOp1().getVarName(), true, null);
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
                                mutexed = new ArrayList<>();
                                if (op1 != null) {
                                    mutexed.add(op1);
                                }
                                op2 = getOrAllocReg4Var(jumpCmp.getCmpOp2().getVarName(), true, mutexed);
                                // op2 = getOrTmpDesignateReg(jumpCmp.getCmpOp2().getVarName(), op1, null);
                            }
                        }
                        op2Imm = (jumpCmp.getCmpOp2() == null || op2 != null) ? null : (Immediate) jumpCmp.getCmpOp2();
                        storeVarsInCurBlock();
                        mipsCodes.add(new Jump(jumpCmp.getJumpType(), op1, op2, op2Imm, jumpCmp.getJumpTgtLabel()));
                        /*if (op1 != null && op1 != Registers.V0) {
                            curAR.regUnmapVar(op1);
                        }
                        if (op2 != null && op2 != Registers.V0) {
                            curAR.regUnmapVar(op2);
                        }*/
                        break;
                    case ARRAY_DEF:
                        // curAR.arrSetToMem(middleCode.getVarName(), ((ArrayDef) middleCode).getSize());
                        // mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(((ArrayDef) middleCode).getSize() * 4)));
                        break;
                    case ARRAY_LOAD:
                        // dst = array[idx]
                        ArrayLoad arrayLoad = (ArrayLoad) middleCode;
                        Reg dstReg = getOrAllocReg4Var(arrayLoad.getDst().getVarName(), false, null);
                        loadOrStoreArray(dstReg, (LValOpd) arrayLoad.getPrimaryOpd(), true);
                        break;
                    case ARRAY_STORE:
                        // array[idx] = src
                        ArrayStore arrayStore = (ArrayStore) middleCode;
                        Reg srcReg;
                        if (arrayStore.getSrc() instanceof Immediate) {
                            srcReg = allocReg4Imm(null);
                            mipsCodes.add(new Li(srcReg, (Immediate) arrayStore.getSrc()));
                        } else if (arrayStore.getSrc() instanceof RetOpd) {
                            assert false;
                            srcReg = Registers.V0;
                        } else {
                            srcReg = getOrAllocReg4Var(arrayStore.getSrc().getVarName(), true, null);
                        }
                        loadOrStoreArray(srcReg, (LValOpd) arrayStore.getPrimaryOpd(), false);
                        if (arrayStore.getSrc() instanceof Immediate) {
                            // 对应上面的 allocReg4Imm
                            srcReg.setAlloced(false);
                        }
                        break;
                    case ASSIGN:
                        AssignCode assignCode = (AssignCode) middleCode;
                        if (assignCode.getOperand() instanceof Immediate) {
                            dstReg = getOrAllocReg4Var(assignCode.getVarName(), false, null);
                            mipsCodes.add(new Li(dstReg, (Immediate) assignCode.getOperand()));
                        } else if (assignCode.getOperand() instanceof RetOpd) {
                            dstReg = getOrAllocReg4Var(assignCode.getVarName(), false, null);
                            mipsCodes.add(new Move(dstReg, Registers.V0));
                        } else {
                            srcReg = getOrAllocReg4Var(assignCode.getOperand().getVarName(), true, null);
                            dstReg = getOrAllocReg4Var(assignCode.getVarName(), false, new ArrayList<>(Collections.singletonList(srcReg)));
                            mipsCodes.add(new Move(dstReg, srcReg));
                        }
                        break;
                    case BINARY:
                        BinaryCode binaryCode = (BinaryCode) middleCode;
                        Operand src1 = binaryCode.getSrc1();
                        Operand src2 = binaryCode.getSrc2();
                        /*if (src1 instanceof Immediate) {
                            Immediate immediate = (Immediate) src1;
                            immReg = getOrAllocReg4Var(src1.getVarName(), false, null, null);
                            srcReg = immReg;    // 不能是AT啊
                            mipsCodes.add(new Li(srcReg, immediate));
                        } else if (src1 instanceof RetOpd) {
                            assert !(src2 instanceof RetOpd);
                            srcReg = Registers.V0;
                        } *//*else if (src1 instanceof LValOpd && ((LValOpd) src1).isArray()) {
                            assert false;
                        } *//* else {*/
                        assert !(src1 instanceof LValOpd && ((LValOpd) src1).isArray() || src1 instanceof Immediate);
                        srcReg = getOrAllocReg4Var(binaryCode.getSrc1().getVarName(), true, new ArrayList<>());
                        // }
                        Immediate immediate = null;
                        Reg srcReg2 = null;
                        boolean src2IsImm = false;
                        if (binaryCode.getSrc2() instanceof Immediate) {
                            immediate = (Immediate) binaryCode.getSrc2();
                            src2IsImm = true;
                        } else {
                            assert !(src2 instanceof LValOpd && ((LValOpd) src2).isArray());
                            srcReg2 = getOrAllocReg4Var(binaryCode.getSrc2().getVarName(), true, new ArrayList<>(Collections.singletonList(srcReg)));
                        }
                        mutexed = new ArrayList<>();
                        mutexed.add(srcReg);
                        if (srcReg2 != null) {
                            mutexed.add(srcReg2);
                        }
                        dstReg = getOrAllocReg4Var(binaryCode.getVarName(), false, mutexed);
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
                                    srcReg2 = allocReg4Imm(new ArrayList<>(Arrays.asList(srcReg, dstReg)));
                                    mipsCodes.add(new Li(srcReg2, immediate));
                                }
                                mipsCodes.add(new Div(srcReg, srcReg2));
                                mipsCodes.add(new Mfhi(dstReg));
                                if (src2IsImm) {
                                    srcReg2.setAlloced(false);
                                }
                                break;
                        }
                        break;
                    case CONSTVAR:
                        ConstVar constVar = (ConstVar) middleCode;
                        /*if (!constVar.isConst()) {
                            // 分配地址空间, 即使没有初始化
                            allocMem4Var(constVar.getVarName());
                        }*/
                        if (!constVar.isConst() && constVar.isInit()) {
                            if (constVar.getOperand() instanceof Immediate) {
                                dstReg = getOrAllocReg4Var(constVar.getVarName(), false, new ArrayList<>());
                                mipsCodes.add(new Li(dstReg, (Immediate) constVar.getOperand()));
                            } else {
                                srcReg = getOrAllocReg4Var(constVar.getOperand().getVarName(), true, new ArrayList<>());
                                dstReg = getOrAllocReg4Var(constVar.getVarName(), false, new ArrayList<>(Collections.singletonList(srcReg)));
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
                            Registers.A0.setAlloced(false);
                        } else {
                            if (operand instanceof Immediate) {
                                mipsCodes.add(new Li(getReg(Registers.A0), (Immediate) operand));
                                Registers.A0.setAlloced(false);
                            } else {
                                assert !(operand instanceof RetOpd || operand instanceof LValOpd && ((LValOpd) operand).isArray());
                                srcReg = getOrAllocReg4Var(putOut.getOperand().getVarName(), true, new ArrayList<>());
                                if (srcReg != Registers.A0) {
                                    // System.out.println("maybe optimization!");
                                    mipsCodes.add(new Move(getReg(Registers.A0), srcReg));
                                    Registers.A0.setAlloced(false);
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
                        mipsCodes.add(new Move(getOrAllocReg4Var(readIn.getVarName(), false, new ArrayList<>()), Registers.V0));
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
                                    assert false;
                                    loadOrStoreArray(Registers.V0, (LValOpd) retCode.getOperand(), true);
                                } else {
                                    Reg reg = curAR.getVarMapReg(middleCode.getVarName());
                                    if (reg == null) {
                                        loadValue2Reg(middleCode.getVarName(), Registers.V0);
                                    } else {
                                        mipsCodes.add(new Move(Registers.V0, reg));
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
                            assert false;
                            srcReg = Registers.V0;
                        } else {
                            // Immediate 应该是不会有的
                            assert !(unaryCode.getSrc() instanceof Immediate);
                            srcReg = getOrAllocReg4Var(unaryCode.getSrc().getVarName(), true, new ArrayList<>());
                        }
                        dstReg = getOrAllocReg4Var(unaryCode.getVarName(), false, new ArrayList<>(Collections.singletonList(srcReg)));
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
                    case ARRAY_BASE:
                        ArrayBase arrayBase = (ArrayBase) middleCode;
                        dstReg = getOrAllocReg4Var(arrayBase.getVarName(), false, new ArrayList<>());
                        LValOpd base = arrayBase.getBase(); // a, a[2]
                        // 传地址
                        Reg tmp;
                        if ((tmp = curAR.getVarMapReg(base.getVarName())) != null) {
                            mipsCodes.add(new Move(dstReg, tmp));
                        } else {
                            loadValueOrAddr(base.getVarName(), dstReg);
                        }
                        // 如果有偏移
                        if (base.isArray()) {
                            // addr = name + idx * colNum * 4
                            int colNum = base.getVarName().getColNum();
                            Operand offset = base.getIdx();
                            if (offset instanceof Immediate) {
                                offset = ((Immediate) offset).procValue("*", 4 * colNum);
                                mipsCodes.add(new Add(dstReg, dstReg, (Immediate) offset));
                            } else {
                                // assert offset instanceof MiddleCode;
                                Reg off = getOrAllocReg4Var(offset.getVarName(), true, new ArrayList<>(Collections.singletonList(dstReg)));
                                tmp = allocReg4Imm(new ArrayList<>(Arrays.asList(off, dstReg)));
                                mipsCodes.add(new Mul(tmp, off, new Immediate(colNum)));
                                mipsCodes.add(new Sll(tmp, tmp, 2));
                                mipsCodes.add(new Add(dstReg, dstReg, tmp));
                                tmp.setAlloced(false);
                            }
                        }
                        break;
                    case FUNCCALL:
                        FuncCallCode funcCallCode = (FuncCallCode) middleCode;
                        // 把全局变量存回
                        storeGlobalBackBeforeFuncCall();
                        for (Reg reg : registers.getARegs()) {
                            if (reg.isAlloced()) {
                                storeBack(reg, reg.getVarName());
                                curAR.varUnmapReg(reg.getVarName());
                            }
                        }
                        // curAR = new ActivationRcd(curAR, curSp);
                        // caller saved                 // push 的时候也会用到寄存器 -> 临时指派
                        ArrayList<Reg> tRegsUsed = registers.getUsedTmpRegs();
                        ArrayList<VarName> tRegsMappedVar = new ArrayList<>();
                        regsStore(tRegsUsed, tRegsMappedVar);   // 保持局部寄存器和局部变量的映射

                        // 保存 fp
                        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
                        mipsCodes.add(new Store(Registers.FP, Registers.SP, new Immediate(0)));
                        curAR.regsMapToMem(4);

                        // push参数
                        ArrayList<Operand> rParaCodes = funcCallCode.getrParaCodes();

                        // argReg 都不分配了、
                        // clearArgRegs(); // todo this one!


                        for (int i = 0; i < rParaCodes.size(); i++) {
                            RParaCode rParaCode = (RParaCode) rParaCodes.get(i);
                            // 用于在需要push参数时的辅助寄存器, 参数push完之后释放
                            Reg toolReg = Registers.V0;
                            // toolReg.setAlloced(true);
                            dstReg = i == 0 ? Registers.A0 : i == 1 ? Registers.A1 : i == 2 ? Registers.A2 : i == 3 ? Registers.A3 : toolReg;
                            // assert !dstReg.isAlloced();
                            // getReg(dstReg);  随便挑?
                            // dstReg.setAlloced(true);
                            operand = rParaCode.getOperand();
                            if (rParaCode.isAddr()) {
                                // 传地址
                                assert rParaCode.getOperand() instanceof ArrayBase;
                                ArrayBase baseAddr = (ArrayBase) rParaCode.getOperand(); // a[#t0] -> dst, in ArrayBase
                                // 获得基地址
                                boolean needAlloc = false;
                                tmp = curAR.getVarMapReg(baseAddr.getVarName());
                                if (tmp == null) {
                                    tmp = Registers.AT; // 抓一个非临时寄存器来
                                    loadValueOrAddr(rParaCodes.get(i).getVarName(), tmp);
                                }
                                // tmp = getOrAllocReg4Var(baseAddr.getVarName(), true, dstReg, null);
                                if (i >= 4) {
                                    mipsCodes.add(new Store(tmp, Registers.SP, new Immediate(-4 * (i + 1/* - 3*/))));
                                } else {
                                    mipsCodes.add(new Move(dstReg, tmp));
                                }
                            } else {
                                // 传数值
                                if (operand instanceof Immediate) {
                                    mipsCodes.add(new Li(dstReg, (Immediate) ((RParaCode) rParaCodes.get(i)).getOperand()));
                                    if (i >= 4) {
                                        mipsCodes.add(new Store(dstReg, Registers.SP, new Immediate(-4 * (i + 1/* - 3*/))));
                                    }
                                } else {
                                    // srcReg = getOrTmpDesignateReg(rParaCodes.get(i).getVarName(), dstReg, null);
                                    srcReg = curAR.getVarMapReg(rParaCodes.get(i).getVarName());
                                    if (srcReg == null) {
                                        srcReg = Registers.AT;
                                        loadValueOrAddr(rParaCodes.get(i).getVarName(), srcReg);
                                    }
                                    // srcReg = getOrAllocReg4Var(rParaCodes.get(i).getVarName(), true, dstReg, null);
                                    if (i >= 4) {
                                        mipsCodes.add(new Store(srcReg, Registers.SP, new Immediate(-4 * (i + 1/* - 3*/))));
                                    } else {
                                        mipsCodes.add(new Move(dstReg, srcReg));
                                    }
                                }
                            }
                            // dstReg.setAlloced(false);
                            // toolReg.setAlloced(false);
                        }
                        mipsCodes.add(new Jal(funcCallCode.getVarName().toString()));

                        // 函数返回之后
                        mipsCodes.add(new Move(Registers.SP, Registers.FP));    // reset 栈顶
                        // mipsCodes.add(new Load(Registers.FP, curAR.getAddr(new VarName("30FP", 0))));
                        mipsCodes.add(new Load(Registers.FP, Registers.SP, new Immediate(0)));
                        curAR.regsUnMapToMem(4);    // fp 空间的释放
                        mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(4)));
                        regsLoad(tRegsUsed, tRegsMappedVar);

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
                Registers.RA.setAlloced(false);
                // curAR.regsUnMapToMem(4); // ra 空间的释放
                // mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(4)));
            }
            // callee saved
            regsLoad(generalRegsUsed, null);
            // 释放局部变量的栈空间
            // if (curAR.getVarOccupiedSpace() != 0) {
            //     mipsCodes.add(new Add(Registers.SP, Registers.SP, new Immediate(curAR.getVarOccupiedSpace())));
            // }
            mipsCodes.add(new Jr(Registers.RA));
        } else {
            mipsCodes.add(new Li(Registers.V0, new Immediate(10)));
            mipsCodes.add(new Syscall());
        }

    }

    public void storeVarsInCurBlock() {
        mipsCodes.add(new Annotation("switch basic block. store back vars that are used"));
        for (Reg reg : registers.getUsedTmpRegs()) {
            if (reg.isAlloced() && reg.getVarName() != null) {
                storeBack(reg, reg.getVarName());
                curAR.regUnmapVar(reg);
            }
        }
        /*for (VarName symVar : curBlock.getVars()) {
            Reg reg = curAR.getVarMapReg(symVar);
            if (reg != null) {
                *//*if (!symVar.isArray() || symVar.isPtr()) {
                    // reg里面存的是fp-offset或者数组形参的值(数组形参不可变), symVar 对应的地址是 fp-offset, 里面是具体数值symVar[0]
                    storeBack(reg, symVar);
                }*//*
                storeBack(reg, symVar);
                curAR.regUnmapVar(reg);
            }
        }*/
        mipsCodes.add(new Annotation("vars store back done!"));
    }

    private void loadOrStoreArray(Reg reg, LValOpd lValOpd, boolean isLoad) {
        if (lValOpd.isGlobalVar()) {
            // 全局数组
            String label = lValOpd.getVarName().toString();
            if (lValOpd.getIdx() instanceof Immediate) {    // g[1]
                Immediate offset = ((Immediate) lValOpd.getIdx()).procValue("*", 4);
                if (isLoad) {
                    mipsCodes.add(new Load(reg, label, offset));
                } else {
                    mipsCodes.add(new Store(reg, label, offset));
                }
            } else {    // g[#t1] or g[a]
                Reg offset, tmp;
                if (lValOpd.getIdx() instanceof RetOpd) {
                    assert false;
                    offset = Registers.V0;
                } else {
                    offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true, new ArrayList<>(Collections.singletonList(reg)));
                }
                tmp = allocReg4Imm(new ArrayList<>(Arrays.asList(offset, reg)));
                mipsCodes.add(new Sll(tmp, offset, 2));
                if (isLoad) {
                    mipsCodes.add(new Load(reg, label, tmp));
                } else {
                    mipsCodes.add(new Store(reg, label, tmp));
                }
                tmp.setAlloced(false);
            }
        } else {
            Reg base = getOrAllocReg4Var(lValOpd.getVarName(), true, new ArrayList<>(Collections.singletonList(reg)));
            if (lValOpd.getIdx() instanceof Immediate) {
                Immediate offset = ((Immediate) lValOpd.getIdx()).procValue("*", 4);
                if (isLoad) {
                    mipsCodes.add(new Load(reg, base, offset));
                } else {
                    mipsCodes.add(new Store(reg, base, offset));
                }
            } else {
                Reg offset, tmp;
                if (lValOpd.getIdx() instanceof RetOpd) {
                    offset = Registers.V0;
                } else {
                    // a[#t0], offset -> #t0
                    offset = getOrAllocReg4Var(lValOpd.getIdx().getVarName(), true, new ArrayList<>(Arrays.asList(reg, base)));
                }
                tmp = allocReg4Imm(new ArrayList<>(Arrays.asList(offset, base, reg)));   // 还有reg
                mipsCodes.add(new Sll(tmp, offset, 2));
                mipsCodes.add(new Add(tmp, base, tmp));
                if (isLoad) {
                    mipsCodes.add(new Load(reg, tmp, null));
                } else {
                    mipsCodes.add(new Store(reg, tmp, null));   // todo 考虑换一个store
                }
                tmp.setAlloced(false);
            }
        }
    }


    private void regsStore(ArrayList<Reg> src, ArrayList<VarName> var) {
        if (src.isEmpty()) {
            return;
        }
        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(src.size() * 4)));
        int offset = 0;
        for (Reg reg : src) {
            mipsCodes.add(new Store(reg, Registers.SP, new Immediate(offset)));
            if (var != null) {
                var.add(reg.getVarName());
                // curAR.regUnmapVar(reg);
                // reg.setAlloced(false);  // 映射关系保留
            }
            offset += 4;
        }
        curAR.regsMapToMem(offset); // 分配空间
    }

    private void regsLoad(ArrayList<Reg> dst, ArrayList<VarName> var) {
        if (dst.isEmpty()) {
            return;
        }
        int offset = 0;
        for (int i = 0; i < dst.size(); i++) {
            mipsCodes.add(new Load(dst.get(i), Registers.SP, new Immediate(offset)));
            if (var != null) {
                curAR.varMap2Reg(var.get(i), dst.get(i));
            }
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

    // note: 临时用完记得“还”
    // 用于 A0,V0 即选即用的
    private Reg getReg(Reg reg) {
        if (reg.isAlloced()) {
            assert reg.getVarName() != null;
            /*// 把 storeVar 移到另外的寄存器或者内存中
            VarName storeVar = curAR.regUnmapVar(reg);
            Reg newReg = registers.allocTReg();
            if (newReg != null) {
                mipsCodes.add(new Move(newReg, reg));
                curAR.varMap2Reg(storeVar, newReg);
            } else {
                storeBack(reg, storeVar);
                curAR.varUnmapReg(storeVar);
            }*/
            storeBack(reg, reg.getVarName());
            curAR.varUnmapReg(reg.getVarName());
            // reg.setAlloced(false);
        }
        reg.setAlloced(true);
        return reg;
    }

    // temporary alloc to
    // note: alloc is true but no map which means if tmpDes then reg.getVarName() = null
    private Reg getOrTmpDesignateReg(VarName name, Reg alloc1, Reg alloc2) {
        Reg reg = curAR.getVarMapReg(name);
        if (reg == null) {
            ArrayList<Reg> mutexed = new ArrayList<>(Arrays.asList(alloc1, alloc2));
            reg = allocReg2Var(name, true, false, mutexed);
        }
        return reg;
    }

    // 找一个寄存器绑定setAlloc(true), 用完需要手动解绑setAlloc(false)
    private Reg allocReg4Imm(ArrayList<Reg> mutexRegs) {
        return allocReg2Var(null, false, false, mutexRegs);
    }

    /**
     * @param name
     * @param needLoad
     * @return Reg, 保存 变量值 or 数组地址
     */
    // todo modify this, alloc1-2 是指和当前需要分配寄存器的变量需要同时使用的
    private Reg getOrAllocReg4Var(VarName name, boolean needLoad, ArrayList<Reg> mutexRegs) {
        Reg reg = curAR.getVarMapReg(name);
        if (reg == null) {
            reg = allocReg2Var(name, needLoad, true, mutexRegs);
        }
        return reg;
    }

    private Reg allocReg2Var(VarName name, boolean needLoad, boolean needMap, ArrayList<Reg> mutexRegs) {
        Reg reg = getOneTReg(mutexRegs);
        if (needLoad) {
            loadValueOrAddr(name, reg);
        }
        if (needMap) {
            curAR.varMap2Reg(name, reg);
        }
        reg.setAlloced(true);
        return reg;
    }

    private Reg getOneTReg(ArrayList<Reg> mutexRegs) {
        Reg reg = registers.allocTReg();
        if (reg == null) {
            reg = registers.getOneTReg2Free();
            boolean available = true;
            if (mutexRegs != null) {
                do {
                    available = true;
                    for (Reg reg1 : mutexRegs) {
                        if (reg == reg1) {
                            available = false;
                            reg = registers.getOneTReg2Free();
                            break;
                        }
                    }
                } while (!available);
            }
            if (reg.getVarName() != null) {
                VarName storeVar = curAR.regUnmapVar(reg);
                // 把 storeVar 给 store 回去
                storeBack(reg, storeVar);
            }
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
            if (reg.isAlloced() && reg.getVarName() != null && reg.getVarName().isGlobalVar()) {
                storeBack(reg, reg.getVarName());
                curAR.regUnmapVar(reg);
            }
        }
    }

    // note: storeBack 的时候没有 unmap, 如需请手动
    private void storeBack(Reg reg, VarName storeVar) {
        // 把 storeVar 给 store 回去
        if (storeVar.isGlobalVar()) {
            // 对于全局变量也是不够了就放回去
            // todo 改为如果更改了，就放回去?
            mipsCodes.add(new Store(reg, storeVar.toString(), new Immediate(0)));
        } else if (!storeVar.isArray() ||   // 如果是数组的话，里面存的是 fp-offset，不能存回，也不用存回
                storeVar.isPtr()) { // 如果是指针，也就是函数的参数，
            if (!curAR.isAddressed(storeVar)) {
                assert false;
                allocMem4Var(storeVar);
            }
            Address addr = curAR.getAddr(storeVar);
            mipsCodes.add(new Store(reg, addr));
        }
    }

    private void allocMem4Var(VarName varName) {
        assert false;
        curAR.varSetToMem(varName);
        mipsCodes.add(new Sub(Registers.SP, Registers.SP, new Immediate(4)));
    }

    public void freeAllTmpReg() {
        // 如果有映射到全局变量的寄存器，要存回
        for (Reg reg : registers.getTempRegs()) {
            if (reg.isAlloced() && reg.getVarName() != null) {
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
