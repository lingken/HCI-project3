package com.dhcs.bakeoff3;

import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;

/**
 * Created by Lincoln on 11/16/15.
 */
public class Test extends PApplet{

    int index = 0;

    //your input code should modify these!!
    float screenTransX = 0;
    float screenTransY = 0;
    float screenRotation = 0;
    float screenZ = 150f;

    int trialCount = 20; //this will be set higher for the bakeoff
    float border = 0; //have some padding from the sides
    int trialIndex = 0;
    int errorCount = 0;
    int startTime = 0; // time starts when the first click is captured
    int finishTime = 0; //records the time of the final click
    boolean userDone = false;

    final int screenPPI = 120; //what is the DPI of the screen you are using
//Many phones listed here: https://en.wikipedia.org/wiki/Comparison_of_high-definition_smartphone_displays

    private class Target
    {
        float x = 0;
        float y = 0;
        float rotation = 0;
        float z = 0;
    }

    ArrayList<Target> targets = new ArrayList<Target>();

    float inchesToPixels(float inch)
    {
        return inch*screenPPI;
    }

    public void setup() {
        //size does not let you use variables, so you have to manually compute this
//        size(400, 700); //set this, based on your sceen's PPI to be a 2x3.5" area.

        rectMode(CENTER);
        textFont(createFont("Arial", inchesToPixels(.15f))); //sets the font to Arial that is .3" tall
        textAlign(CENTER);

        //don't change this!
        border = inchesToPixels(.2f); //padding of 0.2 inches

        for (int i=0; i<trialCount; i++) //don't change this!
        {
            Target t = new Target();
            //t.x = random(-width/2+border, width/2-border); //set a random x with some padding
            //t.y = random(-height/2+border, height/2-border); //set a random y with some padding
            t.x = 0;
            t.y = 0;
            //t.rotation = random(0, 360); //random rotation between 0 and 360
            t.rotation = 30; // that is degree!!!
            t.z = ((i%20)+1)*inchesToPixels(.15f); //increasing size from .15 up to 3.0"
            targets.add(t);
            println("created target with " + t.x + "," + t.y + "," + t.rotation + "," + t.z);
        }

        Collections.shuffle(targets); // randomize the order of the button; don't change this.
    }

    public void draw() {

        background(60); //background is dark grey
        fill(200);
        noStroke();
        text(mouseX, 100, 100);
        text(mouseY, 100, 200);
        if (startTime == 0)
            startTime = millis();

        if (userDone)
        {
            text("User completed " + trialCount + " trials", width/2, inchesToPixels(.2f));
            text("User had " + errorCount + " error(s)", width/2, inchesToPixels(.2f)*2);
            text("User took " + (finishTime-startTime)/1000f/trialCount + " sec per target", width/2, inchesToPixels(.2f)*3);

            return;
        }

        //===========DRAW TARGET SQUARE=================
        //Red Square
        pushMatrix();
        translate(width/2, height/2); //center the drawing coordinates to the center of the screen

        Target t = targets.get(trialIndex);


        translate(t.x, t.y); //center the drawing coordinates to the center of the screen
        translate(screenTransX, screenTransY); //center the drawing coordinates to the center of the screen

        rotate(radians(t.rotation));

        fill(255, 0, 0); //set color to semi translucent
        rect(0, 0, t.z, t.z);

        popMatrix();

        //===========DRAW TARGETTING SQUARE=================
        //White Square
        pushMatrix();
        translate(width/2, height/2); //center the drawing coordinates to the center of the screen
        rotate(radians(screenRotation));

        //custom shifts:
        //translate(screenTransX,screenTransY); //center the drawing coordinates to the center of the screen

        fill(255, 128); //set color to semi translucent
        rect(0, 0, screenZ, screenZ);

        popMatrix();

        // Test rotation
        pushMatrix();
        translate(width/2, height/2);
        if (mousePressed) {
            //Target t = targets.get(trialIndex);
            // t.: x, y, rotation, z
            float s = t.z / sqrt(2);
            float alpha = PI/4 - (radians(t.rotation));
            float dX = s * cos(alpha);
            float dY = s * sin(alpha);
            float lbX = t.x - dX;
            float lbY = t.y + dY;
            //text(lbX + width/2, 200, 100);
            //text(lbY + height/2, 200, 200);

            float mX = mouseX - (width/2);
            float mY = mouseY - (height/2);
            float betta = PI/4 - atan((mY - lbY) / (mX - lbX));
            float newX = (mX + lbX) / 2;
            float newY = (mY + lbY) / 2;
            float newSize = sqrt((mX - lbX)*(mX - lbX) + (mY - lbY)*(mY - lbY)) / sqrt(2);
            translate(newX, newY);
            rotate(-betta);
            rect(0, 0, newSize, newSize);
        }
        popMatrix();

        //scaffoldControlLogic(); //you are going to want to replace this!

        text("Trial " + (trialIndex+1) + " of " +trialCount, width/2, inchesToPixels(.5f));
    }

    void scaffoldControlLogic()
    {
        //upper left corner, rotate counterclockwise
        text("CCW", inchesToPixels(.2f), inchesToPixels(.2f));
        if (mousePressed && dist(0, 0, mouseX, mouseY)<inchesToPixels(.5f))
            screenRotation--;

        //upper right corner, rotate clockwise
        text("CW", width-inchesToPixels(.2f), inchesToPixels(.2f));
        if (mousePressed && dist(width, 0, mouseX, mouseY)<inchesToPixels(.5f))
            screenRotation++;

        //lower left corner, decrease Z
        text("-", inchesToPixels(.2f), height-inchesToPixels(.2f));
        if (mousePressed && dist(0, height, mouseX, mouseY)<inchesToPixels(.5f))
            screenZ-=inchesToPixels(.02f);

        //lower right corner, increase Z
        text("+", width-inchesToPixels(.2f), height-inchesToPixels(.2f));
        if (mousePressed && dist(width, height, mouseX, mouseY)<inchesToPixels(.5f))
            screenZ+=inchesToPixels(.02f);

        //left middle, move left
        text("left", inchesToPixels(.2f), height/2);
        if (mousePressed && dist(0, height/2, mouseX, mouseY)<inchesToPixels(.5f))
            screenTransX-=inchesToPixels(.02f);
        ;

        text("right", width-inchesToPixels(.2f), height/2);
        if (mousePressed && dist(width, height/2, mouseX, mouseY)<inchesToPixels(.5f))
            screenTransX+=inchesToPixels(.02f);
        ;

        text("up", width/2, inchesToPixels(.2f));
        if (mousePressed && dist(width/2, 0, mouseX, mouseY)<inchesToPixels(.5f))
            screenTransY-=inchesToPixels(.02f);
        ;

        text("down", width/2, height-inchesToPixels(.2f));
        if (mousePressed && dist(width/2, height, mouseX, mouseY)<inchesToPixels(.5f))
            screenTransY+=inchesToPixels(.02f);
        ;
    }

    public void mousePressed() {
        //Target t = targets.get(trialIndex);
        //// t.: x, y, rotation, z
        //float s = t.z / sqrt(2);
        //float alpha = PI/4 - (t.rotation + screenRotation);
        //float dX = s * cos(alpha);
        //float dY = s * sin(alpha);
        //float lbX = t.x - dX;
        //float lbY = t.y + dY;

        //float betta = PI/4 - atan((mouseY - lbY) / (mouseX - lbX));

    }

    public void mouseReleased()
    {
        //check to see if user clicked middle of screen
        if (dist(width/2, height/2, mouseX, mouseY)<inchesToPixels(.5f))
        {
            if (userDone==false && !checkForSuccess())
                errorCount++;

            //and move on to next trial
            trialIndex++;

            screenTransX = 0;
            screenTransY = 0;

            if (trialIndex==trialCount && userDone==false)
            {
                userDone = true;
                finishTime = millis();
            }
        }
    }

    //function for testing if the overlap is sufficiently close
//Don't change this function! Check with Chris if you think you have to.
    boolean checkForSuccess()
    {
        Target t = targets.get(trialIndex);
        boolean closeDist = dist(t.x, t.y, -screenTransX, -screenTransY)<inchesToPixels(.1f); //has to be within .1"
        boolean closeRotation = abs(t.rotation - screenRotation)%90<5; //has to be within +-5 deg
        boolean closeZ = abs(t.z - screenZ)<inchesToPixels(.1f); //has to be within .1"
        println("Close Enough Distance: " + closeDist);
        println("Close Enough Rotation: " + closeRotation);
        println("Close Enough Z: " + closeZ);

        return closeDist && closeRotation && closeZ;
    }
}
