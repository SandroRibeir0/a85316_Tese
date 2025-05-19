package com.mvn_tomcat_webapp.servlets;

import org.json.simple.JSONObject;
import java.util.Random;

class ModelB extends Thread implements SensorModel{
    // Declare variables
    double tempBase;
    double maxTempDeviation;
    Random random;
    Sensor s;
    long sleep_time = 25000;

    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // Model B constructor
    public ModelB(JSONObject model_parameters, Sensor s, Simulator setup){

        //tempBase = (Double) model_parameters.get("temp_base");
        //maxTempDeviation = (Double) model_parameters.get("max_temp_deviation");
        tempBase = ((Number) model_parameters.get("temp_base")).doubleValue();
        maxTempDeviation = ((Number) model_parameters.get("max_temp_deviation")).doubleValue();
        this.random = new Random();

        //Initialize class Sensor
        this.s = s;

        this.setup = setup;
    }

    public void run(){
        double temp;

        // Innitiate a cycle
        // Run while the simulation is active
        while(setup.getSimulationSatus()) {
            // Generates a random value between -1.0 and 1.0
            double deviation = (random.nextDouble() * 2) - 1;
            // Multiply the deviation value with the maximum deviation to get the temperature change
            double tempChange = deviation * maxTempDeviation;
            // Get the new temperature value
            temp = tempBase + tempChange;

            // Call sensor method to update temperature value
            //s.setSensorStatus(temp);
            s.setSensorStatus(Math.round(temp * 100.0) / 100.0);

            try {
                Thread.sleep(sleep_time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
