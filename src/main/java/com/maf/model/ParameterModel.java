package com.maf.model;

/**
 * @Author weisongsun
 * @Date ${date} ${time}
 */
public class ParameterModel {
    private int order;
    private String type;
    private String name;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ParameterModel{" +
                "order=" + order +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
