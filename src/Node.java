import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Iterator;
public class Node implements Runnable {
	LinkedList<Message> queue;
	LinkedList<Message> request;
	HashMap<Integer, Node> nodes = DistributedSystem.nodes;
	int id;
	int timestamp;
	int state;
	boolean isOnline;
	boolean sendElection;
	boolean sendOk;
	boolean getElection;
	boolean getOk;
	boolean sendMessage;// simple message send
	boolean getMessage; // simple message get
	boolean isMaster;
	int coordinatorId;
	public final String ELECTION_MESSAGE = "Election";
	public final String OK_MESSAGE = "OK";
	public final String COORDINATOR_MESSAGE = "Coordinator";
	public final String RESOURCE_MESSAGE = "Request";

	public Node(int id, boolean isOnline) {
		timestamp = 0;
		this.isOnline = isOnline;
		this.id = id;
		state = 0;
		coordinatorId = -1;
		queue = new LinkedList<Message>();
		request = new LinkedList<Message>();
	}

	@Override
	public void run() {
		while (true) {

			try {
				sendMessage(this.id, "Salam");
				sendMessage(this.id, RESOURCE_MESSAGE);
			} catch (Exception e2) {
				state = 1;
			}

			switch (state) {
			case 0: {// normal
				if (!getQueue().isEmpty()) {
					Message msg = getQueue().getFirst();
					if (msg.getResource() == -1)
						System.out.println("Node: " + msg.getSenderId() + " send '" + msg.getMessage() + "'"
								+ " to node " + this.getId() + " | at time: " + msg.getTimestamp());
					else {
						System.out.println("Node: " + msg.getSenderId() + " requested for R.[" + msg.getResource() + "]"
								+ " at Node: " + this.getId() + " | at time: " + msg.getTimestamp());
					}
					getQueue().removeFirst();
					if (msg.getMessage().equals("Salam")) {
						timestamp++;
						try {
							sendMessage(id, "Sagol");
						} catch (Exception e) {
							state = 1;
						}
					}

					else if (msg.getMessage().equals("Sagol")) {
						timestamp++;
						// do nothing
					} else if (msg.getMessage().equals(ELECTION_MESSAGE)) {
						timestamp++;
						try {
							sendMessage(id, OK_MESSAGE);

						} catch (Exception e) {
						}
						state = 1;
					} else if (msg.getMessage().equals(COORDINATOR_MESSAGE)) {
						timestamp++;
						coordinatorId = msg.senderId;
						timestamp++;
					} else if (msg.getMessage().equals(RESOURCE_MESSAGE)) {
						msg.setTimestamp(Math.max(msg.getTimestamp(), this.timestamp) + 1);
						msg.setMessage("OK Use");
						if (msg.getResource() != -1)
							request.add(msg);
						sendMessage(this.id, "Ok use");
					}
					if (!request.isEmpty()) {
						Message r = request.getFirst();
						request.removeFirst();
						System.out.println("Node: " + this.getId() + ": Node: " + r.getSenderId() + " can use R.["
								+ r.getResource() + "] at time: | " + r.getTimestamp());
					}
				}
			}
				break;

			case 1: // election
				if (!getQueue().isEmpty()) {
					Message msg = queue.getFirst();
					queue.removeFirst();
					if (msg.getResource() == -1)
						System.out.println("Node: " + msg.getSenderId() + " send '" + msg.getMessage() + "'"
								+ " to node " + this.getId() + " | at time: " + msg.getTimestamp());
					try {
						sendMessage(id, ELECTION_MESSAGE);

					} catch (Exception e1) {
					}
					if (!isSendElection() && !isGetOk() && isSendOk()) {
						state = 3;
					} else {
						state = 0;
					}
				}
				break;

			case 2:
				if (!getQueue().isEmpty()) {
					Message msg = getQueue().getFirst();
					System.out.println("Node: " + msg.getSenderId() + " send '" + msg.getMessage() + "'" + " to node "
							+ this.getId() + " at time " + msg.getTimestamp());
					getQueue().removeFirst();

					if (msg.getMessage().equals("ok") && this.isSendElection()) {
						state = 0;
					}
				}
				break;

			case 3: // set coordinator

				try {
					sendMessage(id, COORDINATOR_MESSAGE);
				} catch (Exception e1) {
				}
				break;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(int id, String m) throws NoSuchElementException {
		if (isMaster() && !isOnline())
			throw new NoSuchElementException("Offline master");

		else {
			if (m.equalsIgnoreCase(ELECTION_MESSAGE)) {
				for (int key : nodes.keySet()) {
					if (id < key) {
						if (nodes.get(key).isOnline()) {
							nodes.get(key).getQueue().add(new Message(id, -1, ELECTION_MESSAGE, timestamp));
							this.setSendElection(true);
							nodes.get(key).setGetElection(false);
						} else {
							nodes.get(key).setGetElection(false);
							this.setSendElection(true);
						}
					}
				}
			} else if (m.equalsIgnoreCase(OK_MESSAGE)) {
				Iterator<Message> it = this.getQueue().iterator();
				while (it.hasNext()) {
					Message msg = it.next();

					if (nodes.get(msg.getSenderId()).isOnline() && msg.getMessage().equalsIgnoreCase("election")) {
						nodes.get(msg.getSenderId()).getQueue()
								.add(new Message(this.getId(), -1, OK_MESSAGE, timestamp));
						this.setSendOk(true);
						nodes.get(msg.getSenderId()).setGetOk(true);

					} else {
						this.setSendOk(true);
						nodes.get(msg.getSenderId()).setGetOk(false);
					}

				}
			} else if (m.equalsIgnoreCase(COORDINATOR_MESSAGE)) {
				for (Node node : nodes.values()) {
					if (node.getId() < this.getId()) {
						node.getQueue().add(new Message(this.id, -1, COORDINATOR_MESSAGE, timestamp));
					}
				}
			} else if (m.equalsIgnoreCase(RESOURCE_MESSAGE)) {
				int c = new Random().nextInt(nodes.size());
				int resource = new Random().nextInt(20);// here it asks for a
														// random resource
				nodes.get(c).getQueue().add(new Message(this.id, resource, m, timestamp));
			}
		}
		int c = new Random().nextInt(nodes.size());
		if (c != id)
			nodes.get(c).getQueue().add(new Message(this.id, -1, m, timestamp));
	}

	public LinkedList<Message> getQueue() {
		return queue;
	}

	public void setQueue(LinkedList<Message> queue) {
		this.queue = queue;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public boolean isSendElection() {
		return sendElection;
	}

	public void setSendElection(boolean sendElection) {
		this.sendElection = sendElection;
	}

	public boolean isSendOk() {
		return sendOk;
	}

	public void setSendOk(boolean sendOk) {
		this.sendOk = sendOk;
	}

	public boolean isGetElection() {
		return getElection;
	}

	public void setGetElection(boolean getElection) {
		this.getElection = getElection;
	}

	public boolean isGetOk() {
		return getOk;
	}

	public void setGetOk(boolean getOk) {
		this.getOk = getOk;
	}

	public boolean isSendMessage() {
		return sendMessage;
	}

	public void setSendMessage(boolean sendMessage) {
		this.sendMessage = sendMessage;
	}

	public boolean isGetMessage() {
		return getMessage;
	}

	public void setGetMessage(boolean getMessage) {
		this.getMessage = getMessage;
	}

	public boolean isMaster() {
		return this.getState() == 3;
	}

	public void setMaster(boolean isMaster) {
		this.setState(3);
	}

}