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
 * @author nakajima, n-kato
 *
 */

//todo �Ѹξ������������ƥ�

/**
 * ���Υ���åɤ��ǡ����Ȥ���ʪ��Ū�ʷ׻����ζ����ˤ��ꡢ
 * tcp60000�֤˵�¤äƤ��롣���ͥ�������褿��LMNtalMessageProcessor����åɤ򤢤����Ԥ������롣
 * LMNtalDaemon�ϰ�Ĥ�ʪ��Ū�ʷ׻�����1����åɤ���������ʤ���
 * 
 * @author nakajima, n-kato
 *
 */
public class LMNtalDaemon implements Runnable {
	/*
	 * ���㡧 rgid  �� runtime group id
	 */
	static boolean DEBUG = true; //LMNtalDaemon��ñ�Τǵ�ư����Τ�Env.debug��0����礭���ʤ�ʤ�...
	
	public static final int DEFAULT_PORT = 60000;
	
	int portnum = DEFAULT_PORT;
	
	/** listen���륽���å� */
	ServerSocket servSocket = null;
	
	/**
	 * ��⡼�ȤΥǡ����Ȥ���³ɽ: InetAddress -> LMNtalNode
	 */
	static HashMap remoteHostTable = new HashMap();
	
	/**
	 * ������ˤ����󥿥����ɽ: rgid (String) -> LMNtalNode
	 */
	static HashMap runtimeGroupTable = new HashMap();

	// todo msgTable �� msgTagTable �� LMNtalNode �˰ܴɤ���
	
	/**
	 * ��å�������ɽ: msgid (String) -> LMNtalNode
	 */
	static HashMap msgTable = new HashMap();
	/**
	 * msgid (String) -> tag (String)
	 */
	static HashMap msgTagTable = new HashMap();

	/*
	 * id����Τ˻Ȥ������४�֥�������
	 */
	static Random r = new Random();

	/**
	 * ���ۥ��Ȥ�fqdn����Ȥ�InetAddress.getLocalHost()
	 */
	static String myhostname;
	
	/**
	 * ���󥹥ȥ饯���� DEFAULT_PORT�ǻ��ꤵ�줿�ݡ����ֹ��ServerSocket�򳫤�������
	 */
	public LMNtalDaemon() {
		this(DEFAULT_PORT);
	}
	
	/**
	 * ���󥹥ȥ饯���� tcp��portnum�֥ݡ��Ȥ�ServerSocket�򳫤�������
	 * @param portnum ServerSocket���Ϥ�tcp�ݡ����ֹ�
	 */
	public LMNtalDaemon(int portnum) {
		this.portnum = portnum;
		try {
			servSocket = new ServerSocket(portnum);
//			myhostname = InetAddress.getLocalHost().toString();
		} catch (IOException e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
			e.printStackTrace();
		}
	}
	static {
		try {
			myhostname = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ᥤ���ϥƥ����ѡ���ʬ���ȡʥ���åɡˤ�1�Ĥ�����Τߡ�
	 * @author nakajima
	 *
	 */
	public static void main(String args[]) {
		Thread t = new Thread(new LMNtalDaemon(), "LMNtalDaemon");
		t.start();
	}

	/**
	 * ��³���Ԥ�����³���褿��LMNtalNode�����������Ͽ��������LMNtalDaemonMessageProcessor����åɤ�ư���롣
	 */
	public void run() {
		if(DEBUG)System.out.println("LMNtalDaemon.run()");

		while (true) {
			try {
				Socket socket = servSocket.accept(); //���ͥ�����󤬤���ޤ��Ԥ�

				if (DEBUG)System.out.println("accepted socket: " + socket);
				LMNtalDaemonMessageProcessor node = new LMNtalDaemonMessageProcessor(socket);
				
				//��Ͽ����
				if (registerRemoteHostNode(node)) {
					//��Ͽ������
					Thread t2 = new Thread(node, "LMNalDaemonMessageProcessor");
					t2.start();
				} else {
					//��Ͽ���ԡ�����λ
					node.close();
				}
			} catch (IOException e) {
				System.out.println("ERROR in LMNtalDaemon.run(): ");
				e.printStackTrace();
				break;
			}
		}
	}

	////////////////////////////////////////////////////////////////
	
	/**
	 * ��⡼�ȥۥ��ȤΥΡ��ɤ�remoteHostTable����Ͽ����
	 * @param node LMNtalNode
	 * @return ���Υۥ��Ȥ�������Ͽ����Ƥ�����false
	 */
	static boolean registerRemoteHostNode(LMNtalDaemonMessageProcessor node) {
		if (DEBUG)System.out.println("registerNode(" + node.toString() + ")");
		
		synchronized (remoteHostTable) {
			if (remoteHostTable.containsKey(node.getInetAddress())) {
				return false;
			}
			remoteHostTable.put(node.getInetAddress(), node);
		}
		return true;
	}
	
	/* 
	 *  fqdn���LMNtalDaemon��������Ͽ����Ƥ��뤫�ɤ�����ǧ����
	 *  @param fqdn Fully Qualified Domain Name�ʥۥ���̾
	 *  @return nodeTable����Ͽ����Ƥ���LMNtalNode��InetAddress����ۥ���̾�������String����Ӥ��롣��äƤ���true������ʳ���false��
	 */
	public static boolean isHostRegistered(String fqdn) {
		// return (getLMNtalNodeFromFQDN(fqdn) != null);
		
		if (DEBUG) System.out.println("now in LMNtalDaemon.isRegisted(" + fqdn + ")");
		

		Collection c = remoteHostTable.values();
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
	
	/**
	 * Fully Qualified Domain Name fqdn���б�����LMNtalNode��õ����
	 * 
	 * @param fqdn �ۥ���̾��Fully Qualified Domain Name�Ǥ������ 
	 * @return nodeTable����Ͽ����Ƥ���LMNtalNode��InetAddress����ۥ���̾�������String����Ӥ��롣��äƤ��餽��LMNtalNode������ʳ���null��
	 */
	public static LMNtalNode getLMNtalNodeFromFQDN(String fqdn) {
		if (DEBUG)System.out.println("now in LMNtalDaemon.getLMNtalNodeFromFQDN(" + fqdn + ")");
		
		Collection c = remoteHostTable.values();
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

	/**
	 * fqdn���LMNtalDaemon����³���롣
	 * @param fqdn �ۥ���̾��Fully Qualified Domain Name�Ǥ������ 
	 */
	public static boolean makeRemoteConnection(String fqdn) {
		//�֥֥�å����ʤ��褦�ˤ����
		//todo 3ʬ��ï���̿��Ǥ��ʤ��ʤ�Τ���򤹤뤿������ѥ���åɤ���ʸ�󤷤Ǥ褤�� n-kato 2004-08-20

		if(DEBUG)System.out.println("LMNtalDaemon.makeRemoteConnection(" + fqdn + ")");
		
		if (isHostRegistered(fqdn)) return true;
		
		Socket socket;
		try {
			//������³�ξ��
			socket = new Socket(fqdn, DEFAULT_PORT);
			//socket.setSoTimeout(180000); //�Ȥꤢ����3ʬ
			//����ö�Ĥʤ���3ʬ�̿����ʤ��Ȥ���timeout��ȯ������ΤǤȤꤢ���������ȥ�����(2004-08-22 nakajima
			//todo ����³���褦�Ȥ����Ȥ�����nʬ������ä���timeout�פˤ���
			LMNtalDaemonMessageProcessor node = new LMNtalDaemonMessageProcessor(socket);
			if (registerRemoteHostNode(node)) {
				Thread t = new Thread(node, "LMNtalDaemonMessageProcessor");
				t.start();
				return true;
			}
			node.close();
			return false;
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.makeRemoteConnection(" + fqdn + ")");
			System.out.println("If java.net.SocketTimeoutException has raised, open TCP " + DEFAULT_PORT);
			e.printStackTrace();
			//todo  SocketTimeoutException�ΤȤ��Ϥ�����socket���Ĥ���٤�����
			//"If the timeout expires, a java.net.SocketTimeoutException is raised, though the Socket is still valid." (1.4.1 api specitifation.)
			
			return false;
		}
	}	

	////////////////////////////////////////////////////////////////

	public static boolean isRuntimeGroupRegistered(String rgid) {
		return runtimeGroupTable.containsKey(rgid);
	}
	public static boolean registerRuntimeGroup(String rgid, LMNtalNode node){
		if (DEBUG)System.out.println("registerRuntimeGroup(" + rgid + ", " + node.toString() + ")");
		
		synchronized(runtimeGroupTable){
			if(runtimeGroupTable.containsKey(rgid)){
				if (DEBUG)System.out.println("registerRuntimeGroup failed");
				return false;
			}
			runtimeGroupTable.put(rgid, node);
		}
		if (DEBUG)System.out.println("registerRuntimeGroup succeeded");
		return true;
	}

	public static boolean unregisterRuntimeGroup(String rgid){
		if (DEBUG)System.out.println("unregisterRuntimeGroup(" + rgid +  ")");
		
		synchronized(runtimeGroupTable){
			if(runtimeGroupTable.containsKey(rgid)){
				runtimeGroupTable.remove(rgid);
				if (DEBUG)System.out.println("unregisterRuntimeGroup succeeded");
				return true;
			}
		}
		return false;
	}

	public static LMNtalNode getRuntimeGroupNode(String rgid){
		return (LMNtalNode)runtimeGroupTable.get(rgid);
	}

	////////////////////////////////////////////////////////////////

	/**
	 * ��å�������HashMap����Ͽ���롣key��msgid, value��LMNtalNode��
	 * <p>
	 * ��å��������Ф��������å��������������Ͽ���롣
	 * todo ������å������������塢�б��Ϻ�����٤��Ǥ��롣
	 * 
	 * @param msgid ��å�����ID
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
	/** �����դ���msgTable����Ͽ���� */
	public static boolean registerMessageWithTag(String msgid, LMNtalNode node, String tag) {
		synchronized(msgTagTable) {
			if (msgTagTable.containsKey(msgid)) return false;
			if (!registerMessage(msgid,node)) return false;
			msgTagTable.put(msgid, tag);
			return true;
		}
	}

	/**
	 * ��å�����msgid���ֵ�����������
	 * @param msgid ��å�����ID
	 * @return ��å�����msgid���ֵѤ���LMNtalNode�����Ĥ���ʤ��ä���null��
	 */
	public static LMNtalNode unregisterMessage(String msgid) {
		synchronized (msgTable) {
			return (LMNtalNode)msgTable.remove(msgid);
		}
	}

	/**
	 * ��å�����msgid��ȯ�Ԥ����Ρ��ɤ�õ���������˻Ȥ�
	 * 
	 * @param msgid ��å�����ID
	 * @return ��å�����msgid��ȯ�Ԥ���LMNtalNode�����Ĥ���ʤ��ä���null��
	 * @deprecated
	 */
	public static LMNtalNode getNodeFromMsgId(String msgid) {
		if (DEBUG)System.out.println("getNodeFromMsgId(" + msgid + ")");

		synchronized (msgTable) {
			return (LMNtalNode)msgTable.get(msgid);
		}
	}

	public static String getTagForMsgId(String msgid) {
		synchronized (msgTagTable) {
			if (!msgTagTable.containsKey(msgid)) return null;
			return (String)msgTagTable.remove(msgid);
		}
	}

	////////////////////////////////////////////////////////////////

	/*
	 * �ǥХå��ѡ�nodeTable, registedRuntimeTable, msgTable����Ϥ��롣 
	 */
	static void dumpHashMap() {
		System.out.println("Dump nodeTable: ");
		System.out.println(remoteHostTable.entrySet());

//		System.out.println("Dump registedLocalRuntimeTable: ");
//		System.out.println(registedLocalRuntimeTable.entrySet());

		System.out.println("Dump registedRuntimeGroupTable: ");
		System.out.println(runtimeGroupTable.entrySet());

		System.out.println("Dump msgTable: ");
		System.out.println(msgTable.entrySet());
	}

	/*
	 * ��դ�int���֤���rgid�Ȥ�msgid�Ȥ��˻Ȥ������ޤȤ����InetAddress.getLocalHost()+":"+Randmom.nextLong()���֤��ͤ��֤��Ƥ��������
	 *  todo ��դ�ID����
	 */
	public static String makeID() {
		return myhostname+ ":" + r.nextLong();
	}
	public static String getLocalHostName() {
		return myhostname;
	}
}