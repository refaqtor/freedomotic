package it.cicolella.grovesystem;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A plugin for the Openpicus Grove Nest by openpicus.com Product page Author
 * Mauro Cicolella - www.emmecilab.net
 *
 */
public class GroveSystem extends Protocol {

    private static ArrayList<Board> boards = null;
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private static int SOCKET_TIMEOUT = 1000;
    public final String UDP_SERVER_HOSTNAME = configuration.getStringProperty("udp-server-hostname", "192.168.1.100");
    public final int UDP_SERVER_PORT = configuration.getIntProperty("udp-server-port", 7331);
    public final String DELIMITER = configuration.getStringProperty("delimiter", ":");
    private int udpPort;
    private static UDPServer udpServer = null;

    /**
     * Initializations
     */
    public GroveSystem() {
        super("Openpicus Grove System", "/it.cicolella.grovesystem/openpicus-grove-system-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            int snmpPort;
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            udpPort = configuration.getTuples().getIntProperty(i, "udp-port", 5010);
            Board board = new Board(ipToQuery, udpPort);
            boards.add(board);
            // aggiungere alla struttura board 
            //UDPClient theClient = new UDPClient();
            //theClient.connectToServer();
            setDescription(getDescription() + " " + ipToQuery + ":" + udpPort);
        }
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            udpServer = new UDPServer(this);
            udpServer.start();
        } catch (IOException iOException) {
            Freedomotic.logger.severe("Error during UDP server creation " + iOException.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //release resources
        udpServer.interrupt();
        udpServer = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Openpicus Grove System"));
    }

    @Override
    protected void onRun() {
    }

    public void sendEvent(String objectAddress, String eventProperty, String eventValue) {
        ProtocolRead event = new ProtocolRead(this, "openpicus-grove-system", objectAddress);
        event.addProperty("sensor.type", eventProperty);
        event.addProperty("sensor.value", eventValue);
        if (eventProperty.equalsIgnoreCase("temperature")) {
            event.addProperty("object.class", "Thermostat");
        } else if (eventProperty.equalsIgnoreCase("luminosity")) {
            event.addProperty("object.class", "Light Sensor");
        }
        // EXTEND WITH MORE SENSORS
        event.addProperty("object.name", objectAddress);
        //publish the event on the messaging bus
        this.notifyEvent(event);
        //System.out.println("Sending eventProperty " + eventProperty); // FOR DEBUG
    }

    private void initializeGroveSystem(String UDPServerAddress, String UDPServerPort) {
        int PACKETSIZE = 100 ;
        DatagramSocket socket = null;
        

        try {
            // Convert the arguments first, to ensure that they are valid
            //InetAddress host = InetAddress.getByName(UDPargs[0]);
            //int port = Integer.parseInt(args[1]);
            // Construct the socket
            socket = new DatagramSocket();
            // Construct the datagram packet
            byte[] data = "Hello Server".getBytes();
            //DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
            // Send it
            //socket.send(packet);
            // Set a receive timeout, 2000 milliseconds
            //socket.setSoTimeout(2000);
            // Prepare the packet for receive
            //packet.setData(new byte[PACKETSIZE]);
            // Wait for a response from the server
            //socket.receive(packet);
            // Print the response
            //System.out.println(new String(packet.getData()));

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
    }

    // create message to send to the board
    // this part must be changed to relect board protocol
    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}