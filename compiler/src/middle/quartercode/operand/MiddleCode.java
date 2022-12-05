package middle.quartercode.operand;

import middle.VarName;

public interface MiddleCode extends Operand {
    enum Op {
        // UnaryOp
        NOT("!"),

        // BinaryOp
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        MOD("%"),
        ;

        private String name;

        Op(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    enum CodeType {
        ARRAY_DEF,
        ARRAY_LOAD,
        ARRAY_STORE,
        GLOBAL_ARRAY,
        FPARA,
        FUNCCALL,
        FUNCDEF,
        RPARA,
        ASSIGN,
        BINARY,
        UNARY,
        CONSTSTR,
        CONSTVAR,
        PRINT,
        SCANF,
        RET,
        SAVECMP,
        JUMPCMP
    }

    // 代码生成part
    CodeType getCodeType();
}
