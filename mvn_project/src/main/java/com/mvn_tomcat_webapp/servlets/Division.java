package com.mvn_tomcat_webapp.servlets;

import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Author: Sandro Ribeiro
 * Date: 07/06/2024
 * Description: This class receives the Division object allowing to create threads in order to build sensors and actuators
 * Division -» Sensors & Division -» actuators
 */

public class Division {

    Data postedData;
    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // Class constructor
    public Division(ConcurrentHashMap<String, Object> sensorStatusMap, Simulator setup) {
        this.sensorStatusMap = sensorStatusMap;
        this.setup = setup;

        postedData = new Data();
    }

    // Function to create divisions
    public void createDivision(String setupType, String blockName, String buildingName, long florNumber, String host, JSONObject division){

        // Check for sensors
        if(division.containsKey("sensors") && division.get("sensors") != null){
            // Get JSON array containing the sensors
            JSONArray sensors = (JSONArray) division.get("sensors");

            // Iterate through the sensors
            for(Object sensorObj: sensors){
                //Get the JSON object sensor
                JSONObject sensor = (JSONObject) sensorObj;

                //Get sensor type & sensor model
                String sensorType = (String) sensor.get("sensor_type");
                String sensorModel = (String) sensor.get("sensor_model");

                // Use the sensor type and model in order to find particular cases created
                // 1 - sensor model simulating an exterior temperature sensor
                if((sensorType.equals("temperatura")) && (sensorModel.equals("modelo_a"))){
                    // Simulate a exterior temperature sensor : SensorTempExt
                    // Start the thread
                    Sensor s = new SensorTempExt(host, blockName, buildingName, sensor, sensorStatusMap, setup);
                    ((SensorTempExt) s).start();
                }
                // 2 - sensor model simulating an interior temperature sensor
                else if((sensorType.equals("temperatura")) && (sensorModel.equals("modelo_b"))){
                    // Simulate a exterior temperature sensor : SensorTempExt
                    // Start the thread
                    Sensor s = new SensorTempInt(host, blockName, buildingName, sensor, sensorStatusMap, setup);
                    ((SensorTempInt) s).start();
                }
                // to deal with regular cases - generate random data
                else{
                    Sensor s = new SensorA(host, blockName, buildingName, sensor, sensorStatusMap, setup);
                    ((SensorA) s).start();
                }

                /* 
                else{
                    System.out.println("random");
                    /* Sensor s = new SensorA(host, sensor);
                    ((SensorA) s).start();                         
                } */ 
            }
        }
        
        //Check for actuators
        if(division.containsKey("actuators") && division.get("actuators") != null){
            // Get JSON array containing the actuators
            JSONArray actuators = (JSONArray) division.get("actuators");
            //System.out.println(" dentro da divisao atuadores");

            //Iterate through the actuators
            for(Object actuatorObj: actuators){
                //System.out.println("ciclo atuadores");
                
                // Get the JSON objet actuator
                JSONObject actuator = (JSONObject) actuatorObj;

                if(actuator.containsKey("subscriptions") && actuator.get("subscriptions") != null){
                    // Get the JSON array subscriptions - topics to subscribe
                    JSONArray subscriptions = (JSONArray) actuator.get("subscriptions");

                    // Iterate through the topics
                    for(Object subsObj : subscriptions){
                        //Get the JSON object sub - 'topic to sub'
                        JSONObject sub = (JSONObject) subsObj;

                        // Get the topic
                        String topicString = (String) sub.get("topic"); 
                        //System.out.println("antes de criar consumidor");
                        Consumer consumer = new Consumer(host, postedData.buildTopic(blockName, buildingName, topicString), setup);
                        consumer.start();
                        // Create and start a consumer thread to said topic
                        //fazer como no sensor: chamar atuador e enviar o topico
                        //no atuador chamar o consumidor
                        //no consumidor atualizar uma variavel mensagem - como fazemos com a temp no sensor
                        //Actuator a = new ActuatorAll(host, topicString);
                        //((ActuatorAll) a).start();
                    }
                }
            }
        }
    
    }    
}
