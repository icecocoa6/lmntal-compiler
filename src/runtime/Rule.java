package runtime;

import java.util.*;

public final class Rule {
	// ��Ȥ� Instruction
	public List memMatch;
	public List atomMatches; //?
	public List body;
	private String text;
	
	/**
	 * �դĤ��Υ��󥹥ȥ饯����
	 *
	 */
	public Rule() {
		memMatch    = new ArrayList();
		atomMatches = new ArrayList();
		body        = new ArrayList();
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
		l = atomMatches.listIterator();
		Env.p("--atommatches :");
		while(l.hasNext()) {
			Iterator ll = ((List)l.next()).iterator();
			Env.p((Instruction)ll.next());
		} 
		
		l = memMatch.listIterator();
		Env.p("--memmatch :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		l = body.listIterator();
		Env.p("--body :");
		while(l.hasNext()) Env.p((Instruction)l.next());
	}
	
	public String toString() {
		return text;
	}
}
