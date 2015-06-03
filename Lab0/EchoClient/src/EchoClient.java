import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (
            Socket echoSocket = new Socket(hostName, portNumber);
        	//gets the socket's output stream and opens a PrintWriter on it
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true);
        	//gets the socket's input stream and opens a BufferedReader on it
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
            	//The loop reads a line at a time from the standard input stream 
            	//and immediately sends it to the server by writing it to 
            	//the PrintWriter connected to the socket
                out.println(userInput);
                //reads a line of information from the BufferedReader connected to the socket
                System.out.println("echo: " + in.readLine());
                /*The while loop then terminates, and the Java runtime automatically 
                 * closes the readers and writers connected to the socket and 
                 * to the standard input stream, and it closes the socket connection 
                 * to the server. The Java runtime closes these resources automatically 
                 * because they were created in the try-with-resources statement. 
                 * The Java runtime closes these resources in reverse order that they were created. */
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 
    }
}
