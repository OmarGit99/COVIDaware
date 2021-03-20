package com.wasim.covidaware;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;


public class MyClusterRenderer extends DefaultClusterRenderer<MyItem> {

    Context context;
    GoogleMap map;

    public MyClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.map = map;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<MyItem> cluster) {
        //start clustering if at least 2 items overlap
        return cluster.getSize() > 1;
    }

    @Override
    protected int getColor(int clusterSize) {
        if(clusterSize>50)
            return Color.RED;
        if(clusterSize>=30&&clusterSize<50)
            return  Color.rgb(255,140,0);
        if(clusterSize>=10&&clusterSize<30)
            return Color.YELLOW;
        else
            return Color.GREEN;

    }

    //ig.setColor(R.color.golden);


}