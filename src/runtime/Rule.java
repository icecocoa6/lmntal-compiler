package runtime;

import java.util.*;

public final class Rule {
	// ��Ȥ� Instruction
	public List memMatch;
	public List atomMatches; //?
	public List body;
	
	public Rule() {
		memMatch    = new ArrayList();
		atomMatches = new ArrayList();
		body        = new ArrayList();
	}
	
	/**
	 * ̿����ξܺ٤���Ϥ���
	 *
	 */
	public void showDetail() {
		Iterator l;
		l = atomMatches.listIterator();
		Env.p("--atommatches :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		l = memMatch.listIterator();
		Env.p("--memmatch :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		l = body.listIterator();
		Env.p("--body :");
		while(l.hasNext()) Env.p((Instruction)l.next());
	}
}
