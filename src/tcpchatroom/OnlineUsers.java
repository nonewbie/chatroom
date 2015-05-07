/**
 * 
 */
package tcpchatroom;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Tao
 *
 */
public class OnlineUsers {
	static ArrayList<Socket> clientList;
	static ArrayList<String> onlineUsers;
	static Map<String,Socket> ssm;
}
