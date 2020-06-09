package com.google.sps.data;

/** Class containing information for a marker on the map. */
public class Marker {

  private final double lat;
  private final double lng;
  private final String state;

  public Marker(double lat, double lng, String state) {
    this.lat = lat;
    this.lng = lng;
    this.state = state;
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public String getState() {
    return state;
  }
}