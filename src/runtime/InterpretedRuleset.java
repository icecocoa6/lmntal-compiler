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
	case Instruction.DEREF:
	    //deref [-dstatom, +srcatom, +srcpos, +dstpos]
	    if (atomArgs[1].args[srcpos] == atomArgs[1].args[dstpos]) {
		//�����Υ��ȥ��dstatom���������롣
	    }
	    break;

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

	case Instruction.ANYMEM:
	    //anymem [??dstmem, srcmem]
	    for (int i = 0; i <  memArgs[1].mems.size(); i++ ){
		//�Υ�֥�å��󥰤ǤΥ�å��������ߤ롣
		//if(����){�ƻ����dstmem����������}
	    }
	    break;

	case Instruction.FINDATOM:
	    // findatom [dstatom, srcmem, func]
	    ListIterator i = memArgs[1].atom.iterator();
	    while (i.hasNext()){
		Atom a;
		a = (Atom)i.next();
		if ( a.functor == atomArgs[1]){
		    atomArgs[0] = a;
		}
	    }
	    break;

	case Instruction.FUNC:
	    //func [srcatom, func]
	    if (atomArgs[0].functor == func){
		//Ʊ�����ä�
	    } else {
		//��äƤ�
	    }
	    break;

	case Instruction.NORULES:
	    //norules [srcmem]
	    if(memArgs[0].rules.isEmpty()){
		//�롼�뤬¸�ߤ��ʤ����Ȥ��ǧ
	    } else {
		//�롼�뤬¸�ߤ��Ƥ��뤳�Ȥ��ǧ
	    }
	    break;

	case Instruction.NATOMS:
	    // natoms [srcmem, count]
	    //if (memArgs[0].atoms.size() == count) { //��ǧ����  }
	    break;

	case Instruction.NFREELINKS:
	    //nfreelinks [srcmem, count]
	    //if (memArgs[0].freeLinks.size() == count) { //��ǧ����  }
	    break;

	case Instruction.NMEMS:
	    //nmems [srcmem, count]
	    //if (memArgs[0].mems.size() == count) { //��ǧ����  }
	    break;

	case Instruction.EQ:
	    //eq [atom1, atom2]
	    //eq [mem1, mem2]
	    if(memArgs.length == 0){
		if (atomArgs[0] == atomArgs[1]){
		    //Ʊ��Υ��ȥ�򻲾�
		}
	    } else {
		if (memArgs[0] == memArgs[1]){
		    //Ʊ�����򻲾�
		}
	    } 
	    break;

	case Instruction.NEQ:
	    //neq [atom1, atom2]
	    //neq [mem1, mem2]
	    if(memArgs.length == 0){
		if (atomArgs[0] != atomArgs[1]){
		    //Ʊ��Υ��ȥ�򻲾�
		}
	    } else {
		if (memArgs[0] != memArgs[1]){
		    //Ʊ�����򻲾�
		}
	    } 
	    break;

	    //�ѻ�
	    //	case Instruction.LOCK:
	    //	    break;

	case Instruction.UNLOCK:
	    //unlock [srcmem]

	    break;

	case Instruction.REMOVEATOM:
	    //removeatom [srcatom]
	    
	    break;

	case Instruction.REMOVEMEM:
	    //removemem [srcmem]

	    break;

	case Instruction.INSERTPROXIES:
	    //insertproxies [parentmem M], [srcmem N]
	    
	    break;

	case Instruction.REMOVEPROXIES:

	    break;

	case Instruction.NEWATOM:
	    //newatom [dstatom, srcmem, func] 
	    memArgs[1].atoms.add(atomArgs[1]);
	    atomArgs[0] = atomArgs[1];
	    break;

	case Instruction.NEWMEM:
	    //newmem [dstmem, srcmem] 
	    memArgs[1] = new Membrane(memArgs[0]);
	    memArgs[0].mems.add(memArgs[1]);
	    break;

	case Instruction.NEWLINK:
	    //newlink [atom1, pos1, atom2, pos2]
	    break;

	case Instruction.RELINK:
	    break;
	case Instruction.UNIFY:
	    break;
	case Instruction.DEQUEUEATOM:
	    break;
	case Instruction.DEQUEUEMEM:
	    break;
	case Instruction.MOVEMEM:
	    break;
	case Instruction.RECURSIVELOCK:
	    break;
	case Instruction.RECURSIVEUNLOCK:
	    break;
	case Instruction.COPY:
	    break;
	case Instruction.NOT:
	    break;
	case Instruction.STOP:
	    break;
	case Instruction.REACT:
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
