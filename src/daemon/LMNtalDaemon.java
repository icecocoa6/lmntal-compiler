package daemon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
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
	 * ���㡧 rgid  �� runtime group id
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
	
	/*
	 * �����åȤ���³����ɽ
	 */
	static HashMap nodeTable = new HashMap();
	
	/*
	 * ������ˤ���runtime��ɽ
	 */
	static HashMap registedLocalRuntimeTable = new HashMap();
	
	/*
	 *  ��³��rgid��ɽ
	 */
	static HashMap registedRemoteRuntimeTable = new HashMap();
	
	/*
	 * ��å�������ɽ
	 */
	static HashMap msgTable = new HashMap();

	/*
	 * ��ư���줿LocalLMNtalRuntime��ɽ
	 */
	//static HashMap slaveRuntimeTable = new HashMap();

	/*
	 * ���η׻����ˤ�����Υ����Х��ID�����
	 */
	//static HashMap localMemTable = new HashMap();


	/*
	 * id����Τ˻Ȥ������४�֥�������
	 */
	static Random r = new Random();

	/*
	 * ���ۥ��Ȥ�fqdn����Ȥ�InetAddress.getLocalHost()
	 */
	static String myhostname;
	
	/*
	 * ���󥹥ȥ饯���� tcp60000�֤�ServerSocket�򳫤�������
	 */
	public LMNtalDaemon() {
		try {
			servSocket = new ServerSocket(60000);
			myhostname = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
			e.printStackTrace();
		}
	}

	/*
	 * ���󥹥ȥ饯���� tcp��portnum�֥ݡ��Ȥ�ServerSocket�򳫤�������
	 * @param portnum ServerSocket���Ϥ�tcp�ݡ����ֹ�
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

/*
 * ��³���Ԥ�����³���褿��LMNtalNode�����������Ͽ��������LMntalDaemonMessageProcessor����åɤ�ư���롣
 * 
 *  (non-Javadoc)
 * @see java.lang.Runnable#run()
 */
	public void run() {
		if(DEBUG)System.out.println("LMNtalDaemon.run()");

		Socket tmpSocket;
		BufferedReader tmpInStream;
		BufferedWriter tmpOutStream;

		while (true) {
			try {
				tmpSocket = servSocket.accept(); //���ͥ�����󤬤���ޤ��Ԥ�

				if (DEBUG)System.out.println("accepted socket: " + tmpSocket);

				//����stream			
				tmpInStream =
					new BufferedReader(
						new InputStreamReader(tmpSocket.getInputStream()));

				//����stream
				tmpOutStream =
					new BufferedWriter(
						new OutputStreamWriter(tmpSocket.getOutputStream()));

				//��Ͽ����
				if (registerNode(tmpSocket,
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
	 * Socket��key, LMNtalNode��value�Ȥ���HashMap(nodeTable)����Ͽ����
	 * 
	 * @param socket �����å�
	 * @param node LMNtal�Ρ���
	 * 
	 * @return socket�Ȥ�������������¸�ߤ��Ƥ�����false
	 */
	static boolean registerNode(Socket socket, LMNtalNode node) {
		if (DEBUG)System.out.println("registerNode(" + socket.toString() + ", " + node.toString() + ")");

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
	 *  LMNtalDaemonMessageProcessor.run()��˰�ư 20040706 nakajima
	 *  
	 * @param msgid ���졼�֥�󥿥��ब�ֻ����֤����˻Ȥ���å�����ID
	 */
//	public static void createRemoteRuntime(int msgid) {
//		String cmdLine =
//			new String(
//				"java daemon/SlaveLMNtalRuntimeLauncher "
//					+ LMNtalDaemon.makeID()
//					+ " "
//					+ msgid);
//		if (DEBUG) {
//			System.out.println(cmdLine);
//		}
//
//		try {
//			remoteRuntime = Runtime.getRuntime().exec(cmdLine);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	/*
	 * createRemoteRuntime���������줿���졼�֥�󥿥���(LocalLMNtalRuntime)����Ͽ����
	 * 
	 * LMNtalDaemonMessageProcessor.run()��˰�ư: 20040706 nakajima
	 * 
	 * @param socket 
	 * @param rgid �������줿LocalLMNtalRuntime��rgid��Integer�ʤΤϻ��ͤǤ���
	 */
//	boolean registerSlaveRuntime(Socket socket, Integer rgid){
//		if (DEBUG)System.out.println("registerSlaveRuntime(" + socket.toString() + ", " + rgid.toString() + ")");
//		
//		synchronized (slaveRuntimeTable){
//			if(slaveRuntimeTable.containsKey(socket)) return false;
//			
//			slaveRuntimeTable.put(socket, rgid);
//		}
//		return true;
//	}

	/*
	 * �������󥿥����HashMap����Ͽ���롣key��rgid, value��Socket��
	 * Ʊ���˳������socket -> ��Ʊ�Τ�socket�Ȥ���HashMap�ˤ���Ͽ���롣
	 * 
	 * @param rgid runtime group id
	 * @param socket rgid����ĥ饤�󥿥��ब���ĥ����å�
	 * @return rgid�ʥ�����¸�ߤ������false
	 */
	public static boolean registerLocal(String rgid, Socket socket) {
		if (DEBUG)System.out.println("registerLocal(" + rgid + ", " + socket.toString() + ")");

		//rgid -> socket
		synchronized (registedLocalRuntimeTable) {
			if (registedLocalRuntimeTable.containsKey(rgid)) {
				if (DEBUG)System.out.println("registerLocal failed");
				
				return false;
			}

			registedLocalRuntimeTable.put(rgid, socket);
		}

		if (DEBUG)System.out.println("registerLocal succeeded");
		return true;
	}

	public static boolean registerRemote(String rgid, Socket socket){
		if (DEBUG)System.out.println("registerRemote(" + rgid + ", " + socket.toString() + ")");
		
		synchronized(registedRemoteRuntimeTable){
			if(registedRemoteRuntimeTable.containsKey(rgid)){
				if (DEBUG)System.out.println("registerRemote failed");
				return false;
			}
			registedRemoteRuntimeTable.put(rgid, socket);
		}
		if (DEBUG)System.out.println("registerRemote succeeded");
		return true;
	}
	
	/*
	 * ��å�������HashMap����Ͽ���롣key��msgid, value��LMNtalNode��
	 * 
	 * @param msgid ��å�����ID��int����ʤ���Integer�ʤΤϻ��ͤǤ���
	 * @param node msgid�ʥ�å�������ȯ�Ԥ���LMNtalNode��
	 * @return msgid�ʥ�����¸�ߤ��Ƥ�����false
	 */
	public static boolean registerMessage(String msgid, LMNtalNode node) {
		if (DEBUG)System.out.println("registerMessage(" + msgid + ", " + node.toString() + ")");

		synchronized (msgTable) {
			if (msgTable.containsKey(msgid)) {
				return false;
			}

			msgTable.put(msgid, node);
		}

		if (DEBUG)System.out.println("registerMessage succeeded");

		return true;
	}

	/* 
	 *  fqdn���LMNtalDaemon��������Ͽ����Ƥ��뤫�ɤ�����ǧ����
	 *  @param fqdn Fully Qualified Domain Name�ʥۥ���̾
	 *  @return nodeTable����Ͽ����Ƥ���LMNtalNode��InetAddress����ۥ���̾�������String����Ӥ��롣��äƤ���true������ʳ���false��
	 */
	public static boolean isRegisted(String fqdn) {
		if (DEBUG) System.out.println("now in LMNtalDaemon.isRegisted(" + fqdn + ")");
		

		Collection c = nodeTable.values();
		Iterator it = c.iterator();

		while (it.hasNext()) {
			if (((LMNtalNode) (it.next()))
				.getInetAddress()
				.getCanonicalHostName()
				.equalsIgnoreCase(fqdn)) {
				if (DEBUG) System.out.println("LMNtalDaemon.isRegisted(" + fqdn + ") is true!");
				return true;
			}
		}

		if (DEBUG) System.out.println("LMNtalDaemon.isRegisted(" + fqdn + ") is false!");
		

		return false;
	}

	public static LMNtalNode getNode(Socket socket){
		return (LMNtalNode) nodeTable.get(socket);
	}

	public static Socket getRemoteSocket(String rgid){
		return (Socket)registedRemoteRuntimeTable.get(rgid);
	}
	
	public static Socket getLocalSocket(String rgid){
		return (Socket)registedLocalRuntimeTable.get(rgid);
	}
	
	/*
	 * ��å�����msgid��ȯ�Ԥ����Ρ��ɤ�õ���������˻Ȥ�
	 * 
	 * @param msgid ��å�����ID��int����ʤ���Integer�ʤΤϻ��ͤǤ���
	 * @return ��å�����msgid��ȯ�Ԥ���LMNtalNode�����Ĥ���ʤ��ä���null��
	 *   
	 */
	public static LMNtalNode getNodeFromMsgId(String msgid) {
		if (DEBUG)System.out.println("getNodeFromMsgId(" + msgid + ")");

		synchronized (msgTable) {
			return (LMNtalNode)msgTable.get(msgid);
		}
	}

	/*
	 * Fully Qualified Domain Name fqdn���б�����LMNtalNode��õ����
	 * 
	 * @param fqdn �ۥ���̾��Fully Qualified Domain Name�Ǥ������ 
	 * @return nodeTable����Ͽ����Ƥ���LMNtalNode��InetAddress����ۥ���̾�������String����Ӥ��롣��äƤ��餽��LMNtalNode������ʳ���null��
	 */
	public static LMNtalNode getLMNtalNodeFromFQDN(String fqdn) {
		if (DEBUG)System.out.println("now in LMNtalDaemon.getLMNtalNodeFromFQDN(" + fqdn + ")");
		

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
	 * ������Ͽ�Ѥߤξ��ϡ���å����������ä������Ƥ��뤫�������롣�ֻ���������true����ʤ��ä���false��
	 */
	public static boolean connect(String fqdn, String rgid) {
		if(DEBUG)System.out.println("now in LMNtalDaemon.connect(" + fqdn + ", rgid:" + rgid +  ")");
		//todo firewall�ˤҤä����äƥѥ��åȤ����Ǥ�������ɤ����뤫��

		if (isRegisted(fqdn)) {
			//���Ǥ���³�Ѥߤξ��

			//todo ����ifʸ����Ȥ���ñ�Υƥ���

			LMNtalNode target = getLMNtalNodeFromFQDN(fqdn);

			//connect������
			send(target.out, fqdn, rgid, "connect");

			return true;

			//�ֻ����Ԥ�  
//			try {
//				String ans = target.in.readLine(); //todo �������Ϥ�connect���Ф����ֻ��Ȥϸ¤�ʤ��ΤǴְ�äƤ���
//
//				if(ans != null){
//					return true; //�������äƤ��Ƥ���������Ƥ��롣�㤨fail�Ǥ⡣
//				} else {
//					return false;
//				}
//			} catch (IOException e1) {
//				e1.printStackTrace();
//				return false; //��λ
//			}
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
				return registerNode(socket, node);
			} catch (Exception e) {
				System.out.println(
					"ERROR in LMNtalDaemon.connect(" + fqdn + ", rgid: " + rgid + ")");
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
				"ERROR in LMNtalDaemon.sendMessage()");
			e.printStackTrace();
		}

		return false;
	}

	static void respond(BufferedWriter out, String msgid, String message){
		try {
			out.write("res " + msgid + " " + message + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.respond()");
			e.printStackTrace();
		}
	}

	static void respondAsOK(BufferedWriter out, String msgid){
		try {
			out.write("res " + msgid + " ok\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.respondAsOK()");
			e.printStackTrace();
		}
	}

	
	static void respondAsFail(BufferedWriter out, String msgid){
		try {
			out.write("res " + msgid + " fail\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.respondAsFail()");
			e.printStackTrace();
		}
	}
	
	static void send(BufferedWriter out, String fqdn, String rgid, String command){
		try {
			out.write(LMNtalDaemon.makeID() + " \"" + fqdn + "\" " + rgid + " " + command + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
		}
	}
	static void send(BufferedWriter out, String fqdn, String rgid, String command, String arg1){
		try {
			out.write(LMNtalDaemon.makeID() + " \"" + fqdn + "\" " + rgid + " " + command + " " + arg1 + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
		}
	}

	static void send(BufferedWriter out, String fqdn, String rgid, String command, String arg1, String arg2){
		try {
			out.write(LMNtalDaemon.makeID() + " \"" + fqdn + "\" " + rgid + " " + command + " " + arg1 + " " + arg2 + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
		}
	}
	static void send(BufferedWriter out, String fqdn, String rgid, String command, String arg1, String arg2, String arg3){
		try {
			out.write(LMNtalDaemon.makeID() + " \"" + fqdn + "\" " + rgid + " " + command + " " + arg1 + " " + arg2 + " " + arg3 + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
		}
	}
	static void send(BufferedWriter out, String fqdn, String rgid, String command, String arg1, String arg2, String arg3, String arg4){
		try {
			out.write(LMNtalDaemon.makeID() + " \"" + fqdn + "\" " + rgid + " " + command + " " + arg1 + " " + arg2 + " " + arg3 + " " + arg4 + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
		}
	}
	static void send(BufferedWriter out, String fqdn, String rgid, String command, String arg1, String arg2, String arg3, String arg4, String arg5){
		try {
			out.write(LMNtalDaemon.makeID() + " \"" + fqdn + "\" " + rgid + " " + command + " " + arg1 + " " + arg2 + " " + arg3 + " " + arg4 + " " + arg5 + "\n");
			out.flush();
		} catch (IOException e) {
			System.out.println("ERROR in LMNtalDaemon.send()");
			e.printStackTrace();
		}
	}
	
	//TODO localhost���Υ�å������Υե����ޥå�
	static void sendLocal(BufferedWriter out, String rgid, String command){
			
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
		System.out.println("Dump nodeTable: ");
		System.out.println(nodeTable.entrySet());

		System.out.println("Dump registedLocalRuntimeTable: ");
		System.out.println(registedLocalRuntimeTable.entrySet());

		System.out.println("Dump registedRemoteRuntimeTable: ");
		System.out.println(registedRemoteRuntimeTable.entrySet());

		System.out.println("Dump msgTable: ");
		System.out.println(msgTable.entrySet());
	}

	/*
	 * ��դ�int���֤���rgid�Ȥ�msgid�Ȥ��˻Ȥ������ޤȤ����InetAddress.getLocalHost()+";"+Randmom.nextLong()���֤��ͤ��֤��Ƥ��������
	 *  todo ��դ�ID����
	 */
	public static String makeID() {
		return myhostname+ ":" + r.nextLong();
	}
}