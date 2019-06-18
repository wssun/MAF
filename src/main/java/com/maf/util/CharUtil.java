package com.maf.util;

/**
  * @Author sunweisong
  * @Date 2018/5/8 下午4:55
  */
public class CharUtil {

    /**
      * 
      * @param 
      * @return
      * @throws
      * @date 2018/5/8 下午4:58
      * @author sunweisong
      */
    public static boolean isVariableAlpha(char alpha) {
        if (alpha >= 'a' && alpha <= 'z') {
            return true;
        }
        if (alpha >= 'A' && alpha <= 'Z') {
            return true;
        }
        if (alpha >= '0' && alpha <= '9') {
            return true;
        }
        if (alpha == '_' || alpha == '$') {
            return true;
        }
        return false;
    }
}
