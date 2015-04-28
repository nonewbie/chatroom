/**
 * 
 */
package tcpchatroom;

/**
 * @author Tao
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JTextArea;

/**
 * 基于TCP的并发多线程聊天室
 * 
 * 服务器端套接字子线程
 * 对应每一个客户端
 * 
 * @author 詹宇衡
 *
 */

public class ServerThread extends Thread{
	private Socket socket;
	private BufferedReader in;
	private JTextArea textArea;
	private ArrayList<Socket> clientList;

	public ServerThread(ArrayList<Socket> clientList, JTextArea area, Socket sock){
		socket = sock;
		textArea = area;
		this.clientList = clientList;
	}

	public void run(){
		try {
			while(true){
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientSentence = in.readLine();
				//显示客户端发来的消息
				textArea.append(clientSentence + "\n");
				//群发至所有客户端
				for(Socket client : clientList){
					PrintWriter out = new PrintWriter(client.getOutputStream(),true);
					out.println(clientSentence);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}