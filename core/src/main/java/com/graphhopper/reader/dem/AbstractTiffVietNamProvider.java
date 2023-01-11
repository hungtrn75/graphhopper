package com.graphhopper.reader.dem;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.FeatureCollection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.graphhopper.storage.DataAccess;
import mil.nga.tiff.Rasters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTiffVietNamProvider extends TileBasedElevationProvider {
    final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new GeometryAdapterFactory())
            .create();
    final Map<String, HeightTile2> cacheData = new HashMap<>();
    final double precision = 1e6;

    final int WIDTH;
    final int HEIGHT;

    // Degrees of latitude covered by this tile
    double LAT_DEGREE;
    // Degrees of longitude covered by this tile
    double LON_DEGREE;

    FeatureCollection featureCollection;
    double memoEle = 0.0;

    public AbstractTiffVietNamProvider(String baseUrl, String cachePath, int width, int height, double latDegree, double lonDegree) {
        super(cachePath);
        this.baseUrl = baseUrl;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.LAT_DEGREE = latDegree;
        this.LON_DEGREE = lonDegree;
        try {
            String json = new String(Files.readAllBytes(Paths.get(cacheDir.getAbsolutePath() + "/grid.geojson")));
            featureCollection = gson.fromJson(json, FeatureCollection.class);
        } catch (Exception ex) {
            throw new RuntimeException("Can not read grid.geojson", ex);
        }

    }

    @Override
    public double getEle(double lat, double lon) {
        if (isOutsideSupportedArea(lat, lon))
            return 0;
        VNMetadata vnMetadata = getMetadata(lat, lon);
        String name = vnMetadata.fileName;
        HeightTile2 demProvider = cacheData.get(name);

        if (demProvider == null) {
            if (!cacheDir.exists())
                cacheDir.mkdirs();

            double minLat = vnMetadata.minLat;
            double minLon = vnMetadata.minLon;
            demProvider = new HeightTile2(minLat, minLon, WIDTH, HEIGHT, LON_DEGREE * precision, LON_DEGREE, LAT_DEGREE);
            demProvider.setInterpolate(interpolate);

            cacheData.put(name, demProvider);
            DataAccess heights = getDirectory().create(name + ".gh");
            demProvider.setHeights(heights);
            boolean loadExisting = false;
            try {
                loadExisting = heights.loadExisting();
            } catch (Exception ex) {
                logger.warn("cannot load provider " + name + ", error: " + ex.getMessage());
            }

            if (!loadExisting) {
                File file = new File(cacheDir, new File(name + ".tif").getName());
                heights.create(2L * WIDTH * HEIGHT);

                Rasters rasters = generateRasterFromFile(file);
                fillDataAccessWithElevationData(rasters, heights, WIDTH);
            }
        }

        if (demProvider.isSeaLevel())
            return 0;
        try {
            memoEle = demProvider.getHeight(lat, lon);
            System.out.println("getEle: " + lat + "/" + lon + ": " + memoEle);
            return memoEle;
        } catch (Exception e) {
            System.out.println(e);
            return memoEle;
        }
    }

    /**
     * Return true if the coordinates are outside of the supported area
     */
    abstract boolean isOutsideSupportedArea(double lat, double lon);

    abstract VNMetadata getMetadata(double lat, double lon);

    abstract Rasters generateRasterFromFile(File file);

    private void fillDataAccessWithElevationData(Rasters rasters, DataAccess heights, int dataAccessWidth) {
        final int height = rasters.getHeight();
        final int width = rasters.getWidth();
        int x = 0;
        int y = 0;
        try {
            for (y = 0; y < height; y++) {
                for (x = 0; x < width; x++) {
                    short val = (short) rasters.getPixel(x, y)[0];
                    if (val < -1000 || val > 12000)
                        val = Short.MIN_VALUE;

                    heights.setShort(2 * (y * dataAccessWidth + x), val);
                }
            }
            heights.flush();
        } catch (Exception ex) {
            throw new RuntimeException("Problem at x:" + x + ", y:" + y, ex);
        }
    }

    @Override
    public void release() {
        cacheData.clear();
        if (dir != null) {
            // for memory mapped type we remove temporary files
            if (autoRemoveTemporary)
                dir.clear();
            else
                dir.close();
        }
    }
}
