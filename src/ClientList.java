import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

// A list of all clients
public class ClientList implements Serializable{
    ArrayList<User> clientUserList = null;
	
	public ClientList()
	{
		this.clientUserList = new ArrayList<User>();
	}
    
	public void add(User n)
	{
		this.clientUserList.add(n);
	}

	// remove client
	public void deleteClient(User n)
	{
		this.clientUserList.remove(n);
	}

	// get the amount of client
	public int getCount()
	{
		return this.clientUserList.size();
	}

	// find client with the username and return it
	public User find(String username)
	{
		Iterator<User> iter = this.clientUserList.iterator();
        while(iter.hasNext()){
            User n = iter.next();
            if(n.username.equals(username)){
                return n;
            }
        }
        return null;
	}

	// return 一个client node
	public User find(int index)
	{
		return this.clientUserList.get(index);
	}

}
