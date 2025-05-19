package com.mvn_tomcat_webapp.servlets;

//import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
//import jakarta.servlet.ServletContext;
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.FileNotFoundException;

/**
 * Author: Sandro Ribeiro
 * Date: 18/10/2024
 * Description: This class reads the configuration file and shares it with the webapp
 *      , starts the simulation build and allows for the simulation to stop.
 */

public class Simulator extends Thread{
    // Servlet configuration
    //private ServletContext servletContext;
    // Timer to handle simulation active time
    private Timer timer;
    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // JSON object with the configuration file
    private JSONObject district;
    // Boolean with the simulation status: active or innactive
    private volatile boolean simulationStatus = false;

    // Class constructor
    public Simulator(JSONObject district , ConcurrentHashMap<String, Object> sensorStatusMap) {
        //this.servletContext = servletContext;
        this.sensorStatusMap = sensorStatusMap;
        this.district = district;
    }

    /*   
    // VERSION 1
    // Function to retrieve and return the configuration file
    public JSONObject getConfig() {
        JSONParser parser = new JSONParser();

        System.out.println("GET JSON FILE");

        try (InputStream inputStream = servletContext.getResourceAsStream("/WEB-INF/confignew.json")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Arquivo de configuração não encontrado");
            }

            // Ler o InputStream como JSON
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            
            // Parse JSON
            Object obj = parser.parse(jsonBuilder.toString());

            // Convert .json file
            district = (JSONObject) obj;

        }catch (IOException | ParseException e) {
            System.out.print("Erro ao abrir ficheiro .json");
            e.printStackTrace();
        }
        System.out.println("RETURN JSON FILE");

        return district;
    }
     */

    // Runnable 
    public void run () {
        // Variables
        System.out.println("SIMULATION START");
        // "simDuration" saves the time limit for each the simulation is active
        Long simDuration = 0L;
        System.out.println("SET START TIME");
        if(getSimulationSatus() == false){
            // Set the simulation status as active/true
            this.simulationStatus = true;
        }
        
        // Get setup type: sensors or actuators
        String setupType = "sensors";
                    
        // Get the simulation configurations: simulation run time and simulation host
        // For that we need to access "sim_config" in the JSON file
        JSONArray configurations = (JSONArray) district.get("sim_config");
        System.out.println("GET CONFIG TIME");
        for (Object configObj : configurations) {
            JSONObject config = (JSONObject) configObj;

            simDuration = (Long) config.get("sim_duration");
            //simHost = (String) config.get("host");
        }

        //Define a timer task to deal with the simulation active time
        TimerTask shutdownTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Tempo esgotado. Fim da simulação...");
                // Set the simulation status to innactive/false to stop the simulation
                simulationStatus = false;
                // Close timer thread
                timer.cancel();
            }
        };
        System.out.println("START TIMER");
        //Create and start the timer
        timer = new Timer();
        timer.schedule(shutdownTask, simDuration * 60 * 1000);

        //Get the array 'blocks', containing all the blocks and info
        JSONArray blocks = (JSONArray) district.get("blocks");
        System.out.println("BLOCKS");
        //Iterate through the blocks
        for (Object blockObj : blocks) {
            
            Block b = new Block(sensorStatusMap, this);
            
            JSONObject block = (JSONObject) blockObj;
            
            //Create a new block
            b.createBlock(setupType, block);
        }  
        System.out.println("END");
    }

    // Function to return configuration file used to build the simulation - so the webapp can access it to also build its structure
    public JSONObject getDistrict() {
        return district;
    }

    // Function to return the simulation status - so sensors stop once the status changes to innactive
    public boolean getSimulationSatus(){
        return simulationStatus;
    }

    public Object getSensorValue (String sensorID){
        return sensorStatusMap.get(sensorID);
    }
    
}