package com.ibmi.ias.servlet;

import com.ibmi.ias.service.DcmSearchService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Universal Native Servlet for IAS (Integrated Application Server v8.5+) and WebSphere Liberty.
 * Guarantees 100% compatibility across all IBM i runtime profiles without requiring jaxrs features.
 */
public class ApiServlet extends HttpServlet {

    private final DcmSearchService searchService = new DcmSearchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = req.getServletPath();
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();

        if (pathInfo == null || pathInfo.equals("/status") || pathInfo.endsWith("/status")) {
            out.print(buildStatusJson());
            out.flush();
            return;
        }

        if (pathInfo.contains("/dcm/decode") || pathInfo.contains("/decode")) {
            String symptom = req.getParameter("symptom");
            if (symptom == null || symptom.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"Query parameter 'symptom' is required.\"}");
                out.flush();
                return;
            }
            Map<String, Object> result = searchService.decode(symptom);
            out.print(mapToJson(result));
            out.flush();
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.print("{\"success\":false,\"error\":\"Unknown API endpoint: " + pathInfo + "\"}");
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = req.getServletPath();
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = resp.getWriter();

        if (pathInfo != null && (pathInfo.contains("/dcm/search") || pathInfo.contains("/search"))) {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            String payload = buffer.toString().trim();
            if (payload.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"Request body cannot be empty.\"}");
                out.flush();
                return;
            }

            String[] lines = payload.split("\\r?\\n");
            List<Map<String, Object>> results = new ArrayList<>();
            for (String l : lines) {
                if (!l.trim().isEmpty()) {
                    results.add(searchService.decode(l));
                }
            }

            out.print(listToJson(results));
            out.flush();
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        out.print("{\"success\":false,\"error\":\"Unknown POST API endpoint: " + pathInfo + "\"}");
        out.flush();
    }

    private String buildStatusJson() {
        return "{" +
                "\"serviceName\":\"IBM i DCM Search Service\"," +
                "\"targetServer\":\"Integrated Application Server (IAS) / WebSphere Liberty Profile\"," +
                "\"status\":\"UP\"," +
                "\"osName\":\"" + escape(System.getProperty("os.name")) + "\"," +
                "\"osVersion\":\"" + escape(System.getProperty("os.version")) + "\"," +
                "\"javaVersion\":\"" + escape(System.getProperty("java.version")) + "\"," +
                "\"javaVendor\":\"" + escape(System.getProperty("java.vendor")) + "\"," +
                "\"timestamp\":" + System.currentTimeMillis() +
                "}";
    }

    @SuppressWarnings("unchecked")
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            first = false;
            sb.append(valueToJson(item));
        }
        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object val) {
        if (val == null) {
            return "null";
        }
        if (val instanceof String) {
            return "\"" + escape((String) val) + "\"";
        }
        if (val instanceof Number || val instanceof Boolean) {
            return val.toString();
        }
        if (val instanceof Map) {
            return mapToJson((Map<String, Object>) val);
        }
        if (val instanceof List) {
            return listToJson((List<?>) val);
        }
        return "\"" + escape(val.toString()) + "\"";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
