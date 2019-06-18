package com.maf.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParenthesisUtil {

    /**
     * @param string
     * @return boolean
     * @date 2018/4/11 下午7:35
     * @author sunweisong
     */
    public static boolean judgeParenthesisMatchAmongString(String string) {
        string = removeBracesFromString(string);
        int size = 0;
        int count = 0;
        char[] charArray = string.toCharArray();
        for (char c : charArray) {
            size++;
            if (c == '{') {
                count++;
            }
            if (c == '}') {
                count--;
            }
        }
        if (count == 0 && size == charArray.length) {
            return true;
        }
        return false;
    }

    /**
     * Remove braces '{' that is contained in quotation mark '"'.
     *
     * @param content
     * @return 2017年12月6日
     * @author sunweisong
     */
    public static String removeBracesFromString(String content) {

        /**
         * "^t\"7{r" is fund on 2017.12.11
         *
         * "Variable var3 = new Variable("\\");" is fund on 2017.12.12
         *
         * "String []f={"__","_","\\","-"};" is fund on 2017.12.19
         */
        StringBuffer buffer = new StringBuffer();
        String tempContent = content;
        boolean flag = false;
        while (tempContent.contains("\\\"")) {
            int index = tempContent.indexOf("\\\"");
            if (tempContent.charAt(index + 2) == ';'
                    || tempContent.charAt(index + 2) == ')'
                    || tempContent.charAt(index + 2) == ',') {
                buffer.append(tempContent.substring(0, index + 3));
                tempContent = tempContent.substring(index + 3);
            } else {
                buffer.append(tempContent.substring(0, index));
                tempContent = tempContent.substring(index + 2);
            }
            flag = true;
        }
        if (flag) {
            buffer.append(tempContent);
        } else {
            buffer.append(content);
        }
        String string = buffer.toString();

        String regex = "\"(.*?)\"";
        Matcher m = Pattern.compile(regex).matcher(string);
        while (m.find()) {
            int begin = m.start();
            int end = m.end();
            String temp = m.group();
            if (temp.contains("{")) {
                temp = temp.replace("{", "[tcsa]");
            }
            if (temp.contains("}")) {
                temp = temp.replace("}", "[tcsa]");
            }
            buffer.replace(begin, end, temp);
        }
        return buffer.toString();
    }
}
