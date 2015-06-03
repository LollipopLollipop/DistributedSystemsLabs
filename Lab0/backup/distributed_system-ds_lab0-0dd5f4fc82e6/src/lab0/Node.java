package lab0;

public class Node {
	private String name;
	private String ip;
	private int port;
	public Node(String Name, String IP, int Port)
	{
		this.name = Name;
		this.ip = IP;
		this.port = Port;
	}
	public String get_name(){return name;}
	public String get_ip(){return ip;}
	public int get_port(){return port;}
}
