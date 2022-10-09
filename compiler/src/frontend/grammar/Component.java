package frontend.grammar;

import java.io.BufferedWriter;
import java.io.IOException;

public interface Component {
    void print(BufferedWriter output) throws IOException;
}
