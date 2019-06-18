package com.maf.model;

/**
  * @Author sunweisong
  * @Date 2018/6/26 下午3:22
  */


public class TFModel {


    private int id;

    private String fragment;
    private int mid;
    private int cid;

    /**
     * 2018.08.08
     */
    private int length;  // fragment.length()
    private int stateNum; // Number of statements

    public TFModel() {
    }

    public TFModel(String fragment, int length, int stateNum, int mid, int cid) {
        this.fragment = fragment;
        this.length = length;
        this.stateNum = stateNum;
        this.mid = mid;
        this.cid = cid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStateNum() {
        return stateNum;
    }

    public void setStateNum(int stateNum) {
        this.stateNum = stateNum;
    }

    @Override
    public String toString() {
        return "TFModel{" +
                "id=" + id +
                ", fragment='" + fragment + '\'' +
                ", mid=" + mid +
                ", cid=" + cid +
                ", length=" + length +
                ", stateNum=" + stateNum +
                '}';
    }
}
