package net.machina.sdmmistrzejowice.common;

/**
 * Created by machi on 14.07.2016.
 */
public class MarkerData {
    private double lat;
    private double lng;
    private int category;
    private String extra;

    public MarkerData(double lat, double lng, int category, String extra) {
        this.lat = lat;
        this.lng = lng;
        this.category = category;
        this.extra = extra;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getCategory() {
        return category;
    }

    public String getExtra() {
        return extra;
    }
}
