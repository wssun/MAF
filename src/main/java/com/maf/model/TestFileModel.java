package com.maf.model;

import java.util.List;

/**
  * @Author sunweisong
  * @Date 2018/5/9 下午8:23
  */
public class TestFileModel {
    private String fileName;
    private List<InvokeMethodModel> testMethodList;

    public TestFileModel(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<InvokeMethodModel> getTestMethodList() {
        return testMethodList;
    }

    public void setTestMethodList(List<InvokeMethodModel> testMethodList) {
        this.testMethodList = testMethodList;
    }
}
