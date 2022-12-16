package com.graphhopper.reader.dem;

public class VNMetadata {
    final String fileName;
    final double minLat;
    final double minLon;

    public VNMetadata(String fileName, double minLat, double minLon) {
        this.fileName = fileName;
        this.minLat = minLat;
        this.minLon = minLon;
    }
}


