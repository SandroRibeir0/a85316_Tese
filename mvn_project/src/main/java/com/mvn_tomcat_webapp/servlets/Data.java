package com.mvn_tomcat_webapp.servlets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import java.util.*;

public class Data {
    
    // Function to build a message with the format: sensor value/status + timestamp 
    public String buildMessage(String sensorValue){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = now.format(formatter);

        return sensorValue + " " + timestamp;
    }

    // Function to create the topic structure - providing hierarchy: block -> building ->    
    public String buildTopic(String block, String building, String topic){
        String newTopic = block + "_" + building + "_" + topic;

        return newTopic;
    }
}

