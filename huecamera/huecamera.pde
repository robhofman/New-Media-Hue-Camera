import processing.video.*;
import controlP5.*;
import java.awt.Color;
import java.util.*;

ControlP5 gui;

Capture cam;
int time;
int wait=1000;
Button b;
boolean counting= true;
color avgColor;
double[] xyVals;
ArrayList<Integer> lamps = new ArrayList<Integer>();
ArrayList<String> lampjezz = new ArrayList<String>();

ArrayList<String> selectedLamps = new ArrayList<String>();


String IP = "172.23.190.22";
String dev = "nicolslavanda";
DefaultHttpClient httpClient;


void setup() {

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

  background(#E9F2F9);
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


void draw() {
  if (cam.available() == true) {
    cam.read();
    if(counting){
	    getAvgColorAndDrawRect();
     if(millis()-time>=wait){
      //lamps.indexOf(6))
      //lamps.remove((Integer)6);

        for(int i : selectedLamps){
          stuurKleurdoor(cam, i);
          println("Lamp aangestuurd: "+i);
        }

        time=millis();
      
      
     }

    }

  }
  image(cam, 0, 0);
}

void stuurKleurdoor(PImage img, int lamp){
  println("test");
  int[] hsb = getHSBhue(img);
  int hue = hsb[0];
  int sat = hsb[1];
  int bright = hsb[2];

  setColor(lamp, true, hue, sat, bright, 2);
}


void getAvgColorAndDrawRect(){
  PImage img = cam;
    avgColor = getAverageColor(img);
    fill(avgColor);
    
    rect(320, 0, 320, 180);

    //xyVals = getRGBtoXY(avgColor);
}

void lockColor(){
	if(counting)
		counting=false;
	else
		counting=true;

}


color getAverageColor(PImage img) {
  img.loadPixels();
  int r = 0, g = 0, b = 0;
  for (int i=0; i<img.pixels.length; i++) {
    color c = img.pixels[i];
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
        color c = img.pixels[i];
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
void setColor(int lamp, boolean on, int hue, int sat, int bri, int trans){
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

public double[] getRGBtoXY(color c) {
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
    if (normalizedToOne[0] > 0.04045) {
        red = (float) Math.pow(
                (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        red = (float) (normalizedToOne[0] / 12.92);
    }

    // Make green more vivid
    if (normalizedToOne[1] > 0.04045) {
        green = (float) Math.pow((normalizedToOne[1] + 0.055)
                / (1.0 + 0.055), 2.4);
    } else {
        green = (float) (normalizedToOne[1] / 12.92);
    }

    // Make blue more vivid
    if (normalizedToOne[2] > 0.04045) {
        blue = (float) Math.pow((normalizedToOne[2] + 0.055)
                / (1.0 + 0.055), 2.4);
    } else {
        blue = (float) (normalizedToOne[2] / 12.92);
    }

    float X = (float) (red * 0.649926 + green * 0.103455 + blue * 0.197109);
    float Y = (float) (red * 0.234327 + green * 0.743075 + blue * 0.022598);
    float Z = (float) (red * 0.0000000 + green * 0.053077 + blue * 1.035763);

    float x = X / (X + Y + Z);
    float y = Y / (X + Y + Z);

    double[] xy = new double[2];
    xy[0] = x;
    xy[1] = y;
    return xy;
}



void stop(){
  httpClient.getConnectionManager().shutdown();
  super.stop();
}


void getLampen()
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



