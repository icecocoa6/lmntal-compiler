package runtime;

import java.util.*;

public final class Rule {
	// ��Ȥ� Instruction
	public List memMatch;
	public List atomMatch; //?
	public List body;
	private String text;
	
	/**
	 * �դĤ��Υ��󥹥ȥ饯����
	 *
	 */
	public Rule() {
		memMatch    = new ArrayList();
		atomMatch = new ArrayList();
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
		l = atomMatch.listIterator();
		Env.p("Rule.showDetail  this = "+this);
		
		/*
		Env.p("--atommatches :", 1);
		while(l.hasNext()) {
			Iterator ll = ((List)l.next()).iterator();
			while(ll.hasNext()) Env.p(indent+(Instruction)ll.next());
		}
		*/
		
		l = memMatch.listIterator();
		Env.p("--memmatch :", 1);
		while(l.hasNext()) Env.p((Instruction)l.next(), 2);
		
		l = body.listIterator();
		Env.p("--body :", 1);
		while(l.hasNext()) Env.p((Instruction)l.next(), 2);
		
		Env.p("");
	}
	
	public String toString() {
		return text;
	}
}
