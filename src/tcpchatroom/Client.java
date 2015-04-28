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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 基于TCP的并发多线程聊天室
 * 
 * 客户端窗口程序
 * 
 * @author 詹宇衡
 *
 */

public class Client extends JFrame {
	private JLabel label_IPaddr = new JLabel("服务器IP地址：");
	private JLabel label_port = new JLabel("端口：");
	private JLabel label_name = new JLabel("名字：");

	private JTextField textField_IPaddr = new JTextField(15);
	private JTextField textField_port = new JTextField(6);
	private JTextField textField_name = new JTextField(12);
	private JTextField textField_inputText = new JTextField(30);

	private JButton button_join = new JButton("加入聊天室");
	private JButton button_sent = new JButton("发送");

	/* 消息窗口 */
	private JTextArea contentWindow = new ContentArea();
	private  JScrollPane scrollPane = new JScrollPane(contentWindow);

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;

	public Client(){
		setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
		setTitle("TCP Chat Room Client");
		setSize(600, 400);
		setResizable(false);

		contentWindow.setLineWrap(true);
		contentWindow.setEditable(false);

		JPanel p1 = new JPanel();
		p1.setSize(600, 40);
		p1.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 10));
		p1.add(label_IPaddr);
		p1.add(textField_IPaddr);
		p1.add(label_port);
		p1.add(textField_port);

		JPanel p2 = new JPanel();
		p2.setSize(600, 40);
		p2.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
		p2.add(label_name);
		p2.add(textField_name);
		p2.add(button_join);

		JPanel p3 = new JPanel();
		p3.setSize(600, 40);
		p3.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 10));
		p3.add(textField_inputText);
		p3.add(button_sent);

		add(p1);
		add(p2);
		add(scrollPane);
		add(p3);

		ButtonJoinListener joinListener = new ButtonJoinListener();
		button_join.addActionListener(joinListener);
		ButtonSentListener sentListener = new ButtonSentListener();
		button_sent.addActionListener(sentListener);

		//缺省服务器地址、端口
		textField_IPaddr.setText("localhost");
		textField_port.setText("10000");
	}

	class ContentArea extends JTextArea{
		protected ContentArea(){
			super(10, 40);
		}
	}

	class ButtonJoinListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String ip = textField_IPaddr.getText();
			int port = Integer.parseInt(textField_port.getText());
			try {
				socket = new Socket(ip, port);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(),true);
				out.println(getName() + "加入聊天室！\n");
				ClientThread thread = new ClientThread(contentWindow, socket);
				thread.start();

				//加入聊天室后禁止修改参数
				button_join.setEnabled(false);
				button_join.setText("已加入");
				textField_port.setEditable(false);
				textField_IPaddr.setEditable(false);
				textField_name.setEditable(false);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	class ButtonSentListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String sentence = getName() + ": " + textField_inputText.getText() + "\n";
			out.println(sentence);
			textField_inputText.setText("");
		}

	}

	public String getName(){
		String name = textField_name.getText();
		if(name.trim().equals(""))
			return "匿名";
		else 
			return name;
	}

	public static void main(String[] args) {
		Client chatRoom = new Client();
		chatRoom.setLocationRelativeTo(null);
		chatRoom.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		chatRoom.setVisible(true);
	}
}