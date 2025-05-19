package com.mvn_tomcat_webapp.servlets;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.config.TopicConfig;
import java.util.Properties;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.*;

public class Producer {
    //Declare variables
    Properties props;
    KafkaProducer<String, String> producer;
    String host;

    // Producer constructor
    public Producer(String host) {
        props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        this.producer = new KafkaProducer<>(props);
        this.host = host;
    }

    
    // chamar esta função ao inicialmente ler os topicos no sensor
    // Method to configure retention for a topic
    public void configureTopicRetention(String topic) throws ExecutionException, InterruptedException {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.host);
        //int retentionMs = 604800000; // 7 days in milliseconds
        int retentionMs = 86400000; // 1 day
        //int retentionMs = 2700000; // 45 minutes
        //int retentionMs = 600000; // 10 minutes 

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            // Check if the topic exists
            boolean topicExists;
            try {
                // Retrieve the list of topics
                ListTopicsResult topicsResult = adminClient.listTopics();
                topicExists = topicsResult.names().get().contains(topic);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Failed to list topics", e);
            }

            if (!topicExists) {
                // Topic does not exist, create it
                NewTopic newTopic = new NewTopic(topic, 1, (short) 1).configs(Collections.singletonMap(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(retentionMs)));
                CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));
                createTopicsResult.all().get();
                System.out.printf("Topic '%s' created with retention.ms = %d\n", topic, retentionMs);
            }
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
        /* 
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            // Define new topic with retention policy
            NewTopic newTopic = new NewTopic(topic, 1, (short) 1)
                    .configs(Collections.singletonMap(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(retentionMs)));
            // Create topic
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.printf("Topic '%s' created with retention.ms = %d\n", topic, retentionMs);
        } */
    } 

    public void publishMessage(String topic, String message) {
        // ver para que serve a key (chat), + utilizar sensor ID????
        //ProducerRecord<String, String> record = new ProducerRecord<>(topic, sensorID, message);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
                    System.out.printf("Message: '%s' sent successfully to topic '%s' partition %d offset %d\n",
                            message ,metadata.topic(), metadata.partition(), metadata.offset());
                } else {
                    exception.printStackTrace();
                }
            }
        });

        // try this
        /*producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.printf("Message: '%s' sent successfully to topic '%s' partition %d offset %d\n",
                        message ,metadata.topic(), metadata.partition(), metadata.offset());
            } else {
                exception.printStackTrace();
            }
        });*/
    }

    public void closeProducer(){
        producer.close();
    }
}
