package com.mvn_tomcat_webapp.servlets;

import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Author: Sandro Ribeiro
 * Date: 18/10/2024
 * Description: This class receives the block object in order to get its configurations (kafka host IP),
 * and continue to build de simulation Block -» Buildings
 */

public class Block {
    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // Class constructor
    public Block(ConcurrentHashMap<String, Object> sensorStatusMap, Simulator setup) {
        this.sensorStatusMap = sensorStatusMap;
        this.setup = setup;
    }

    // Function to read the JSON object and build the blocks
    public void createBlock(String setupType, JSONObject block){
        
        //Get block name/id
        String blockName = (String) block.get("block");

        //Get the kafka server config: host
        String host = "";
        if (block.containsKey("configuration") && block.get("configuration") != null) {
            // Get the JSON array with the configurations
            JSONArray configurations = (JSONArray) block.get("configuration");

            //Read the block configurations object and get the host IP
            for (Object configObj : configurations) {
                JSONObject config = (JSONObject) configObj;

                //Get host IP
                host = (String) config.get("host");
            }
        }
        
        // Check for buildings
        if (block.containsKey("buildings") && block.get("buildings") != null) {
            // Get the JSON array with the buildings
            JSONArray buildings = (JSONArray) block.get("buildings");
            
            // Iterate through the buildings
            for (Object buildingObj : buildings) {                
                Building b = new Building(sensorStatusMap, setup);
                // Get the building as a JSON Object
                JSONObject building = (JSONObject) buildingObj;

                //Create new building
                b.createBuilding(setupType, blockName, host, building);
            }
        }

        // Check for streets
        /* if (block.containsKey("streets") && block.get("streets") != null) {
            // Get the JSON array with the buildings
            JSONArray streets = (JSONArray) block.get("streets");
            
            // Iterate through the buildings
            for (Object streetObj : streets) {                
                Building b = new Building();

                JSONObject street = (JSONObject) streetObjObj;

                //Create new building
                b.createStreet(setupType, blockName, host, street);
            }
        } */
    }
}
