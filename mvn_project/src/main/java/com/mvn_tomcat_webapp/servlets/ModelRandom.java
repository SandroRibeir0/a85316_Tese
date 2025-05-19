package com.mvn_tomcat_webapp.servlets;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
import java.util.Random;
//import java.util.*;

/**
 * Author: Sandro Ribeiro
 * Date: 11/06/2024
 * Description: This class creates random values to sensors in order to test the program while developing extra specific sensor models,
 */

class ModelRandom extends Thread implements SensorModel {
    // Declare Variables
    Random rand;
    Sensor s;
    long sleep_time = 8000;

    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // ModelRandom constructor
    public ModelRandom(Sensor s, Simulator setup){
        // Initialize class Sensor
        this.s = s;
        this.setup = setup;
        
        rand = new Random();
    }

    public void run()  {
        double temp;

        // Iniciate cyle
        // Run while the simulation is running
        while(setup.getSimulationSatus()) {
            // Generate random integers in range 0 to 99
            //temp = (double) rand.nextInt(100);
            temp = rand.nextInt((100 - 50) + 1) + 50;
            //System.out.println("random_model_value: " + temp);

            // Call Sensor method to update temperature value
            s.setSensorStatus(temp);

            //System.out.println("random_model_value: " + temp);

            try {
                Thread.sleep(sleep_time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
