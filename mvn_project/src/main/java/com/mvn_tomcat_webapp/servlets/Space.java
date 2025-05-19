package com.mvn_tomcat_webapp.servlets;

import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Author: Sandro Ribeiro
 * Date: 06/06/2024
 * Description: This class receives the Space object,
 * and continues to build de simulation: Space -» Divisions
 */

public class Space {

    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // Class constructor
    public Space(ConcurrentHashMap<String, Object> sensorStatusMap, Simulator setup) {
        this.sensorStatusMap = sensorStatusMap;
        this.setup = setup;
    }

    // Function to create building spaces
    public void createSpace(String setupType, String blockName, String buildingName, Long floorNumber, String host, JSONObject space){

        //Check for divisions
        if(space.containsKey("divisions") && space.get("divisions") != null){
            // Get JSON array containing the divisions
            JSONArray divisions = (JSONArray) space.get("divisions");

            // Iterate through the divisions
            for(Object divisionObj : divisions){
                Division d = new Division(sensorStatusMap, setup);

                // Get the division as a JSON Object
                JSONObject division = (JSONObject) divisionObj;

                // Create new division
                d.createDivision(setupType, blockName, buildingName, floorNumber, host, division);
            }
        }
    }
}
