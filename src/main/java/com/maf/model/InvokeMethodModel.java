package com.maf.model;

import java.util.ArrayList;
import java.util.List;

/**
  *
  * @param
  * @return
  * @throws
  * @date 2018/4/25 下午6:40
  * @author sunweisong
  */
public class InvokeMethodModel {

    private int mId;
    private String object;
    private String invokeMethod;
    private String testCaseName;
    private String methodName;

    public int getInvokeTimes() {
        return invokeTimes;
    }

    public void setInvokeTimes(int invokeTimes) {
        this.invokeTimes = invokeTimes;
    }

    private int invokeTimes;

    private List<ParameterModel> parameterList;

    private List<StatementModel> relatedInstantiateStatementList;
    private List<StatementModel> relatedParameterizedStatementList;
    private List<StatementModel> relatedInvokedStatementList;


    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    /**
     * cut : Class Under Test
     */
    private String cutName;

    public InvokeMethodModel() {
    }

    public InvokeMethodModel(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getInvokeMethod() {
        return invokeMethod;
    }

    public void setInvokeMethod(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getCutName() {
        return cutName;
    }

    public void setCutName(String cutName) {
        this.cutName = cutName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<ParameterModel> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<ParameterModel> parameterList) {
        this.parameterList = parameterList;
    }

    public List<StatementModel> getRelatedInstantiateStatementList() {
        return relatedInstantiateStatementList;
    }

    public void setRelatedInstantiateStatementList(List<StatementModel> relatedInstantiateStatementList) {
        this.relatedInstantiateStatementList = relatedInstantiateStatementList;
    }

    public List<StatementModel> getRelatedParameterizedStatementList() {
        return relatedParameterizedStatementList;
    }

    public void setRelatedParameterizedStatementList(List<StatementModel> relatedParameterizedStatementList) {
        this.relatedParameterizedStatementList = relatedParameterizedStatementList;
    }

    public List<StatementModel> getRelatedInvokedStatementList() {
        return relatedInvokedStatementList;
    }

    public void setRelatedInvokedStatementList(List<StatementModel> relatedInvokedStatementList) {
        this.relatedInvokedStatementList = relatedInvokedStatementList;
    }

    public List<String> getParameterNameList() {
        List<String> parameterNameList = new ArrayList<>(this.parameterList.size());
        for (ParameterModel parameterModel : this.parameterList ) {
            parameterNameList.add(parameterModel.getName());
        }
        return parameterNameList;
    }

    @Override
    public String toString() {
        return "InvokeMethodModel{" +
                "object='" + object + '\'' +
                ", invokeMethod='" + invokeMethod + '\'' +
                ", testCaseName='" + testCaseName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", cutName='" + cutName + '\'' +
                '}';
    }
}
