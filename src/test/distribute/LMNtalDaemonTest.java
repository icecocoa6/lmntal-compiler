package test.distribute;

//import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

//TODO ��λ����褦�ˤ���

public class LMNtalDaemonTest {
	public static void main(String args[]) {
		Thread t1 = new Thread(new LMNtalDaemon(60000));
		t1.start();

		//Thread t2 = new Thread(new LMNtalDaemon(60001));
		//t2.start();

	}
}

class GlobalConstants {
	public static int LMNTAL_DAEMON_PORT = 60000;
}

/**
 * ʪ��Ū�ʷ׻����ζ����ˤ��äơ�LMNtalRuntime���󥹥��󥹤ȥ�⡼�ȥΡ��ɤ��б�ɽ���ݻ����롣
 * @author nakajima
 *
 */
class LMNtalDaemon implements Runnable {

	ServerSocket servSocket = null;
	static HashMap nodeTable = new HashMap();
	static HashMap registedRuntimeTable = new HashMap();
	static HashMap msgTable = new HashMap();

	public LMNtalDaemon() {
		try {
			servSocket = new ServerSocket(GlobalConstants.LMNTAL_DAEMON_PORT);

		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
		}
	}

	public LMNtalDaemon(int portnum) {
		try {
			servSocket = new ServerSocket(portnum);
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
		}
	}

	public void run() {
		System.out.println("LMNtalDaemon.run()");

		Socket tmpSocket;
		BufferedReader tmpInStream;
		BufferedWriter tmpOutStream;

		while (true) {
			try {
				tmpSocket = servSocket.accept();
				System.out.println("accepted socket: " + tmpSocket);

				//����stream			
				tmpInStream =
					new BufferedReader(
						new InputStreamReader(tmpSocket.getInputStream()));

				//����stream
				tmpOutStream =
					new BufferedWriter(
						new OutputStreamWriter(tmpSocket.getOutputStream()));

				if (register(tmpSocket,
					new LMNtalNode(
						tmpSocket.getInetAddress(),
						tmpInStream,
						tmpOutStream))) {
					//��Ͽ������
					Thread t2 =
						new Thread(
							new LMNtalDaemonThread2(
								tmpSocket,
								tmpInStream,
								tmpOutStream));
					t2.start();
				} else {
					//��Ͽ���ԡ�����
					tmpInStream.close();
					tmpOutStream.close();
					tmpSocket.close();
				}

			} catch (IOException e) {
				System.out.println(
					"ERROR in LMNtalDaemon.run() " + e.toString());
				break;
			}
		}
	}

	static boolean register(Socket socket, LMNtalNode node) {
		System.out.println(
			"register(" + socket.toString() + ", " + node.toString() + ")");

		synchronized (nodeTable) {
			if (nodeTable.containsKey(socket)) {
				return false;
			}

			nodeTable.put(socket, node);
		}
		return true;
	}

	public static boolean registerLocal(Integer rgid, Socket socket) {
		System.out.println(
			"registerLocal(" + rgid + ", " + socket.toString() + ")");

		synchronized (registedRuntimeTable) {
			if (registedRuntimeTable.containsKey(rgid)) {
				System.out.println("registerLocal failed");
				return false;
			}

			registedRuntimeTable.put(rgid, socket);
		}

		System.out.println("registerLocal succeeded");
		return true;
	}

	public static boolean registerMessage(Integer msgid, LMNtalNode node) {
		System.out.println(
			"registerMessage(" + msgid + ", " + node.toString() + ")");

		synchronized (msgTable) {
			if (msgTable.containsKey(msgid)) {
				return false;
			}

			registedRuntimeTable.put(msgid, node);
		}

		return true;
	}

	public static LMNtalNode getNodeFromMsgId(Integer msgid) {
		System.out.println("getNodeFromMsgId(" + msgid + ")");

		synchronized (msgTable) {
			return (LMNtalNode) msgTable.get(msgid);
		}
	}

	/* 
	 *  fqdn���LMNtalDaemon��������Ͽ����Ƥ��뤫�ɤ�����ǧ����
	 */
	public static boolean isRegisted(String fqdn) {
		System.out.println("now in LMNtalDaemon.isRegisted(" + fqdn + ")");
		
		//TODO ñ�Υƥ���
		Collection c = nodeTable.values();
		Iterator it = c.iterator();

		while (it.hasNext()) {
			if (((LMNtalNode) (it.next()))
				.getInetAddress()
				.getCanonicalHostName()
				.equalsIgnoreCase(fqdn)) {
					System.out.println("LMNtalDaemon.isRegisted("  + fqdn + ") is true!" );
					return true;
			}
		}

		System.out.println("LMNtalDaemon.isRegisted("  + fqdn + ") is false!" );
		return false;
	}

	/*
	 *  fqdn���LMNtalDaemon����Ͽ�������Ͽ���Ƥ�餦
	 */
	public static boolean connect(String fqdn) {
		System.out.println("now in LMNtalDaemon.connect(" + fqdn + ")");

		//TODO ���ͥ������firewall�ˤҤä����äƥѥ��åȤ����Ǥ�������ɤ����뤫��
		
		boolean result = false;

		if (isRegisted(fqdn)) {
			return result;
		}

		try {
			InetAddress ip = InetAddress.getByName(fqdn);

			Socket socket =
				new Socket(fqdn, GlobalConstants.LMNTAL_DAEMON_PORT);

			BufferedReader in =
				new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			BufferedWriter out =
				new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));

			LMNtalNode node = new LMNtalNode(ip, in, out);
			result = register(socket, node);
		} catch (Exception e) {
			System.out.println("ERROR in connect����: " + e.toString());
		}

		return result;
	}

	boolean disconnect(Socket socket) {
		LMNtalNode node = (LMNtalNode) nodeTable.get(socket);

		try {
			node.getInputStream().close();
			node.getOutputStream().close();
			socket.close();

			return true;
		} catch (Exception e) {
			System.out.println(
				"LMNtalDaemon.disconnect() failed!!! " + e.toString());
		}

		return false;
	}

	static void dumpHashMap() {
		Set tmpSet;

		tmpSet = nodeTable.entrySet();
		System.out.println("Dump nodeTable");
		System.out.println(tmpSet);

		tmpSet = registedRuntimeTable.entrySet();
		System.out.println("Dump registedRuntimeTable");
		System.out.println(tmpSet);

		tmpSet = msgTable.entrySet();
		System.out.println("Dump msgTable");
		System.out.println(tmpSet);
	}
}

class LMNtalNode {
	InetAddress ip;
	BufferedReader in;
	BufferedWriter out;

	LMNtalNode(
		InetAddress tmpIp,
		BufferedReader tmpInStream,
		BufferedWriter tmpOutStream) {
		ip = tmpIp;
		in = tmpInStream;
		out = tmpOutStream;
	}

	BufferedReader getInputStream() {
		return in;
	}

	BufferedWriter getOutputStream() {
		return out;
	}

	InetAddress getInetAddress() {
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
}

class LMNtalDaemonThread2 implements Runnable {
	BufferedReader in;
	BufferedWriter out;
	Socket socket;

	public LMNtalDaemonThread2(
		Socket tmpSocket,
		BufferedReader inTmp,
		BufferedWriter outTmp) {
		in = inTmp;
		out = outTmp;
		socket = tmpSocket;
	}

	public void run() {
		System.out.println("LMNtalDaemonThread2.run()");

		while (true) {
			try {
				String input = in.readLine();
				//�ƥ����ѡ�
				//String input = new String("msgid \"localhost\" runtimegroupid connect\n");
				System.out.println("input: " + input);
				if (input == null) {
					break;
				}

				//���������å�����������ʬ
				Integer msgid;
				Integer rgid;
				boolean result;
				String[] tmpString = new String[3];

				tmpString = input.split(" ", 3);

				if (tmpString[0].equalsIgnoreCase("res")) {
					//res msgid ���
					//ľ���᤻�Ф褤

					//TODO ñ�Υƥ���
					msgid = new Integer(tmpString[1]);

					LMNtalNode returnNode =
						LMNtalDaemon.getNodeFromMsgId(msgid);

					returnNode.getOutputStream().write(input);
					returnNode.getOutputStream().flush();
				} else if (tmpString[0].equalsIgnoreCase("registerlocal")) {
					//registerlocal rgid
					//rgid�ȥ����åȤ���Ͽ
					rgid = new Integer(tmpString[1]);
					result = LMNtalDaemon.registerLocal(rgid, socket);
					if (result == true) {
						//����
						out.write("ok\n");
						out.flush();
					} else {
						//����
						out.write("fail\n");
						out.flush();
					}
				} else if (tmpString[0].equalsIgnoreCase("dumphash")) {
					//dumphash
					LMNtalDaemon.dumpHashMap();
				} else {
					//msgid����ĤŤ�̿����Ȥߤʤ�
					//msgid "FQDN" rgid ��å�����
					msgid = new Integer(tmpString[0]);

					//��å���������Ͽ
					LMNtalNode returnNode =
						new LMNtalNode(socket.getInetAddress(), in, out);
					result = LMNtalDaemon.registerMessage(msgid, returnNode);

					if (result == true) {
						//��Ͽ����
						boolean result2;

						//��³���˹Ԥ�
						result2 =
							LMNtalDaemon.connect(
								(tmpString[1].split("\"", 3))[1]);

						

						if (result2 == true) {
							//��³����
							out.write("OK\n");
							out.flush();
						} else {
							//��³����
							out.write("fail\n");
							out.flush();
						}
					} else {
						//����msgTable����Ͽ����Ƥ���� or �̿����Ի�
						out.write("fail\n");
						out.flush();
					}

				}

			} catch (IOException e) {
				System.out.println("ERROR:���Υ���åɤˤϽ񤱤ޤ���! " + e.toString());
				break;
			} catch (ArrayIndexOutOfBoundsException ae) {
				//�����Ƥ�����å�������û��������Ȥ��ʡ������ʻ�
				//'hoge' �Ȥ�����������
				System.out.println("Invalid Message: " + ae.toString());
				break;
			}
		}
	}

}
