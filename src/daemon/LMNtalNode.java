package daemon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import runtime.Env;
import util.HybridInputStream;
import util.HybridOutputStream;

/** �����å��̿�ϩ��ɽ�����饹��
 * <p>LMNtalDaemonMessageProcessor ����� LMNtalRuntimeMessageProcessor �οƥ��饹��
 * @author nakajima, n-kato */

public class LMNtalNode {
	private Socket socket = null;
	private InetAddress ip = null;
	private HybridInputStream in;
	private HybridOutputStream out;
	
//	public static LMNtalNode connect(String hostname, int port) {
//		try {
//			Socket socket = new Socket(hostname, port);
//			InetAddress ip = InetAddress.getByName(hostname);
//			return new LMNtalNode(socket, ip);
//		} catch (Exception e) {
//			return null;
//		}
//	}

	/** �̾�Υ��󥹥ȥ饯�� */
	public LMNtalNode(Socket socket) {
		this(socket, socket.getInetAddress());		
	}
	public LMNtalNode(Socket socket, InetAddress ip) {
		try {
			this.ip = ip;
			in = new HybridInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new HybridOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			this.socket = socket;
		} catch (Exception e) {}
	}
	
	//
	
	public void close() {
		if(Env.debug > 0)System.out.println("LMNtalNode.close(): "+ socket);
		try {
			in.close();
			out.close();
			if(Env.debug > 0)System.out.println("LMNtalNode.close(): BufferedStreams closed.");
			socket.close();
			if(Env.debug > 0)System.out.println("LMNtalNode.close(): socket has closed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////////
	// ����μ���
	
	public HybridInputStream getInputStream() {
		return in;
	}
	public HybridOutputStream getOutputStream() {
		return out;
	}
	public Socket getSocket() {
		return socket;
	}
	public InetAddress getInetAddress() {
		return ip;
	}

	public String toString() {
		return "LMNtalNode[IP:"
			+ ip
			+ ", "
			+ in.toString()
			+ ", "
			+ out.toString()
			+ "]";
	}

	/**
	 * �ۥ���fqdn����ʬ���Ȥ�Ƚ�ꡣ
	 * 
	 * @author nakajima
	 * @param fqdn hostname  
	 * @return ��ʬ���Ȥ˳�꿶���Ƥ���IP���ɥ쥹����ۥ���̾������ơ�fqdn��ʸ������Ӥ������
	 * @version 1.1
	 */
//	public static boolean isMyself(String fqdn) {
//		try {
//			String hostaddr = InetAddress.getByName(fqdn).getHostAddress();
//			if (hostaddr.equals("127.0.0.1")) return true;
//			return InetAddress.getLocalHost().getHostAddress().equals(hostaddr);
//		} catch (java.net.UnknownHostException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
	public static boolean isMyself(String ipstr) {
		//if(true)System.out.println("LMNtalNode.isMyself(" + ipstr + ")");
		
		boolean result = false; 
		
		if (ipstr.equals("127.0.0.1")) result = true;
		try {
			if(InetAddress.getLocalHost().getHostAddress().equals(ipstr)) result = true;
		} catch (java.net.UnknownHostException e) {
			e.printStackTrace();
		}
		
		if(true)System.out.println("LMNtalNode.isMyself(" + ipstr + ") is " + result);
		return result;
	}
	
	////////////////////////////////
	// ������
	
	/**
	 * ����LMNtalNode��ɽ���ۥ��Ȥ˥�å���������������
	 * @param message ��å�����
	 */
	public boolean sendMessage(String message) {
		return sendMessage(message, null);
	}
	/**
	 * ����LMNtalNode��ɽ���ۥ��Ȥ˥�å���������������
	 * @param message ��å�����
	 * @param rawData �Х��ʥ�ǡ�����message�θ��³������������롣
	 */
	public boolean sendMessage(String message, byte[] rawData) {
		try {
			synchronized(out) { //�֤�¾�Υ���åɤΥ�å�����������ʤ��褦�ˤ��롣
				out.write(message);
				if (rawData != null) {
					out.writeBytes(rawData);
				}
				out.flush();
			}
			return true;
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalNode.sendMessage()");
			e.printStackTrace();
		}
		return false;
	}

	void respond(String msgid, String message, byte[] rawData) {
		sendMessage("RES " + msgid + " " + message + "\n", rawData);
	}
	void respond(String msgid, String message){
//		sendMessage("RES " + msgid + " " + message + "\n");
		respond(msgid, message, null);
	}
	void respond(String msgid, boolean value) {
		respond(msgid, value ? "OK" : "FAIL");
	}
	void respondAsOK(String msgid){
		respond(msgid,"OK");
	}	
	void respondAsFail(String msgid){
		respond(msgid,"FAIL");
	}
	void respondRawData(String msgid, byte[] data) {
		respond(msgid, "RAW", data);
	}
	////////////////////////////////
	// ������
	
	protected String readLine() throws IOException {
		return in.readLine();
	}
	protected byte[] readBytes() throws IOException {
		return in.readBytes();
	}
	
/*
 *  @author nakajima
 *  @return true if the socket has been closed 
 */
	protected boolean isSocketClosed() {
		return socket.isClosed();
	}
	
}