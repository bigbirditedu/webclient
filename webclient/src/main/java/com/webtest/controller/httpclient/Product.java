package com.webtest.controller.httpclient;

/**
 * @Author: zhoupengcheng
 * @Date: 2021/12/6 17:26
 */
public class Product {
    public String name;
    public String id;
    public double price;

    public Product() {

    }

    public Product(String name, String id, double price) {
        this.name = name;
        this.id = id;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", price=" + price +
                '}';
    }
}

