package lab3;

public class Node {
	private String name;
	private String ip;
	private int port;
	public Node(String name, String ip, int port)
	{
		this.name = name;
		this.ip = ip;
		this.port = port;
	}
	public String get_name(){return name;}
	public String get_ip(){return ip;}
	public int get_port(){return port;}
}