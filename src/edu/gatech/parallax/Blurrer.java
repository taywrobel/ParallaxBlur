package edu.gatech.parallax;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import edu.syr.pcpratts.rootbeer.runtime.Kernel;
import edu.syr.pcpratts.rootbeer.runtime.Rootbeer;

public class Blurrer {
    
    private static Blurrer instance;
    private BlurType type;
    
    private int[][][] pixels;
    private int[][] depths;
    
    private boolean dirty;
    private boolean gpu;
    
    public enum BlurType {
        LINEAR_MOTION, PARALLAX_MOTION
    }
    
    static{
        instance = new Blurrer();
    }
    
    private Blurrer() {
        // NO-OP, private constructor for singleton
        pixels = null; depths = null;
        dirty = true;
        gpu = false;
    }
    
    public static void setBlurType(BlurType type){
        instance.type = type;
    }
    
    public static BufferedImage blur(BufferedImage im, BufferedImage depth, float dist, float angle){
        if(depth == null && instance.type.equals(BlurType.PARALLAX_MOTION))
            throw new IllegalArgumentException("Depth map required for parallax blur!");
        
        if(instance.type.equals(BlurType.PARALLAX_MOTION))
            return instance.parallaxBlur(im, depth, dist, angle);
        else
            return instance.linearBlur(im, dist, angle);
    }
    
    private BufferedImage parallaxBlur(BufferedImage im, BufferedImage depth, float dist, float angle){
        int[][] samplePoints = getLinePoints(dist, angle);
        
        // Populate data if not already there
        if(pixels == null || depth == null || dirty){
            pixels = pullPixelData(im);
            int[][][] allDepth = pullPixelData(depth);
            depths = new int[allDepth[0].length][allDepth[0][0].length];
            for(int x = 0; x < allDepth[0].length; x++){
                for(int y = 0; y < allDepth[0][0].length; y++){
                    depths[x][y] = (allDepth[0][x][y] + allDepth[1][x][y] + allDepth[2][x][y])/3;
                }
            }
            dirty = false;
        }
        
        int[][][] result = new int[pixels.length][pixels[0].length][pixels[0][0].length];
        
        for (int y = 0; y < pixels[0][0].length; y++) {
            for (int x = 0; x < pixels[0].length; x++) {
                pBlur(samplePoints, pixels, depths, result, x, y);
            }
        }
        
        return packPixels(result, im.getData().getSampleModel(), im.getColorModel());
    }
    
    private static void pBlur(int[][] samples, int[][][] src, int[][] dm, int[][][] dest, int x, int y){
        int width = src[0].length;
        int height = src[0][0].length;
        for(int c = 0; c < src.length; c++){
            int count = 0;
            int sum = 0;
            for(int i = 0; i < samples.length; i++){
                int sx = x + samples[i][0];
                int sy = y + samples[i][1];
                if(sx >= 0 && sx < width){
                    if(sy >= 0 && sy < height){
                        count++;
                        sum += src[c][sx][sy];
                    }
                }
            }
            int avg = sum / count;
            dest[c][x][y] = avg;
        }
    }
    
    private BufferedImage linearBlur(BufferedImage im, float dist, float angle){
        int[][] samplePoints = getLinePoints(dist, angle);
        
        // Populate data if not already there
        if(pixels == null || dirty){
            pixels = pullPixelData(im);
            dirty = false;
        }
        
        int[][][] result = new int[pixels.length][pixels[0].length][pixels[0][0].length];
        
        if (!gpu) {
            for (int y = 0; y < pixels[0][0].length; y++) {
                for (int x = 0; x < pixels[0].length; x++) {
                    lBlur(samplePoints, pixels, result, x, y);
                }
            }
        } else {
            List<Kernel> jobs = new ArrayList<Kernel>();
            for (int i = 0; i < pixels[0].length; i++) {
                jobs.add(new LinearKernel(samplePoints, pixels, result, i,
                        pixels[0][0].length));
            }
            
            Rootbeer rootbeer = new Rootbeer();
            rootbeer.runAll(jobs);
        }
        
        return packPixels(result, im.getData().getSampleModel(), im.getColorModel());
    }
    
    private static void lBlur(int[][] samples, int[][][] src, int[][][] dest, int x, int y){
        int width = src[0].length;
        int height = src[0][0].length;
        for(int c = 0; c < src.length; c++){
            int count = 0;
            int sum = 0;
            for(int i = 0; i < samples.length; i++){
                int sx = x + samples[i][0];
                int sy = y + samples[i][1];
                if(sx >= 0 && sx < width){
                    if(sy >= 0 && sy < height){
                        count++;
                        sum += src[c][sx][sy];
                    }
                }
            }
            int avg = sum / count;
            dest[c][x][y] = avg;
        }
    }
    
    private static int[][][] pullPixelData(BufferedImage source){
        Raster raster = source.getData();
        
        int[][] data = new int[3][];
        int[][][] xyarr = new int[3][source.getWidth()][source.getHeight()];
        for(int i = 0; i < data.length; i++){
            data[i] = raster.getSamples(0, 0, source.getWidth(), source.getHeight(), i, data[i]);
            int y = 0;
            int x = 0;
            for(int j = 0; j < data[i].length; j++){
                if(x >= source.getWidth()){
                    x = 0;
                    y++;
                }
                xyarr[i][x][y] = (data[i][j] < 0 ? data[i][j] + 256 : data[i][j]);
                x++;
            }
        }
        
        return xyarr;
    }
    
    private static BufferedImage packPixels(int[][][] pixels, SampleModel sm, ColorModel cm){
        int width = pixels[0].length;
        int height = pixels[0][0].length;
        WritableRaster wr = Raster.createWritableRaster(sm, null);
        
        int[][] data = new int[3][width * height];
        
        for(int i = 0; i < data.length; i++){
            int y = 0;
            int x = 0;
            for(int j = 0; j < data[i].length; j++){
                if(x >= width){
                    x = 0;
                    y++;
                }
                data[i][j] = (pixels[i][x][y] > 127 ? pixels[i][x][y] - 256 : pixels[i][x][y]);
                x++;
            }
            wr.setSamples(0, 0, width, height, i, data[i]);
        }
        
        
        return new BufferedImage(cm, wr, false, null);
    }
    
    private static int[][] getLinePoints(float dist, float angle){
        int rangeX = (int)(dist * Math.cos(angle));
        int rangeY = (int)(dist * Math.sin(angle));
        
        int x0 = 0-rangeX/2;
        int x1 = rangeX/2;
        int y0 = 0-rangeY/2;
        int y1 = rangeY/2;
        
        return BresenhamLine(x0, y0, x1, y1);
    }
    
    private static int[][] BresenhamLine(int x0, int y0, int x1, int y1){
        ArrayList<Point> points = new ArrayList<>();
        
//        dx := abs(x1-x0)
//                dy := abs(y1-y0) 
        int dx = Math.abs(x1-x0);
        int dy = Math.abs(y1-y0);
//                if x0 < x1 then sx := 1 else sx := -1
//                if y0 < y1 then sy := 1 else sy := -1
        int sx = (x0 < x1 ? 1 : -1);
        int sy = (y0 < y1 ? 1 : -1);
//                err := dx-dy
        int err = dx-dy;
//              
        while(true){
            
//                loop
//                  plot(x0,y0)
            points.add(new Point(x0,y0));
//                  if x0 = x1 and y0 = y1 exit loop
            if(x0 == x1 && y0 == y1)
                break;
//                  e2 := 2*err
            int e2 = 2 * err;
//                  if e2 > -dy then 
//                    err := err - dy
//                    x0 := x0 + sx
//                  end if
            if(e2 > -1 * dy){
                err = err - dy;
                x0 = x0 + sx;
            }
//                  if e2 <  dx then 
//                    err := err + dx
//                    y0 := y0 + sy 
//                  end if
            if(e2 < dx){
                err = err + dx;
                y0 = y0 + sy;
            }
//                end loop
        }
        
        int[][] pArr = new int[points.size()][2];
        int i = 0;
        for(Point p : points){
            pArr[i][0] = p.x;
            pArr[i][1] = p.y;
            i++;
        }
        
        return pArr;
    }
    
    public static void dirty(){
        instance.dirty = true;
    }
    
    public static void setGPU(boolean gpu){
        instance.gpu = gpu;
    }
}
