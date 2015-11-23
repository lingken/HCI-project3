package com.dhcs.bakeoff3;

import java.util.ArrayList;

import processing.core.PApplet;
import java.util.Collections;
/**
 * Created by Lincoln on 11/16/15.
 */
public class DragWithAnchor extends PApplet{

    int index = 0;

    // NO USE
    float screenTransX = 0;
    float screenTransY = 0;
    float screenRotation = 0;

    // Gray Square parameter
    float graySquareZ = 300f;


    // Assistant square
    // the square displayed when the finger is dragging the red one
    float assistantX;
    float assistantY;
    float assistantSize;
    float assistantTheta;

    // Assistant line
    float assistantLineX1;
    float assistantLineX2;
    float assistantLineY1;
    float assistantLineY2;

    // Anchor point of the square
    float anchorX;
    float anchorY;

    float fingerOffset = 100f;

    int trialCount = 2; //this will be set higher for the bakeoff
    float border = 0; //have some padding from the sides
    int trialIndex = 0;
    int errorCount = 0;
    int startTime = 0; // time starts when the first click is captured
    int finishTime = 0; //records the time of the final cl4ick
    boolean userDone = false;

    final int screenPPI = 432; //what is the DPI of the screen you are using
    //Many phones listed here: https://en.wikipedia.org/wiki/Comparison_of_high-definition_smartphone_displays

    boolean isDragging = false;
    float threshold = 100f;

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
//        This code will cause crash when using Andoird Studio. It's fine with processing.
//        size(400, 700); //set this, based on your sceen's PPI to be a 2x3.5" area.
        orientation(PORTRAIT);
        rectMode(CENTER);
        textFont(createFont("Arial", inchesToPixels(.15f))); //sets the font to Arial that is .3" tall
        textAlign(CENTER);

        //don't change this!
        border = inchesToPixels(.2f); //padding of 0.2 inches

        for (int i=0; i<trialCount; i++) //don't change this!
        {
            Target t = new Target();
            t.x = random(-width/2+border, width/2-border); //set a random x with some padding
            t.y = random(-height/2+border, height/2-border); //set a random y with some padding
            t.rotation = random(0, 360); //random rotation between 0 and 360
            t.z = ((i%20)+1)*inchesToPixels(.15f); //increasing size from .15 up to 3.0"
            targets.add(t);
            println("created target with " + t.x + "," + t.y + "," + t.rotation + "," + t.z);
        }

        Collections.shuffle(targets); // randomize the order of the button; don't change this.
    }

    public void reset() {
         trialIndex = 0;
         errorCount = 0;
         startTime = 0; // time starts when the first click is captured
         finishTime = 0;
        userDone = false;
        targets.clear();
        setup();
    }

    public void draw() {

        background(60); //background is dark grey
        fill(200);
        noStroke();

        if (startTime == 0)
            startTime = millis();

        if (userDone)
        {
            text("User completed " + trialCount + " trials", width/2, inchesToPixels(.2f));
            text("User had " + errorCount + " error(s)", width/2, inchesToPixels(.2f)*2);
            text("User took " + (finishTime-startTime)/1000f/trialCount + " sec per target", width/2, inchesToPixels(.2f)*3);

            text("TOUCH TO START NEW TRIAL", width/2, inchesToPixels(.2f)*5);

            return;
        }

        //===========DRAW TARGET SQUARE=================
        //Red square
        pushMatrix();
        translate(width/2, height/2); //center the drawing coordinates to the center of the screen

        Target t = targets.get(trialIndex);


        translate(t.x, t.y); //center the drawing coordinates to the center of the screen
        translate(screenTransX, screenTransY); //center the drawing coordinates to the center of the screen

        rotate(radians(t.rotation));

        if (this.assistantSquareActive()) {
            noFill();
        } else if (this.checkTempForSuccess()) {
            fill(0,255,0);
        } else {
            fill(255, 0, 0); //set color to semi translucent
        }
        rect(0, 0, t.z, t.z);
        popMatrix();


        //============Draw Assistant Square============
        pushMatrix();
        if (this.checkTempForSuccess()) {
            fill(0,255,0);
        } else if (this.singleCornerClose()) {
            fill(255, 255, 0);
        } else {
            fill(255, 0,0);
        }
        translate(width / 2, height / 2);
        translate(assistantX, assistantY);
        rotate(assistantTheta);
        rect(0, 0, assistantSize, assistantSize);
        popMatrix();

        pushMatrix();
        translate(width / 2, height / 2);
        stroke(255);
        strokeWeight(5);
        line(assistantLineX1, assistantLineY1, assistantLineX2, assistantLineY2);
        stroke(0);
        strokeWeight(0);
        popMatrix();



        //===========DRAW TARGETTING SQUARE=================
        //Gray square
        pushMatrix();
        translate(width/2, height/2); //center the drawing coordinates to the center of the screen
        rotate(radians(screenRotation));

        if (this.checkTempForSuccess()) {
            fill(0,255,0);
        } else {
            fill(255, 128); //set color to semi translucent
        }
        rect(0, 0, graySquareZ, graySquareZ);

        popMatrix();

        //===========DRAW Information============
        text("Trial " + (trialIndex+1) + " of " +trialCount, width/2, inchesToPixels(.5f));

        text("Submission", width / 2, height - inchesToPixels(.2f));
    }

    public void mousePressed() {
        if (userDone) {
            reset();
            return;
        }
        if (dist(width / 2, height, mouseX, mouseY)<inchesToPixels(.5f)) {
            // on the submission button
            return;
        }

        // Calculate the coordinate of four corner of the red square
        if (trialIndex >= targets.size()) {
            return;
        }
        pushMatrix();
        translate(width / 2, height / 2);
        fill(255, 128);
        Target t = targets.get(trialIndex);
        float xArray[] = new float[4]; // left top, right top, right bottom, left bottom
        float yArray[] = new float[4];
        xArray[0] = -t.z / 2;
        xArray[1] = t.z / 2;
        xArray[2] = t.z / 2;
        xArray[3] = -t.z / 2;

        yArray[0] = t.z / 2;
        yArray[1] = t.z / 2;
        yArray[2] = -t.z / 2;
        yArray[3] = -t.z / 2;
        float theta = radians(t.rotation % 90);

        for (int i = 0; i < 4; i ++) {
            float tmpX = xArray[i] * cos(theta) - yArray[i] * sin(theta);
            float tmpY = xArray[i] * sin(theta) + yArray[i] * cos(theta);
            xArray[i] = tmpX + t.x;
            yArray[i] = tmpY + t.y;
        }
        // Find out which corner is nearest to the tapping area
        float mX = mouseX - (width/2);
        float mY = mouseY - (height/2);
        int index = -1;
        float dist = 2147483747f;
        for (int i = 0; i < 4; i ++) {
            float tmpDist = (mX - xArray[i]) * (mX - xArray[i]) + (mY - yArray[i]) * (mY - yArray[i]);
            if (tmpDist < threshold*threshold && tmpDist < dist) {
                dist = tmpDist;
                index = i;
                isDragging = true;
            }
        }
        if (isDragging) {
//            isDragging = true;
            // The diagonal corner is the anchor point
            anchorX = xArray[(index + 2)%4];
            anchorY = yArray[(index + 2)%4];

            // Generate the assistant square
            assistantTheta = atan((mY - anchorY) / (mX - anchorX)) - PI/4;
            assistantX = (mX + anchorX) / 2;
            assistantY = (mY + anchorY) / 2;
            assistantSize = sqrt((mX - anchorX)*(mX - anchorX) + (mY - anchorY)*(mY - anchorY)) / sqrt(2);
        }
        popMatrix();
    }

    public void mouseDragged(){
        if (dist(width / 2, height, mouseX, mouseY)<inchesToPixels(.5f)) {
            // on the submission button
            return;
        }
        // change the assistant square continuously
        if (isDragging) {
            pushMatrix();
            translate(width / 2, height / 2);
            fill(255, 128);
            float mX = mouseX - (width/2) - fingerOffset;
            float mY = mouseY - (height/2);
            assistantTheta = atan((mY - anchorY) / (mX - anchorX)) - PI/4;
            assistantX = (mX + anchorX) / 2;
            assistantY = (mY + anchorY) / 2;
            assistantSize = sqrt((mX - anchorX)*(mX - anchorX) + (mY - anchorY)*(mY - anchorY)) / sqrt(2);

            assistantLineX1 = mouseX - (width/2);
            assistantLineY1 = mY;
            assistantLineX2 = mX;
            assistantLineY2= mY;
            popMatrix();
        }
    }

    public void mouseReleased()
    {
        if (dist(width/2, height, mouseX, mouseY)<inchesToPixels(.5f)) {
            // Click the submission button
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
            assistantX = 0;
            assistantY = 0;
            assistantTheta = 0;
            assistantSize = 0;
        } else {
            // Change the red square to the position and size of assistant square
            if (trialIndex >= targets.size()) {
                return;
            }
            if (isDragging) {
                Target t = targets.get(trialIndex);
                t.x = assistantX;
                t.y = assistantY;
                t.rotation = assistantTheta / PI * 180;
                t.z = assistantSize;
            }
            isDragging = false;
        }

        assistantLineY1 = 0;
        assistantLineY2 = 0;
        assistantLineX1 = 0;
        assistantLineX2 = 0;
    }

    //function for testing if the overlap is sufficiently close
    //Don't change this function! Check with Chris if you think you have to.

    public boolean checkForSuccess()
    {
        Target t = targets.get(trialIndex);
        boolean closeDist = dist(t.x,t.y,-screenTransX,-screenTransY)<inchesToPixels(.05f); //has to be within .1"
        boolean closeRotation = calculateDifferenceBetweenAngles(t.rotation,screenRotation)<=5;
        boolean closeZ = abs(t.z - graySquareZ)<inchesToPixels(.05f); //has to be within .1"
        println("Close Enough Distance: " + closeDist);
        println("Close Enough Rotation: " + closeRotation + " ("+(t.rotation+360)%90+","+ (screenRotation+360)%90+")");
        println("Close Enough Z: " + closeZ);
        return closeDist && closeRotation && closeZ;
    }

    public boolean checkTempForSuccess()
    {
        Target t = new Target();
        t.x = assistantX;
        t.y = assistantY;
        t.rotation = assistantTheta / PI * 180;
        t.z = assistantSize;

        // There is bug in the close rotation function
        boolean closeDist = dist(t.x,t.y,-screenTransX,-screenTransY)<inchesToPixels(.05f); //has to be within .1"
//        boolean closeRotation = abs((t.rotation + 360) % 90 - (screenRotation + 360) % 90)%90<5; //has to be within +-5 deg
        boolean closeRotation = calculateDifferenceBetweenAngles(t.rotation,screenRotation)<=5;
        boolean closeZ = abs(t.z - graySquareZ)<inchesToPixels(.05f); //has to be within .1"

        return closeDist && closeRotation && closeZ;
    }

    double calculateDifferenceBetweenAngles(float a1, float a2)
    {
        a1+=360;
        a2+=360;
        if (abs(a1-a2)>45)
            return abs(abs(a1-a2)%90-90);
        else
            return abs(a1-a2)%90;
    }

    public boolean assistantSquareActive() {
        return !(assistantTheta == 0 && assistantSize == 0 && assistantX == 0 && assistantY == 0);
    }

    public boolean singleCornerClose() {
        pushMatrix();
        translate(width / 2, height / 2);

        float xArray[] = new float[4]; // left top, right top, right bottom, left bottom
        float yArray[] = new float[4];
        xArray[0] = -assistantSize / 2;
        xArray[1] = assistantSize / 2;
        xArray[2] = assistantSize / 2;
        xArray[3] = -assistantSize / 2;

        yArray[0] = assistantSize / 2;
        yArray[1] = assistantSize / 2;
        yArray[2] = -assistantSize / 2;
        yArray[3] = -assistantSize / 2;
        float theta = assistantTheta;

        for (int i = 0; i < 4; i ++) {
            float tmpX = xArray[i] * cos(theta) - yArray[i] * sin(theta);
            float tmpY = xArray[i] * sin(theta) + yArray[i] * cos(theta);
            xArray[i] = tmpX + assistantX;
            yArray[i] = tmpY + assistantY;
        }

        float xGray[] = new float[4];
        float yGray[] = new float[4];

        xGray[0] = graySquareZ/2;
        xGray[1] = graySquareZ/2;
        xGray[2] = -graySquareZ/2;
        xGray[3] = -graySquareZ/2;

        yGray[0] = graySquareZ/2;
        yGray[1] = -graySquareZ/2;
        yGray[2] = -graySquareZ/2;
        yGray[3] = graySquareZ/2;


        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (dist(xArray[i],yArray[i],xGray[j],yGray[j])<inchesToPixels(.05f)) {
                    println("Single corner is close!");
                    popMatrix();
                    return true;
                }
            }
        }

        popMatrix();
        return false;
    }

}
