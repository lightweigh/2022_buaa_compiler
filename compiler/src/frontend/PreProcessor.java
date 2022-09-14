package frontend;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PreProcessor {
    private String file;
    private List<Line> lines = new ArrayList<>();
    private int lineNo = 1;
    private boolean ign = false;

    public PreProcessor(String file) {
        this.file = file;

    }

    public List<Line> getLines() {
        try {
            BufferedReader reader = new BufferedReader((new FileReader(file)));
            String tmp = null;
            while ((tmp = reader.readLine()) != null) {
                tmp = tmp.trim();
                System.out.println(tmp);
                dealWithLine(tmp);
                lineNo++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public void dealWithLine(String line) {
        if(ign && line.matches("\\*/")) {
            int pos = line.indexOf("*/") + "*/".length();
            ign = false;
            dealWithLine(line.substring(pos));
        } else if (!ign && !line.matches("^//")){
            if (line.matches("^/\\*")) {
                ign = true;
                dealWithLine(line.substring(2));
            } else if (line.matches("/\\*")) {
                dealWithLine(line.substring(0, line.indexOf("/*")));
                ign = true;
            } else if (line.matches("//")) {
                dealWithLine(line.substring(0,line.indexOf("//")));
            } else {
                lines.add(new Line(lineNo, line));
            }
        }
    }

    public void printLines() {
        for (int i = 0; i < lines.size(); i++) {
            System.out.println(lines.get(i));
        }
    }
}
