package runtime;

import java.util.HashMap;
import java.util.Iterator;

import daemon.LMNtalNode;
import daemon.LMNtalDaemon;
import daemon.LMNtalRuntimeMessageProcessor;
import java.net.Socket;


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
	
	/** ���ꤵ�줿�ۥ��Ȥ���³�����׻��Ρ���ɽ����Ͽ���롣
	 *  ���Ǥ���Ͽ����Ƥ��������¸���ǧ���롣��¸���ǧ�Ǥ��ʤ�����null���֤���
	 *  ���Ƥ�ʬ���ƤӽФ��ʤ�С�������Υǡ�������³���롣
	 *  <p>���ߤμ����Ǥϡ���ǧ��˥֥�å����롣
	 * �������롼�륹��åɤ�Ĺ���֥֥�å�����ΤǤ褯�ʤ��Ȥ����ܼ�Ū�����꤬���롣
	 * 
	 * @param nodedesc �Ρ��ɼ��̻ҡʸ��ߤ�fqdn�Τߡ� */
	public static AbstractLMNtalRuntime connectRuntime(String nodedesc) {
		String fqdn = nodedesc;
		//�����褬localhost�ʤ�  ��ʬ���Ȥ��֤�
		if(LMNtalNode.isMyself(fqdn)){
			return Env.theRuntime;
		}
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
		if (daemon != null) return true;
		try {
			// ����VM�ϥޥ����Ρ��ɤǤ���
			Socket socket = new Socket("localhost", LMNtalDaemon.DEFAULT_PORT);
			String rgid = Env.theRuntime.runtimeid;
			daemon = new LMNtalRuntimeMessageProcessor(socket,rgid);
			Thread t = new Thread(daemon);
			t.start();
			if (!daemon.sendWaitRegisterLocal("master")) {
				throw new Exception("cannot connect to daemon");
			}
			return true;
		}
		catch (Exception e) {
			System.out.println("Cannot connect to LMNtal deamon (not started?)");
			if (daemon != null) {
				daemon.close();
				daemon = null;
			}
			return false;
		}
	}
	
	private static Object terminateLock = "";
	/** ��Ͽ����Ƥ������Ƥ�RemoteLMNtalRuntime��λ�����׻��Ρ���ɽ����Ͽ�������롣
	 *  Env.theRuntime �� terminate ���ʤ���*/
	public static void terminateAllNeighbors() {
		synchronized(terminateLock) { // ��ʣž���ɻߤΤ���ʲ���
			Iterator it = runtimeids.keySet().iterator();
			while (it.hasNext()) {
				AbstractLMNtalRuntime machine = (AbstractLMNtalRuntime)runtimeids.get(it.next());
				machine.terminate();
			}
			runtimeids.clear();
		}
	}
}
