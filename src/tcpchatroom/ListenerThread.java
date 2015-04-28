/**
 * 
 */
package tcpchatroom;

/**
 * @author Tao
 *
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JTextArea;

/**
 * 基于TCP的并发多线程聊天室
 * 
 * 服务器端监听端口线程
 * 监听指定端口，若收到新的连接请求即生成一个ServerThread子线程处理
 * 
 * @author 詹宇衡
 *
 */

public class ListenerThread extends Thread{
	private int port;
	private ServerSocket ss;
	private JTextArea textArea;
	private ArrayList<Socket> clientList;

	public ListenerThread(JTextArea area, int port){
		this.port = port;
		this.textArea = area;
		this.clientList = new ArrayList<Socket>();
	}

	public void run(){
		try {
			ss = new ServerSocket(port);
			while(true){
				Socket socket = ss.accept();
				//加入客户列表
				clientList.add(socket);
				ServerThread thread = new ServerThread(clientList, textArea, socket);
				thread.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}