package net.app.izot;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.pubnub.api.Pubnub;

public class IzotServletContextListener implements ServletContextListener {
    public static final String PUB_KEY = "pub-c-b2a4b991-340e-4168-9961-057d48f97e5c";

    public static final String SUB_KEY = "sub-c-c41ca018-d894-11e3-a226-02ee2ddab7fe";
    
    public static final String ATTR_PUBNUB = "pubnub";

    private Pubnub pubnub = null;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        pubnub = new Pubnub(PUB_KEY, SUB_KEY, false);
        event.getServletContext().setAttribute(ATTR_PUBNUB, pubnub);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener destroyed");
        if (pubnub != null) {
            pubnub.shutdown();
        }
    }
}