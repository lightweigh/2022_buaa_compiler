package frontend.symbol;

import java.util.HashMap;

public class SymTable {

    private HashMap<String, Symbol> symbols = new HashMap<>();
    // private String field; // 作用域
    private SymTable parent;

    public SymTable(SymTable parent) {
        this.parent = parent;
    }

    public SymTable getParent() {
        return parent;
    }

    public Symbol getSymbol(String symName) {
        return symbols.get(symName);
    }

    public boolean hasSymbol(String name) {
        return symbols.containsKey(name);
    }

    public void addSymbol(Symbol symbol) {
        symbols.put(symbol.getSymName(),symbol);
    }
}
