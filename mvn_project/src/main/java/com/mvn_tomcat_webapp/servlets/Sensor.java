package com.mvn_tomcat_webapp.servlets;

/**
 * Author: Sandro Ribeiro
 * Date: 06/06/2024
 * Description: This interface defines the methods needed by the classes implementing it.
 */

interface Sensor {

        // mudar para uma função mais geral de modo a servir qualquer tipo de sensor
        void setSensorStatus(double status);

        // Método para obter o status atual do sensor
        // Object getSensorStatus();

        //void setSensorStatus(int status);

}
