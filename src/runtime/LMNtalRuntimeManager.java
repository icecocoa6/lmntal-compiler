package runtime;

import java.util.HashMap;
import java.util.Iterator;

import daemon.LMNtalDaemonMessageProcessor;


/*
 * �ַ׻��Ρ��ɴ������饹��
 */

final class LMNtalRuntimeManager{

	/** �׻��Ρ���ɽ��String -> AbstractLMNtalRuntime��*/
	static HashMap runtimeids = new HashMap();
	/** �׻��Ρ���ɽ�����ѳ��Ϥ��� */
	public static void init() {}
	/** ���ꤵ�줿ʪ���ޥ������³�����׻��Ρ���ɽ����Ͽ����
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
		
		//�����Ƥ��뤫����		
		if (ret.connect()){
			//�����Ƥ�����
		} else {
			//���Ԥ�����null
			return null;
		}

		return ret;
	}

	/** ��Ͽ����Ƥ������Ƥ�ʪ���ޥ����λ�����׻��Ρ���ɽ����Ͽ��������
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
