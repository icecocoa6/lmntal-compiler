package runtime;

import java.util.HashMap;
import java.util.Iterator;


/*
 * �ַ׻��Ρ��ɴ������饹��
 */

final class LMNtalRuntimeManager{

	/** �׻��Ρ���ɽ��String -> AbstractLMNtalRuntime��*/
	static HashMap runtimeids = new HashMap();
	/** �׻��Ρ���ɽ�����ѳ��Ϥ��� */
	public static void init() {}
	/** ���ꤵ�줿ʪ���ޥ������³�����׻��Ρ���ɽ����Ͽ���� */
	public static AbstractLMNtalRuntime connectRuntime(String node) {
		node = node.intern();
		AbstractLMNtalRuntime ret = (AbstractLMNtalRuntime)runtimeids.get(node);
		if (ret == null) {
			ret = new RemoteLMNtalRuntime(node);
			runtimeids.put(node,ret);
		}
		return ret;
	}
	/** ��Ͽ����Ƥ������Ƥ�ʪ���ޥ����λ�����׻��Ρ���ɽ����Ͽ�������� */
	public static void terminateAll() {
		Iterator it = runtimeids.keySet().iterator();
		while (it.hasNext()) {
			AbstractLMNtalRuntime machine = (AbstractLMNtalRuntime)runtimeids.get(it.next());
			machine.terminate();
		}
		runtimeids.clear();
	}
}
