/**
 * 
 */
package tcpchatroom;

/**
 * @author Tao
 *
 */

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import tcpchatroom.datagram.ClientDataGram;
import vo.User;

/**
 * 基于TCP的并发多线程聊天室
 * 
 * 客户端窗口程序
 *
 *
 */

public class Client extends JFrame implements WindowListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel label_username = new JLabel("用户名 ");
	private JLabel label_password = new JLabel("密码 ");
	private String[] friends = null;
	
	private User user = new User();

	private JTextField textField_username = new JTextField(15);
	private JTextField textField_password = new JTextField(6);
	private JTextField textField_inputText = new JTextField(30);

	private JButton button_join = new JButton("登陆");
	private JButton button_sent = new JButton("发送");
	private JButton button_regist = new JButton("注册");
	
	/* 消息窗口 */
	private JTextArea textarea_messagerecord = new ContentArea();
	private  JScrollPane scrollPane = new JScrollPane(textarea_messagerecord);

	private Socket socket;
	private PrintWriter out;
	public BufferedReader in = null;

	public Client(){
		
		try{
			socket = new Socket("localhost",10000);
			if(!socket.isBound()){//绑定失败
				textarea_messagerecord.setText("绑定失败");
			}
			out = new PrintWriter(socket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(Exception e){
			
		}
		
		
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
		setTitle("TCP Chat Room Client");
		setSize(600, 400);
		setResizable(true);

		textarea_messagerecord.setLineWrap(true);
		textarea_messagerecord.setEditable(false);

		JPanel p1 = new JPanel();//面板，嵌板
		p1.setSize(600, 40);
		p1.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 10));
		p1.add(label_username);
		p1.add(textField_username);
		p1.add(label_password);
		p1.add(textField_password);
		
		
		JPanel p2 = new JPanel();
		p2.setSize(600, 40);
		p2.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
		p2.add(button_join);
		p2.add(button_regist);

		JPanel p3 = new JPanel();
		p3.setSize(600, 40);
		p3.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 10));
		p3.add(textField_inputText);
		p3.add(button_sent);

		JPanel p4 = new JPanel();
		p4.setSize(600, 40);
		p4.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 10));

		
		
		add(p1);
		add(p2);
		add(scrollPane);
		add(p3);
		add(p4);

		ButtonJoinListener joinListener = new ButtonJoinListener();
		button_join.addActionListener(joinListener);
		ButtonSentListener sentListener = new ButtonSentListener();
		button_sent.addActionListener(sentListener);
		
		ButtonRegistListener registListener = new ButtonRegistListener();
		button_regist.addActionListener(registListener);

		//默认用户名和密码都是空
		textField_username.setText("admin");
		textField_password.setText("admin");
		
		
		this.addWindowListener(this);
		//按关闭按钮，啥事也不做
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

	}

	class ContentArea extends JTextArea{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected ContentArea(){
			super(10, 40);
		}
	}
	
	class ButtonJoinListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			user.setUserName(textField_username.getText());
			user.setPasswd(textField_password.getText());
			
			ClientDataGram logindatagram = new ClientDataGram((short) 1,user.getUserName(),user.getPasswd(),true);//登录
			out.println(logindatagram.toString());
			
			//加入聊天室后禁止修改参数
			button_join.setEnabled(false);
			button_join.setText("已加入");
			textField_password.setEditable(false);
			textField_username.setEditable(false);
		}
	}
	
	class ButtonRegistListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			user.setUserName(textField_username.getText());
			user.setPasswd(textField_password.getText());
			ClientDataGram logindatagram = new ClientDataGram((short) 4,user.getUserName(),user.getPasswd(),true);//注册
			out.println(logindatagram.toString());
			
			textField_password.setEditable(true);
			textField_username.setEditable(true);
		}
	}

	class ButtonSentListener implements ActionListener{//发送信息
		public void actionPerformed(ActionEvent arg0) {//**********************************需要修改
			String message_text = textField_inputText.getText();

			friends  = (String[]) OnlineUsers.onlineUsers.toArray(new String [OnlineUsers.onlineUsers.size()]);
			ClientDataGram clientdatagram = new ClientDataGram((short) 0,friends,message_text);//普通消息	
			try {
				out.println(clientdatagram.toString());
			}catch(Exception e){
				e.printStackTrace();
			}
			//textarea_messagerecord.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			textarea_messagerecord.append(user.getUserName() + "  "+ textField_inputText.getText()+'\n');
			textField_inputText.setText("");
		}

	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		   int option = JOptionPane.showConfirmDialog(this, "确定退出系统?", "提示",JOptionPane.YES_NO_OPTION);
			       if (option == JOptionPane.YES_OPTION)
			       {
			               if (e.getWindow() == this) {
			                      this.dispose();
			                      ClientDataGram exitdatagram = new ClientDataGram((short)2);//退出
			                      out.print(exitdatagram.toString());
			                      try {
			                    	  out.close();
			                    	  in.close();
									socket.close();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
			                      
			                      System.exit(0);
			       } else {
			              return;
			       }
			    }
			   else if(option == JOptionPane.NO_OPTION){
			          if (e.getWindow() == this) {
			                   return;
			          }else{
			        	  return;
			          }
			     }
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) throws IOException {
		OnlineUsers.onlineUsers  = new ArrayList<String>();
		
		Client chatRoom = new Client();
		
		ClientThread thread = new ClientThread(chatRoom.textarea_messagerecord, chatRoom.socket,chatRoom.in);
		thread.start();
		
		chatRoom.setLocationRelativeTo(null);
		//chatRoom.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		chatRoom.setVisible(true);
	}
}