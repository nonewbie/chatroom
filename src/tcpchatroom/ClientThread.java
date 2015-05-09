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
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import tcpchatroom.datagram.ServerDataGramAnalyzer;

/**
 * 基于TCP的并发多线程聊天室
 * 
 * 客户端子线程
 * 接收服务器端发来的消息并显示
 *
 */

public class ClientThread extends Thread{
	private Socket socket;
	private BufferedReader in;
	private JTextArea textArea;
	private JTextArea textArea2;
	
	
	public ClientThread(JTextArea area,JTextArea area2, Socket socket,BufferedReader in) throws IOException{
		this.textArea = area;
		this.textArea2 = area2;
		this.socket = socket;
		this.in = in;
	}

	public void run(){
		try {
			//循环读取
			while(!this.socket.isClosed()){
				String stocdatagram = in.readLine();//************************抛出异常
				switch(ServerDataGramAnalyzer.getType(stocdatagram)){
				
				
				case(0)://收到普通消息
//					System.out.println("接收到server 消息:" + stocdatagram);
					//textArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
					textArea.append(ServerDataGramAnalyzer.getName(stocdatagram)+ ":"  + ServerDataGramAnalyzer.getMessage(stocdatagram)+"\n");
					break;
				
				case(4)://注册成功
				case(1)://登陆成功 接受好友列表	
					String [] usernames = ServerDataGramAnalyzer.getNames(stocdatagram);
					for(int i = 1 ;i < usernames.length;i++){
						OnlineUsers.onlineUsers.add(usernames[i]);
						textArea2.append(usernames[i]+"\n");
//						System.out.println("at client thread 接受好友列表:" + OnlineUsers.onlineUsers.get(i-1));
					}
					
					break;
					//显示好哟列表
				case(2)://登陆失败
					JOptionPane.showMessageDialog(null,"登录失败", "信息错误",JOptionPane.ERROR_MESSAGE);break;
				case(3)://重复登陆
					JOptionPane.showMessageDialog(null,"已经在线上", "信息错误",JOptionPane.ERROR_MESSAGE);break;
				case(5)://注册重复
					JOptionPane.showMessageDialog(null,"用户名已经注册过", "信息错误",JOptionPane.ERROR_MESSAGE);break;
				case(6):// 用户退出 重新显示列表
					OnlineUsers.onlineUsers.remove(ServerDataGramAnalyzer.getName(stocdatagram));//列表显示***************************************
					textArea.append(ServerDataGramAnalyzer.getName(stocdatagram)+"  下线了"+ "\n");
					textArea2.setText(null);
					textArea2.append("		好友列表\n");
//					for (String name : OnlineUsers.onlineUsers){
//						System.out.println("******" + name);
//						textArea2.append(name + "\n");
//					}
//					
				break;
				case(7)://设置在线状态
					break;//**********************************
				case(8)://用户上线提醒
					OnlineUsers.onlineUsers.add(ServerDataGramAnalyzer.getName(stocdatagram));
					//textArea.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
					//每次接收到消息 都要打印一下好友列表:
					textArea.append(ServerDataGramAnalyzer.getName(stocdatagram)+"  上线了"+ "\n");
					textArea2.append(ServerDataGramAnalyzer.getName(stocdatagram)+ "\n");
					break;
				}
				
			
//				System.out.println("在线好友数:"+OnlineUsers.onlineUsers.size() + "   好友列表:\n");
//				for (String  username: OnlineUsers.onlineUsers){
//					System.out.println( "  "+ username + "、");
//				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}