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
import java.util.Date;

import factory.DaoFactory;
import tcpchatroom.datagram.ClientDataGramAnalyzer;
import tcpchatroom.datagram.ServerDataGram;
import vo.User;

/**
 * 基于TCP的并发多线程聊天室
 * 
 * 服务器端套接字子线程
 * 对应每一个客户端

 */

public class ServerThread extends Thread{
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private User user = new User();
	public ServerThread(Socket socket){
		this.socket = socket;
	}

	public void run(){
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(),true);
			while(!this.socket.isClosed()){
				
				String clientdatagram = in.readLine();
				//System.out.println("登陆用户:"+this.user.getUserName()+"	"+"用户密码:"+ this.user.getPasswd() + "	online state:" + ClientDataGramAnalyzer.getState(clientdatagram));
				
	
				switch (ClientDataGramAnalyzer.getType(clientdatagram)){
					case(0)://发送消息：
						String accepters[] = ClientDataGramAnalyzer.getAccepters(clientdatagram);	
						System.out.println("接收到消息:" + user.getUserName()+":"+" "+ ClientDataGramAnalyzer.getMessage(clientdatagram));
						
						for(int i = 1; i< accepters.length;i++){//首个字符串 一定是null 所以不用
								PrintWriter toclient = new PrintWriter(OnlineUsers.ssm.get(accepters[i]).getOutputStream(),true);
								///发送正常信息  **************************************
								ServerDataGram message = new ServerDataGram((short)0,this.user.getUserName(),ClientDataGramAnalyzer.getMessage(clientdatagram));
								toclient.println(message.toString());
						}break;		
					case(1)://login
						this.user.setUserName(ClientDataGramAnalyzer.getName(clientdatagram));
						System.out.println("login name : " + this.user.getUserName());
						this.user.setPasswd(ClientDataGramAnalyzer.getPassword(clientdatagram));
						System.out.println("login password : " + ClientDataGramAnalyzer.getPassword(clientdatagram));
						try {
							
							if(OnlineUsers.onlineUsers.contains(this.user.getUserName())){
								ServerDataGram logdulp = new ServerDataGram((short)3);//改用户已经登录^^^^^^^^^^^^^^^
								out.println(logdulp.toString());
								break;
							}
								
							this.user = DaoFactory.getIUserInstance().findByUserNameandPasswd(this.user.getUserName(), this.user.getPasswd());
							if(this.user==null){
								ServerDataGram loginfailedmessage = new ServerDataGram((short)2);//用户名或密码错误
								out.println(loginfailedmessage.toString());
							}
							else{
								//发送在线好友列表
								String[] friends = null ;
								friends  = (String[]) OnlineUsers.onlineUsers.toArray(new String[OnlineUsers.onlineUsers.size()]);
								
								ServerDataGram loginsuccessedmessage = new ServerDataGram((short)1,friends);//登录成功  ^^^^^^^^^^^^^^^^^
								
								System.out.println("server datagram: "+  loginsuccessedmessage.toString());
								out.println(loginsuccessedmessage.toString());
								//上线提醒：发送所有在线人
								//if(ClientDataGramAnalyzer.getState(clientdatagram).equals("online")){
		
								ServerDataGram onlineinform = new ServerDataGram((short)8,this.user.getUserName());//上线提醒信息^^^^^^^^^^
								for(Socket client : OnlineUsers.clientList){
										PrintWriter messagetofriend = new PrintWriter(client.getOutputStream(),true);
										messagetofriend.println(onlineinform.toString());
									}
								//}
								OnlineUsers.clientList.add(this.socket);
								OnlineUsers.onlineUsers.add(this.user.getUserName());//*****************	
								OnlineUsers.ssm.put(this.user.getUserName(),this.socket);
								
								System.out.print("在线好友数"+ OnlineUsers.onlineUsers.size() + "    online users: ");
								for(String user: OnlineUsers.onlineUsers)
									System.out.print(user + " ");
								System.out.println();
								
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}break;
						
					case(2)://用户退出				
						OnlineUsers.onlineUsers.remove(OnlineUsers.onlineUsers.indexOf(this.user.getUserName()));// ***************未正常登陆的用户 退出时抛出异常
						//OnlineUsers.clientList.remove(this.socket);
						OnlineUsers.ssm.remove(OnlineUsers.onlineUsers.indexOf(this.user.getUserName()));
						
						OnlineUsers.clientList.remove(OnlineUsers.clientList.indexOf(this.socket));
						
						
						for(Socket socket :OnlineUsers.clientList){
							PrintWriter toclient= new PrintWriter(socket.getOutputStream(),true);
							toclient.println(new ServerDataGram((short)6,this.user.getUserName()).toString());
						}
					
						this.in.close();
						this.out.close();
						this.socket.close();
						break;
					case(3)://设置在线状态
						break;
					case(4)://注册
						this.user.setUserName(ClientDataGramAnalyzer.getName(clientdatagram));
						System.out.println("regist name : " + this.user.getUserName());
						this.user.setPasswd(ClientDataGramAnalyzer.getPassword(clientdatagram));
						System.out.println("regist password : " + ClientDataGramAnalyzer.getPassword(clientdatagram));
						try {
							user.setRegisterTime(new Date());
							if(DaoFactory.getIUserInstance().findByUserName(this.user.getUserName()) != null){
								ServerDataGram logdulp = new ServerDataGram((short)5);//用户名已经注册
								out.println(logdulp.toString());
								break;
							}
							
							DaoFactory.getIUserInstance().doCreate(user);
							//发送在线好友列表
							String[] friends = (String[]) OnlineUsers.onlineUsers.toArray(new String[OnlineUsers.onlineUsers.size()]);
								
							ServerDataGram registermessage = new ServerDataGram((short)4,friends);//注册成功
								
							out.println(registermessage.toString());
							//上线提醒：发送所有在线人
							//if(ClientDataGramAnalyzer.getState(clientdatagram).equals("online")){
		
							ServerDataGram onlineinform = new ServerDataGram((short)8,this.user.getUserName());//**************
							for(Socket client : OnlineUsers.clientList){
									PrintWriter messagetofriend = new PrintWriter(client.getOutputStream(),true);
									messagetofriend.println(onlineinform.toString());
							}
							//}
							OnlineUsers.clientList.add(this.socket);
							OnlineUsers.onlineUsers.add(this.user.getUserName());//*****************	
							OnlineUsers.ssm.put(this.user.getUserName(),this.socket);
							
							for(String user: OnlineUsers.onlineUsers)
								System.out.print("在线好友数" + OnlineUsers.onlineUsers.size()+ "online users: " + user);
							
						}
						 catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}break;
				}
//				String clientSentence = in.readLine();
//				//显示客户端发来的消息
//				textArea.append(clientSentence + "\n");
//				//群发至所有客户端
//				for(Socket client : OnlineUsers.clientList){
//					PrintWriter out = new PrintWriter(client.getOutputStream(),true);
//					out.println(clientSentence);
//				}
			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(user.getUserName()+"exit!");
	}

}