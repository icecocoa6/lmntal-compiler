package runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import util.Stack;

/** ��ݥޥ��󥯥饹 */
abstract class AbstractMachine {
	/** �롼���� */
	protected AbstractMembrane root;
	
	/** �롼����μ��� */
	AbstractMembrane getRoot() {
		return root;
	}
}

/**
 * TODO �֥������פ�̾���ѹ�
 * TODO �����ƥ�롼�륻�åȤΥޥå��ƥ��ȡ��ܥǥ��¹�
 * TODO �ޥ���֤ξ岼�ط��μ�����newMachine��ޥ��󤬻��Ĥ褦�ˤ���
 *
 */
final class Machine extends AbstractMachine {
	/** �¹��쥹���å� */
	Stack memStack = new Stack();
	boolean idle;
	static final int maxLoop = 10;
	
	Machine() {
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

/** �׻��Ρ��� */
final class LMNtalRuntime {
	List machines = new ArrayList();
	AbstractMembrane rootMem;
	
	/** �׻��Ρ��ɤ����ĥޥ������Ƥ�idle�ˤʤ�ޤǼ¹ԡ�<br>
	 *  machines���Ѥޤ줿��˼¹Ԥ��롣�ƥޥ���ͥ��ˤ��뤿��ˤ�
	 *  �ޥ����ڹ�¤�ˤʤäƤ��ʤ��Ƚ���ʤ���ͥ���٤Ϥ��Ф餯̤������
	 */
	LMNtalRuntime(Ruleset init){
		Machine root = newMachine();
		rootMem = root.getRoot();
		init.react((Membrane)rootMem);
	}
	
	AbstractMembrane getRootMem(){
		return rootMem;
	}
	
	void exec() {
		boolean allIdle;
		Iterator it;
		Machine m;
		do{
			allIdle = true; // idle�Ǥʤ��ޥ��󤬸��Ĥ��ä���false�ˤʤ롣
			it = machines.iterator();
			while(it.hasNext()){
				m = (Machine)it.next();
				if(!m.isIdle()){ // idle�Ǥʤ��ޥ��󤬤��ä���
					m.exec(); // �ҤȤ�����¹�
					allIdle = false; // idle�Ǥʤ��ޥ��󤬤���
					break;
				}
			}
		}while(!allIdle);
	}
	
	Machine newMachine() {
		Machine m = new Machine();
		machines.add(m);
		return m;
	}
}