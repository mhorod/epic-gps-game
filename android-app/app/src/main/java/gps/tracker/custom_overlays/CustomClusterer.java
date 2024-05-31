package gps.tracker.custom_overlays;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;

import gps.tracker.MainActivity;

public class CustomClusterer extends RadiusMarkerClusterer {

    MainActivity mainActivity;

    public CustomClusterer(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
    }

}