package lab2;

import java.io.Serializable;


public abstract class ClockService<T> implements Serializable{

	public ClockService(ClockType model) {
		this.model = model;
	}
	public ClockService() {

	}
    public T update(int d){
		return null;
	}
    public T get_timestamp(){
		return null;
    	
    }
    public String get_timestamp_string(){
		return null;
    	
    }
    
    public void check_and_update(TimeStampedMessage received_msg, int d)
   	{}

    // Do subclass level processing in this method
    protected abstract void construct();
 
    private ClockType model = null;
 
    public ClockType get_clock_type() {
        return model;
    }
 
    public void set_clock_type(ClockType model) {
        this.model = model;
    }
    
	public int compare(Object timestamp_1, Object timestamp_2) {
		// TODO Auto-generated method stub
		return 0;
	}

}
