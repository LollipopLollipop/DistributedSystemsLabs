package lab1;
import java.io.Serializable;

public class Message implements Serializable {
	protected int seqNum;
	protected String src;
	protected String dest;
	protected String kind;
	protected Object data;
	protected String dupe;
	
	public Message(String src, String dest, String kind, Object data){
		this.src = src;
		this.dest = dest;
		this.kind = kind;
		this.data = data;
		this.seqNum = -1; // -1 represents not used message
		this.dupe = "false";
	}
	
	// These settors are used by MessagePasser.send, not your app
	
	//Interfaces - set value
	public void set_source(String source){
		this.src = source;
	}
	public void set_dest(String dest){
		this.dest = dest;
	}
	public void set_kind(String kind){
		this.kind = kind;
	}
	public void set_data(String data){
		this.data = data;
	}	
	public void set_seq_num(int sequenceNumber){
		this.seqNum = sequenceNumber;
	}
	public void set_duplicate(String dupe){
		this.dupe = dupe;
	}
	
	//Interfaces - get values
	public String get_source(){
		return this.src;
	}
	public String get_dest(){
		return this.dest;
	}
	public String get_kind(){
		return this.kind;
	}
	public Object get_data(){
		return this.data;
	}	
	public int get_seq_num(){
		return this.seqNum;
	}
	public String get_duplicate(){
		return this.dupe;
	}
	
	//toString
	/*
	public String toString()
	{
		return ("id:" + this.seqNum + ",source:" + this.src + ",destination:" + this.dest + ",kind:" + this.kind + ",data:" + this.data + ",duplication" + ((this.dupe==true)?"Yes":"No"));
	}*/
	
	//Copy
	public Message copy(){
		Message new_message = new Message(this.src,this.dest,this.kind,this.data);
		new_message.set_seq_num(this.seqNum);
		return new_message;	
		
	}

}