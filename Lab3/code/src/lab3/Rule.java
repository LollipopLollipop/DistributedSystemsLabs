package lab3;

public class Rule {
	private String action;
	private String src = null;
	private String dest = null;
	private String kind = null;
	private int seq_num = -1;
	private String duplicate = null;
	public Rule(String action)
	{
		this.action = action;
	}
	public Rule()
	{
	}
	public void set_action(String action){this.action = action;}
	public void set_src(String src){this.src = src;}
	public void set_dst(String dest){this.dest = dest;}
	public void set_kind(String kind){this.kind = kind;}
	public void set_seq_num(int seq_num){this.seq_num = seq_num;}
	public void set_duplicate(String duplicate){this.duplicate = duplicate;}
	
	public String get_action(){return action;}
	public String get_src(){return src;}
	public String get_dst(){return dest;}
	public String get_kind(){return kind;}
	public int get_seq_num(){return seq_num;}
	public String get_duplicate(){return duplicate;}

}