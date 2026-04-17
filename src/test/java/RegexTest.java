import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    @Test
    public void test() {
        Pattern pattern = Pattern.compile("10kV#(\\d[甲乙])母线5\\1PT间隔直流电源故障");
        String s = "10kV#2甲母线52甲PT间隔直流电源故障";
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
        System.out.println(pattern.matcher(s).matches());
    }

}
