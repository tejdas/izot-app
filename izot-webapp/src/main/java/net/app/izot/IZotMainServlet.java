package net.app.izot;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class IZotMainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Map<String, SampleSubscriber> subscribers = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setStatus(200);

        PrintWriter writer = response.getWriter();
        writer.println("Hello World: " + new Date().toString() + " Context path: " + request.getContextPath());
        writer.println("Path Info : " + request.getPathInfo());
        writer.println("Request URI: " + request.getRequestURI());
        writer.println("Servlet Path: " + request.getServletPath());
        writer.println("Query String: " + request.getQueryString());

        String servletPath = request.getServletPath();
        if (StringUtils.startsWith(servletPath, "/")) {
            String channelName = "DefaultChannel";
            String query = request.getQueryString();
            if (!StringUtils.isEmpty(query)) {
                String[] tokens = query.split("=");
                if (StringUtils.equalsIgnoreCase(tokens[0], "channel")) {
                    channelName = tokens[1];
                }
            }

            servletPath = servletPath.substring(1);
            if (StringUtils.equalsIgnoreCase(servletPath, "start")) {
                startSubscriber(channelName);
                writer.println("Started subscriber: " + channelName);
            } else if (StringUtils.equalsIgnoreCase(servletPath, "stop")) {
                stopSubscriber(channelName);
                writer.println("Stopped subscriber: " + channelName);
            } else if (StringUtils.equalsIgnoreCase(servletPath, "count")) {
                int messageCount = getMessageCount(channelName);
                writer.println("Channel: " + channelName + " MessageCount: " + messageCount);
            } else if (StringUtils.equalsIgnoreCase(servletPath, "message")) {
                getAndDisplayMessage(channelName, writer);
                //writer.println("Channel: " + channelName + " Message: " + message);
            }
        }

        writer.close();
    }

    private static void startSubscriber(String channel) {
        if (!subscribers.containsKey(channel)) {
            SampleSubscriber subscriber = new SampleSubscriber(channel);
            subscribers.put(channel, subscriber);
            subscriber.start();
        }
    }

    private static void stopSubscriber(String channel) {
        if (subscribers.containsKey(channel)) {
            SampleSubscriber subscriber = subscribers.remove(channel);
            subscriber.stop();
        }
    }

    private static int getMessageCount(String channel) {
        if (subscribers.containsKey(channel)) {
            SampleSubscriber subscriber = subscribers.get(channel);
            return subscriber.getMessageCount();
        } else {
            return 0;
        }
    }

    private static void getAndDisplayMessage(String channel, PrintWriter writer) {
        if (subscribers.containsKey(channel)) {
            SampleSubscriber subscriber = subscribers.get(channel);
            JSONObject message = subscriber.getMessage();
            if (message != null) {
                writer.println("Message received at Channel: " + channel);
                String[] names = JSONObject.getNames(message);
                if (names != null) {
                    for (String name : names) {
                        try {
                            writer.println("name: " + name + " value: " + message.get(name));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                writer.println("No messages in Channel: " + channel);
            }
        } else {
            writer.println("Channel not subscribed: " + channel);
        }
    }
}