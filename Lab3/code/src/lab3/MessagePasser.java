package lab3;

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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	private int local_id = 0;
	private LinkedHashMap<String, Node> nodes = new LinkedHashMap<String, Node>();
	private LinkedHashMap<String, Integer> node_ids = new LinkedHashMap<String, Integer>();
	private HashMap<String, HashSet<String>> groups = new HashMap<String,HashSet<String>>();
	private ArrayList<MulticastMessage> received_msgs = new ArrayList<MulticastMessage>();
	private ArrayList<Rule> send_rules = new ArrayList<Rule>();
	private ArrayList<Rule> receive_rules = new ArrayList<Rule>();
	private BlockingQueue<Message> send_queue = new LinkedBlockingQueue<Message>();
	private Queue<Message> delayed_send_queue = new LinkedList<Message>();
	private Queue<Message> receive_queue = new LinkedList<Message>();
	private Queue<Message> delayed_receive_queue = new LinkedList<Message>();
	private ConcurrentLinkedQueue<MulticastMessage> hold_back_queue_1 = new ConcurrentLinkedQueue<MulticastMessage>();
	private ConcurrentLinkedQueue<MulticastMessage> hold_back_queue_2 = new ConcurrentLinkedQueue<MulticastMessage>();
	private ConcurrentLinkedQueue<BackgroundStreamListener> socket_queue = new ConcurrentLinkedQueue<BackgroundStreamListener>();
	//private ConcurrentLinkedQueue<LoggerStreamListener> loggers_queue = new ConcurrentLinkedQueue<LoggerStreamListener>();
	private HashMap<String, Integer> received_ack_msgs = new HashMap<String, Integer>();
	private Lock lock = new ReentrantLock();
	private Condition not_in_critical_section = lock.newCondition();
	private boolean voted = false;
	private BlockingQueue<Message> request_release_queue = new LinkedBlockingQueue<Message>();
	private AtomicInteger send_count=new AtomicInteger(0);
	private AtomicInteger receive_count= new AtomicInteger(0);
	/*-1 stands for not in critical section 
	 * 0 stands for waiting to enter critical section 
	 * 1 stands for currently in critical section
	 */
	private int in_critical_section = -1;
	//private HashMap<String, ObjectOutputStream> existed_sockets = new HashMap<String, ObjectOutputStream>();
	
	private String clock_type = "";
	private ClockService clock_service = null;
	
	//ObjectInputStream in;
	//private HashMap<String, ObjectOutputStream> existed_sockets = new HashMap<String, ObjectOutputStream>();

	public int flag_use = 0;

	public MessagePasser(String configuration_filename, String local_name) {
		this.conf_filename = configuration_filename;
		this.local_name = local_name;
	}
	
	public void send(Message message, boolean is_multicasted) {
		//System.out.println("sending");
		if(!is_multicasted)
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
				//message_copy.set_timestamp((Object)this.clock_service.update(1));
				this.clock_service.update(1);
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
	public void multicast(MulticastMessage message) {
		//System.out.println(target_group);
		//send universally to every node
		message.set_seq_num(seq_num.addAndGet(1));
		for (String node_key : nodes.keySet()) {
			MulticastMessage msg = message.copy();
			msg.set_dest(node_key);
			send(msg, true);
			System.out.println("message multicast to "+node_key);
		}
		
	}
	public void sent_to_group(Message message, int group_id){
		message.set_seq_num(seq_num.addAndGet(1));
		for (String node : groups.get("Group"+Integer.toString(group_id))) {
			Message message_copy = message.copy();
			message_copy.set_dest(node);
			send(message_copy, true);
			send_count.addAndGet(1);
			System.out.println("Message send to "+node+" in group " + group_id);
		}
	}
	public void send_to_all(TimeStampedMessage message){
		for (String node_key : nodes.keySet()) {
			TimeStampedMessage msg = message.copy();
			msg.set_dest(node_key);
			if(node_key.equals(message.get_dest()))
				msg.set_sent_to_me(true);
			send(msg, false);
			
			System.out.println("send to all: message sent to "+node_key);
		}
	}
	
	public void request(){
		lock.lock();
		if(this.flag_use == 1){
			lock.unlock();
			System.out.println("Action forbidden...Request already made.");
			return;
		}
		if(in_critical_section!=-1){
			System.out.println("Action forbidden...Request already made.");
			return;
		}
		in_critical_section = 0;
		lock.unlock();
		Message req_msg = 
				new Message(local_name, null, "REQUEST", null);
				
		sent_to_group(req_msg, local_id);
		//System.out.println("Set it to 1");
		this.flag_use = 1;
		/*lock.lock();
		while(in_critical_section!=1){
			try {
				not_in_critical_section.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		lock.unlock();
		*/
		//System.out.println("Enter critical section...hoho");
	}
	public void release(){
		lock.lock();
		if(this.flag_use == 1){
			System.out.println("Action forbidened...not in critical section.");
			lock.unlock();
			return;
		}
		if(in_critical_section!=1){
			System.out.println("Action forbidened...not in critical section.");
			return;
		}
		in_critical_section=-1;
		lock.unlock();
		Message release_msg = 
				new Message(local_name, null, "RELEASE", null);
				
		sent_to_group(release_msg, local_id);
		//System.out.println("Exit critical section...see you next time");
	}
	public void display_status(){
		lock.lock();
		String status = null;
		if(in_critical_section==-1){
			status = "Outside critical section";
		}
		else if(in_critical_section==0){
			status = "Currently requesting critical section";
		}
		else{
			status = "Inside critical section";
		}
		System.out.println(status);
		if(voted)
			System.out.println("Already voted");
		else
			System.out.println("Not voted");
		lock.unlock();
	}
	public void display_msg_record(){
		System.out.println(this.send_count+" messages sent");
		System.out.println(this.receive_count+" messages received");
	}
	public void parse_config() throws IOException
	{
		FileInputStream file_in = null;
		Yaml yaml = new Yaml();

		file_in = new FileInputStream(conf_filename);
		HashMap<String, Object> conf_data = (HashMap<String, Object>)yaml.load(file_in);
		
		int i = 0;
		//read in groups 
		ArrayList<HashMap<String, Object> > group_list = (ArrayList<HashMap<String, Object> >)conf_data.get("groups");
		for(HashMap<String, Object> group_unit : group_list)
		{
			String group_name = (String)group_unit.get("name");
			if(debug){
				System.out.println("Read Group "+group_name);
			}
			ArrayList<String> members = (ArrayList<String>)group_unit.get("members");
			HashSet<String> members_set = new HashSet<String>();
			for(String member : members){
				System.out.println(member);
				members_set.add(member);
			}
			groups.put(group_name, members_set);
		}
		//read in participating processes
		ArrayList<HashMap<String, Object> > conf_nodes = (ArrayList<HashMap<String, Object> >)conf_data.get("configuration");
		for(HashMap<String, Object> conf_node : conf_nodes)
		{
			String name = (String)conf_node.get("name");
			if(debug){
				System.out.println("Read Users "+name);
			}
			Node node = new Node(name, (String)conf_node.get("ip"), (Integer)conf_node.get("port"));
			nodes.put(name, node);
			node_ids.put(name,i);
			if(name.equals(local_name)){
				local_id = i;
			}
			i++;
		}
		System.out.println("Your local id is " + local_id);
		
		if(clock_service == null){
			//read in clock type
			clock_type = ((ArrayList<String>) conf_data.get("clockType")).get(0);
			clock_service = new ClockFactory(local_id,nodes.size()).buildClock(ClockType.valueOf(clock_type));
		}
		
		if(nodes.containsKey(local_name))
		{
			//read send and receive rules
			read_rules(conf_data, true, send_rules);
			read_rules(conf_data, false, receive_rules);
			file_in.close();
		}
		else{
			System.out.println("Your local host is not in conf env");
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
		hold_back_queue_1.clear();
		hold_back_queue_2.clear();
		
		//Reload new configuration
		parse_config(); 
		
	}
	
	//Interface
	public boolean get_debug(){return debug;}
	public BlockingQueue<Message> get_send_queue(){return send_queue;}
	//public HashMap<String, ObjectOutputStream> get_existed_sockets(){return existed_sockets;}
	public Queue<Message> get_receive_queue(){return receive_queue;}
	public ConcurrentLinkedQueue<MulticastMessage> get_hold_back_queue_1(){return hold_back_queue_1;}
	public ConcurrentLinkedQueue<MulticastMessage> get_hold_back_queue_2(){return hold_back_queue_2;}
	public Queue<Message> get_delayed_send_queue(){return delayed_send_queue;}
	public Queue<Message> get_delayed_receive_queue(){return delayed_receive_queue;}
	public LinkedHashMap<String, Node> get_users(){return nodes;}
	public LinkedHashMap<String, Integer> get_node_ids(){return node_ids;}
	public AtomicInteger get_seq_num(){return seq_num;}
	public ConcurrentLinkedQueue<BackgroundStreamListener> get_all_sockets(){return socket_queue;}
	//public ConcurrentLinkedQueue<LoggerStreamListener> get_all_loggers(){return loggers_queue;}
	@SuppressWarnings("unchecked")
	//public ClockService<ClockType> get_clock_service(){return (ClockService<ClockType>)clock_service;}
	public ClockService get_clock_service(){return this.clock_service;}
	public String get_clock_service_string(){return this.clock_type;}
	public HashMap<String, HashSet<String>> get_groups(){return this.groups;}
	public String get_local_name(){return this.local_name;}
	public int get_local_id(){return this.local_id;}
	public ArrayList<MulticastMessage> get_received_msgs(){return this.received_msgs;}
	public HashMap<String, Integer> get_received_ack_msgs(){return this.received_ack_msgs;}
	public int get_critical_section_status(){return this.in_critical_section;}
	public void set_critical_section_status(int d){this.in_critical_section = d;}
	public boolean get_voted_status(){return voted;}
	public void set_voted_status(boolean flag){this.voted=flag;}
	public Condition get_not_in_critical_section_condition(){return not_in_critical_section;}
	public Lock get_lock(){return this.lock;}
	public BlockingQueue<Message> get_request_release_queue(){return this.request_release_queue;}
	public AtomicInteger get_receive_count(){return this.receive_count;}
	public void set_receive_count(int d){this.receive_count=new AtomicInteger(d);}
	public AtomicInteger get_send_count(){return this.send_count;}
	public void set_send_count(int d){this.send_count=new AtomicInteger(d);}

}