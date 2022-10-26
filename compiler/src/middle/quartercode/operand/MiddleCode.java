package middle.quartercode.operand;

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

    String getName();
}
