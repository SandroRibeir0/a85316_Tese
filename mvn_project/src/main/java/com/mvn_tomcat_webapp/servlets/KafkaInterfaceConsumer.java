package com.mvn_tomcat_webapp.servlets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import java.time.Duration;
import java.util.*;

public class KafkaInterfaceConsumer {
    Properties props;
    
    public KafkaInterfaceConsumer (){
        props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "interface_consumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest"); // "earliest" garante leitura desde o início caso necessário
    }

    public List<String> getSensorStatusHistory (String topic){

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //String topic = "bairro3_predio13_lum1";
        consumer.subscribe(Collections.singletonList(topic));

        // Calcula o timestamp das últimas 24 horas
        long last24HoursTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        // Obtém as partições atribuídas após o primeiro poll
        consumer.poll(Duration.ofSeconds(1)); // Garante que a atribuição ocorre
        Set<TopicPartition> assignedPartitions = consumer.assignment();

        // Map para buscar offsets com base em timestamps
        Map<TopicPartition, Long> timestampsToSearch = new HashMap<>();
        for (TopicPartition partition : assignedPartitions) {
            timestampsToSearch.put(partition, last24HoursTimestamp);
        }

        // Encontra os offsets baseados nos timestamps
        Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestampsToSearch);

        // Posiciona o consumidor nos offsets encontrados ou no início da partição
        for (TopicPartition partition : assignedPartitions) {
            OffsetAndTimestamp offsetAndTimestamp = offsets.get(partition);

            if (offsetAndTimestamp != null) {
                consumer.seek(partition, offsetAndTimestamp.offset());
            } else {
                consumer.seekToBeginning(Collections.singletonList(partition));
            }
        }

        // Lista para armazenar mensagens
        List<String> messages = new ArrayList<>();

        boolean keepReading = true;
        while (keepReading) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            if (records.isEmpty()) {
                keepReading = false; // Termina se não houver mais mensagens
            } else {
                for (ConsumerRecord<String, String> record : records) {
                    // Armazena todas as mensagens encontradas
                    messages.add(String.format("value = %s, timestamp = %d",
                            record.value(), record.timestamp()));
                }

                // Manualmente comita o deslocamento processado
                consumer.commitSync();
            }
        }

        consumer.close();

        return messages;
    }
}
