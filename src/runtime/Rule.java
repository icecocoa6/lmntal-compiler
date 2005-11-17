package runtime;

import java.util.*;
import java.io.*;

public final class Rule implements Serializable {
	// Instruction �Υꥹ��
	
	/** ���ȥ��Ƴ�롼��Ŭ�Ѥ�̿�����atomMatchLabel.insts��
	 * ��Ƭ��̿���spec[2,*]�Ǥʤ���Фʤ�ʤ���*/
	public List atomMatch;
	/** ���Ƴ�롼��Ŭ�Ѥ�̿�����memMatchLabel.insts��
	 * ��Ƭ��̿���spec[1,*]�Ǥʤ���Фʤ�ʤ���*/
	public List memMatch;
	
	/** ������̿�����guardLabel.insts�ˤޤ���null��
	 * ��Ƭ��̿���spec[*,*]�Ǥʤ���Фʤ�ʤ���*/
	public List guard;
	/** �ܥǥ�̿�����bodyLabel.insts�ˤޤ���null��
	 * ��Ƭ��̿���spec[*,*]�Ǥʤ���Фʤ�ʤ���*/
	public List body;
	
	/** ��٥��դ����ȥ��Ƴ�롼��Ŭ��̿���� */
	public InstructionList atomMatchLabel;
	/** ��٥��դ����Ƴ�롼��Ŭ��̿���� */
	public InstructionList memMatchLabel;	
	/** ��٥��դ�������̿����ޤ���null */
	public InstructionList guardLabel;
	/** ��٥��դ��ܥǥ�̿����ޤ���null */
	public InstructionList bodyLabel;
	/** ���Υ롼���ɽ����ʸ���� */
	public String text;
	
	/** �롼��̾ */
	public String name;
	
	// todo ������4�ĤȤ�InstructionList���ݻ�����褦�ˤ���List���ѻߤ��롣
	
	/**
	 * �դĤ��Υ��󥹥ȥ饯����
	 *
	 */
	public Rule() {
//		atomMatch = new ArrayList();
//		memMatch  = new ArrayList();
		atomMatchLabel = new InstructionList();
		memMatchLabel = new InstructionList();
		atomMatch = atomMatchLabel.insts;
		memMatch = memMatchLabel.insts;
	}
	/**
	 * �롼��ʸ����Ĥ����󥹥ȥ饯��
	 * @param text �롼���ʸ����ɽ��
	 */
	public Rule(String text) {
		this.text = text;
	}
	/** �ѡ����������Ѥ��륳�󥹥ȥ饯�� */
	public Rule(InstructionList atomMatchLabel, InstructionList memMatchLabel, InstructionList guardLabel, InstructionList bodyLabel) {
		this.atomMatchLabel = atomMatchLabel;
		this.memMatchLabel = memMatchLabel;
		this.guardLabel = guardLabel;
		this.bodyLabel = bodyLabel;
		atomMatch = atomMatchLabel.insts;
		memMatch = memMatchLabel.insts;
		if (guardLabel != null)
			guard = guardLabel.insts;
		if (bodyLabel != null)
			body = bodyLabel.insts;
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
//		return text;
		return name!=null && !name.equals("") ? name : text;
//		return name;
	}
}
