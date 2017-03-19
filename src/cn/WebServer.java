package cn;

import java.net.ServerSocket;
import java.net.Socket;

public final class WebServer {

    public static void main(String[] args) throws Exception {
         ServerSocket servercon = new ServerSocket(2016);
       System.out.println("server waiting for client..");
        while (true) {
            Socket soc = servercon.accept();//Accepts client trying to connect
            System.out.println("Client has been connected to server");
          // Construct an object to process the HTTP request message.
          //HttpRequest request = new HttpRequest( ? );
  
            HttpRequest req = new HttpRequest(soc);
          // Create a new thread to process the request.
//Thread thread = new Thread(request);

            Thread thread = new Thread(req);
          //starting thread
            thread.start(); 
     
    }
    }

}