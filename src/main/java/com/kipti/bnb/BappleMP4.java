package com.kipti.bnb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;

public class BappleMP4 {

    // this was for generating the frames.

    public static void main(String[] args) {

        // if for whatever reason you want to do this yourself...

        // replace this path with your ffmpeg install
        String ffmpegPath = "C:\\Users\\USER\\Downloads\\ffmpeg.exe";
        // replace this path with your video
        String inputPath = "C:\\Users\\USER\\Downloads\\badapple.mp4";
        // replace this path with your video
        String outputDir = "C:\\Users\\USER\\Downloads\\badapple_frames";

        new File(outputDir).mkdirs();
        String outputPattern = outputDir + "\\frame_%04d.png";

        String filterChain = "scale=48:36:flags=neighbor,format=gray,eq=contrast=1000";

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-y",
                "-i", inputPath,
                // remove this line for gray
                "-vf", filterChain,
                // remove this line for gray
                outputPattern
        );

        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Success! frames saved to: " + outputDir);
            } else {
                System.err.println("Failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
