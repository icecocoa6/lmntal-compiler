package runtime;

import java.util.*;

public final class Rule {
	// Instruction �Υꥹ��
	
	/** ���ȥ��Ƴ�롼��Ŭ�Ѥ�̿������Ƭ��̿���spec[2,*]�Ǥʤ���Фʤ�ʤ���*/
	public List atomMatch;
	/** ���Ƴ�롼��Ŭ�Ѥ�̿������Ƭ��̿���spec[1,*]�Ǥʤ���Фʤ�ʤ���*/
	public List memMatch;
	
	/** ������̿�����guardLabel.insts�ˤޤ���null */
	public List guard;
	/** �ܥǥ�̿�����bodyLabel.insts�ˤޤ���null */
	public List body;
	/** ��٥��դ�������̿����ޤ���null */
	public InstructionList guardLabel;
	/** ��٥��դ��ܥǥ�̿����ޤ���null */
	public InstructionList bodyLabel;
	/** ���Υ롼���ɽ����ʸ���� */
	private String text;
	
	//
	
	/**
	 * �դĤ��Υ��󥹥ȥ饯����
	 *
	 */
	public Rule() {
		atomMatch = new ArrayList();
		memMatch  = new ArrayList();
	}
	/**
	 * �롼��ʸ����Ĥ����󥹥ȥ饯��
	 * @param text �롼���ʸ����ɽ��
	 */
	public Rule(String text) {
		this.text = text;
	}
	
	/**
	 * ̿����ξܺ٤���Ϥ���
	 *
	 */
	public void showDetail() {
		Iterator l;
		Env.d("Compiled Rule " + this);
		
		/*
		l = atomMatch.listIterator();
		Env.d("--atommatches:", 1);
		while(l.hasNext()) {
			Iterator ll = ((List)l.next()).iterator();
			while(ll.hasNext()) Env.d(indent+(Instruction)ll.next());
		}
		*/
		
	
		l = atomMatch.listIterator();
		Env.d("--atommatch:", 1);
		while(l.hasNext()) Env.d((Instruction)l.next(), 2);

		l = memMatch.listIterator();
		Env.d("--memmatch:", 1);
		while(l.hasNext()) Env.d((Instruction)l.next(), 2);
		
		if (guard != null) {
			l = guard.listIterator();
			Env.d("--guard:" + guardLabel + ":", 1);
			while(l.hasNext()) Env.d((Instruction)l.next(), 2);
		}
		
		if (body != null) {
			l = body.listIterator();
			Env.d("--body:" + bodyLabel + ":", 1);
			while(l.hasNext()) Env.d((Instruction)l.next(), 2);
		}
			
		Env.d("");
	}
	
	public String toString() {
		return text;
	}
}
