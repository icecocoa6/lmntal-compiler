package runtime;

import java.util.*;


/**
 * compile.RuleCompiler �ˤ�ä���������롣
 * @author n-kato, nakajima
 */
public final class InterpretedRuleset extends Ruleset {
    /** �롼�륻�å��ֹ� */
    private int id;
    private static int lastId=600;
	
	
    /** �Ȥꤢ�����롼�������Ȥ��Ƽ��� */
    public List rules;
	
    //	/** ���Ƴ�¹���̿���󡣣����ܤ�ź�����ϥ롼���ֹ� */
    //	public Instruction[] memMatch;
    //	/** ���ȥ��Ƴ�¹���̿����Map�ˤ��٤��� */
    //	public Instruction[] atomMatches;
    //	/** �ܥǥ��¹���̿���󡣣����ܤ�ź�����ϥ롼���ֹ� */
    //	public Instruction[] body;
	
    /**
     * RuleCompiler �Ǥϡ��ޤ��������Ƥ���ǡ�����������ࡣ
     * �Τǡ��äˤʤˤ⤷�ʤ�
     */
    public InterpretedRuleset() {
	rules = new ArrayList();
	id = ++lastId;
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
     * @author nakajima
     *
     * 
     */
    private void body(int ruleid, AbstractMembrane[] memArgs, Atom[] atomArgs) {
	switch (ruleid){


	case Instruction.GETMEM:
	    //getmem [dstmem, srcatom]
	    //TODO:ʹ��:����ν�°��פȤϡ�
	    //ruby�Ǥ���getparent��Ʊ�������ˤʤäƤ�ΤǤȤꤢ������������
	    memArgs[0] = memArgs[1].mem;
	    break;

	case Instruction.GETPARENT:
	    //getparent [dstmem, srcmem] 
	    memArgs[0] = memArgs[1].mem;
	    break;

	case Instruction.NEWMEM:
	    //newmem [dstmem, srcmem] 
	    memArgs[1] = new Membrane(memArgs[0]);
	    memArgs[0].mems.add(memArgs[1]);
	    break;

	case Instruction.NEWATOM:
	    //newatom [dstatom, srcmem, func] 
	    memArgs[1].atoms.add(atomArgs[1]);
	    atomArgs[0] = atomArgs[1];
	    break;

	case Instruction.REMOVEATOM:

	    break;

	case Instruction.REMOVEMEM:

	    break;

 	default:
	    System.out.println("Invalid rule");
	    break;
	}



    }
	
    public void showDetail() {
	Iterator l;
	l = rules.listIterator();
	while(l.hasNext()) ((Rule)l.next()).showDetail();
    }
}
