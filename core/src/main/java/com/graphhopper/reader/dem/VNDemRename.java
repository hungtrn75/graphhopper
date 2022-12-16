package com.graphhopper.reader.dem;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class VNDemRename {
    public static void main(String[] argv) throws IOException {
        String folderPath =
                "/Users/hungtran/Desktop/android/graphhopper/vn30dem";
        File myFolder = new File(folderPath);

        File[] fileArray = myFolder.listFiles();
        for (int i = 0; i < Objects.requireNonNull(fileArray).length; i++) {
            if (fileArray[i].isFile()) {
                String fileName = fileArray[i].getName();
                if (fileName.toLowerCase().contains(".tif")) {
                    String newName = fileName.toLowerCase(Locale.ROOT).replace("dem_100k_", "b_");
                    File newFile = new File(folderPath + "/" + newName);
                    boolean result = fileArray[i].renameTo(newFile);
                    if (result)
                        System.out.println("Rename " + fileName + " successfully");
                }
                if(fileName.toLowerCase().contains(".gh")){
                    fileArray[i].delete();
                }
            }
        }
    }
}
