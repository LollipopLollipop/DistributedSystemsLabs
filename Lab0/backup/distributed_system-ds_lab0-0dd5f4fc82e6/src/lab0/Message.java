package lab0;

import java.io.Serializable;

/*
 * This is a class of Message, containing attributes of Message and related interfaces.
 */
public class Message implements Serializable {
	private int seq_num;
	private String src;
	private String dest;
	private String kind;
	private Object data;
	private String dupe;
	
	public Message(String src, String dest, String kind, Object data){
		this.src = src;
		this.dest = dest;
		this.kind = kind;
		this.data = data;
		this.seq_num = -1; // -1 represents not used message
		this.dupe = null;
	}
	
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
	public void set_seq_num(int seq_num){
		this.seq_num = seq_num;
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
		return this.seq_num;
	}
	public String get_duplicate(){
		return this.dupe;
	}
	
	//Print message as format
	public String toString()
	{
		return ("This a message from" + src + "to" + dest + ".\n" + "Sequence Number:" + seq_num + ".\t  Kind:"+ kind + "\t Duplicate:" + dupe + "\n Data:" + data);
	}
	
	//Copy message when duplicate
	public Message copy(){
		Message new_message = new Message(this.src,this.dest,this.kind,this.data);
		return new_message;	
	}

}