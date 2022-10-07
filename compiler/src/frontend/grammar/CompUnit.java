package frontend.grammar;

import frontend.grammar.decl.Decl;
import frontend.grammar.funcDef.FuncDef;
import frontend.parser.Parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CompUnit extends Component {
    //  CompUnit → {ConstDecl | VarDecl} {FuncDef} MainFuncDef
    private ArrayList<Decl> decls = new ArrayList<>();
    private ArrayList<FuncDef> funcDefs = new ArrayList<>();
    private MainFuncDef mainFuncDef = new MainFuncDef();

    public void addDecl() {
        decls.add(Parser.declParser());
    }

    public void addFuncDef() {
        funcDefs.add(Parser.funcDefParser());
    }


    public void mainParser() {
        mainFuncDef.parser();
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



}
