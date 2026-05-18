package cz.autosalon.logic;

import cz.autosalon.model.Vehicle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Třída pro ukládání a načítání dat do/z JSON souboru.
 * (Class for saving and loading data to/from a JSON file.)
 */
public class DataStorage {
    private static final String FILE_PATH = "data.json";

    public static void save(List<Vehicle> vehicles) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v = vehicles.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": \"").append(escape(v.getId())).append("\",\n");
            sb.append("    \"brand\": \"").append(escape(v.getBrand())).append("\",\n");
            sb.append("    \"model\": \"").append(escape(v.getModel())).append("\",\n");
            sb.append("    \"year\": ").append(v.getYear()).append(",\n");
            sb.append("    \"price\": ").append(v.getPrice()).append(",\n");
            sb.append("    \"description\": \"").append(escape(v.getDescription())).append("\"\n");
            sb.append("  }");
            if (i < vehicles.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");

        try {
            Files.write(Paths.get(FILE_PATH), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static List<Vehicle> load() {
        List<Vehicle> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return list;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            int start = 0;
            while ((start = content.indexOf("{", start)) != -1) {
                int end = start + 1;
                boolean inString = false;
                // Find matching closing brace
                while (end < content.length()) {
                    char c = content.charAt(end);
                    if (c == '"' && content.charAt(end - 1) != '\\') {
                        inString = !inString;
                    }
                    if (c == '}' && !inString) {
                        break;
                    }
                    end++;
                }
                
                if (end >= content.length()) break;
                
                String objContent = content.substring(start + 1, end);
                
                try {
                    String id = extractString(objContent, "\"id\"");
                    String brand = extractString(objContent, "\"brand\"");
                    String model = extractString(objContent, "\"model\"");
                    int year = Integer.parseInt(extractNumber(objContent, "\"year\""));
                    double price = Double.parseDouble(extractNumber(objContent, "\"price\""));
                    String description = extractString(objContent, "\"description\"");
                    
                    list.add(new Vehicle(id, brand, model, year, price, description));
                } catch (Exception e) {
                    System.err.println("Error parsing an object: " + e.getMessage());
                }
                
                start = end + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private static String extractString(String content, String key) {
        String searchKey = key + ":";
        int keyIdx = content.indexOf(searchKey);
        if (keyIdx == -1) {
            searchKey = key + " :";
            keyIdx = content.indexOf(searchKey);
        }
        if (keyIdx == -1) return "";
        
        int valStart = content.indexOf("\"", keyIdx + searchKey.length());
        if (valStart == -1) return "";
        
        int valEnd = valStart + 1;
        while (valEnd < content.length()) {
            if (content.charAt(valEnd) == '"' && content.charAt(valEnd - 1) != '\\') {
                break;
            }
            valEnd++;
        }
        
        if (valEnd >= content.length()) return "";
        
        String val = content.substring(valStart + 1, valEnd);
        return unescape(val);
    }
    
    private static String extractNumber(String content, String key) {
        String searchKey = key + ":";
        int keyIdx = content.indexOf(searchKey);
        if (keyIdx == -1) {
            searchKey = key + " :";
            keyIdx = content.indexOf(searchKey);
        }
        if (keyIdx == -1) return "0";
        
        int start = keyIdx + searchKey.length();
        while (start < content.length() && (Character.isWhitespace(content.charAt(start)) || content.charAt(start) == '"')) {
            start++;
        }
        int end = start;
        while (end < content.length() && (Character.isDigit(content.charAt(end)) || content.charAt(end) == '.')) {
            end++;
        }
        return content.substring(start, end);
    }
    
    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
