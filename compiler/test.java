import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main( String args[] ){

//        // 按指定模式在字符串查找
//        String line = "This order was placed for QT\"3000! OK?";
//        String pattern = "(.*?)(\"\\d+)(.*)";
//
//        // 创建 Pattern 对象
//        Pattern r = Pattern.compile(pattern);
//
//        // 现在创建 matcher 对象
//        Matcher m = r.matcher(line);
//        if (m.find( )) {
//            System.out.println("Found value: " + m.group(0) );
//            System.out.println("Found value: " + m.group(1) );
//            System.out.println("Found value: " + m.group(2) );
//            System.out.println("Found value: " + m.group(3) );
//        } else {
//            System.out.println("NO MATCH");
//        }
        // 按指定模式在字符串查找
        String line1 = "mainint";
        String line2 = "const int ()";
        String pattern = "^const(?![0-9a-zA-Z])";

        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m1 = r.matcher(line1);
        Matcher m2 = r.matcher(line2);

        if (m1.find( )) {
            System.out.println("Found value: " + m1.group(0) );
        } else {
            System.out.println("m1 NO MATCH");
        }
        if (m2.find( )) {
            System.out.println("Found value: " + m2.group(0) );
        } else {
            System.out.println("m1 NO MATCH");
        }
    }
}
