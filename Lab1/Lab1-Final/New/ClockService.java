package lab1;

public abstract class ClockService {

	public ClockService(ClockType model) {
		this.model = model;
        arrangeParts();
	}
	public ClockService() {

	}
 
    private void arrangeParts() {
        // Do one time processing here
    }
 
    // Do subclass level processing in this method
    protected abstract void construct();
 
    private ClockType model = null;
 
    public ClockType getModel() {
        return model;
    }
 
    public void setModel(ClockType model) {
        this.model = model;
    }
}
