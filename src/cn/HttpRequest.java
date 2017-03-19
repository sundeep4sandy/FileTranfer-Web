package cn;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public final class HttpRequest implements Runnable {

    final static String CRLF = "\r\n"; // return line feed
    Socket socket; // Constructor

    public HttpRequest(Socket sock) {
        this.socket = sock;
    }

  //write file  to the OutputStream
    private static void sendBytes(FileInputStream istream, OutputStream ostream) throws Exception {
        byte[] buff = new byte[1024];
        int bytes = 0;
        while ((bytes = istream.read(buff)) != -1) {
        	ostream.write(buff, 0, bytes);
        }
        ostream.close();
    }

    private static String contentType(String fileName) { //reads file name and gives the content type of the file
        if (fileName.endsWith(".htm") || fileName.endsWith(".html") || fileName.endsWith(".txt")) {
            return "text/html";
        } else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";

    }

    @Override
  // Implement the run() method of the Runnable interface.

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static String clientRequest(InputStream IS) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(IS)); //collects the request from client
            String requestLine = br.readLine(); // reads first line of the request, which contains filename
            System.out.println("request received from client");
            String headerLine;
            System.out.println(requestLine);
            while ((headerLine = br.readLine()).length() != 0) { //Reads header lines one by one
                System.out.println(headerLine);                  //Printing header lines
            }

          // Extract the filename from the request line.

            StringTokenizer tokens = new StringTokenizer(requestLine); //splits request line at spaces 
            tokens.nextToken(); // skip over the method, which should be "GET"

            String fileName = tokens.nextToken(); //collects filename from request line
            System.out.println("File requested is " + fileName.substring(1));
            fileName = "." + fileName;
            return fileName;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    // It is called from the run() in the try block
    private void processRequest() throws Exception {
        InputStream IS = socket.getInputStream(); // Get a reference to the socket's input and output streams.
        DataOutputStream Dataos = new DataOutputStream(socket.getOutputStream()); // gets output stream from socket
        String fileName = clientRequest(IS); // handles client request and returns file name
        FileInputStream fis = null; // Open the requested file.

        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);  //reads file into file input stream
        } catch (Exception e) {
            fileExists = false;  //assigns false if no file
        }
      // Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = "HTTP/1.1 200 ok" + CRLF;
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;  //reads file name and gives the content type of the file
        } else {
            statusLine = "HTTP/1.1 404 Not Found" + CRLF;
            contentTypeLine = "Content-type: html" + CRLF;
            entityBody = "<HTML>" 
                    + "<HEAD>"
                    + "<TITLE>Not Found</TITLE>"
                    + "</HEAD>"
                    + "<BODY>Not Found</BODY>"
                    + "</HTML>";
        }
        Dataos.writeBytes(statusLine);//writes status line to the OutputStream
        //writing server parameter to the OutputStream
        Dataos.writeBytes("TCP No Delay : " + socket.getTcpNoDelay() + CRLF);
        Dataos.writeBytes("Server Address : " + socket.getLocalSocketAddress() + CRLF);
        Dataos.writeBytes("server Port : " + socket.getLocalPort() + CRLF);
        Dataos.writeBytes("Time out : " + socket.getSoTimeout() + CRLF);
        Dataos.writeBytes("Server Name : " + socket.getLocalAddress().getHostName() + CRLF);
        Dataos.writeBytes(contentTypeLine);// Send the status line.
      // Send a blank line to indicate the end of the header lines.

        Dataos.writeBytes(CRLF);
      // Send the entity body.

        if (fileExists) {
            sendBytes(fis, socket.getOutputStream()); //write file content to OutputStream
            System.out.println("File Sent");
            fis.close();
        } else {
        	Dataos.writeBytes(entityBody);
        	Dataos.close();
            System.out.println("File not found");
        }
    }

}

// file checking used from https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/utils/AssetsFileGenerator.java