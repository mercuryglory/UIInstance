package com.mercury.scrollflexiblelayout;

import java.io.Serializable;

public class TestBean implements Serializable {

    private String title;
    private int img;

    public TestBean(String title, int img) {
        this.title = title;
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}