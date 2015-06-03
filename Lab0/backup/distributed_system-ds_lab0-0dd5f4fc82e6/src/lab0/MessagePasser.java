package lab0;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.yaml.snakeyaml.*;

public class MessagePasser {
	private final String conf_filename;
	private final String local_name;
	private int seq_num = 0;//keep track of seq_num of msg
	private HashMap<String, Node> nodes = new HashMap<String, Node>();//denotes all nodes involved
	private ArrayList<Rule> send_rules = new ArrayList<Rule>();
	private ArrayList<Rule> receive_rules = new ArrayList<Rule>();
	
	private BlockingQueue<Message> send_queue = new LinkedBlockingQueue<Message>();
	private Queue<Message> delayed_send_queue = new LinkedList<Message>();
	private Queue<Message> receive_queue = new LinkedList<Message>();
	private Queue<Message> delayed_receive_queue = new LinkedList<Message>();
	
	private ConcurrentLinkedQueue<BackgroundStreamListener> socket_queue = new ConcurrentLinkedQueue<BackgroundStreamListener>();
	
	//ObjectInputStream in;
	//store sockets for all communications
	//private HashMap<String, ObjectOutputStream> existed_sockets = new HashMap<String, ObjectOutputStream>();
	public int get_seq_num(){return seq_num;}
	public HashMap<String, Node> get_all_nodes(){return nodes;}
	public BlockingQueue<Message> get_send_queue(){return send_queue;}
	public Queue<Message> get_receive_queue(){return receive_queue;}
	public Queue<Message> get_delayed_send_queue(){return delayed_send_queue;}
	public Queue<Message> get_delayed_receive_queue(){return delayed_receive_queue;}
	public ConcurrentLinkedQueue<BackgroundStreamListener> get_all_sockets(){return socket_queue;}
	
	public MessagePasser(String configuration_filename, String local_name) {
		this.conf_filename = configuration_filename;
		this.local_name = local_name;
		//setup();
	}
	
	//called every time conf file updated
	public void setup()
	{
		
		this.seq_num = 0;
		send_queue.clear();
		delayed_send_queue.clear();
		receive_queue.clear();
		delayed_receive_queue.clear();
		nodes.clear();
		send_rules.clear();
		receive_rules.clear();
		socket_queue.clear();
		//existed_sockets.clear();
		
		parse_config();
		 
	}
	//parse conf_file to store in nodes and send_rules/receive_rules
	public void parse_config()
	{
		FileInputStream file_in = null;
		try
		{
			file_in = new FileInputStream(conf_filename);
			Yaml conf_file = new Yaml();
			HashMap<String, Object> conf_data = (HashMap<String, Object>)conf_file.load(file_in);
			ArrayList<HashMap<String, Object> > conf = (ArrayList<HashMap<String, Object> >)conf_data.get("configuration");
			for(HashMap<String, Object> conf_unit : conf)
			{
				String name = (String)conf_unit.get("name");
				Node node = new Node(name, (String)conf_unit.get("ip"), (Integer)conf_unit.get("port"));
				nodes.put(name, node);
			}
			if(!nodes.containsKey(local_name))
			{
				System.err.println("your local host is not in the conf file");
				System.exit(1);
			}
			read_rules(conf_data, true, send_rules);
			read_rules(conf_data, false, receive_rules);
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(file_in != null)
					file_in.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
	public void read_rules(HashMap<String, Object> conf_data, boolean flag, ArrayList<Rule> rules_to_update){
		String rule_name = null;
		//ArrayList<Rule> rules_to_return = null;
		if (flag == true){
			rule_name = "sendRules";
			//rules_to_return = send_rules;
		}
		else {
			rule_name = "receiveRules";
			//rules_to_return = receive_rules;
		}
		
		ArrayList<HashMap<String, Object>> orig_rules = (ArrayList<HashMap<String, Object> >)conf_data.get(rule_name);
		//store rules read from conf_file to receive_rules array list
		for(HashMap<String, Object> rule_unit : orig_rules)
		{	
			for(String key: rule_unit.keySet())
			{
				Rule rule = new Rule();
				switch(key){
				case "action": rule.set_action((String)rule_unit.get(key)); break;
				case "src": rule.set_src((String)rule_unit.get(key)); break;
				case "dest": rule.set_dst((String)rule_unit.get(key)); break;
				case "kind": rule.set_kind((String)rule_unit.get(key)); break;
				case "seqNum": rule.set_seq_num((Integer)rule_unit.get(key)); break;
				case "duplicate": rule.set_duplicate((String)rule_unit.get(key)); break;
				}
				if(rule.get_action()!=null)
					rules_to_update.add(rule);
				//if(key.equals("Nth"))
					//r.setNth((Integer)r_rule.get(key));
				//if(key.equals("EveryNth"))
					//r.setEveryNth((Integer)r_rule.get(key));
			}
			
		}
	}
	public void send(Message message) {
		message.set_seq_num(seq_num++);
		Rule rule = send_rule_processing(message);//check and return matched rule
		if(rule==null){
			//None matched 
			//setViaSocket(message);
			send_queue.add(message);
			while(!delayed_send_queue.isEmpty()){
				//setViaSocket(send_delay_queue.poll());
				send_queue.add(delayed_send_queue.poll());
			}
		}
		else{
			//System.out.println(rule.getAction());
			if(rule.get_action().equals("drop")){
				return; // irgnore this message
			}
			else if(rule.get_action().equals("delay")){
				delayed_send_queue.add(message);
				return;
			}
			//setViaSocket(message);
			message.set_duplicate("true");
			send_queue.add(message);
			if(rule.get_action().equals("duplicate")){
				//System.out.println("duplicate");
				Message message_copy = message.copy();
				message_copy.set_seq_num(seq_num++);
				//setViaSocket(message_copy);
				send_queue.add(message_copy);
			}
			while(!delayed_send_queue.isEmpty()){
				//setViaSocket(send_delay_queue.poll());
				send_queue.add(delayed_send_queue.poll());
			}
		}
	}
	
	public Message receive() {
		//return the head of the receive queue if non empty
		if(receive_queue.isEmpty())
			return null;
		return receive_queue.poll();
	} 
	
	public Rule send_rule_processing(Message message)
	{
		for(Rule rule: send_rules)
		{
			if(rule.get_src() != null && !rule.get_src().equals(message.get_source()))
				continue;
			else if(rule.get_dst() != null && !rule.get_dst().equals(message.get_dest()))
				continue;
			else if(rule.get_kind() != null && !rule.get_kind().equals(message.get_kind()))
				continue;
			else if(rule.get_seq_num() > 0 && rule.get_seq_num() != message.get_seq_num())
				continue;

			return rule;  
		}
		return null;
	}
	public Rule receive_rule_processing(Message message)
	{
		for(Rule rule: receive_rules)
		{
			if(rule.get_src() != null && !rule.get_src().equals(message.get_source()))
				continue;
			if(rule.get_dst() != null && !rule.get_dst().equals(message.get_dest()))
				continue;
			else if(rule.get_kind() != null && !rule.get_kind().equals(message.get_kind()))
				continue;
			else if(rule.get_seq_num() > 0 && rule.get_seq_num() != message.get_seq_num())
				continue;
			else if(rule.get_duplicate() != null && !rule.get_duplicate().equals(message.get_duplicate()))
				continue;
			return rule;  //Matched
		}
		return null;// No rule matched
	}
	
}