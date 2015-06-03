package lab3;

import java.io.Serializable;
import java.util.Arrays;

public class MulticastMessage extends TimeStampedMessage{
	private String target_group;
	private String orig_src;

	public MulticastMessage(String src, String dest, String kind, Object data, Object timestamp, String clock_type, String target_group, String orig_src)
	{
		super(src, dest, kind, data, timestamp, clock_type);
		this.target_group = target_group;
		this.orig_src = orig_src;
	}

	public String get_target_group(){return target_group;}
	public String get_orig_src(){return orig_src;}
	
	public String toString()
	{
		return (super.toString() + "|target group:" + this.target_group + "|original sender:" + this.orig_src);
	}
	public String toOrigString()
	{
		return (super.toOrigString() + "|target group:" + this.target_group + "|original sender:" + this.orig_src);
	}
	
	public void set_target_group(String target_group){this.target_group = target_group;}
	public void set_orig_src(String orig_src){this.orig_src = orig_src;}
	
	
	public MulticastMessage copy(){
		MulticastMessage new_message = new MulticastMessage(super.src,super.dest,super.kind,super.data, super.timestamp,super.clock_type, this.target_group, this.orig_src);
		new_message.set_seq_num(super.seqNum);
		return new_message;	
	}
	
}


