package collectors;

import java.io.Serializable;

public class CollectorBase implements Serializable {

	private boolean active = false;

	public boolean isActive() { return active; }

	public void setActive(boolean active) { this.active = active; }
	
	
}
