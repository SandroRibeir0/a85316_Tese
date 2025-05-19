/* 

package com.mvn_tomcat_webapp.servlets;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/test")
public class TestServlet extends HttpServlet {
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Setup setup = new Setup(config.getServletContext());
            System.out.println("\nServidor iniciado, executando setup...\n");
            setup.start();
        } catch (Exception e) {
            throw new ServletException("Falha na inicialização do servlet.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    response.getWriter().write("<html><body>");
    response.getWriter().write("<h1>Mensagem do TestServlet no servidor! server started</h1>");
    response.getWriter().write("<button onclick=\"location.href='/mvn_tomcat_webapp/add_sensor.html'\">Adicionar Novo Sensor</button>");
    response.getWriter().write("<h2>Sensor adicionado</h2>");
    response.getWriter().write("</body></html>");
}


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\nServidor iniciado, doPOST...\n");
        // Simulation variables 
        String host = request.getParameter("host");
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        String model = request.getParameter("model");
        String blockName = request.getParameter("block");
        String buildingName = request.getParameter("building");
        String topics = request.getParameter("topics");
        
        // Model specific variables 
        // Initialize them
        double tempMin = 0.0f;
        double tempMax = 0.0f;
        long tempPeakHour = 0;
        float tempBase = 0.0f;
        float maxTempDeviation = 0.0f;
        // Try to request values, because different sensors request different parameters
        try {
            if (request.getParameter("tempMin") != null && !request.getParameter("tempMin").trim().isEmpty()) {
                tempMin = Float.parseFloat(request.getParameter("tempMin"));
            }
            if (request.getParameter("tempMax") != null && !request.getParameter("tempMax").trim().isEmpty()) {
                tempMax = Float.parseFloat(request.getParameter("tempMax"));
            }
            if (request.getParameter("tempPeakHour") != null && !request.getParameter("tempPeakHour").trim().isEmpty()) {
                tempPeakHour = Long.parseLong(request.getParameter("tempPeakHour"));
            }
            if (request.getParameter("tempBase") != null && !request.getParameter("tempBase").trim().isEmpty()) {
                tempBase = Float.parseFloat(request.getParameter("tempBase"));
            }
            if (request.getParameter("maxTempDeviation") != null && !request.getParameter("maxTempDeviation").trim().isEmpty()) {
                maxTempDeviation = Float.parseFloat(request.getParameter("maxTempDeviation"));
            }
        } catch (NumberFormatException e) {
            System.err.println("Erro ao converter um dos parâmetros numéricos: " + e.getMessage());
            // Optional: Return an error response or handle the error
        }

        if (type != null && model != null) {
            AddSensor newSensor = new AddSensor();
            newSensor.createSensor(host, id,type, model, blockName, buildingName, topics, tempMin, tempMax, tempPeakHour, tempBase, maxTempDeviation);

            response.getWriter().write("<h1>Sensor criado</h1>");
            response.getWriter().write("<a href='/mvn_tomcat_webapp/add_sensor.html'>Adicionar Outro Sensor</a><br>");
            response.getWriter().write("<a href='/mvn_tomcat_webapp/test'>Voltar à Página Principal</a>");
            response.getWriter().write("</body></html>");
        } else {
            response.getWriter().write("Por favor, forneça os parâmetros 'type' e 'model'.");
        }
    }
}
 */