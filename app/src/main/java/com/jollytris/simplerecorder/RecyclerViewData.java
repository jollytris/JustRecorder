package com.jollytris.simplerecorder;

/**
 * Created by zic325 on 2016. 10. 4..
 */

public class RecyclerViewData {

    private String title;
    private String path;
    private boolean isSelected;

    public RecyclerViewData(String title, String path) {
        this.title = title;
        this.path = path;
        this.isSelected = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
