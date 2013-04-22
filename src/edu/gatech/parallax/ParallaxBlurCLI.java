package edu.gatech.parallax;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.gatech.parallax.Blurrer.BlurType;

public class ParallaxBlurCLI {
    
    public ParallaxBlurCLI() {
        // TODO Auto-generated constructor stub
    }
    
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("help", false, "Print this message");
        options.addOption("i", true, "Input image");
        options.addOption("m", false,
                "Use linear motion blue only (no parallax)");
        options.addOption("d", true, "Depthmap image");
        
        options.addOption("l", true, "Blur length");
        options.addOption("a", true, "Angle, in degrees");
        options.addOption("g", false, "Process images on GPU");
        
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                printHelp(options);
                System.exit(0);
            }
            
            if(line.hasOption("g")){
                Blurrer.setGPU(true);
            }
            
            if (line.hasOption("m")) {
                if (!line.hasOption("i")) {
                    System.err
                            .println("Requires input image, using the -i flag.");
                    printHelp(options);
                    System.exit(-1);
                } else {
                    // Doing linear motion blur
                    String inFile = line.getOptionValue("i");
                    if (line.hasOption("l") && line.hasOption("a")) {
                        runLinear(inFile, line.getOptionValue("a"),
                                line.getOptionValue("l"));
                    } else {
                        System.err
                                .println("Requires angle and length to be specified");
                        printHelp(options);
                    }
                }
            } else if (!line.hasOption("i")) {
                System.err.println("Requires input image, using the -i flag.");
                printHelp(options);
                System.exit(-1);
            } else if (!line.hasOption("d")) {
                System.err
                        .println("Requires depth map image, using the -d flag.");
                printHelp(options);
                System.exit(-1);
            } else {
                // Doing parallax blur
                String inFile = line.getOptionValue("i");
                String depthFile = line.getOptionValue("d");
                if (line.hasOption("l") && line.hasOption("a")) {
                    runParallax(inFile, depthFile, line.getOptionValue("a"),
                            line.getOptionValue("l"));
                } else {
                    System.err
                            .println("Requires angle and length to be specified");
                    printHelp(options);
                }
            }
        } catch (ParseException pe) {
            printHelp(options);
        }
    }
    
    private static void printHelp(Options options){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ParallaxBlur", options);
    }
    
    private static void runLinear(String filename, String angle, String distance) {
        float angleF = Float.parseFloat(angle);
        angleF = (float)(angleF * 2 * Math.PI / 360);
        float distF = Float.parseFloat(distance);
        
        try {
            
            File imFile = new File(filename);
            File outFile = new File(filename.split("\\.")[0] + "_LINEAR.png");
            
            BufferedImage im = null;
            
            im = ImageIO.read(imFile);
            
            Blurrer.setBlurType(BlurType.LINEAR_MOTION);
            BufferedImage out = null;
            for(int i = 0; i < 5; i++){
                long time = System.currentTimeMillis();
                out = Blurrer.blur(im, null, distF, angleF);
                System.out.println(System.currentTimeMillis() - time);
            }
            
            ImageIO.write(out, "png", outFile);
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
    }
    
    private static void runParallax(String filename, String depthFile,
            String angle, String distance) {
        float angleF = Float.parseFloat(angle);
        float distF = Float.parseFloat(distance);
        
        try {
            
            File imFile = new File(filename);
            File outFile = new File(filename.split("\\.")[0] + "_PARALLAX.png");
            File dmFile = new File(depthFile);
            
            BufferedImage im = null, dm = null;
            
            im = ImageIO.read(imFile);
            dm = ImageIO.read(dmFile);
            
            Blurrer.setBlurType(BlurType.PARALLAX_MOTION);
            BufferedImage out = Blurrer.blur(im, dm, distF, angleF);
            
            ImageIO.write(out, "png", outFile);
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        }
    }
    
}
