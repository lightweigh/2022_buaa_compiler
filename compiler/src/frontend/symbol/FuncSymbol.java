package frontend.symbol;

import frontend.grammar.funcDef.FuncType;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private FuncType funcType;
    private ArrayList<Symbol> fParams = new ArrayList<>();


    public FuncSymbol(FuncType funcType, String symName) {
        super(symName);
        this.funcType = funcType;
    }

    public boolean isInt() {
        return funcType.needRet();
    }

    public ArrayList<Symbol> getfParams() {
        return fParams;
    }

    public void addFParam(Symbol symbol) {
        fParams.add(symbol);
    }
}
