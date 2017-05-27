package collectors;

import java.io.Serializable;

public class CollectorBase implements Serializable {
	private static final long serialVersionUID = 6418211605960262874L;

	boolean active = false;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	
}
