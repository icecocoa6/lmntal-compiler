package runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import util.Stack;

/** ���ʪ���ޥ��󥯥饹 */
abstract class AbstractMachine {
	protected String runtimeid;
	/** ����ʪ���ޥ���ˡʿ��������ʤ��˥롼�����������롣
	 * <p>
	 * �롼����ο���ޤ���null������˼��褦�ˤ��������褤�����Τ�ʤ���
	 * �ܺ٤ϡ����������������빽ʸ����ꤹ�뺢�˷��� */
	abstract AbstractTask newTask();
}

/** ��ݥ��������饹 */
abstract class AbstractTask {
	/** ʪ���ޥ��� */
	protected AbstractMachine runtime;
	/** �롼���� */
	protected AbstractMembrane root;
	
	/** ʪ���ޥ���μ��� */
	AbstractMachine getMachine() {
		return runtime;
	}
	/** �롼����μ��� */
	AbstractMembrane getRoot() {
		return root;
	}
}

/**
 * TODO �����ƥ�롼�륻�åȤΥޥå��ƥ��ȡ��ܥǥ��¹�
 * TODO �������֤ξ岼�ط��μ����򤹤롩
 */
final class Task extends AbstractTask {
	/** �¹��쥹���å� */
	Stack memStack = new Stack();
	Stack bufferedStack = new Stack();
	boolean idle;
	static final int maxLoop = 10;
	
	Task() {
		root = new Membrane(this);
		memStack.push(root);
		idle = false;
	}
	
	boolean isIdle(){
		return idle;
	}
	
	void exec() {
		if(memStack.isEmpty()){ // ���ʤ�idle�ˤ��롣
			idle = true;
			return;
		}
		// �¹��쥹���å������Ǥʤ�
		Membrane mem = (Membrane)memStack.peek();
		if(!mem.lock(mem)) return; // ��å�����
		
		Atom a;
		for(int i=0; i < maxLoop && mem == memStack.peek(); i++){
			// ���줬�Ѥ��ʤ��� & �롼�ײ����ۤ��ʤ���
			
			a = mem.popReadyAtom();
			Iterator it = mem.rulesetIterator();
			boolean flag;
			if(a != null){ // �¹��쥹���å������Ǥʤ��Ȥ�
				flag = false;
				while(it.hasNext()){ // ����Τ�ĥ롼���a��Ŭ��
					if(((Ruleset)it.next()).react(mem, a)) flag = true;
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					if(!mem.isRoot()) {mem.getParent().enqueueAtom(a);} 
				}
				else {}// �����ƥॳ���륢�ȥ�ʤ����ˤĤߡ�����������
			}else{ // �¹��쥹���å������λ�
				flag = false;
				while(it.hasNext()){ // ���Ƴ�ƥ��Ȥ�Ԥ�
					if(((Ruleset)it.next()).react(mem)) flag = true;
				}
				if(flag == false){ // �롼�뤬Ŭ�ѤǤ��ʤ��ä���
					memStack.pop(); // �����pop
					// ���줬root�줫�Ŀ������Ĥʤ顢����������
					if(mem.isRoot() && mem.getParent() != null) {
						mem.getParent().activate();
					}
					// ���줬����stable�ʤ顢�������stable�ˤ��롣
					it = mem.memIterator();
					flag = false;
					while(it.hasNext()){
						if(((Membrane)it.next()).isStable() == false)
								flag = true;
					}
					if(flag == false) mem.toStable();
				}
			}
		}
		// ���줬�Ѥ�ä�or�����������֤����顢��å���������ƽ�λ
		mem.unlock();
	}
}

public final class LMNtalRuntime extends Machine {
	Membrane globalRoot;
	
	public LMNtalRuntime(){
		AbstractTask t = newTask();
		globalRoot = (Membrane)t.getRoot();
	}

	/**
	 * �������Ŭ�Ѥ���롼���globalRoot���Ŭ�Ѥ��롣
	 * ������롼�롢�����REPL���������������������롼���Ŭ�Ѥ��뤿��˻��Ѥ��롣
	 * @deprecated
	 */
	public void applyRulesetOnce(Ruleset r){
		r.react(globalRoot);
	}
	
	public Membrane getGlobalRoot(){
		return globalRoot;
	}
	/**@deprecated*/
	public Membrane getRoot(){
		return globalRoot;
	}
}

/** ʪ���ޥ��� */
class Machine extends AbstractMachine {
	List tasks = new ArrayList();
	
	/** ʪ���ޥ��󤬻��ĥ��������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
	 *  Tasks���Ѥޤ줿��˼¹Ԥ��롣�ƥ�����ͥ��ˤ��뤿��ˤ�
	 *  ���������ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
	 */

	/**
	 * 	���Υޥ���˥��������롣�����Ƥ���塢�롼�����membrane.setParent()���ƤФ�롩
	 * �������ơ�Task�Υ��󥹥ȥ饯���˿�����Ϥ��褦�ˤ����ۤ���������
	 */
	AbstractTask newTask() {
		Task t = new Task();
		tasks.add(t);
		return t;
	}

/* TODO���õMaster�ʳ��Ǥϡ���������ǽ�˺��ɬ�פϤʤ���
	public Machine(){
		newTask();
	}
*/	
//	AbstractMembrane getRootMem(){
//		return rootMem;
//	}
	
	public void exec() {
		boolean allIdle;
		Iterator it;
		Task m;
		do{
			allIdle = true; // idle�Ǥʤ������������Ĥ��ä���false�ˤʤ롣
			it = tasks.iterator();
			while(it.hasNext()){
				m = (Task)it.next();
				if(!m.isIdle()){ // idle�Ǥʤ������������ä���
					m.exec(); // �ҤȤ�����¹�
					allIdle = false; // idle�Ǥʤ�������������
					break;
				}
			}
		}while(!allIdle);
	}
}


