import java.security.Timestamp;

public class Message {
	int senderId;
	String message;
    int resource;
    int timestamp;
	public Message(int senderId, int resourceId, String message, int timestamp) {
		this.message = message;
		this.senderId = senderId;
		this.resource = resourceId;
		this.timestamp = timestamp;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getResource() {
		return resource;
	}

	public void setResource(int resourceId) {
		this.resource = resourceId;
	}
	
	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	
}
