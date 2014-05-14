package net.app.izot;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

@Path("pubnub")
public class IzotService {
    private static final Map<String, PubnubSubscriber> subscribers = new HashMap<>();

    @Context
    UriInfo uriInfo;

    @Context
    ServletContext context;

    @GET
    @Path("/subscribe")
    @Produces(MediaType.TEXT_PLAIN)
    public Response subscribe(@QueryParam("channel") String channel) {
        Pubnub pubnub = getPubnub();
        createSubscriber(channel, pubnub);
        return Response.status(Status.OK).build();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> messageCount(@QueryParam("channel") String channel) {
        int messageCount = getMessageCount(channel);
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("messageCount", messageCount);
        return jsonData;
    }

    @GET
    @Path("/message")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> message(@QueryParam("channel") String channel) {
        JSONObject message = getMessage(channel);
        if (message != null) {
            Map<String, Object> jsonData = new HashMap<>();
            String[] names = JSONObject.getNames(message);
            if (names != null) {
                for (String name : names) {
                    try {
                        jsonData.put(name, message.get(name));
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return jsonData;
        }
        return null;
    }

    @POST
    @Path("/publish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response publishMessage(@QueryParam("channel") String channel, Map<String, Object> input) {
        JSONObject message = new JSONObject();
        for (Entry<String, Object> entry : input.entrySet()) {
            try {
                message.put(entry.getKey(), entry.getValue());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Pubnub pubnub = getPubnub();
        pubnub.publish(channel, message, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                System.out.println("SENT : " + channel + " : " + message.getClass() + " : " + message.toString());
            }
        });
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI uri = ub.path(channel).build();
        return Response.status(201).location(uri).build();
    }

    private static void createSubscriber(String channel, Pubnub pubnub) {
        if (!subscribers.containsKey(channel)) {

            PubnubSubscriber subscriber = new PubnubSubscriber(channel, pubnub);
            subscribers.put(channel, subscriber);
            try {
                subscriber.start();
            }
            catch (PubnubException e) {
                throw new InternalServerErrorException("Error subscribing to Pubnub");
            }
        }
    }

    private static int getMessageCount(String channel) {
        if (subscribers.containsKey(channel)) {
            PubnubSubscriber subscriber = subscribers.get(channel);
            return subscriber.getMessageCount();
        }
        else {
            throw new NotFoundException(channel);
        }
    }

    private static JSONObject getMessage(String channel) {
        if (subscribers.containsKey(channel)) {
            PubnubSubscriber subscriber = subscribers.get(channel);
            return subscriber.getMessage();
        }
        else {
            throw new NotFoundException(channel);
        }
    }

    private Pubnub getPubnub() {
        Object obj = context.getAttribute(IzotServletContextListener.ATTR_PUBNUB);
        if (obj instanceof Pubnub) {
            return (Pubnub) obj;
        }
        else {
            return null;
        }
    }
}
