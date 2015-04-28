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
import java.net.Socket;

import javax.swing.JTextArea;

/**
 * 基于TCP的并发多线程聊天室
 * 
 * 客户端子线程
 * 接收服务器端发来的消息并显示
 * 
 * @author 詹宇衡
 *
 */

public class ClientThread extends Thread{
	private Socket socket;
	private BufferedReader in;
	private JTextArea textArea;

	public ClientThread(JTextArea area, Socket socket){
		this.textArea = area;
		this.socket = socket;
	}

	public void run(){
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(true){
				//循环读取
				String clientSentence = in.readLine();
				//显示
				textArea.append(clientSentence + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}