package com.mvn_tomcat_webapp.servlets;

import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Author: Sandro Ribeiro
 * Date: 06/06/2024
 * Description: This class receives the Floor object and has 2 different function to deal with the possible configurations,
 * then it continues to build de simulation: Floor -» Divisions or Floor -» Spaces
 */

public class Floor {

    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // Class constructor
    public Floor(ConcurrentHashMap<String, Object> sensorStatusMap, Simulator setup) {
        this.sensorStatusMap = sensorStatusMap;
        this.setup = setup;
    }

    //This function builds the floors from type_2 buildings (buildings with no spaces)
    public void createFloor(String setupType, String blockName, String buildingName,  String host, JSONObject floor){
        // Get floor number
        Long floorNumber = (Long) floor.get("floor_number");

        // Check for divisions
        if(floor.containsKey("divisions") && floor.get("divisions") != null){
            // Get JSON array containing the divisions config
            JSONArray divisions = (JSONArray) floor.get("divisions");

            // Iterate through the divisions
            for(Object divisionObj : divisions){
                Division d = new Division(sensorStatusMap, setup);
                
                // Get the division as a JSON Object
                JSONObject division = (JSONObject) divisionObj;

                // Create division
                d.createDivision(setupType, blockName, buildingName, floorNumber, host, division);
            }
        }
    }

    //This function builds the floors from type 1 buildings (buildings with spaces)
    public void createBuildingFloor(String setupType,String blockName, String buildingName,  String host, JSONObject floor){
        // Get floor number
        Long floorNumber = (Long) floor.get("floor_number");

        //Check for spaces
        if(floor.containsKey("spaces") && floor.get("spaces") != null){
            // Get JSON array containing the spaces config
            JSONArray spaces = (JSONArray) floor.get("spaces");

            // Iterate through the spaces
            for (Object spaceObj : spaces) {
                Space s = new Space(sensorStatusMap, setup);

                // Get the space as a JSON Object
                JSONObject space = (JSONObject) spaceObj;

                // Create new space
                s.createSpace(setupType, blockName, buildingName, floorNumber, host, space);
            }
        }
    }
}
