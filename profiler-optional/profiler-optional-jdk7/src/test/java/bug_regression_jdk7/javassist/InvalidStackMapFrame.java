package bug_regression_jdk7.javassist;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InvalidStackMapFrame {

    public void bytecodeVerifyError() {
        // javassist bug : invalid stack map frame
        List<Integer> test = new ArrayList<Integer>();
        String[] newLine = new String[10];
        for (Integer idx : test) {
            String address = newLine[1];
            int tabPos = -1;
            if (tabPos != -1) {
                address = address.substring(tabPos + 1);
            }
            newLine[4] = address;
        }

    }
}
