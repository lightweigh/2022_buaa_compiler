package frontend.grammar;

import frontend.Error;
import frontend.grammar.decl.Decl;
import frontend.grammar.funcDef.FuncDef;
import frontend.parser.Parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CompUnit implements Component {
    //  CompUnit → {ConstDecl | VarDecl} {FuncDef} MainFuncDef
    private ArrayList<Decl> decls = new ArrayList<>();
    private ArrayList<FuncDef> funcDefs = new ArrayList<>();
    private MainFuncDef mainFuncDef = new MainFuncDef();

    public void addDecl() {
        decls.add(Parser.declParser());
    }

    public void addFuncDef() {
        FuncDef funcDef = Parser.funcDefParser();
        Error.errorDetect(funcDef, "funcDef");
        funcDefs.add(funcDef);
    }


    public void mainParser() {
        mainFuncDef.parser();
        Error.errorDetect(mainFuncDef, "main");
    }

    public void print(BufferedWriter output) throws IOException {
        for (Decl decl : decls) {
            decl.print(output);
        }

        for (FuncDef funcDef : funcDefs) {
            funcDef.print(output);
        }

        mainFuncDef.print(output);
    }

    public ArrayList<Decl> getDecls() {
        return decls;
    }

    public ArrayList<FuncDef> getFuncDefs() {
        return funcDefs;
    }

    public MainFuncDef getMainFuncDef() {
        return mainFuncDef;
    }
}
