package com.pefgloble.pefchate.JsonClasses.otherClasses;

import android.graphics.Bitmap;

import com.zomato.photofilters.imageprocessors.Filter;

public class ThumbnailFilter {
    public String filterName;
    public Bitmap image;
    public Filter filter;

    public ThumbnailFilter() {
        image = null;
        filter = new Filter();
    }

    public ThumbnailFilter(String filterName,Filter filter) {
        this.filterName = filterName;
        this.filter = filter;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
