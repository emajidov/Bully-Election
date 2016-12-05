import java.io.IOException;
import java.util.HashMap;

public class DistributedSystem {
	static HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();


	public static void main(String[] args) throws IOException
	{
		for (int i = 0; i <= 9; i++) {
			nodes.put(i, new Node(i, true));
		}
		
		nodes.get(4).setState(3);
		nodes.get(4).setOnline(false);
		nodes.get(3).setOnline(false);

		for (Node node : nodes.values()) {
			Thread th = new Thread(node);
			th.start();
		}
		
	}
}