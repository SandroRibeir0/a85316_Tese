package com.mvn_tomcat_webapp.servlets;

import java.time.LocalDateTime;
import org.json.simple.JSONObject;

/**
 * Author: Sandro Ribeiro
 * Date: 12/06/2024
 * Description: This class receives the object SensorModel containing the data for the simulation of the values for an exterior based temperature sensor,
 * this specific case simulates the temperature in a 24-hour day
 */

class ModelA extends Thread implements SensorModel {
    //Declare variables
    double tempMin;
    double tempMax;
    long peakHour;
    LocalDateTime localTime;
    Sensor s;
    long sleep_time = 30000;

    // Setup instance to allow access to the simulation status
    private Simulator setup;

    //Model A constructor
    public  ModelA(JSONObject model_parameters, Sensor s ,Simulator setup) {
        
        tempMin = (Double) model_parameters.get("temp_min");
        tempMax = (Double) model_parameters.get("temp_max");
        peakHour = (Long) model_parameters.get("temp_peak_hour");

        //Initialize class Sensor
        this.s = s;

        this.setup = setup;
    }

    public void run()  {
        double temp;

        // Set phase
        double phase = (peakHour / 24.0) * 2 * Math.PI - Math.PI / 2;
        // Set amplitude
        double amplitude = (double) (tempMax - tempMin) / 2;
        // Set offset
        double offset = (double) (tempMax + tempMin) / 2;

        // Initiate a cycle
        // Run while the simulation is active
        while(setup.getSimulationSatus()) {   
            
            localTime = LocalDateTime.now();
        
            // Get the current hours and minutes
            int hours = localTime.getHour();
            int minutes = localTime.getMinute();
            
            // Convert the hour and minute variables into a decimal value
            double decimalHours = hours + (minutes / 60.0);
            // Set the time to a value between  0 e 2*pi
            double rads = (decimalHours / 24.0) * 2 * Math.PI;

            // Use the sine function to calculate the temperature relative to [tempMin, tempMax]
            temp = offset + amplitude * Math.sin(rads - phase);

            // Call Sensor method to update temperature value
            //s.setSensorStatus(temp);
            s.setSensorStatus(Math.round(temp * 100.0) / 100.0);

            System.out.println("___Exterior Model Temperature___   " + temp);

            try {
                Thread.sleep(sleep_time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
