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
	public String text = "";
	/** ���Υ롼���ɽ����ʸ����ʾ�ά�ʤ��� */
	public String fullText ="";
	
	/** �롼��̾ */
	public String name;
	
	/** ���ֹ� by inui */
	public int lineno;
	
	/* �롼��Ŭ�Ѳ�� */
	public int apply = 0;
	/* �롼��Ŭ�Ѥ�������� */
	public int succeed = 0;
	/* �롼��Ŭ�Ѥι�׻��� */
	public long time = 0;
	
	/** ���� */
	public Uniq uniq;
	
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
		this();
		this.text = text;
	}
	/**
	 * �롼��ʸ����ʾ�ά�ʤ��ˤĤ����󥹥ȥ饯��
	 * @param text �롼���ʸ����ɽ��
	 * @param fullText �롼���ʸ����ɽ���ʾ�ά�ʤ���
	 */
	public Rule(String text, String fullText) {
		this(text);
		this.fullText = fullText;
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
		if (Env.debug == 0 && !Env.compileonly) return;
		
		Iterator l;
		Env.p("Compiled Rule " + this);
		
		l = atomMatch.listIterator();
		Env.p("--atommatch:", 1);
		while(l.hasNext()) Env.p((Instruction)l.next(), 2);

		l = memMatch.listIterator();
		Env.p("--memmatch:", 1);
		while(l.hasNext()) Env.p((Instruction)l.next(), 2);
		
		if (guard != null) {
			l = guard.listIterator();
			Env.p("--guard:" + guardLabel + ":", 1);
			while(l.hasNext()) Env.p((Instruction)l.next(), 2);
		}
		
		if (body != null) {
			l = body.listIterator();
			Env.p("--body:" + bodyLabel + ":", 1);
			while(l.hasNext()) Env.p((Instruction)l.next(), 2);
		}
			
		Env.p("");
	}
	
	public String toString() {
//		return text;
		if (Env.compileonly) return "";
//		if (Env.compileonly) return (name!=null) ? name : "";
		return name!=null && !name.equals("") ? name : text;
//		return name;
	}
	
	/**
	 * @return fullText �롼��Υ���ѥ����ǽ��ʸ����ɽ��
	 */
	public String getFullText() {
		return fullText;
	}
}
