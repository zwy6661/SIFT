package com.surf;

import java.util.List;

 

/**
 * 类InterestPointFileList.java的实现描述：每张图片的SURFInterestPoint信息，用于缓存logo信息。其中width和height在比较的时候需要用到，maxSize为
 * 该图片需要的最少匹配点数
 * @author axman 2013-5-20 上午9:55:51
 */
public class InterestPointListInfo {

    private String                  imageFile;
    private List<SURFInterestPoint> list;
    private int                     width;
    private int                     height;
    private int                     maxSize = 10;

    

    public int getMaxSize() {
        return maxSize;
    }


    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public List<SURFInterestPoint> getList() {
        return list;
    }

    public void setList(List<SURFInterestPoint> list) {
        this.list = list;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
