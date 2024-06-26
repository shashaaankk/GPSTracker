package com.example.gpstracker;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class fileWriter {
    List<String> data = new ArrayList<>();
    public void header (){
        data.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        data.add("<gpx version=\"1.1\" creator=\"MyApp\">\n");
        data.add("  <trk>\n");
        data.add("    <name>Example GPX</name>\n");
        data.add("    <trkseg>\n");
    }
    public void footer(){
        data.add("    </trkseg>\n");
        data.add("  </trk>\n");
        data.add("</gpx>\n");
    }
    public void append (float lat, float lon){
        data.add("<trkpt lat=\""+lat+"\" lon=\""+lon+"\">\n"+"</trkpt>\n");
    }
    public void write (Context context) throws IOException {
        StringBuilder str = new StringBuilder();
        data.forEach(str::append);
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("CalcData.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(str.toString());
//            outputStreamWriter.close();
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyAppData");
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            Toast.makeText(context, "Failed to create directory", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(storageDir, "data.gpx");
        try (FileWriter writer = new FileWriter(file)) {
            for (String item : data) {
                writer.write(item + "\n");
            }
            Toast.makeText(context, "Data written to file", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to write file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
