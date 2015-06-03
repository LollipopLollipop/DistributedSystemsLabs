import java.io.*;
import java.net.*;

public class Message implements Serializable {
  public Message(String dest, String kind, Object data);
  // These settors are used by MessagePasser.send, not your app
  public void set_source(String source);
  public void set_seqNum(int sequenceNumber);
  public void set_duplicate(Boolean dupe);
 // other accessors, toString, etc as needed
}