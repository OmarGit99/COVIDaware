package com.wasim.covidaware;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {

    @NonNull
    @Override
    public LatLng getPosition() {
        return Position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return Title;
    }

    public MyItem(LatLng position, String title, String snippet) {
        Position = position;
        Title = title;
        this.snippet = snippet;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }

    private LatLng Position;
    private String Title;
    private String snippet;

    public MyItem(String title) {
        Title = title;
    }

    public MyItem(LatLng position) {
        Position = position;
    }
}
