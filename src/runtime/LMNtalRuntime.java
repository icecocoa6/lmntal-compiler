package runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import util.Stack;

/** ���ʪ���ޥ��󥯥饹 */
abstract class AbstractMachine {
	protected String runtimeid;
	/** ����ʪ���ޥ���˿��������ʤ���å�����Ƥ��ʤ��롼���������������Ǥʤ��¹��쥹���å����Ѥࡣ*/
	abstract AbstractTask newTask();
	/** ����ʪ���ޥ���˻���ο������ĥ�å����줿�롼���������������μ¹��쥹���å����Ѥࡣ*/
	abstract AbstractTask newTask(AbstractMembrane parent);
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
	boolean idle = false;
	static final int maxLoop = 10;
	
	Task() {
		root = new Membrane(this);
		memStack.push(root);
	}
	Task(AbstractMembrane parent) {
		root = new Membrane(this);
		root.lock(root);
		root.activate(); // ���μ¹��쥹���å����Ѥ�
		parent.addMem(root);
	}
	
	boolean isIdle(){
		return idle;
	}
	
	void exec() {
		Membrane mem = (Membrane)memStack.peek();
		if(mem == null || !mem.lock(mem)) {
			// ���줬̵�����ޤ�������Υ�å�������Ǥ��ʤ��Ȥ�
			if (mem != null) { System.out.println(mem);}			idle = true;
			return;
		}
		
		for(int i=0; i < maxLoop && mem == memStack.peek(); i++){
			// ���줬�Ѥ��ʤ��� & �롼�ײ����ۤ��ʤ���
//			System.out.println("mems  = " + memStack);
//			System.out.println("atoms = " + mem.getReadyStackStatus());
			Atom a = mem.popReadyAtom();
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
					if(((Ruleset)it.next()).react(mem)) {
						flag = true;
						if (memStack.peek() != mem) break;
					}
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
						if(((AbstractMembrane)it.next()).isStable() == false)
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
		// Inline
		Inline.initInline();
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
	
	AbstractTask newTask() {
		Task t = new Task();
		tasks.add(t);
		return t;
	}
	AbstractTask newTask(AbstractMembrane parent) {
		Task t = new Task(parent);
		tasks.add(t);
		return t;
	}

	/** ʪ���ޥ��󤬻��ĥ��������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
	 *  Tasks���Ѥޤ줿��˼¹Ԥ��롣�ƥ�����ͥ��ˤ��뤿��ˤ�
	 *  ���������ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
	 */
	public void exec() {
		boolean allIdle;
		do {
			allIdle = true; // idle�Ǥʤ������������Ĥ��ä���false�ˤʤ롣
			Iterator it = tasks.iterator();
			while (it.hasNext()) {
				Task task = (Task)it.next();
				if (!task.isIdle()) { // idle�Ǥʤ������������ä���
					task.exec(); // �ҤȤ�����¹�
					allIdle = false; // idle�Ǥʤ�������������
					break;
				}
			}
		} while(!allIdle);
	}
}


