package runtime;

import java.util.Iterator;
import java.util.Arrays;

public final class InterpretedRuleset extends Ruleset {
	/** �Ȥꤢ�����롼�������Ȥ��Ƽ��� */
	private int id;
	private static int lastId=600;
	
	
	/** ���Ƴ�¹���̿���󡣣����ܤ�ź�����ϥ롼���ֹ� */
	public Instruction[] memMatch;
	/** ���ȥ��Ƴ�¹���̿����Map�ˤ��٤��� */
	public Instruction[] atomMatches;
	/** �ܥǥ��¹���̿���󡣣����ܤ�ź�����ϥ롼���ֹ� */
	public Instruction[] body;
	
	public InterpretedRuleset() {
		id = ++lastId;
	}
	
	public void showDetail() {
		Iterator l;
		l = Arrays.asList( atomMatches ).listIterator();
		Env.p("--atommatches :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		l = Arrays.asList( memMatch ).listIterator();
		Env.p("--memmatch :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		l = Arrays.asList( body ).listIterator();
		Env.p("--body :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		Env.p(toString());
	}
	
	public String toString() {
		return "@" + id;
	}
	/**
	 * ���ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	boolean react(Membrane mem, Atom atom) {
		return false;
	}
	/**
	 * ���Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	boolean react(Membrane mem) {
		return false;
	}
	/**
	 * �롼���Ŭ�Ѥ��롣<br>
	 * ��������ȡ������Υ��ȥ�ν�°��Ϥ��Ǥ˥�å�����Ƥ����ΤȤ��롣
	 * @param ruleid Ŭ�Ѥ���롼��
	 * @param memArgs �°����Τ�������Ǥ�����
	 * @param atomArgs �°����Τ��������ȥ�Ǥ�����
	 */
	private void body(int ruleid, AbstractMembrane[] memArgs, Atom[] atomArgs) {
	}
}
