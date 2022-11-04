package backend.mipsCode.global;

import backend.mipsCode.MipsCode;

public class GlobalStr implements MipsCode {
    private String content;
    private String name;

    public GlobalStr(String content, String name) {
        this.content = content;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ": .asciiz \"" + content + "\"\n";
    }
}
