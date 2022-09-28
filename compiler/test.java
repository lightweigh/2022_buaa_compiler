import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main( String args[] ){
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        int i = 0;
        System.out.println(integers.get(++i));
        System.out.println(integers.get(++i));
        System.out.println(integers.get(i));
    }
}
