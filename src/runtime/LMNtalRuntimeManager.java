package runtime;

import java.util.HashMap;
import java.util.Iterator;

import daemon.LMNtalDaemonMessageProcessor;


/**
 * �׻��Ρ��ɴ������饹
 * @author n-kato, nakajima
 */

public final class LMNtalRuntimeManager {

	/** �׻��Ρ���ɽ��String -> AbstractLMNtalRuntime��*/
	static HashMap runtimeids = new HashMap();
	/** �׻��Ρ���ɽ�����ѳ��Ϥ��� */
	public static void init() {}
	
	/** ���ꤵ�줿�ۥ��Ȥ���³�����׻��Ρ���ɽ����Ͽ����
	 *  ���Ǥ���Ͽ����Ƥ��������¸���ǧ���롣��¸���ǧ�Ǥ��ʤ�����null���֤���*/
	public static AbstractLMNtalRuntime connectRuntime(String node) {
		node = node.intern();

		//������Ϥɤ���
		if(LMNtalDaemonMessageProcessor.isMyself(node)){
			//localhost�ʤ�  ��ʬ���Ȥ��֤�
			return Env.theRuntime;
		}
		
		//remote
		RemoteLMNtalRuntime ret = (RemoteLMNtalRuntime)runtimeids.get(node);			

		if (ret == null) {
			ret = new RemoteLMNtalRuntime(node);
			runtimeids.put(node,ret);
		}
		
		if (ret.connect()){
			//�����Ƥ���
			return ret;			
		} else {
			//���Ԥ�����null
			return null;
		}
	}

	/** ��Ͽ����Ƥ������Ƥ�LMNtalRuntime��λ�����׻��Ρ���ɽ����Ͽ�������롣
	 * TODO Env.theRuntime �� terminate ���ʤ��Ƥ褤�Τ��ɤ��������餫�ˤ��� */
	public static void terminateAll() {
		Iterator it = runtimeids.keySet().iterator();
		while (it.hasNext()) {
			AbstractLMNtalRuntime machine = (AbstractLMNtalRuntime)runtimeids.get(it.next());
			machine.terminate();
		}
		runtimeids.clear();
	}
}
