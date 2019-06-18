package com.maf.util;

/**
 * @Author weisongsun
 * @Date ${date} ${time}
 */
public class StringUtil {

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/10 下午10:20
      * @author sunweisong
      */
    public static boolean isMoreThanOneLayerParenthesesInString(String string) {
        boolean flag = false;
        int count = 0;
        for (char alpha : string.toCharArray()) {
            if (alpha == '(') {
                count++;
            }
            if (alpha == ')') {
                count--;
            }
            if (count > 1) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/7 下午4:26
      * @author sunweisong
      */
    public static String removeParenthesesAtStringEnd(String string) {
        while (true) {
            if (string.length() == 0) {
                return string;
            }
            char alpha = string.charAt(string.length() - 1);
            if (alpha == ')') {
                string = string.substring(0, string.length() - 1);
            } else {
                break;
            }
        }
        return string;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/10 上午10:56
      * @author sunweisong
      */
    public static boolean isParenthesesMatchInString(String string) {
        char[] alphaArray = string.toCharArray();
        int count = 0;
        int index = 0;
        for (char alpha : alphaArray) {
            index++;
            if (alpha == '(') {
                count++;
            }
            if (alpha == ')') {
                count--;
            }
            if (count == 0 && index == alphaArray.length) {
                return true;
            }
        }
        return false;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/10 下午10:13
      * @author sunweisong
      */
    public static boolean isQuotationMatchInString(String string) {
        char[] alphaArray = string.toCharArray();
        int count = 0;
        for (char alpha : alphaArray) {
            if (alpha == '\"') {
                count++;
            }
        }
        if (count % 2 == 0) {
            return true;
        }
        return false;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/11 上午9:52
      * @author sunweisong
      */
    public static String extractPartStringIsParenthesesMatch(String string) {



        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer alphaBuffer = new StringBuffer();
        int count = 0;
        for (char alpha : string.toCharArray()) {
            if (alpha == '(') {
                count++;
            }
            if (alpha == ')') {
                count--;
            }
            alphaBuffer.append(alpha);
            if (count == 0 && alphaBuffer.length() > 0) {
                String temp = alphaBuffer.toString();
                stringBuffer.append(temp);
                alphaBuffer.setLength(0);
            }
        }
        String result = stringBuffer.toString();
        alphaBuffer = null;
        stringBuffer = null;
        return result;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/11 上午11:20
      * @author sunweisong
      */
    public static boolean isMoreThanOnePointInString(String string) {
        int count = 0;
        for (char alpha : string.toCharArray()) {
            if (alpha == '.') {
                count++;
            }
        }
        if (count > 1) {
            return true;
        }
        return false;
    }

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/11 上午11:52
      * @author sunweisong
      */
    public static String extractInvokeSentenceInString(String string) {
        StringBuffer stringBuffer = new StringBuffer();
        int count = 0;
        for (char alpha : string.toCharArray()) {
            if (alpha == '(') {
                count++;
            }
            if (alpha == ')') {
                count--;
            }
            stringBuffer.append(alpha);
            if (count == 0 && stringBuffer.length() > 0
                    && stringBuffer.indexOf("(") != -1) {
                break;
            }
        }
        String result = stringBuffer.toString();
        stringBuffer = null;
        return result;
    }

}


