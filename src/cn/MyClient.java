package cn;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

final class MyClient {

    final static String CRLF = "\r\n";
    static Socket sock;

    public static void main(String... args) throws Exception {
        BufferedReader br = null;
        DataOutputStream Dataos = null;
        try {

            sock = new Socket("localhost", 2016); //connects client to given IP and port
            double stime = System.nanoTime(); // stime is the start time of it
            Dataos = new DataOutputStream(sock.getOutputStream());//getting output stream
            double t1 = System.nanoTime() - stime;
            Scanner scan = new Scanner(System.in);
            System.out.println("request the file");
            String request = scan.nextLine();// reads file name
            //writting client request and client parameters to output stream
            Dataos.writeBytes("GET /" + request + " HTTP/1.1" + CRLF);
            Dataos.writeBytes("No Delay(TCP) : " + sock.getTcpNoDelay() + CRLF);
            Dataos.writeBytes("Client Address : " + sock.getLocalSocketAddress() + CRLF);
            Dataos.writeBytes("Port of the client : " + sock.getLocalPort() + CRLF);
            Dataos.writeBytes("Time out : " + sock.getSoTimeout() + CRLF);
            Dataos.writeBytes("Client Name : " +sock.getLocalAddress().getHostName() + CRLF);
            Dataos.writeBytes(CRLF);
            Dataos.flush();
            sock.shutdownOutput();
            stime = System.nanoTime();
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));//reads server response
            double t2 = System.nanoTime() - stime;
            StringBuilder response = new StringBuilder();
          // Get and display the header lines.

            String HeaderLines = "";
            String serverresponse = br.readLine(); // first line of server response which contains status code
            while ((HeaderLines = br.readLine()) != null) {
                response.append(HeaderLines);
                response.append("\n");
            }
            StringTokenizer token = new StringTokenizer(serverresponse);
            token.nextToken();
            System.out.println("Status Code : " + token.nextToken());
            System.out.println(serverresponse);
            System.out.println(response);// printing server response
            System.out.println("Round trip time : " + ((t1 + t2) / 1000000000) + "seconds"); //  round trip time

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            br.close();//closing streams and socket
            Dataos.close();
            sock.close();
        }
    }

}