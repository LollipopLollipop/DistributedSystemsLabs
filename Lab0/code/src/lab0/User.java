package lab0;

public class User {
	private String name;
	private String ip;
	private int port;
	public User(String Name, String IP, int Port)
	{
		name = Name;
		ip = IP;
		port = Port;
	}
	public String getName(){return name;}
	public String getIp(){return ip;}
	public int getPort(){return port;}
}