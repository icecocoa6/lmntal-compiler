package runtime;


/** ��ݥ�󥿥���ʵ졧���ʪ���ޥ��󡢵졹����ݷ׻��Ρ��ɡ˥��饹��*/

abstract class AbstractLMNtalRuntime {
	protected String runtimeid;
	/** ���Υ�󥿥���˿��������ʤ���å�����Ƥ��ʤ��롼���������������Ǥʤ��¹��쥹���å����Ѥࡣ*/
	abstract AbstractTask newTask(AbstractMembrane parent);
	/** ���Υ�󥿥���μ¹Ԥ�λ���� */
	abstract public void terminate();
	
//	/** ��󥿥���Υ롼�륹��åɤ��Ф��ƺƼ¹Ԥ��׵᤹�롣*/
//	abstract public void awake();
}



/** ��󥿥��॰�롼�פ���ӥ����Х�롼����������������������󥿥���Υ��饹 */

public final class MasterLMNtalRuntime extends LocalLMNtalRuntime {
	private Membrane globalRoot;	// masterTask�ؤλ��Ȥ��������ʬ����䤹�����⤷��ʤ�

	public MasterLMNtalRuntime(){
		Task masterTask = new Task(this);
		tasks.add(masterTask);
		globalRoot = (Membrane)masterTask.getRoot();
		// Inline
		Inline.initInline();
		Env.theRuntime = this;
	}

//	/**
//	 * �������Ŭ�Ѥ���롼���globalRoot���Ŭ�Ѥ��롣
//	 * ������롼�롢�����REPL���������������������롼���Ŭ�Ѥ��뤿��˻��Ѥ��롣
//	 * @deprecated
//	 */
//	public void applyRulesetOnce(Ruleset r){
//		r.react(globalRoot);
//	}
	
	public final Membrane getGlobalRoot(){
		return globalRoot;
	}
	public final Task getMasterTask(){
		return (Task)globalRoot.getTask();
	}
//	/** �ޥ�����󥿥���Ȥ��Ƽ¹Ԥ��� */
//	public void run() {
//		RemoteLMNtalRuntime.init();
//		while (true) {
//			if (Env.fTrace) {
//				Env.p( Dumper.dump(getGlobalRoot()) );
//			}
//			localExec();
//			if (globalRoot.isStable()) break;
//			synchronized(this) {
//				try {
//					if (terminated) break;
//					wait();
//				}
//				catch (InterruptedException e) {}
//			}
//		}
//		RemoteLMNtalRuntime.terminateAll();
//	}
}

