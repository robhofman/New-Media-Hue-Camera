import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 
import controlP5.*; 
import java.awt.Color; 
import java.util.*; 

import org.apache.http.*; 
import org.apache.http.impl.io.*; 
import org.apache.http.client.params.*; 
import org.apache.http.impl.client.*; 
import org.apache.http.annotation.*; 
import org.apache.http.client.protocol.*; 
import org.apache.http.util.*; 
import org.apache.http.impl.auth.*; 
import org.apache.http.client.methods.*; 
import org.apache.http.protocol.*; 
import org.apache.http.cookie.params.*; 
import org.apache.http.entity.*; 
import org.apache.http.auth.*; 
import org.apache.http.client.entity.*; 
import org.apache.http.conn.socket.*; 
import org.apache.http.conn.params.*; 
import org.apache.http.cookie.*; 
import org.apache.http.conn.routing.*; 
import org.apache.commons.logging.*; 
import org.apache.http.impl.conn.*; 
import org.apache.http.impl.pool.*; 
import org.apache.http.config.*; 
import org.apache.http.impl.entity.*; 
import org.apache.http.conn.util.*; 
import org.apache.commons.logging.impl.*; 
import org.apache.http.concurrent.*; 
import org.apache.http.conn.*; 
import org.apache.http.client.config.*; 
import org.apache.http.pool.*; 
import org.apache.http.io.*; 
import org.apache.http.client.*; 
import org.apache.http.impl.conn.tsccm.*; 
import org.apache.http.impl.*; 
import org.apache.http.client.utils.*; 
import org.apache.http.impl.cookie.*; 
import org.apache.http.auth.params.*; 
import org.apache.http.conn.ssl.*; 
import org.apache.http.params.*; 
import org.apache.http.message.*; 
import org.apache.http.impl.execchain.*; 
import org.apache.http.conn.scheme.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class huecamera extends PApplet {






ControlP5 gui;

Capture cam;
int time;
int wait=1000;
Button b;
boolean counting= true;
int avgColor;
double[] xyVals;
ArrayList<Integer> lamps = new ArrayList<Integer>();
ArrayList<String> lampjezz = new ArrayList<String>();


String IP = "172.23.190.22";
String dev = "nicolslavanda";
DefaultHttpClient httpClient;


public void setup() {

  gui = new ControlP5(this);

  b = gui.addButton("lockColor")
	         .setPosition(550,150)
	         .setSize(90,30)
	         .setCaptionLabel("Lock/Unlock color");

  httpClient = new DefaultHttpClient();
  size(640, 600);

  noStroke();

  time=millis();

  String[] cameras = Capture.list();

  background(0xffE9F2F9);
  if (cameras.length == 0) {
    println("There are no cameras available for capture.");
    exit();
  } else {
    println("Available cameras:");
    for (int i = 0; i < cameras.length; i++) {
      //println(cameras[i]);
    }
    
    // The camera can be initialized directly using an 
    // element from the array returned by list():
    cam = new Capture(this, cameras[6]);
    cam.start();     
  }    
  getLampen();  
}


public void draw() {
  if (cam.available() == true) {
    cam.read();
    if(counting){
	    getAvgColorAndDrawRect();
     if(millis()-time>=wait){
      //lamps.indexOf(6))
      //lamps.remove((Integer)6);

        for(int i : lamps){
          stuurKleurdoor(cam, i);
          println("Lamp aangestuurd: "+i);
        }

        time=millis();
      
      
     }

    }

  }
  image(cam, 0, 0);
}

public void stuurKleurdoor(PImage img, int lamp){
  println("test");
  int[] hsb = getHSBhue(img);
  int hue = hsb[0];
  int sat = hsb[1];
  int bright = hsb[2];

  setColor(lamp, true, hue, sat, bright, 2);
}


public void getAvgColorAndDrawRect(){
  PImage img = cam;
    avgColor = getAverageColor(img);
    fill(avgColor);
    
    rect(320, 0, 320, 180);

    //xyVals = getRGBtoXY(avgColor);
}

public void lockColor(){
	if(counting)
		counting=false;
	else
		counting=true;

}


public int getAverageColor(PImage img) {
  img.loadPixels();
  int r = 0, g = 0, b = 0;
  for (int i=0; i<img.pixels.length; i++) {
    int c = img.pixels[i];
    r += c>>16&0xFF;
    g += c>>8&0xFF;
    b += c&0xFF;
  }
  r /= img.pixels.length;
  g /= img.pixels.length;
  b /= img.pixels.length;


  return color(r, g, b);
}



public int[] getHSBhue (PImage img){
  int pixel; //ARGB variable with 32 int bytes where
    //sets of 8 bytes are: Alpha, Red, Green, Blue
    float r=0;
    float g=0;
    float b=0;
     
    int skipValue = 1;
    int x = displayWidth; //possibly displayWidth
    int y =  displayHeight; //possible displayHeight instead
     
    //get screenshot into object "screenshot" of class BufferedImage
    PImage screenshot = cam;

     

    //I skip every alternate pixel making my program 4 times faster
    img.loadPixels();

      for (int i=0; i<img.pixels.length; i++) {
        int c = img.pixels[i];
        r += c>>16&0xFF;
        g += c>>8&0xFF;
        b += c&0xFF;
      }
      r /= img.pixels.length;
      g /= img.pixels.length;
      b /= img.pixels.length;
     
    //println(r+","+g+","+b);
     
    // filter values to increase saturation
    float maxColorInt;
    float minColorInt;
     
    maxColorInt = max(r,g,b);
    if(maxColorInt == r){
      // red
      if(maxColorInt < (225-20)){
        r = maxColorInt + 20;  
      }
    }
    else if (maxColorInt == g){
      //green
      if(maxColorInt < (225-20)){
        g = maxColorInt + 20;  
      }
    }
    else {
       //blue
       if(maxColorInt < (225-20)){
        b = maxColorInt + 20;  
      }  
    }
     
    //minimise smallest
    minColorInt = min(r,g,b);
    if(minColorInt == r){
      // red
      if(minColorInt > 20){
        r = minColorInt - 20;  
      }
    }
    else if (minColorInt == g){
      //green
      if(minColorInt > 20){
        g = minColorInt - 20;  
      }
    }
    else {
       //blue
       if(minColorInt > 20){
        b = minColorInt - 20;  
      }  
    }

    //Convert RGB values to HSV(Hue Saturation and Brightness) 
    float[] hsv = new float[3];
    Color.RGBtoHSB(Math.round(r),Math.round(g),Math.round(b),hsv);
    //You can multiply SAT or BRI by a digit to make it less saturated or bright
    float HUE= hsv[0] * 65535;
    float SAT= hsv[1] * 255;
    float BRI= hsv[2] * 255;

    //Convert floats to integers
    int hue = Math.round(HUE);
    int sat = Math.round(SAT);
    int bri = Math.round(BRI);

    return new int[]{hue,sat,bri};
}


//formule uit demo lichtjes aangepast easy peasy
public void setColor(int lamp, boolean on, int hue, int sat, int bri, int trans){
  try {
    String data = "{\"on\":true, \"hue\":"+hue+", \"bri\":"+bri+", \"sat\":"+sat+", \"transitiontime\":"+trans+"}";

    StringEntity se = new StringEntity(data);
    HttpPut httpPut = new HttpPut("http://"+IP+"/api/"+dev+"/lights/"+lamp+"/state");

    httpPut.setEntity(se);

    HttpResponse response = httpClient.execute(httpPut);
    HttpEntity entity = response.getEntity();
    if (entity != null) entity.consumeContent();
  }
  catch(Exception e) {
    e.printStackTrace();
  }


}

public double[] getRGBtoXY(int c) {
    // For the hue bulb the corners of the triangle are:
    // -Red: 0.675, 0.322
    // -Green: 0.4091, 0.518
    // -Blue: 0.167, 0.04
    double[] normalizedToOne = new double[3];
    float cred, cgreen, cblue;
    cred = red(c);
    cgreen = green(c);
    cblue = blue(c);
    normalizedToOne[0] = (cred / 255);
    normalizedToOne[1] = (cgreen / 255);
    normalizedToOne[2] = (cblue / 255);
    float red, green, blue;

    // Make red more vivid
    if (normalizedToOne[0] > 0.04045f) {
        red = (float) Math.pow(
                (normalizedToOne[0] + 0.055f) / (1.0f + 0.055f), 2.4f);
    } else {
        red = (float) (normalizedToOne[0] / 12.92f);
    }

    // Make green more vivid
    if (normalizedToOne[1] > 0.04045f) {
        green = (float) Math.pow((normalizedToOne[1] + 0.055f)
                / (1.0f + 0.055f), 2.4f);
    } else {
        green = (float) (normalizedToOne[1] / 12.92f);
    }

    // Make blue more vivid
    if (normalizedToOne[2] > 0.04045f) {
        blue = (float) Math.pow((normalizedToOne[2] + 0.055f)
                / (1.0f + 0.055f), 2.4f);
    } else {
        blue = (float) (normalizedToOne[2] / 12.92f);
    }

    float X = (float) (red * 0.649926f + green * 0.103455f + blue * 0.197109f);
    float Y = (float) (red * 0.234327f + green * 0.743075f + blue * 0.022598f);
    float Z = (float) (red * 0.0000000f + green * 0.053077f + blue * 1.035763f);

    float x = X / (X + Y + Z);
    float y = Y / (X + Y + Z);

    double[] xy = new double[2];
    xy[0] = x;
    xy[1] = y;
    return xy;
}



public void stop(){
  httpClient.getConnectionManager().shutdown();
  super.stop();
}


public void getLampen()
{
  JSONObject jObject = loadJSONObject("http://"+IP+"/api/"+dev+"/lights");
  Iterator x = jObject.keys().iterator();
  while( x.hasNext() ) 
  {
    Integer key = Integer.parseInt(x.next().toString());
    lamps.add(key);
    println(key); 
  }
}



  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "huecamera" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
