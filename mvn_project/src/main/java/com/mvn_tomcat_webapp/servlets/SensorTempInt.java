package com.mvn_tomcat_webapp.servlets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SensorTempInt extends Thread implements Sensor{
    //Declare variables
    String block;
    String building;
    String sensorID;
    Producer producer;
    Data postedData;
    JSONArray topics;
    SensorModel m;
    public double temperature = 0;
    long sleep_time = 15000;

    // HashMap to share sensor status with the webapp
    private ConcurrentHashMap<String, Object> sensorStatusMap;
    // Setup instance to allow access to the simulation status
    private Simulator setup;

    // Sensor constructor
    public SensorTempInt(String host, String blockName, String buildingName, JSONObject sensor, ConcurrentHashMap<String, Object> sensorStatusMap, Simulator setup) {
        this.block = blockName;
        this.building = buildingName;
        this.sensorStatusMap = sensorStatusMap;
        this.setup = setup;

        //Get sensor name/id
        this.sensorID = String.valueOf(sensor.get("sensor_id"));

        //this.host = host;
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

        //Check for sensor model data
        if (sensor.containsKey("sensor_model_data") && sensor.get("sensor_model_data") != null){

            //Get an array with sensor model configuration
            JSONArray models = (JSONArray) sensor.get("sensor_model_data");

            for (Object modelObj : models) {
                JSONObject model_parameters = (JSONObject) modelObj;

                //Start the sensor data model: send Sensor s - in order to update the temperature value
                m = new ModelB(model_parameters, this, setup);
                ((ModelB) m).start();
            }
        }
    }

    public void run() {

        // Initiate a cycle
        // Run while the simulation is active
        while(setup.getSimulationSatus()) {
            System.out.println("___Interior Sensor Temperature___   " + temperature);

            //Build a message
            //String message = postedData.buildMessage(String.valueOf(temperature));

            //This cycle allows a sensor to publish to multiples topics, defined in the json file
            for (Object topicObj : topics) {
                JSONObject topic = (JSONObject) topicObj;
                String jsonTopic = (String) topic.get("topic");
                
                String topicString = postedData.buildTopic(block, building, jsonTopic);
                // Publish the message to the kafka_topic
                //producer.publishMessage(topicString, String.valueOf(temperature));
                producer.publishMessage(topicString, postedData.buildMessage(String.valueOf(temperature)));
            }

            try {
                //Wait for 'sleep_time' and repeat
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
    /* 
    public Object getSensorStatus() {
        return sensorStatusMap.get(sensorID); // Recuperar o status do sensor
    } */

    /* public void setSensorStatus(int status) {
    } */
}
