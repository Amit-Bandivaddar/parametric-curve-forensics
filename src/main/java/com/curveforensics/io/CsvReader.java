package com.curveforensics.io;

import com.curveforensics.model.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Minimal CSV reader for x,y datasets. */
public final class CsvReader {
    private CsvReader() {}

    public static List<Point> readXY(Path path) throws IOException {
        List<Point> points = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    throw new IOException("Invalid CSV row: " + line);
                }

                // Skip a conventional x,y header.
                if (first && !isDouble(parts[0].trim())) {
                    first = false;
                    continue;
                }
                first = false;

                points.add(new Point(
                        Double.parseDouble(parts[0].trim()),
                        Double.parseDouble(parts[1].trim())
                ));
            }
        }

        if (points.size() < 2) {
            throw new IOException("CSV must contain at least two data points.");
        }
        return points;
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
