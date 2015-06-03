import java.io.*;
import java.net.*;

public class MessagePasser {
	String hostName;
	int portNumber;
	public MessagePasser(String configuration_filename, String local_name){
	//The constructor will initialize buffers to hold incoming and 
	//outgoing messages to the rest of the nodes in the system
		//parse the conf_file
		//file should be checked to see if it was modified and
		//re-read before processing each send and receive method
		
		
		//setup sockets for all processes loop?
	}
	/* A helper function used in init(), or when the configuration file is modified */
	public void load_config()
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(conf_filename);
			Yaml yaml = new Yaml();
			Map<String, Object> data = (Map<String, Object>)yaml.load(fis);
			ArrayList<HashMap<String, Object> > config = (ArrayList<HashMap<String, Object> >)data.get("Configuration");
			for(HashMap<String, Object> mm : config)
			{
				String Name = (String)mm.get("Name");
				User uu = new User(Name);
				uu.setIp((String)mm.get("IP"));
				uu.setPort((Integer)mm.get("Port"));
				users.put(Name, uu);
			}
			if(!users.containsKey(local_name))
			{
				System.err.println("local_name: " + local_name + " isn't in " + conf_filename + ", please check again!");
				System.exit(1);
			}
			ArrayList<HashMap<String, Object> > send_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("SendRules");
			
			for(HashMap<String, Object> mm : send_rule_arr)
			{
				String action = (String)mm.get("Action");
				Rule r = new Rule(action);
				for(String key: mm.keySet())
				{
					if(key.equals("Src"))
						r.setSrc((String)mm.get(key));
					if(key.equals("Dest"))
						r.setDest((String)mm.get(key));
					if(key.equals("Kind"))
						r.setKind((String)mm.get(key));
					if(key.equals("ID"))
						r.setId((Integer)mm.get(key));
					if(key.equals("Nth"))
						r.setNth((Integer)mm.get(key));
					if(key.equals("EveryNth"))
						r.setEveryNth((Integer)mm.get(key));
				}
				SendRules.add(r);
			}

			ArrayList<HashMap<String, Object> > receive_rule_arr = (ArrayList<HashMap<String, Object> >)data.get("ReceiveRules");
			for(HashMap<String, Object> mm : receive_rule_arr)
			{
				String action = (String)mm.get("Action");
				Rule r = new Rule(action);
				for(String key: mm.keySet())
				{
					if(key.equals("Src"))
						r.setSrc((String)mm.get(key));
					if(key.equals("Dest"))
						r.setDest((String)mm.get(key));
					if(key.equals("Kind"))
						r.setKind((String)mm.get(key));
					if(key.equals("ID"))
						r.setId((Integer)mm.get(key));
					if(key.equals("Nth"))
						r.setNth((Integer)mm.get(key));
					if(key.equals("EveryNth"))
						r.setEveryNth((Integer)mm.get(key));
				}
				ReceiveRules.add(r);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(fis != null)fis.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
	void send(Message message){
		//set the sequence number of the message
		//Sequence Numbers should be non-reused, strictly incrementing integer values, starting at zero
		//check the message against any SendRules
		out.println(fromUser);
		//out denotes the printwriter for the particular process
		//fromUser is the string parsed from message
	}
	Message receive( ){
		//checking each received message against ReceiveRules
		fromServer = in.readLine(); // may block.  Doesn't have to.
		//fromServer is the string denoting msg from server
		//in denotes the BufferedReader for the particular process
		//storing them in an input queue
		//deliver a single message from the front of this input queue
		
	}
}