package com.maf.model;

/**
  * @Author sunweisong
  * @Date 2018/5/6 下午6:48
  */
public class StatementModel {

    private String content;
    private int order;

    /**
     * tag == 0 : have been used;
     * tag == 1 : not be used;
     */
    private int tag;

    public StatementModel() {
    }

    public StatementModel(String content) {
        this.content = content;
    }

    public StatementModel(String content, int order, int tag) {
        this.content = content;
        this.order = order;
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    /**
      * Judge two StatementModel is same;
      * @param 
      * @return
      * @throws
      * @date 2018/5/6 下午8:45
      * @author sunweisong
      */
    public boolean isEqualStatement(StatementModel statementModel) {
        if ((this.getContent().equals(statementModel.getContent())
                && this.getOrder() == statementModel.getOrder()
                && this.getTag() == statementModel.getTag())) {
            return true;
        }
        return false;
    }


    @Override
    public String toString() {
        return "StatementModel{" +
                "content='" + content + '\'' +
                ", order=" + order +
                ", tag=" + tag +
                '}';
    }
}
