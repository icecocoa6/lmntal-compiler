/*
 * ������: 2004/01/05
 *
 */
package util;

import java.net.*;
import java.io.*;
import runtime.Env;

/**
 * �ʰ��̿����饹��
 * 
 * @author hara
 *
 */
public class Net {
	static final int default_port = 10001;
	protected Socket sock;
	
	/**
	 * �ǥե���ȥݡ��Ȥ��������롣
	 * 
	 * @param addr �����ФΥ��ɥ쥹
	 * @param s    ��������
	 */
	public void send(String addr, String s) {
		send(addr, default_port, s);
	}
	/**
	 * �������롣
	 * 
	 * @param addr �����ФΥ��ɥ쥹
	 * @param port �����ФΥݡ���
	 * @param s    ��������
	 */
	public void send(String addr, int port, String s) {
		try {
			sock = new Socket(addr, port);
			PrintWriter pw = new PrintWriter(sock.getOutputStream());
			pw.print(s);
			pw.close();
		} catch (Exception e) {
			Env.e(e);
		}
	}
	
	/**
	 * �ǥե���ȥݡ��Ȥ���Ʊ��Ū�˼������롣
	 * 
	 * @return
	 */
	public String recv() {
		return recv(default_port);
	}
	
	/**
	 * Ʊ��Ū�˼������롣
	 * 
	 * @param port �Ԥ������ݡ���
	 * @return
	 */
	public String recv(int port) {
		StringBuffer buf = new StringBuffer();
		try {
			ServerSocket ss = new ServerSocket(port);
			sock = ss.accept();
			BufferedReader br = new BufferedReader(
				new InputStreamReader(sock.getInputStream()));
			String res;
			while(null!=(res = br.readLine())) {
				recvHandler(res);
				buf.append(res).append("\n");
				//Env.p("<<< " + res);
			}
			return buf.toString();
		} catch(Exception e) {
			Env.e(e);
		}
		return buf.toString();
	}
	
	/** u can override */
	public void recvHandler(String line) {
	}
}

