package daemon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * ʪ��Ū�ʷ׻����ζ����ˤ��äơ�LMNtalRuntime���󥹥��󥹤ȥ�⡼�ȥΡ��ɤ��б�ɽ���ݻ����롣
 * �̿���ʬ�ν�����Ԥ���
 * 
 * @author nakajima
 *
 */

//TODO �Ѹξ������������ƥ�

/*
 * ���Υ���åɤ��ǡ����Ȥ���ʪ��Ū�ʷ׻����ζ����ˤ��ꡢ
 * tcp60000�֤˵�¤äƤ��롣���ͥ�������褿��LMNtalMessageProcessor����åɤ򤢤����Ԥ������롣
 * LMNtalDaemon�ϰ�Ĥ�ʪ��Ū�ʷ׻�����1����åɤ���������ʤ���
 * 
 * @author nakajima
 *
 */
public class LMNtalDaemon implements Runnable {
	/*
	 * ��(ά��):    a :- { b }@"banon" 
	 * ��:          a :- connectruntime("banon", B) | {b}@B.
	 *              a :- connectruntimefailure("banon") | fail("banon").
	 * 
	 * 
	 * �Ȥꤢ�����ͤ��Ƥߤ��ץ�ȥ���
	 * 
	 * ���㡧 rgid  �� runtime group id
	 * 
	 * ��Ͽ�ط�
	 * HELO �ġʥΎߧ��ߡˤ��Ϥ褦
	 * READY ��ack
	 * REGISTERLOCAL rgid �ĥ�����ˤ���ޥ�����󥿥������Ͽ
	 * OK, msgid �� ����
	 * FAIL, msgid �� ����
	 * REGISTERREMOTE, rgid �ļ긵����Ͽ���Ƥ���ޥ�����󥿥������������
	 * REGISTERFINISHED, rgid �Ĺ�������Ǥ������Ȥ�ޥ�����󥿥��ब����׻�����daemon������
	 * CONNECTRUNTIME, msgid, "banon"
	 * TERMINATE, rgid 
	 * 
	 * �¹Դط�
	 * COPYRULESET �ĥ롼�륻�åȤ���������
	 * COPYRULE �ĥ롼���������
	 * COPYPROCESSCONTEXT ��$p������
	 * COPYFREELINK
	 * COPYATOM
	 * 
	 *
	 * 
	 */

	/*��n-kato����Υ����ȡ�
	 * - REGIST �� REGISTER ����������(��: 2004-05-18 nakajima)
	 * - REGISTREMOTE �ϡ�runtimegroupid�ʡ�ޥ�����󥿥����(runtime)id�ˤ�����ɬ�פ����롣
	 * - REGISTFINISHED �ϡ��ޥ�����󥿥��ब����׻����ǤϤʤ���REGISTREMOTE��ȯ�Ԥ����׻����������֤���
	 *   �������äơ�runtimegroupid ������˻���ɬ�פ����롣
	 * - TERMINATE runtimegroupid ��ɬ�ס����������鼫ʬ���ΤäƤ������ƤΥ�󥿥����Ʊ����å����������롣
	 */
	static boolean DEBUG = true;

	ServerSocket servSocket = null;
	static HashMap nodeTable = new HashMap();
	static HashMap registedRuntimeTable = new HashMap();
	static HashMap msgTable = new HashMap();
	static int id = 0;
	static Random r = new Random();
	static Process remoteRuntime;

	/*
	 * ���󥹥ȥ饯���� tcp60000�֤�ServerSocket�򳫤�������
	 */
	public LMNtalDaemon() {
		try {
			servSocket = new ServerSocket(60000);
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
			e.printStackTrace();
		}
	}

	/*
	 * ���󥹥ȥ饯���� tcp��portnum�֥ݡ��Ȥ�ServerSocket�򳫤�������
	 * @param portnum ServerSocket���Ϥ�
	 */
	public LMNtalDaemon(int portnum) {
		try {
			servSocket = new ServerSocket(portnum);
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
		}
	}

	/*
	 * �ᥤ���ϥƥ����ѡ���ʬ���ȡʥ���åɡˤ�1�Ĥ�����Τߡ�
	 * @author nakajima
	 *
	 */
	public static void main(String args[]) {
		Thread t = new Thread(new LMNtalDaemon());
		t.start();
	}

	public void run() {
		System.out.println("LMNtalDaemon.run()");

		Socket tmpSocket;
		BufferedReader tmpInStream;
		BufferedWriter tmpOutStream;

		while (true) {
			try {
				tmpSocket = servSocket.accept(); //���ͥ�����󤬤���ޤ��Ԥ�

				if (DEBUG) {
					System.out.println("accepted socket: " + tmpSocket);
				}

				//����stream			
				tmpInStream =
					new BufferedReader(
						new InputStreamReader(tmpSocket.getInputStream()));

				//����stream
				tmpOutStream =
					new BufferedWriter(
						new OutputStreamWriter(tmpSocket.getOutputStream()));

				//��Ͽ����
				if (register(tmpSocket,
					new LMNtalNode(
						tmpSocket.getInetAddress(),
						tmpInStream,
						tmpOutStream))) {
					//��Ͽ������
					Thread t2 =
						new Thread(
							new LMNtalDaemonMessageProcessor(
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
				e.printStackTrace();
				break;
			}
		}
	}

	/*
	 * Socket��key, LMNtalNode��value�Ȥ���HashMap����Ͽ����
	 * 
	 * @param socket �����å�
	 * @param node LMNtal�Ρ���
	 * 
	 * @return socket�Ȥ�������������¸�ߤ��Ƥ�����false
	 */
	static boolean register(Socket socket, LMNtalNode node) {
		if (DEBUG) {
			System.out.println(
				"register(" + socket.toString() + ", " + node.toString() + ")");
		}

		synchronized (nodeTable) {
			if (nodeTable.containsKey(socket)) {
				return false;
			}

			nodeTable.put(socket, node);
		}
		return true;
	}

	/*
	 * ��⡼��¦�ǻȤ��륹�졼�֥�󥿥�����������롣
	 * 
	 * @param msgid ���졼�֥�󥿥��ब�ֻ����֤����˻Ȥ���å�����ID
	 */
	public static void createRemoteRuntime(int msgid) {
		String cmdLine =
			new String(
				"java daemon/SlaveLMNtalRuntimeLauncher "
					+ LMNtalDaemon.makeID()
					+ " "
					+ msgid);
		if (DEBUG) {
			System.out.println(cmdLine);
		}

		try {
			remoteRuntime = Runtime.getRuntime().exec(cmdLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * �������󥿥����HashMap����Ͽ���롣key��rgid, value��Socket��
	 * 
	 * @param rgid runtime group id
	 * @param socket rgid����ĥ饤�󥿥��ब���ĥ����å�
	 * @return rgid�ʥ�����¸�ߤ������false
	 */
	public static boolean registerLocal(Integer rgid, Socket socket) {
		if (DEBUG) {
			System.out.println(
				"registerLocal(" + rgid + ", " + socket.toString() + ")");
		}

		synchronized (registedRuntimeTable) {
			if (registedRuntimeTable.containsKey(rgid)) {
				if (DEBUG) {
					System.out.println("registerLocal failed");
				}
				return false;
			}

			registedRuntimeTable.put(rgid, socket);
		}

		if (DEBUG) {
			System.out.println("registerLocal succeeded");
		}

		return true;
	}

	/*
	 * ��å�������HashMap����Ͽ���롣key��msgid, value��LMNtalNode��
	 * 
	 * @param msgid ��å�����ID��int����ʤ���Integer�ʤΤϻ��ͤǤ���
	 * @param node msgid�ʥ�å�������ȯ�Ԥ���LMNtalNode��
	 * @return msgid�ʥ�����¸�ߤ��Ƥ�����false
	 */
	public static boolean registerMessage(Integer msgid, LMNtalNode node) {
		if (DEBUG) {
			System.out.println(
				"registerMessage(" + msgid + ", " + node.toString() + ")");
		}

		synchronized (msgTable) {
			if (msgTable.containsKey(msgid)) {
				return false;
			}

			msgTable.put(msgid, node);
		}

		if (DEBUG) {
			System.out.println("registerMessage succeeded");
		}

		return true;
	}

	/* 
	 *  fqdn���LMNtalDaemon��������Ͽ����Ƥ��뤫�ɤ�����ǧ����
	 *  @param fqdn Fully Qualified Domain Name�ʥۥ���̾
	 *  @return nodeTable����Ͽ����Ƥ���LMNtalNode��InetAddress����ۥ���̾�������String����Ӥ��롣��äƤ���true������ʳ���false��
	 */
	public static boolean isRegisted(String fqdn) {
		if (DEBUG) {
			System.out.println("now in LMNtalDaemon.isRegisted(" + fqdn + ")");
		}

		Collection c = nodeTable.values();
		Iterator it = c.iterator();

		while (it.hasNext()) {
			if (((LMNtalNode) (it.next()))
				.getInetAddress()
				.getCanonicalHostName()
				.equalsIgnoreCase(fqdn)) {
				if (DEBUG) {
					System.out.println(
						"LMNtalDaemon.isRegisted(" + fqdn + ") is true!");
				}
				return true;
			}
		}

		if (DEBUG) {
			System.out.println(
				"LMNtalDaemon.isRegisted(" + fqdn + ") is false!");
		}

		return false;
	}

	/*
	 * ��å�����msgid��ȯ�Ԥ����Ρ��ɤ�õ���������˻Ȥ�
	 * 
	 * @param msgid ��å�����ID��int����ʤ���Integer�ʤΤϻ��ͤǤ���
	 * @return ��å�����msgid��ȯ�Ԥ���LMNtalNode�����Ĥ���ʤ��ä���null��
	 *   
	 */
	public static LMNtalNode getNodeFromMsgId(Integer msgid) {
		if (DEBUG) {
			System.out.println("getNodeFromMsgId(" + msgid + ")");
		}

		synchronized (msgTable) {
			return (LMNtalNode) msgTable.get(msgid);
		}
	}

	/*
	 * Fully Qualified Domain Name fqdn���б�����LMNtalNode��õ����
	 * 
	 * @param fqdn �ۥ���̾��Fully Qualified Domain Name�Ǥ������ 
	 * @return nodeTable����Ͽ����Ƥ���LMNtalNode��InetAddress����ۥ���̾�������String����Ӥ��롣��äƤ��餽��LMNtalNode������ʳ���null��
	 */
	public static LMNtalNode getLMNtalNodeFromFQDN(String fqdn) {
		if (DEBUG) {
			System.out.println(
				"now in LMNtalDaemon.getLMNtalNodeFromFQDN(" + fqdn + ")");
		}

		Collection c = nodeTable.values();
		Iterator it = c.iterator();

		try {
			InetAddress ip = InetAddress.getByName(fqdn);
			String ipstr = ip.getHostAddress();
			LMNtalNode node;

			while (it.hasNext()) {
				node = (LMNtalNode) (it.next());

				if (node.getInetAddress().getHostAddress().equals(ipstr)) {
					return node;
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	 *  fqdn���LMNtalDaemon����Ͽ�������Ͽ���Ƥ�餦
	 * 
	 * @param fqdn �ۥ���̾��Fully Qualified Domain Name�Ǥ������ 
	 * @return �����åȤ�������register(socket,node)������������true������ʳ���false��
	 */
	public static boolean connect(String fqdn) {
		System.out.println("now in LMNtalDaemon.connect(" + fqdn + ")");
		//TODO firewall�ˤҤä����äƥѥ��åȤ����Ǥ�������ɤ����뤫��

		if (isRegisted(fqdn)) {
			//���Ǥ���³�Ѥߤξ��

			//TODO �����������Ƥ��뤫���ǧ����

			//Socket��ȤäƤ���

			//connect������

			//OK��������true���֤�

			//�ֻ����ʤ��ä���false���֤�

			return true;
		} else {
			try {
				//������³�ξ��
				InetAddress ip = InetAddress.getByName(fqdn);

				Socket socket = new Socket(fqdn, 60000);

				BufferedReader in =
					new BufferedReader(
						new InputStreamReader(socket.getInputStream()));

				BufferedWriter out =
					new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));

				Thread t =
					new Thread(
						new LMNtalDaemonMessageProcessor(socket, in, out));
				t.start();

				LMNtalNode node = new LMNtalNode(ip, in, out);
				return register(socket, node);
			} catch (Exception e) {
				System.out.println(
					"ERROR in LMNtalDaemon.connect(" + fqdn + ")");
				e.printStackTrace();
				return false;
			}
		}
	}

	/*
	 * ��å�����message��target����ž������
	 * 
	 * @param target ž����
	 * @param message ��å�����
	 * @return BufferedWriter.write()��Ƥ����true���֤��Ƥ��롣
	 */
	public static boolean sendMessage(LMNtalNode target, String message) {
		try {
			target.out.write(message);
			target.out.flush();

			return true;
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.sendMessage: " + e.toString());
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * ��³���ڤ롣
	 * 
	 * @param socket ��³���ڤꤿ�������åȡ�
	 * @return socket.close()������true������ʳ���false��  
	 */
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
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * �ǥХå��ѡ�nodeTable, registedRuntimeTable, msgTable����Ϥ��롣 
	 */
	static void dumpHashMap() {
		Set tmpSet;

		tmpSet = nodeTable.entrySet();
		System.out.println("Dump nodeTable: ");
		System.out.println(tmpSet);

		tmpSet = registedRuntimeTable.entrySet();
		System.out.println("Dump registedRuntimeTable: ");
		System.out.println(tmpSet);

		tmpSet = msgTable.entrySet();
		System.out.println("Dump msgTable: ");
		System.out.println(tmpSet);
	}

	/*
	 * ��դ�int���֤���rgid�Ȥ�msgid�Ȥ��˻Ȥ���Randmom.nextInt()���֤��Ƥ�������ġ�
	 */
	public static int makeID() {
		return r.nextInt();
	}
}
