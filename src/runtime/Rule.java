package runtime;

import java.util.*;

public final class Rule {
	// Instruction �Υꥹ��
	public List atomMatch;
	public List memMatch;
	public List body;
	private String text;
	
	/**
	 * �դĤ��Υ��󥹥ȥ饯����
	 *
	 */
	public Rule() {
		atomMatch = new ArrayList();
		memMatch  = new ArrayList();
		body      = new ArrayList();
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
		
		l = body.listIterator();
		Env.d("--body:", 1);
		while(l.hasNext()) Env.d((Instruction)l.next(), 2);
	
		Env.d("");
	}
	
	public String toString() {
		return text;
	}
}
