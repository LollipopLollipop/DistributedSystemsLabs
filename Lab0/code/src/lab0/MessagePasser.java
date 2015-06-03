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
import java.util.concurrent.atomic.AtomicInteger;

import org.yaml.snakeyaml.*;

/*
 * This is the functional class, in charge of handling sending and receiving messages.
 * Also it check rules and manage rules.
 */
public class MessagePasser {
	private boolean debug = true;
	private AtomicInteger seq_num = new AtomicInteger(-1);
	private final String conf_filename;
	private final String local_name;
	private HashMap<String, Node> nodes = new HashMap<String, Node>();
	private ArrayList<Rule> send_rules = new ArrayList<Rule>();
	private ArrayList<Rule> receive_rules = new ArrayList<Rule>();
	private BlockingQueue<Message> send_queue = new LinkedBlockingQueue<Message>();
	private Queue<Message> delayed_send_queue = new LinkedList<Message>();
	private Queue<Message> receive_queue = new LinkedList<Message>();
	private Queue<Message> delayed_receive_queue = new LinkedList<Message>();
	private ConcurrentLinkedQueue<BackgroundStreamListener> socket_queue = new ConcurrentLinkedQueue<BackgroundStreamListener>();	
	//ObjectInputStream in;
	//private HashMap<String, ObjectOutputStream> existed_sockets = new HashMap<String, ObjectOutputStream>();


	public MessagePasser(String configuration_filename, String local_name) {
		this.conf_filename = configuration_filename;
		this.local_name = local_name;
	}
	
	public void send(Message message) {
		message.set_seq_num(seq_num.addAndGet(1));
		Rule rule = send_rule_processing(message);		
		if(rule != null){
			if(debug){
				System.out.println(rule.get_action());
			}
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
				//message_copy.set_seq_num(seq_num.addAndGet(1));
				//setViaSocket(message_copy);
				send_queue.add(message_copy);
			}
			while(!delayed_send_queue.isEmpty()){
				//setViaSocket(send_delay_queue.poll());
				send_queue.add(delayed_send_queue.poll());
			}
		}
		else{
			//setViaSocket(message);
			send_queue.add(message);
			while(!delayed_send_queue.isEmpty()){
				//setViaSocket(send_delay_queue.poll());
				send_queue.add(delayed_send_queue.poll());
			}
		}
	}
	
	public Message receive() {
		if(receive_queue.isEmpty())
			return null;
		return receive_queue.poll();
	}
	
	public void parse_config() throws IOException
	{
		FileInputStream file_in = null;
		Yaml yaml = new Yaml();

		file_in = new FileInputStream(conf_filename);
		HashMap<String, Object> conf_data = (HashMap<String, Object>)yaml.load(file_in);
		ArrayList<HashMap<String, Object> > conf_nodes = (ArrayList<HashMap<String, Object> >)conf_data.get("configuration");
		for(HashMap<String, Object> conf_node : conf_nodes)
		{
			String name = (String)conf_node.get("name");
			if(debug){
				System.out.println("Read Users"+name);
			}
			Node node = new Node(name, (String)conf_node.get("ip"), (Integer)conf_node.get("port"));
			nodes.put(name, node);
		}
		
		if(nodes.containsKey(local_name))
		{
			//read send and receive rules
			read_rules(conf_data, true, send_rules);
			read_rules(conf_data, false, receive_rules);
			file_in.close();
		}
		else{
			System.out.println("your local host is not in conf env");
			return;

		}
	}
	
	/*
	 * Parse rules. 
	 * flag = true: parse send rules
	 * flag = false: parse receive rules
	 */
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
		if (orig_rules != null){
			//store rules read from conf_file to receive_rules array list
			for(HashMap<String, Object> rule_unit : orig_rules)
			{	
				String action = (String)rule_unit.get("action");
				Rule rule = new Rule(action);
				for(String key: rule_unit.keySet())
				{
					switch(key){
					//case "action": rule.set_action((String)rule_unit.get(key)); break;
					case "src": rule.set_src((String)rule_unit.get(key)); break;
					case "dest": rule.set_dst((String)rule_unit.get(key)); break;
					case "kind": rule.set_kind((String)rule_unit.get(key)); break;
					case "seqNum": rule.set_seq_num((Integer)rule_unit.get(key)); break;
					case "duplicate": rule.set_duplicate((String)rule_unit.get(key)); break;
					}	
				}
				rules_to_update.add(rule);	
			}
		}
	}
	
	/*
	 * Processing receive and send rulls.
	 */
	public Rule send_rule_processing(Message message)
	{
		for(Rule rule: send_rules)
		{
			if(rule.get_seq_num() > 0 && rule.get_seq_num() != message.get_seq_num())
				continue;
			if(rule.get_src() != null && !rule.get_src().equals(message.get_source()))
				continue;
			if(rule.get_dst() != null && !rule.get_dst().equals(message.get_dest()))
				continue;
			if(rule.get_kind() != null && !rule.get_kind().equals(message.get_kind()))
				continue;
			return rule;  
		}
		return null;
	}
	public Rule receive_rule_processing(Message message)
	{
		for(Rule rule: receive_rules)
		{
			if(rule.get_seq_num() > 0 && rule.get_seq_num() != message.get_seq_num())
				continue;
			if(rule.get_src() != null && !rule.get_src().equals(message.get_source()))
				continue;
			if(rule.get_dst() != null && !rule.get_dst().equals(message.get_dest()))
				continue;
			if(rule.get_kind() != null && !rule.get_kind().equals(message.get_kind()))
				continue;
			if(rule.get_duplicate() != null && !rule.get_duplicate().equals(message.get_duplicate()))
				continue;
			return rule; 
		}
		return null;
	}
	
	//Initialization.
	public void setup() throws IOException
	{
		//this.seq_num = new AtomicInteger(-1);
		nodes.clear();
		//Clear current rules
		send_rules.clear();
		receive_rules.clear();
		socket_queue.clear();
		
		//Empty current send and receive queue.
		send_queue.clear();
		receive_queue.clear();
		delayed_send_queue.clear();
		delayed_receive_queue.clear();
		
		//Reload new configuration
		parse_config(); 
	}
	
	//Interface
	public boolean get_debug(){return debug;}
	public BlockingQueue<Message> get_send_queue(){return send_queue;}
	public Queue<Message> get_receive_queue(){return receive_queue;}
	public Queue<Message> get_delayed_send_queue(){return delayed_send_queue;}
	public Queue<Message> get_delayed_receive_queue(){return delayed_receive_queue;}
	public HashMap<String, Node> get_users(){return nodes;}
	public AtomicInteger get_seq_num(){return seq_num;}
	public ConcurrentLinkedQueue<BackgroundStreamListener> get_all_sockets(){return socket_queue;}
	
}