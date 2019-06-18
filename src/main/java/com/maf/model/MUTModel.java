package com.maf.model;


import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

public class MUTModel {

    private int methodId;

    private String className;

    private String methodName;

    private String arguments;

    private String access;

    private String isConstructor;

    public MUTModel(String str) {

        str = str.substring(9,str.length()-1);
        StreamTokenizer streamTokenizer = new StreamTokenizer(new StringReader(str));
        try {
            streamTokenizer.nextToken();
            while(streamTokenizer.ttype!=StreamTokenizer.TT_EOF){

                if(streamTokenizer.ttype==StreamTokenizer.TT_WORD){
                    switch (streamTokenizer.sval){

                        case "methodId":
                            streamTokenizer.nextToken();
                            this.methodId = (int)streamTokenizer.nval;

                        case "className":
                            streamTokenizer.nextToken();
                            this.className = streamTokenizer.sval;

                        case "arguments":
                            streamTokenizer.nextToken();
                            this.arguments = streamTokenizer.sval;

                        case "access":
                            streamTokenizer.nextToken();
                           this.access = streamTokenizer.sval;

                        case "isConstructor":
                            streamTokenizer.nextToken();
                            this.isConstructor = streamTokenizer.sval;
                    }

                }
                streamTokenizer.nextToken();

            }

        }catch (IOException ioe){
            ioe.printStackTrace();
        }



    }

    public MUTModel() {
    }

    public MUTModel(int methodId, String className, String methodName, String arguments, String access, String isConstructor) {
        this.methodId = methodId;
        this.className = className;
        this.methodName = methodName;
        this.arguments = arguments;
        this.access = access;
        this.isConstructor = isConstructor;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getIsConstructor() {
        return isConstructor;
    }

    public void setIsConstructor(String isConstructor) {
        this.isConstructor = isConstructor;
    }

    @Override
    public String toString() {
        return "MUTModel{" +
                "methodId=" + methodId +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", arguments='" + arguments + '\'' +
                ", access='" + access + '\'' +
                ", isConstructor='" + isConstructor + '\'' +
                '}';
    }


    public boolean equals(MUTModel mutModel) {
        if (this.className.equals(mutModel.getClassName())
                && this.methodName.equals(mutModel.getMethodName())
                && this.arguments.equals(mutModel.getArguments())
                && this.access.equals(mutModel.getAccess())) {
            return true;
        }
        return false;
    }
}
