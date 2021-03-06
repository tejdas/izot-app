package net.izot.bridge;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class IzotBridgeDriver {
    public static final String DEFAULT_LISTEN_PORT = "5005";

    public static final String DEFAULT_PUBLISH_CHANNEL = "HelloWorldTej";

    public static int listenPort = Integer.parseInt(DEFAULT_LISTEN_PORT);

    public static String publishChannel = DEFAULT_PUBLISH_CHANNEL;

    private static final Listener listener = new Listener();

    private static final HelpFormatter formatter = new HelpFormatter();

    private static volatile boolean isInitialized = false;

    private static final class ShutdownHook extends Thread {
        @Override
        public void run() {
            IzotBridgeDriver.shutdown();
        }
    }

    public static void shutdown() {
        if (isInitialized) {
            IzotConnectionFactory.shutdown();
            listener.shutdown();
            PubnubContext.shutdown();
        }
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("listenPort", true,
                "Izot Bridge TCP listen port; default: " + DEFAULT_LISTEN_PORT);
        options.addOption("publishChannel", true,
                "Pubnub publish channel; default: " + DEFAULT_PUBLISH_CHANNEL);
        options.addOption("help", false, "help text");
        if (args.length != 0) {
            CommandLineParser parser = new BasicParser();
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("help")) {
                formatter.printHelp("IzotBridgeDriver", options);
                return;
            }

            String listenPortVal = cl.getOptionValue("listenPort",
                    DEFAULT_LISTEN_PORT);
            publishChannel = cl.getOptionValue("publishChannel",
                    DEFAULT_PUBLISH_CHANNEL);
            listenPort = Integer.parseInt(listenPortVal);
        }
        System.out.println("Listen port: " + listenPort + " Pubnub publishChannel: " + publishChannel);
        initialize();
    }

	private static void initialize() {
		PubnubContext.initialize();
        IzotConnectionFactory.initialize();
        listener.start(listenPort);
        isInitialized = true;
        final ShutdownHook sh = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(sh);
	}
}
