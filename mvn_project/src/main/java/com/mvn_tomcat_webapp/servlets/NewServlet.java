package com.mvn_tomcat_webapp.servlets;

import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//import org.json.JSONArray;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import java.io.OutputStream;

//@WebServlet("/mensagem")

@WebServlet("/app")
@MultipartConfig
public class NewServlet extends HttpServlet {
    // Create a Setup instance
    private Simulator simulator;
    // HashMap to store the sensors status
    private static ConcurrentHashMap<String, Object> sensorStatusMap;
    // "simStartTime" refers and saves the simulation start timestamp
    // private long simStartTime;
    // HashMap to store the simulations start timestamp
    private static ConcurrentHashMap<String, Long> timestampList = new ConcurrentHashMap<>();
    // HashMap to store the list of available simulations in the format Name - JSON ConfigFile
    private static ConcurrentHashMap<String, Object> simulationList = new ConcurrentHashMap<>();
    // HashMap to store the simulation ID and its Simulator instante
    private static ConcurrentHashMap<String, Simulator> activeSimulationList = new ConcurrentHashMap<>();
    // Instace for data support class
    private Data dataModel = new Data();
    // Instance for the interface consumer class
    private KafkaInterfaceConsumer interfaceConsumer = new KafkaInterfaceConsumer();
    // Instance for a second interface consumer class, dealing with an interval of timstamps
    private KafkaInterfaceConsumerTimestamps intervalConsumer = new KafkaInterfaceConsumerTimestamps();

    // Servlet constructor
    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize the Setup instance
        // setup = new Setup(getServletContext(), sensorStatusMap);
    }

    /*
    private void printSensorStatuses() {
        System.out.println("Estado dos Sensores:");
        for (String sensorID : sensorStatusMap.keySet()) {
            Object sensorStatus = sensorStatusMap.get(sensorID);
            System.out.println("Sensor ID: " + sensorID + ", Status: " + sensorStatus);
        }
    } */

    // GET method
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Parameter to define the GET request
        String action = request.getParameter("action");

        // If "action" is null return error
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Action is NULL\"}");
            return;
        }
        
        // Switch case to choose from the available functionalities
        switch (action) {
            // VERSION 1
            // "getJSON" returns the JSON configuration file applied in the simulation
            case "getJSON":
                handleJSONRequest(request, response);
                break;
            // "getHTML" would return the html page to load - NOT USED
            case "getHTML":
                handleHTMLRequest(request, response);
                break; 
            /* 
            // VERSION 1 
            // "startSIM" starts the simulation   
            case "startSIM":
                handleSimulationStart();
                break;
             */                
            // VERSION 1
            // "getSimStatus" return a JSON with a boolean refering to the simulation status
            case "getSimStatus":
                handleGetSimlationStatusRequest(request, response);
                break;  
            /* 
            // VERSION 1
            // "getSimStartTime" is only used when a page is loaded while the simulation is active. This case return the simulation start timestamp
            case "getSimStartTime":
                handleSimStartTimeRequest(request, response);
                break;
            */    
            // VERSION 2
            // "getSimulations" returns the list of available simulations
            case "getSimulationList":
                handleSimulationListRequest(request, response);
                break;
            // default deals with invalid inputs
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid action\"}");
                break;
        }
    }

    // VERSION 1
    // Function to handle the request for the JSON configuration file
    private void handleJSONRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Call the Setup function that returns the JSON configuration file
        JSONObject jsonResponse = simulator.getDistrict();
        System.out.println("____----GOT JSON FILE ---_____");
        
        // If the returned JSON is not empty
        if (jsonResponse != null) {
            // Responds to the user interface with the JSON Object
            response.getWriter().write(jsonResponse.toString());
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Error loading JSON\"}");
        }
    }
    
    /* 
    // VERSION 1
    // Function to handle the request for the start of the simulation
    private void handleSimulationStart(){ 
        simulator = new Simulator(getServletContext(), sensorStatusMap);
        
        // Calls the function to store the simulation start timestamp
        //setSimulationStartTime();
        // Calls the Setup function to start the simulation
        simulator.start();
        //new Thread(setup).start();
        //setup.runSimulation();
    } */
    
    // VERSION 1
    // Function to handle the request for the Simulation Status
    @SuppressWarnings("unchecked")
    private void handleGetSimlationStatusRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Calls the Setup function that returns the simulation status
        boolean simStatus = simulator.getSimulationSatus();
        
        // Creates a JSON Object and puts in the simulation status linked to "simulationActive"
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("simulationActive", simStatus);

        // Responds to the user interface in a JSON format
        response.getWriter().write(jsonResponse.toString());
    }

    // Function to handle the request for the html page - not used
    private void handleHTMLRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // Obter o caminho real do arquivo HTML dentro do diretório WEB-INF
        ServletContext context = getServletContext();
        String filePath = "/WEB-INF/flask_tomcat.html"; // Caminho relativo ao WEB-INF
        InputStream inputStream = context.getResourceAsStream(filePath);

        if (inputStream != null) {
            // Obter o OutputStream da resposta
            OutputStream outputStream = response.getOutputStream();

            // Buffer para leitura eficiente do arquivo
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Ler o arquivo em blocos de bytes e escrever na resposta
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Fechar streams
            inputStream.close();
            outputStream.flush();
        } else {
            // Arquivo não encontrado, retorna erro
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("<html><body><h1>Erro 404: File not found</h1></body></html>");
        }
    }
    /* 
    // VERSION 1
    // Function to hangle the request for timestamp of the simulation start
    @SuppressWarnings("unchecked")
    private void handleSimStartTimeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Creates a JSON Object and puts in the timestamp linked to "simulationStartTime"
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("simulationStartTime", simStartTime);

        // responds to the user interface in a JSON format
        response.getWriter().write(jsonResponse.toString());
    }
    */

    // Function to set the simulation timestamp, at the moment it starts
    private void setSimulationStartTime(String simID){
        long simStartTime = System.currentTimeMillis();
        timestampList.put(simID, simStartTime);
    }

    // function to handle the request for the list of available simulations
    @SuppressWarnings("unchecked")
    private void handleSimulationListRequest (HttpServletRequest request, HttpServletResponse response) throws IOException{
        // Define o tipo de conteúdo e codificação da resposta
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Cria um JSON contendo a lista de simulações
        JSONArray jsonResponse = new JSONArray();
        
        simulationList.keySet().forEach(simulationName -> {
            JSONObject simulationObject = new JSONObject();
            simulationObject.put("name", simulationName);
            simulationObject.put("url", "/mvn_tomcat_webapp/tomcat_app.html?simulation=" + simulationName);
            jsonResponse.add(simulationObject);
        });

        // Envia a resposta como JSON
        response.getWriter().write(jsonResponse.toJSONString());
    }

    // POST method
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Parameter to define the GET request
        String action = request.getParameter("action");

        // If "action" is null return error
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Action is NULL\"}");
            return;
        }

        // Switch case to choose from the available functionalities
        switch (action) {
            // VERSION 2
            // "getSensorStatus" gets the requested sensor ID and returns its status
            case "getSensorStatus":
                handleSensorStatusRequest(request, response);
                break;
            // "addNewSimulation" handles the addition of a new simulation
            case "addNewSimulation":
                //System.out.println("addNewSimulation");
                handleAddNewSimulation(request, response);
                break;
            // "getSimulationConfig" return the configfile for the defined simulation
            case "getSimulationConfig":
                handleGetSimulationConfig(request, response);
                break;
            // "getSimStatus" return a JSON with a boolean refering to the simulation status
            case "getSimStatus":
                handlePostGetSimlationStatusRequest(request, response);
                break;
            // "getSimStartTime" is only used when a page is loaded while the simulation is active. This case return the simulation start timestamp
            case "getSimStartTime":
                handlePostSimStartTimeRequest(request, response);
                break;
            // "startSIM" starts the simulation   
            case "startSIM":
                handlePostSimulationStart(request, response);
                break;   
            // handle the request for a sensor status history
            case "getSensorHistory":
                handleSensorHistoryRequest(request, response);
                break;    
            case "checkSimulationName":
                handleFormRequestForSimulationName(request, response);
                break;
            // handle the request for a sensor status history between an timestamps interval
            case "getSensorHistoryBetweenTimestamps":
                handleSensorHistoryRequestBetweenTimestamps(request, response);
                break;
            // default deals with invalid inputs
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid action\"}");
                break;
        }
    }

    // Function to handle the request for a sensor's status
    @SuppressWarnings("unchecked")
    private void handleSensorStatusRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Reads  the simID sent in the request's body
        String simID = request.getParameter("simID");
        // Reads  the sensorID sent in the request's body
        String sensorID = request.getParameter("sensorID");

        // If simID is not empty
        if (simID != null) {
            Simulator simulator = activeSimulationList.get(simID);
            if (simulator != null) {
                // If sensorID is not empty
                if (sensorID != null) {
                    // Get the sensor's status in the HashMap 
                    Object sensorStatus = simulator.getSensorValue(sensorID);
                    
                    // If sensorStatus is valid
                    if (sensorStatus != null) {
                        // Create a JSON Object and build the response
                        JSONObject jsonResponse = new JSONObject();
                        jsonResponse.put("sensorID", sensorID);
                        jsonResponse.put("status", sensorStatus);
                        
                        // Respond to the user interface in a JSON format
                        response.getWriter().write(jsonResponse.toString());
                    } else {
                        // If the sensorStatus is not valid - error
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().write("{\"error\": \"Sensor not found\"}");
                    }
                } else {
                    // If the sensorID is empty, or not sent in the request - error
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"sensorID not found\"}");
                }
            } else {
                // If simulator not available - error
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"simulator not found\"}");
            }
        } else {
            // If simID is empty, or not sent in the request - error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"simID not found\"}");
        }

        
    }

    // Handle the request to load a new simulation - receives a simulation name and a configuration file in JSON
    private void handleAddNewSimulation (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("START FORM POST");
        String simulationName = request.getParameter("simulationName");
        System.out.println(simulationName);
        Part configFilePart = request.getPart("configFile");
        
        JSONParser parser = new JSONParser();
        if (simulationName != null && configFilePart != null) {
            try (InputStream fileContent = configFilePart.getInputStream()) {
                String jsonText = new BufferedReader(new InputStreamReader(fileContent, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

                JSONObject configFile = (JSONObject) parser.parse(jsonText);
                simulationList.put(simulationName, configFile);

                // Setup a new Simulator instance
                // New Map to store the sensor's status
                sensorStatusMap = new ConcurrentHashMap<>();
                // New simulator instance
                simulator = new Simulator(configFile, sensorStatusMap);
                // Store the simulator instance refering to it's ID
                activeSimulationList.put(simulationName, simulator);

                //System.out.println(configFile);
                response.getWriter().write("{\"message\": \"Simulação armazenada com sucesso.\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Falha ao processar o arquivo JSON.\"}");
                e.printStackTrace();
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Nome da simulação ou arquivo JSON ausente.\"}");
        }
    }

    // VERSION 2
    // Function to handle the request for the JSON configuration file
    @SuppressWarnings("unchecked")
    private void handleGetSimulationConfig (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Reads  the simID sent in the request's body
        String simID = request.getParameter("simID");

        // If simID is not empty
        if (simID != null) {
            // Get the configuration file form the HashMap "simulationList" 
            Object simConfig = simulationList.get(simID);
            
            // If simConfig is valid
            if (simConfig != null) {
                // Create a JSON Object and build the response
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("configFile", simConfig);

                // Respond to the user interface in a JSON format
                response.getWriter().write(jsonResponse.toString());
            } else {
                // If the simConfig is not valid - error
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"File not found\"}");
            }
        } else {
            // If simID is empty, or not sent in the request - error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"simID not found\"}");
        }
    }

    // VERSION 2
    // Handle the request for the simulation status
    @SuppressWarnings("unchecked")
    private void handlePostGetSimlationStatusRequest (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Reads  the simID sent in the request's body
        String simID = request.getParameter("simID");

        // If simID is not empty
        if (simID != null) {
            Simulator simulator = activeSimulationList.get(simID);
            if (simulator != null) {
                // Calls the Simulator function that returns the simulation status
                boolean simStatus = simulator.getSimulationSatus();
                
                // Creates a JSON Object and puts in the simulation status linked to "simulationActive"
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("simulationActive", simStatus);

                // Responds to the user interface in a JSON format
                response.getWriter().write(jsonResponse.toString());
            } else {
                // If simulator not available - error
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"simulator not found\"}");
            }

        } else {
            // If simID is empty, or not sent in the request - error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"simID not found\"}");
        }
    }

    // function to handle the request for a start timestamp
    @SuppressWarnings("unchecked")
    private void handlePostSimStartTimeRequest (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Reads  the simID sent in the request's body
        String simID = request.getParameter("simID");

        // If simID is not empty
        if (simID != null) {
            // Get the timestamp from the HashMap "timestampList" 
            long simTimestamp = timestampList.get(simID);

            // Creates a JSON Object and puts in the timestamp linked to "simulationStartTime"
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("simulationStartTime", simTimestamp);

            // responds to the user interface in a JSON format
            response.getWriter().write(jsonResponse.toString());   
        } else {
            // If simID is empty, or not sent in the request - error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"simID not found\"}");
        }
    }

    // Function to handle the request to start the simulation
    private void handlePostSimulationStart (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Reads  the simID sent in the request's body
        String simID = request.getParameter("simID");

        // If simID is not empty
        if (simID != null) {
            Simulator simulator = activeSimulationList.get(simID);
            if (simulator != null) {
                // Set the start time
                setSimulationStartTime(simID);
                // Calls the Simulator function to start the simulation
                //simulator.start();
                new Thread(simulator).start();
            } else {
                // If simulator not available - error
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"simulator not found\"}");
            }
        } else {
            // If simID is empty, or not sent in the request - error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"simID not found\"}");
        }
    }

    // Function to handle the request for a sensor's history to draw its graphic
    @SuppressWarnings("unchecked")
    private void handleSensorHistoryRequest (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Reads  the simID sent in the request's body
        String simID = request.getParameter("simID");
        String blockID = request.getParameter("blockID");
        String buildingID = request.getParameter("buildingID");
        String sensorTopic = request.getParameter("sensorTopic");

        String topic = dataModel.buildTopic(blockID, buildingID, sensorTopic);

        // If simID is not empty
        if (simID != null) {
            Simulator simulator = activeSimulationList.get(simID);
            if (simulator != null) {
                List<String> sensorHistory = interfaceConsumer.getSensorStatusHistory(topic);

                // Criar JSON para resposta
                JSONObject jsonResponse = new JSONObject();
                
                // Criar o JSONArray manualmente
                JSONArray historyArray = new JSONArray();
                for (String message : sensorHistory) {
                    historyArray.add(message);
                }
                jsonResponse.put("sensorHistory", historyArray);

                // Enviar resposta JSON
                response.getWriter().write(jsonResponse.toString());

            } else {
                // If simulator not available - error
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"simulator not found\"}");
            }
        } else {
            // If simID is empty, or not sent in the request - error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"simID not found\"}");
        }
    }

    // Function to handle the request to check if a simulation name already exists
    private void handleFormRequestForSimulationName (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Get simulation name
        String simulationName = request.getParameter("name");
        response.setContentType("application/json");
        
        // Check for the simulation in the simulations list
        boolean exists = simulationList.containsKey(simulationName.trim());

        // Return true or false
        response.getWriter().write("{\"exists\":" + exists + "}");
    }

    // Function to handle the request for a sensor's history to draw its graphic
    @SuppressWarnings("unchecked")
    private void handleSensorHistoryRequestBetweenTimestamps (HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Reads  the simID sent in the request's body
        String simID = request.getParameter("simID");
        String blockID = request.getParameter("blockID");
        String buildingID = request.getParameter("buildingID");
        String sensorTopic = request.getParameter("sensorTopic");
        long startTime = Long.parseLong(request.getParameter("startTime"));
        long endTime = Long.parseLong(request.getParameter("endTime"));

        String topic = dataModel.buildTopic(blockID, buildingID, sensorTopic);

        // If simID is not empty
        if (simID != null) {
            Simulator simulator = activeSimulationList.get(simID);
            if (simulator != null) {
                List<String> sensorHistory = intervalConsumer.getSensorStatusHistoryBetweenTimestamps(topic, startTime, endTime);

                // Criar JSON para resposta
                JSONObject jsonResponse = new JSONObject();
                
                // Criar o JSONArray manualmente
                JSONArray historyArray = new JSONArray();
                for (String message : sensorHistory) {
                    historyArray.add(message);
                }
                jsonResponse.put("sensorHistory", historyArray);

                // Enviar resposta JSON
                response.getWriter().write(jsonResponse.toString());

            } else {
                // If simulator not available - error
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"simulator not found\"}");
            }
        } else {
            // If simID is empty, or not sent in the request - error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"simID not found\"}");
        }
    }
}
