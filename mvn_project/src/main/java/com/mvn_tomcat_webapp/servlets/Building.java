package com.mvn_tomcat_webapp.servlets;

import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Author: Sandro Ribeiro
 * Date: 06/06/2024
 * Description: This class receives the Building object - differentiates between building types,
 * and continue to build de simulation Building -» Floors
 */

public class Building {

    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // Class constructor
    public Building(ConcurrentHashMap<String, Object> sensorStatusMap, Simulator setup) {
        this.sensorStatusMap = sensorStatusMap;
        this.setup = setup;
    }

    // Function to read the JSON object and create the buildings
    public void createBuilding(String setupType,String blockName, String host, JSONObject building){
        
        // Get building type
        String buildingType = (String) building.get("building_type");

        // Get building name
        String buildingName = (String) building.get("building_name");

        if(building.containsKey("floors") && building.get("floors") != null){
            //Get JSON array with the building floors
            JSONArray floors = (JSONArray) building.get("floors");
            
            // Iterate through the floors
            for(Object floorObj : floors){
                Floor f = new Floor(sensorStatusMap, setup);
                
                // Get the floor as a JSON Object
                JSONObject floor = (JSONObject) floorObj;

                // Different types of buildings require different treatment
                // In this case we only deal with 2 types of buildings:
                // 1 - "prédios" - buildings that have spaces like: stores, apartments, offices
                // 2 - buildings that don't have spaces
                // This choice was made in order to simplify and fit as many building types into 1 category
                if (buildingType.equals("predio")){
                    //In this function we will deal with buildings that fit in the first type
                    f.createBuildingFloor(setupType, blockName, buildingName, host, floor);
                }
                else{
                    //Here we deal with the other buildings
                    f.createFloor(setupType, blockName, buildingName, host, floor);
                }
            }
        }
    }
}
