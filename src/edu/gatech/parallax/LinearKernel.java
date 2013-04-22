package edu.gatech.parallax;

import edu.syr.pcpratts.rootbeer.runtime.Kernel;

public class LinearKernel implements Kernel{
    
    private int[][] samples; 
    private int[][][] src, dest;
    private int x, h;
    
    public LinearKernel(int[][] samples, int[][][] src, int[][][] dest, int x, int h){
      this.samples = samples;
      this.src = src;
      this.dest = dest;
      this.x = x;
      this.h = h;
    }
    
    public void gpuMethod(){
      for(int y = 0; y < h; y++){
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
    }
    
}
