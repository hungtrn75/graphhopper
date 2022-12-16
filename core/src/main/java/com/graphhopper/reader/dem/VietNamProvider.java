package com.graphhopper.reader.dem;

import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.positions.Positions;
import com.github.filosganga.geogson.model.positions.SinglePosition;
import mil.nga.tiff.FileDirectory;
import mil.nga.tiff.Rasters;
import mil.nga.tiff.TIFFImage;
import mil.nga.tiff.TiffReader;

import java.io.File;
import java.util.List;

/**
 * Driver: GTiff/GeoTIFF
 * Files: /Users/hungtran/Desktop/android/graphhopper-v5/vn30dem/dem.tif
 * Size is 30551, 55505
 * Coordinate System is:
 * GEOGCRS["WGS 84",
 * ENSEMBLE["World Geodetic System 1984 ensemble",
 * MEMBER["World Geodetic System 1984 (Transit)"],
 * MEMBER["World Geodetic System 1984 (G730)"],
 * MEMBER["World Geodetic System 1984 (G873)"],
 * MEMBER["World Geodetic System 1984 (G1150)"],
 * MEMBER["World Geodetic System 1984 (G1674)"],
 * MEMBER["World Geodetic System 1984 (G1762)"],
 * MEMBER["World Geodetic System 1984 (G2139)"],
 * ELLIPSOID["WGS 84",6378137,298.257223563,
 * LENGTHUNIT["metre",1]],
 * ENSEMBLEACCURACY[2.0]],
 * PRIMEM["Greenwich",0,
 * ANGLEUNIT["degree",0.0174532925199433]],
 * CS[ellipsoidal,2],
 * AXIS["geodetic latitude (Lat)",north,
 * ORDER[1],
 * ANGLEUNIT["degree",0.0174532925199433]],
 * AXIS["geodetic longitude (Lon)",east,
 * ORDER[2],
 * ANGLEUNIT["degree",0.0174532925199433]],
 * USAGE[
 * SCOPE["Horizontal component of 3D system."],
 * AREA["World."],
 * BBOX[-90,-180,90,180]],
 * ID["EPSG",4326]]
 * Data axis to CRS axis mapping: 2,1
 * Origin = (101.387638888887963,23.464583333333437)
 * Pixel Size = (0.000277777777778,-0.000277777777778)
 * Metadata:
 * AREA_OR_POINT=Area
 * OVR_RESAMPLING_ALG=NEAREST
 * properties={'mode': 'gray', 'bands': [0, 0, 0], 'contrastEnhancement': 'stretch_to_minmax', 'listValue': [], 'formulas': {'bandRed': None, 'bandGreen': None, 'bandBlue': None, 'bandGray': None}, 'interpolation': None, 'stretchValue': 'W3sibWF4IjoxNDUxLCJtaW4iOi0zMDd9XQ=='}
 * Image Structure Metadata:
 * COMPRESSION=DEFLATE
 * INTERLEAVE=BAND
 * Corner Coordinates:
 * Upper Left  ( 101.3876389,  23.4645833) (101d23'15.50"E, 23d27'52.50"N)
 * Lower Left  ( 101.3876389,   8.0465278) (101d23'15.50"E,  8d 2'47.50"N)
 * Upper Right ( 109.8740278,  23.4645833) (109d52'26.50"E, 23d27'52.50"N)
 * Lower Right ( 109.8740278,   8.0465278) (109d52'26.50"E,  8d 2'47.50"N)
 * Center      ( 105.6308333,  15.7555556) (105d37'51.00"E, 15d45'20.00"N)
 * Band 1 Block=512x512 Type=Int16, ColorInterp=Gray
 * Min=-2035.000 Max=3121.000
 * Minimum=-2035.000, Maximum=3121.000, Mean=229.862, StdDev=440.215
 * NoData Value=-32768
 * Overviews: 15276x27753, 7638x13877, 3819x6939, 1910x3470, 955x1735, 478x868
 * Metadata:
 * STATISTICS_MAXIMUM=3121
 * STATISTICS_MEAN=229.86234835322
 * STATISTICS_MINIMUM=-2035
 * STATISTICS_STDDEV=440.21473763334
 */
public class VietNamProvider extends AbstractTiffVietNamProvider {
    public VietNamProvider(String cacheDir) {
        super("", cacheDir, 1799, 1799, 21.4990278 - 20.9993056, 103.5018056 - 103.0020833);
    }

    public static void main(String[] args) {
        VietNamProvider provider = new VietNamProvider("./vn30dem");
        System.out.println(provider.getEle(22.757617158687466, 103.26987813827498));
        System.out.println(provider.getEle(22.83611690965141, 103.9433484486363));
        System.out.println(provider.getEle(22.57904094027107, 104.08268713353914));
        System.out.println(provider.getEle(22.966849181758633, 104.53166734044544));
        System.out.println(provider.getEle(21.999208708600634, 104.50368292863118));
    }


    @Override
    boolean isOutsideSupportedArea(double lat, double lon) {

        return false;
    }

    @Override
    VNMetadata getMetadata(double lat, double lon) {
        for (int i = 0; i < featureCollection.features().size(); i++) {
            Feature feature = featureCollection.features().get(i);
            List<? extends Positions> coordinates = feature.geometry().positions().children().get(0).children().get(0).children();
            Pair<SinglePosition, SinglePosition> minLatLng = getMinLatLng(coordinates);
            SinglePosition sw = minLatLng.first;
            SinglePosition nw = minLatLng.second;
            String name = feature.properties().get("Ten_Manh").getAsString();
            if (lat >= sw.lat() && lat <= nw.lat() && lon >= sw.lon() && lon <= nw.lon()) {
                return new VNMetadata(name.toLowerCase(), sw.lat(), sw.lon());
            }
        }

        throw new RuntimeException("Can't find tiff: " + lat + "," + lon);
    }

    private Pair<SinglePosition, SinglePosition> getMinLatLng(List<? extends Positions> coordinates) {
        SinglePosition nw = ((SinglePosition) coordinates.get(0));
        double minLat = nw.lat();
        double maxLat = nw.lat();
        double minLon = nw.lon();
        double maxLon = nw.lon();
        for (int i = 1; i < coordinates.size(); i++) {
            SinglePosition element = ((SinglePosition) coordinates.get(i));
            if (minLat > element.lat()) {
                minLat = element.lat();
            }
            if (maxLat < element.lat()) {
                maxLat = element.lat();
            }
            if (minLon > element.lon()) {
                minLon = element.lon();
            }
            if (maxLon < element.lon()) {
                maxLon = element.lon();
            }
        }
        SinglePosition first = new SinglePosition(minLon, minLat, 0);
        SinglePosition second = new SinglePosition(maxLon, maxLat, 0);
        return new Pair(first, second);
    }

    @Override
    Rasters generateRasterFromFile(File file) {
        try {
            TIFFImage tiffImage = TiffReader.readTiff(file);
            List<FileDirectory> directories = tiffImage.getFileDirectories();
            FileDirectory directory = directories.get(0);
            return directory.readRasters();
        } catch (Exception e) {
            throw new RuntimeException("Can't decode " + file.getName(), e);
        }
    }
}
