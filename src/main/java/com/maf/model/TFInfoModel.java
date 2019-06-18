package com.maf.model;

import java.util.List;

/**
  * @Author sunweisong
  * @Date 2019/6/14 5:35 PM
  */
public class TFInfoModel {
    private int methodId;
    private String methodName;
    private String className;
    private String fileName;
    private String testFragment;

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTestFragment() {
        return testFragment;
    }

    public void setTestFragment(String testFragment) {
        this.testFragment = testFragment;
    }

    @Override
    public String toString() {
        return "TFInfoModel{" +
                "methodId=" + methodId +
                ", methodName='" + methodName + '\'' +
                ", className='" + className + '\'' +
                ", fileName='" + fileName + '\'' +
                ", testFragment='" + testFragment + '\'' +
                '}';
    }
}
