import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

// as a client object
public class User implements Serializable{
	// have username、ip、port attributes
	String username=null;
	InetAddress ip;
	int port;

	// store commands
	ArrayList<String> commands = new ArrayList<String>();

}
