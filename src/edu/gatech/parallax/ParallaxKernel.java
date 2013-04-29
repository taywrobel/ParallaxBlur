package edu.gatech.parallax;

import edu.syr.pcpratts.rootbeer.runtime.Kernel;

/**
 * Kernel to compute a linear motion blur on a column of pixels in an image
 * 
 * @author Taylor Wrobel
 * 
 */
public class ParallaxKernel implements Kernel {
    
    private int[][] samples, dm;
    private int[][][] src, dest;
    private int x, h;
    private float maxDist;
    
    public ParallaxKernel(int[][] samples, int[][][] src, int[][] dm,
            int[][][] dest, int x, int h, float maxDist) {
        this.samples = samples;
        this.src = src;
        this.dm = dm;
        this.dest = dest;
        this.x = x;
        this.h = h;
        this.maxDist = maxDist;
    }
    
    public void gpuMethod() {
        for (int y = 0; y < h; y++) {
            int width = src[0].length;
            int height = src[0][0].length;
            int srcDepth = dm[x][y];
            
            for (int c = 0; c < src.length; c++) {
                int count = 0;
                int sum = 0;
                for (int i = 0; i < samples.length; i++) {
                    int sx = x + samples[i][0];
                    int sy = y + samples[i][1];
                    float dist = (float) Math.sqrt(Math.abs(samples[i][0])
                            + Math.abs(samples[i][1]));
                    if (sx >= 0 && sx < width) {
                        if (sy >= 0 && sy < height) {
                            if ((dist <= (maxDist * (float) dm[sx][sy] / 256.0)) || 
                                    (dm[sx][sy] < srcDepth && (dist < maxDist * (float) dm[x][y] / 256.0))) {
                                count++;
                                sum += src[c][sx][sy];
                            } else {
                                count++;
                                sum += src[c][x][y];
                            }
                        }
                    }
                }
                int avg = src[c][x][y];
                if (count > 0)
                    avg = sum / count;
                dest[c][x][y] = avg;
            }
        }
    }
    
}
