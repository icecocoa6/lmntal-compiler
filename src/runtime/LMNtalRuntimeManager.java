package runtime;

import java.util.HashMap;
import java.util.Iterator;

import daemon.LMNtalNode;
import daemon.LMNtalDaemon;
import daemon.LMNtalRuntimeMessageProcessor;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * �׻��Ρ��ɴ������饹
 * @author n-kato, nakajima
 */

public final class LMNtalRuntimeManager {
	/** ������Υǡ����Ȥ��̿�ϩ */
	public static LMNtalRuntimeMessageProcessor daemon = null;
	/** �׻��Ρ���ɽ: nodedesc (String) -> RemoteLMNtalRuntime */
	static HashMap runtimeids = new HashMap();

	/** �׻��Ρ���ɽ�����ѳ��Ϥ��� */
	public static void init() {}
	
	/** �Ρ��ɼ��̻�nodedesc���̿��ѥΡ���̾�ʸ��ߤ�globalIPAddress�Υƥ�����ɽ���ˤ��Ѵ����롣
	 * ���Ԥ������null���֤���*/
	public static String nodedescToFQDN(String nodedesc) {
		try {
			String fqdn = java.net.InetAddress.getByName(nodedesc).getHostAddress();
			return fqdn;
		} catch (UnknownHostException e) {
			return null;
		}
	}
	/** ���ꤵ�줿�ۥ��Ȥ���³�����׻��Ρ���ɽ����Ͽ���롣
	 *  ���Ǥ���Ͽ����Ƥ��������¸���ǧ���롣��¸���ǧ�Ǥ��ʤ�����null���֤���
	 *  ���Ƥ�ʬ���ƤӽФ��ʤ�С�������Υǡ�������³���롣
	 *  <p>���ߤμ����Ǥϡ���¸�γ�ǧ��˥֥�å����롣
	 * �������롼�륹��åɤ�Ĺ���֥֥�å�����ΤǤ褯�ʤ��Ȥ����ܼ�Ū�����꤬���롣
	 * 
	 * @param nodedesc �Ρ��ɼ��̻ҡʸ��ߤ�fqdn�Τߡ� */
	public static AbstractLMNtalRuntime connectRuntime(String nodedesc) {
		if(Env.debug > 0)System.out.println("LMNtalRuntimeManager.connectRuntime()");
		String fqdn = nodedescToFQDN(nodedesc);
		if (fqdn == null) return null;
		//�����褬localhost�ʤ�  ��ʬ���Ȥ��֤�
		if(LMNtalNode.isMyself(fqdn)){
			//if(Env.debug > 0)System.out.println("LMNtalRuntimeManager.connectRuntime(): �����褬localhost�����鼫ʬ���Ȥ��֤�");
			return Env.theRuntime;
		}
		
		//if(Env.debug > 0)System.out.println("LMNtalRuntimeManager.connectRuntime(): �����褬remote�ˤ�����");
		//�ʲ��ϰ����褬remote�ˤ�����
		if (!connectToDaemon()) return null;			
		
		RemoteLMNtalRuntime ret = (RemoteLMNtalRuntime)runtimeids.get(fqdn);			

		if (ret == null) {
			ret = new RemoteLMNtalRuntime(fqdn);
			runtimeids.put(fqdn, ret);
		}
		
		if (ret.connect()){
			//�����Ƥ���
			return ret;			
		} else {
			//���Ԥ�����null
			return null;
		}
	}
	
	/** ��⡼�ȥۥ��Ȥ�����³�����ä��Ȥ��˸ƤФ�롣
	 * �б�����RemoteLMNtalRuntime��¸�ߤ��ʤ���к������롣
	 * @param nodedesc ��⡼�ȥۥ��Ȥ�̾��ä��Ρ��ɼ��̻ҡʸ��ߤ�fqdn�Τߡ� */
	public static AbstractLMNtalRuntime connectedFromRemoteRuntime(String nodedesc) {
		RemoteLMNtalRuntime ret = (RemoteLMNtalRuntime)runtimeids.get(nodedesc);
		if (ret == null) {
			ret = new RemoteLMNtalRuntime(nodedesc);
			runtimeids.put(nodedesc, ret);
		}
		return ret;
	}
	
	/** ������Υǡ�������³���롣���Ǥ���³���Ƥ�����ϲ��⤷�ʤ���*/
	public static boolean connectToDaemon() {
		if(Env.debug > 0)System.out.println("LMNtalRuntimeManager.connectToDaemon()");
		
		if (daemon != null) return true;
		try {
			// ����VM�ϥޥ����Ρ��ɤǤ���
			Socket socket = new Socket("localhost", LMNtalDaemon.DEFAULT_PORT);
			String rgid = Env.theRuntime.runtimeid;
			daemon = new LMNtalRuntimeMessageProcessor(socket,rgid);
			Thread t = new Thread(daemon, "LMNtalRuntimeMessageProcessor");
			t.start();
			if (!daemon.sendWaitRegisterLocal("MASTER")) {
				throw new Exception("LMNtalRuntimeManager.connectToDaemon(): cannot connect to daemon");
			}
			return true;
		}
		catch (Exception e) {
			System.out.println("LMNtalRuntimeManager.connectToDaemon(): Cannot connect to LMNtal deamon (not started?)");
			e.printStackTrace();
			if (daemon != null) {
				daemon.close();
				daemon = null;
			}
			return false;
		}
	}
	public static void disconnectFromDaemon() {
		if (daemon != null) {
			if(Env.debug > 0)System.out.println("LMNtalRuntimeManager.disconnectFromDaemon()");
			// �ڼ����(n-kato) unregisterlocal��Ȥ鷺������̵�Ѥ��ڤ�Ф����ȻפäƤ�����Ǥ����ɡ�����ʤ�Ǥ����� �ڤ줿��daemon¦�Ǿ����MASTERɽ�������Ƹ������Υ���åɤ������Ȥ������ˡ�2004-08-27
			// �ڲ�����(mizuno)
			//     LMNtalRuntimeMessageProcessor��in.readLine()�ǥ֥�å����Ƥ������˥����åȤ��Ĥ����SocketException��ȯ�����Ƥ��ޤ��Τǡ����β�����ˡ�Ȥ��ơ����礵������̤��Ƥ������Ƥߤޤ�����
			//     unregisterlocal�ˤ�äƥǡ����¦��OutputStream���Ĥ��Ƥ�館�С�EOF�������Ƥ���Τ�readLine��null���֤��Ƥ���ޤ���
			//     �Ǥ⡢������ˡ�Ǥ�ǡ����¦��OutputStream���Ĥ�������close���ƤФ�Ƥ��ޤ���Ʊ�����ʤΤǤ⤦�Ҥȹ���ɬ�פʤ褦�Ǥ����ʡĤȤ������Ȥ˺������Ĥ��ޤ�������
			//     �֤⤦�Ҥȹ��ספΰ� : readLine()����null�����äƤ�����close����
			//     unregisterlocal��Ȥ鷺�ˡ�readLine()��SocketException��ȯ���������ۤäƽ�λ���롢�Ȥ�����ˡ������󤢤�ޤ�����
			//     ���������꤬�������Ȥ����㳰�ȶ��̤��Ǥ��ʤ��Τ���������Ȥ�����ͳ�Ǥ�����ˡ�����򤷤ޤ�����
			daemon.sendWaitUnregisterLocal();

			daemon.close(); 
			if(Env.debug > 0)System.out.println("LMNtalRuntimeManager.disconnectFromDaemon(): the socket has closed.");
			daemon = null;
		}
	}
	private static Object terminateLock = "";
	/** ��Ͽ����Ƥ������Ƥ�RemoteLMNtalRuntime��λ�����׻��Ρ���ɽ����Ͽ�������롣
	 *  Env.theRuntime �� terminate ���� (n-kato) 2004-09-17 */
	public static void terminateAll() {
		Env.theRuntime.terminate();
		synchronized(terminateLock) { // ��ʣž���ɻߤΤ���ʲ���
			Iterator it = runtimeids.keySet().iterator();
			while (it.hasNext()) {
				RemoteLMNtalRuntime machine = (RemoteLMNtalRuntime)runtimeids.get(it.next());
				daemon.sendWait(machine.hostname,"TERMINATE");
			}
			runtimeids.clear();
		}
	}
}
