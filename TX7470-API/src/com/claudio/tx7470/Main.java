package com.claudio.tx7470;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {

    private static String ip; //Ip Of Transmitter TX7470
    private static Socket clientSocket; //Socket/Port of IP Transmitter used to communicate, default 3700
    private static BufferedReader in_answer; //Buffer to Read Responses from the Transmitter
    private static PrintWriter writer;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        //Receive arguments of line command executing by Jar file
        String arg1 = args[0];
        String arg2 = args[1];
        String arg3 = args[2];
        String arg4 = args[3];
        String arg5 = args[4];
        String arg6 = args[5];

        //Convert the arguments into it's respective types
        ip = arg1; //IP of Transmitter
        Integer pager = Integer.valueOf(arg2); //Pager Number
        String message = arg3; //Pager Message Text, Supported Caracters: https://pt.wikipedia.org/wiki/ISO/IEC_8859-1
        String type = arg4; //Pager Type, consonant the Pager Hardware Used, Example: RX-SP4 = AlphaCoaster, reference: https://paging-systems.readme.io/docs/netpage-service#section-pager-sub-element
        int system_id = Integer.valueOf(arg5); //System ID Defined on the TX-7470
        int times = Integer.valueOf(arg6); //Number of times to repeat the message, if none use just "1";

        initCommunication(ip, pager, message, type, system_id); //Call of communication
        System.out.print("\n" + "Sent (1): \n" + "IP:" + ip + "\n" + "Pager Number: " + pager + "\n" + "Message: " + message + "\n" + "Type of Pager: " + type + "\n" + "System ID: " + system_id + "\n");

        /**
         * Repeat system used to repeat the messages the amount of times choosen
         */
        if (!arg6.equals("0")) {
            int count = 0;
            //Repeat System
            //new RepeatSystem(ip, pager, message, type, system_id, times).start();
            while (true) {

                if (count >= (times - 1)) {
                    break;
                }

                try {
                    Thread.sleep(31000);
                }  catch (InterruptedException e) {
                    e.printStackTrace();
                }

                initCommunication(ip, pager, message, type, system_id); //Call of communication
                System.out.print("\n" + "Sent" + " (" + (count +1) +"):" +  "\n" + "IP:" + ip + "\n" + "Pager Number: " + pager + "\n" + "Message: " + message + "\n" + "Type of Pager: " + type + "\n" + "System ID: " + system_id + "\n");

                count++;
            }
        } else {
            System.out.print("Error: Message sent, the last argument cannot be 0");
        }

    }

    private static void initCommunication(String ip, int pager, String message, String type, int system_id) {
        try {
            clientSocket = new Socket(ip,3700); //Connection to TX7470 with Default port: 3700

            //Read Response Object from Transmitter
            in_answer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


            //Write Response Object to Transmitter
            writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while((line = in_answer.readLine()) != null)
            {
                System.out.println("\nAnswer From Transmitter: " + line); //Result codes reference: https://paging-systems.readme.io/docs/result-codes
                if (line.contains("<LRSN"))
                {
                    servicesXML(clientSocket);
                }

                if (line.contains("<LoginAck ret=\"0\""))
                {
                    callPager(clientSocket, pager, message, type, system_id);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /*
     * Iniate the communication services on the TX7470
     */
    public static void servicesXML(Socket localsocket) throws IOException
    {
        writer.write("<Login services=\"NetPage:2.0;\" /> \n");
        writer.flush();
    }

    /*
     * Call the Pager choosen through the XML message sent to the TX7470
     */
    public static void callPager(Socket localsocket, int pager, String message, String type, int system_id) throws IOException
    {
        String local = "<PageRequest id=\"42\">";
        local += "<Pager Type=\"" + type + "\" ID=\""+ pager + "\" SystemID=\"" + system_id + "\" />";
        local += "<Message>"+ message +"</Message>";
        local += "</PageRequest> \n";
        writer.write(local);
        writer.flush();

        stopRunning();
    }

    /*
     * Close connection of the socket
     */
    public static void stopRunning()
    {
        try {
            clientSocket.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
