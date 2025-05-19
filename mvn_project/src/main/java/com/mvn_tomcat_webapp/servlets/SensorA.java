package com.mvn_tomcat_webapp.servlets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Author: Sandro Ribeiro
 * Date: 06/06/2024
 * Description: This class receives the object Sensor for the simulation of random value based sensor,
 * the values are defined by its own data model - running in a separate thread
 */

public class SensorA extends Thread implements Sensor {
    //Declare variables
    String block;
    String building;
    String sensorID;
    Producer producer;
    Data postedData;
    JSONArray topics;
    SensorModel m;
    public double temperature = 0;
    long sleep_time = 10000;

    //private Object status;
    
    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    //Sensor A constructor
    public SensorA(String host, String blockName, String buildingName, JSONObject sensor, ConcurrentHashMap<String, Object> sensorStatusMap, Simulator setup){
        this.block = blockName;
        this.building = buildingName;
        this.sensorStatusMap = sensorStatusMap;
        this.setup = setup;

        //Get sensor name/id
        this.sensorID = String.valueOf(sensor.get("sensor_id"));

        producer = new Producer(host);
        postedData = new Data();

        // Check for topics
        if (sensor.containsKey("topics") && sensor.get("topics") != null){
            //Get an array with the topics to publish
            topics = (JSONArray) sensor.get("topics");

            for (Object topicObj : topics) {
                JSONObject setupTopic = (JSONObject) topicObj;
                String setupJsonTopic = (String) setupTopic.get("topic");

                try {
                    producer.configureTopicRetention(postedData.buildTopic(block, building, setupJsonTopic));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
        }

        //Start the sensor data model: send Sensor s - in order to update the temperature value
        m = new ModelRandom(this, setup);
        ((ModelRandom) m).start();

    }

    public void run() {
        
        while(setup.getSimulationSatus()) { 
            //System.out.println("random_sensor_value: " + temperature);

            // Build a message
            //String message = postedData.buildMessage(String.valueOf(temperature));

            //This cycle allows the sensor to publish in multiple topics, defined in the json file
            for(Object topicObj: topics){
                JSONObject topic = (JSONObject) topicObj;
                String jsonTopic = (String) topic.get("topic");
            
                String topicString = postedData.buildTopic(block, building, jsonTopic);
                //publish the message to a kafka_topic
                //producer.publishMessage(topicString, String.valueOf(temperature));
                producer.publishMessage(topicString, postedData.buildMessage(String.valueOf(temperature)));
            }

            try {
                Thread.sleep(sleep_time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Close producer
        producer.closeProducer();
    }

    // Function to get the sensor status from the model, in order to publish it
    // and to store the sensor status in the hashMap - to be accessed by the webapp
    public void setSensorStatus(double temperature) {
        this.temperature = temperature;
        sensorStatusMap.put(sensorID, postedData.buildMessage(String.valueOf(temperature))); 
    }

    /* public Object getSensorStatus() {
        return sensorStatusMap.get(sensorID); // Recuperar o status do sensor
    } */

    /* public void setSensorStatus(int status) {
    } */
}
