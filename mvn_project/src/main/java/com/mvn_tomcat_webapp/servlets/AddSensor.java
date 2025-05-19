/* 
package com.mvn_tomcat_webapp.servlets;
//import org.json.JSONArray;
//import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class AddSensor {

    @SuppressWarnings("unchecked")
    public void createSensor(String host, String id, String sensorType, String sensorModel, String blockName, String buildingName, String topics, double tempMin, double tempMax, long tempPeakHour, float tempBase, float maxTempDeviation){
        // Split topics list
        String[] topcisList = topics.split(",");

        // Build the Sensor json config
        // Create sensor object
        JSONObject sensorObject = new JSONObject();
        // Add ID and sensor type
        sensorObject.put("sensor_id", id);
        sensorObject.put("sensor_type", sensorType);

        // Create a JSON array to store objects
        JSONArray topicsArray = new JSONArray();
        // Iterate through the topics and add them to the array
        for (String topic : topcisList) {
            JSONObject topicObject = new JSONObject();
            topicObject.put("topic", topic);
            topicsArray.add(topicObject);
        }

        // Add the topics array to the sensor object
        sensorObject.put("topics", topicsArray);
        // Add sensor model
        sensorObject.put("sensor_model",sensorModel);

        System.out.println(sensorObject.toJSONString());       
        
        if((sensorType.equals("temperatura")) && (sensorModel.equals("modelo_a"))){
            // Finish the sensor json object. Needed by the sensors java classes
            // Create a data Array
            JSONArray dataModelArray = new JSONArray();

            // Create a data model object, and add the necessary data for the specific sensor
            JSONObject dataModel1Object = new JSONObject();
            dataModel1Object.put("temp_min", tempMin);
            dataModel1Object.put("temp_max", tempMax);
            dataModel1Object.put("temp_peak_hour", tempPeakHour);

            // Add the pbject to the data model array
            dataModelArray.add(dataModel1Object);            
            // finally add the data array to the sensor object
            sensorObject.put("sensor_model_data", dataModelArray);
            
            System.out.println("n\new exterior sensor\n");

            // Simulate a exterior temperature sensor : SensorTempExt
            // Start the thread
            Sensor s = new SensorTempExt(host, blockName, buildingName, sensorObject);
            ((SensorTempExt) s).start();
        }
        // 2 - sensor model simulating an interior temperature sensor
        else if((sensorType.equals("temperatura")) && (sensorModel.equals("modelo_b"))){
            // Finish the sensor json object. Needed by the sensors java classes
            // Create a data Array
            JSONArray dataModelArray = new JSONArray();

            // Create a data model object, and add the necessary data for the specific sensor
            JSONObject dataModel1Object = new JSONObject();
            dataModel1Object.put("temp_base", tempBase);
            dataModel1Object.put("max_temp_deviation", maxTempDeviation);

            // Add the pbject to the data model array
            dataModelArray.add(dataModel1Object);            
            // finally add the data array to the sensor object
            sensorObject.put("sensor_model_data", dataModelArray);

            System.out.println("n\new interior sensor\n");
            
            // Simulate a exterior temperature sensor : SensorTempExt
            // Start the thread
            Sensor s = new SensorTempInt(host, blockName, buildingName, sensorObject);
            ((SensorTempInt) s).start();
        }
        // to deal with regular cases - generate random data
        else{
            System.out.println("n\new random sensor\n");

            Sensor s = new SensorA(host, blockName, buildingName, sensorObject);
            ((SensorA) s).start();
        } 

    }
    
}

*/