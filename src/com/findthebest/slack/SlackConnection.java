package com.findthebest.slack;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by aoverton on 6/11/15.
 */
public class SlackConnection {
    /*
     * Instance Variables
     */

    private Gson gson = new Gson();
    private int OK = 200;
    private String baseAuthUrl = "https://slack.com/api/auth.test?token=";
    private StringBuilder baseMessageUrl = new StringBuilder("https://slack.com/api/chat.postMessage?");
    private String configFile = "config.properties";
    private Boolean authStatus;
    private String token;
    private final static Logger LOGGER = Logger.getLogger(SlackConnection.class.getName());


     
    /*
     * Constructors
     */

    public SlackConnection(String token) {
        configLogger(Level.CONFIG);
        this.token = token;
        String authUrlString = baseAuthUrl + token;
        try {
            LOGGER.config("Attempting to Auth");
            HttpsURLConnection con = sendGetRequest(new URL(authUrlString));
            LOGGER.config("Checking response code");
            int response = con.getResponseCode();
            if (response == OK) {
                LOGGER.config("Setting auth status");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                LinkedTreeMap result = gson.fromJson(in, LinkedTreeMap.class);
                String status = result.get("ok").toString();
                if (status.equals("true")) {
                    authStatus = true;
                } else {
                    authStatus = false;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * Methods
     */

    private void configLogger(Level logLevel) {
        LOGGER.setLevel(logLevel);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(logLevel);
        LOGGER.addHandler(handler);
    }

    private HttpsURLConnection sendGetRequest(URL url) throws IOException {
        return (HttpsURLConnection) url.openConnection();
    }


    public void postToSlack(String channel, String message) throws MalformedURLException, IOException {
        if (authStatus) {
            LOGGER.config("Building GET request");
            LinkedHashMap<String,String> params = new LinkedHashMap<String, String>();
            params.put("token", token);
            params.put("channel", channel);
            params.put("text", message);
            for (Map.Entry entry : params.entrySet()) {
                baseMessageUrl.append(entry.getKey());
                baseMessageUrl.append("=");
                baseMessageUrl.append(URLEncoder.encode((String) entry.getValue(), "UTF-8"));
                baseMessageUrl.append("&");
            }
            baseMessageUrl.deleteCharAt(baseMessageUrl.length() - 1);
            LOGGER.config("Sending GET request");
            HttpsURLConnection con = sendGetRequest(new URL(baseMessageUrl.toString()));
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            LOGGER.config(response.toString());
        } else {
            LOGGER.severe("Client not authed");
        }
    }

    public Boolean getAuthStatus() {
        return authStatus;
    }

    public static void main(String[] args) throws FileNotFoundException, MalformedURLException, IOException {
        SlackConnection slack = new SlackConnection("xoxp-2225323168-4194257109-6101248807-defb57");
        slack.postToSlack("kettle-bot", "Test");
    }

}
