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

//TODO �Ѹξ������������ƥ�

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

	static boolean DEBUG = true;
	
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

	// TODO msgTable �� msgTagTable �� LMNtalNode �˰ܴɤ���
	
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
	 * ���󥹥ȥ饯���� tcp60000�֤�ServerSocket�򳫤�������
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
			myhostname = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * �ᥤ���ϥƥ����ѡ���ʬ���ȡʥ���åɡˤ�1�Ĥ�����Τߡ�
	 * @author nakajima
	 *
	 */
	public static void main(String args[]) {
		Thread t = new Thread(new LMNtalDaemon());
		t.start();
	}

	/**
	 * ��³���Ԥ�����³���褿��LMNtalNode�����������Ͽ��������LMNtalDaemonMessageProcessor����åɤ�ư���롣
	 */
	public void run() {
		if(DEBUG)System.out.println("LMNtalDaemon.run()");

		while (true) {
			try {
				Socket tmpSocket = servSocket.accept(); //���ͥ�����󤬤���ޤ��Ԥ�

				if (DEBUG)System.out.println("accepted socket: " + tmpSocket);
				LMNtalDaemonMessageProcessor node = new LMNtalDaemonMessageProcessor(tmpSocket);
				
				//��Ͽ����
				if (registerRemoteHostNode(node)) {
					//��Ͽ������
					Thread t2 = new Thread(node);
					t2.start();
				} else {
					//��Ͽ���ԡ�����λ
					node.close();
				}
			} catch (IOException e) {
				System.out.println(
					"ERROR in LMNtalDaemon.run() " + e.toString());
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
		//TODO firewall�ˤҤä����äƥѥ��åȤ����Ǥ�������ɤ����뤫��

		if (isHostRegistered(fqdn)) return true;
		try {
			//������³�ξ��
			Socket socket = new Socket(fqdn, DEFAULT_PORT);
			LMNtalDaemonMessageProcessor node = new LMNtalDaemonMessageProcessor(socket);
			if (registerRemoteHostNode(node)) {
				Thread t = new Thread(node);
				t.start();
				return true;
			}
			node.close();
			return false;
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.makeRemoteConnection(" + fqdn + ")");
			e.printStackTrace();
			return false;
		}
	}	

	////////////////////////////////////////////////////////////////

	public static boolean isRuntimeGroupRegistered(String rgid) {
		return runtimeGroupTable.containsKey(rgid);
	}
	public static boolean registerRuntimeGroup(String rgid, LMNtalNode node){
		if (DEBUG)System.out.println("registerRemote(" + rgid + ", " + node.toString() + ")");
		
		synchronized(runtimeGroupTable){
			if(runtimeGroupTable.containsKey(rgid)){
				if (DEBUG)System.out.println("registerRemote failed");
				return false;
			}
			runtimeGroupTable.put(rgid, node);
		}
		if (DEBUG)System.out.println("registerRemote succeeded");
		return true;
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
	 *   
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
	 *  todo �����˰�դ�ID����
	 */
	public static String makeID() {
		return myhostname+ ":" + r.nextLong();
	}
}