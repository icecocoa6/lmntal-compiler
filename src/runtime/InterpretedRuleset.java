package runtime;

import java.util.*;

private class InterpretedReactor {
	InterpretedReactor(
		AbstractMembrane[] mems;
		Atom[] atoms;
		List vars;
		List insts;
		);

	/** ̿������᤹�롣
	 * @param mems  ���ѿ��Υ٥���
	 * @param atoms ���ȥ��ѿ��Υ٥���
	 * @param vars  ����¾���ѿ��Υ٥�����mems��atoms���ѻߤ���vars�����礹�롩��
	 * @param insts ̿����
	 * @param pc    ̿������Υץ���५����
	 * @return ̿����μ¹Ԥ������������ɤ������֤�
	 */
	private boolean interpret(int pc) {
		Iterator it;
		Functor func;
		while (pc < insts.size()) {
			Instruction inst = (Instruction)insts.get(pc++);
			switch (inst.getKind()) {

				//��⡧LOCALHOGE��HOGE��Ʊ�������ɤǤ�����
				//nakajima: 2003-12-12

			case Instruction.REACT:
				Rule rule = (Rule)inst.getArg1();
				List bodyInsts = (List)rule.body;
				Instruction spec = (Instruction)bodyInsts.get(0);
				int formals = spec.getIntArg1();
				int locals  = spec.getIntArg2();
				AbstractMembrane[] bodymems  = new AbstractMembrane[locals];
				Atom[]             bodyatoms = new Atom[locals];
				List memformals  = (List)inst.getArg2();
				List atomformals = (List)inst.getArg3();
				for (int i = 0; i < memformals.size(); i++) {
					bodymems[i]  = mems[((Integer)memformals.get(i)).intValue()];
				}
				for (int i = 0; i < atomformals.size(); i++) {
					bodyatoms[i]  = atoms[((Integer)atomformals.get(i)).intValue()];
				}
				InterpretedReactor ir = new InterpretedReactor(bodymems, bodyatoms, new ArrayList(), bodyInsts);
				ir.interpret(0);
				return true;
			case Instruction.ANYMEM: // anymem [-dstmem, srcmem] 
				it = mems[inst.getIntArg2()].mems.iterator();
				while (it.hasNext()){
					AbstractMembrane submem = (AbstractMembrane)it.next();
					if (submem.lock(mems[0])) {
						mems[inst.getIntArg1()] = submem;
						if (interpret(pc)) return true;
						submem.unlock();
					}
				}
				break;
			case Instruction.FINDATOM: // findatom [-dstatom, srcmem, funcref]
				func = (Functor)inst.getArg3();
				it = mems[inst.getIntArg2()].atoms.iteratorOfFunctor(func);
				while (it.hasNext()){
					Atom a = (Atom)it.next();
					atoms[inst.getIntArg1()] = a;					
					if (interpret(pc)) return true;
				}
				break;
			case Instruction.NEWATOM: // newatom [-dstatom, srcmem, funcref]
				func = (Functor)inst.getArg3();
				atoms[inst.getIntArg1()] = mems[inst.getIntArg2()].newAtom(func);
				break;
			case Instruction.PROCEED:
				return true;	



			default:
				System.out.println("Invalid rule");
				break;
			}
		}
		return false;
	}
}


// TODO �ڽ��סۥޥå��󥰸���������Ǽ���������å������Ʋ�������ɬ�פ�����
// memo����å���������ñ�̤��ꥹ�Ȥˤʤ�褦�˽���

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
	
	
	/**
	 * RuleCompiler �Ǥϡ��ޤ��������Ƥ���ǡ�����������ࡣ
	 * �Τǡ��äˤʤˤ⤷�ʤ�
	 */
	public InterpretedRuleset() {
		rules = new ArrayList();
		id = ++lastId;
	}
	
	/**
	 * ����롼��ˤĤ��ƥ��ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	public boolean react(Membrane mem, Atom atom) {
		boolean result = false;
		Iterator it = rules.iterator();
		while(it.hasNext()) {
			Rule r = (Rule)it.next();
			result |= matchTest(mem, atom, r.atomMatch, (Instruction)r.body.get(0));
		}
		return result;
	}
	
	/**
	 * ����롼��ˤĤ������Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	public boolean react(Membrane mem) {
		boolean result = false;
		Iterator it = rules.iterator();
		while(it.hasNext()) {
			Rule r = (Rule)it.next();
			result |= matchTest(mem, null, r.memMatch, (Instruction)r.body.get(0));
		}
		return result;
	}
	
	/**
	 * ���Ƴ�����ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	private boolean matchTest(Membrane mem, Atom atom, List matchInsts, Instruction spec) {
		Env.p("match."+matchInsts);
		int formals = spec.getIntArg1();
		int locals  = spec.getIntArg2();
		AbstractMembrane[] mems  = new AbstractMembrane[formals];
		Atom[]             atoms = new Atom[formals];
		mems[0]  = mem;
		atoms[1] = atom;
		InterpretedReactor ir = new InterpretedReactor(mems,atoms,new ArrayList(),matchInsts);
		return ir.interpret(0);
	}

	
	/**
	 * �롼���Ŭ�Ѥ��롣<br>
	 * ��������ȡ������Υ��ȥ�ν�°��Ϥ��Ǥ˥�å�����Ƥ����ΤȤ��롣
	 * @param ruleid Ŭ�Ѥ���롼��
	 * @param memArgs �°����Τ�������Ǥ�����
	 * @param atomArgs �°����Τ��������ȥ�Ǥ�����
	 * @author nakajima
	 * @deprecated
	 * 
	 */
	private void body(List rulebody, AbstractMembrane[] mems, Atom[] atoms) {
	/*	Iterator it = rulebody.iterator();
		while(it.hasNext()){
			Instruction hoge = (Instruction)it.next();


			//�����ʲ���switch�ϰ�ưͽ�� body��guard��ξ�������᥽�åɤ��������ͽ��
		switch (hoge.getID()){
		case Instruction.DEREF:
			//deref [-dstatom, +srcatom, +srcpos, +dstpos]
			//if (atomArgs[1].args[srcpos] == atomArgs[1].args[dstpos]) {
				//�����Υ��ȥ��dstatom���������롣
			//            }
			break;

		case Instruction.GETMEM:
			//getmem [-dstmem, srcatom]
			//TODO ������ʹ��:����ν�°��פȤϡ���[1]����ΤȤ���GETPARENT��Ȥ����ֿ���פΰ�̣��
			//ruby�Ǥ���getparent��Ʊ�������ˤʤäƤ�ΤǤȤꤢ������������
			//�����ȡ�Java�ǤǤ�̿�ᤴ�Ȥ˰����η�����ꤹ�뤿��ˡ�̾����ʬ���뤳�Ȥˤ��ޤ���
            
				memArgs[0] = memArgs[1].mem;
				//TODO �롼��¹�����ѿ��٥����򻲾��Ϥ����ơ������Ǵ��ܻ��Ȥ����߷פˤ��������褤��
				// �������ʤ��ȡ����ϰ��������ޤ�ȿ�Ǥ��ʤ���
				break;

		case Instruction.GETPARENT:
			//getparent [-dstmem, srcmem] 
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
			//atomArgs[0].args[pos1]    atomArgs[1].args[pos2]
			break;

		case Instruction.RELINK:
			//relink [atom1, pos1, atom2, pos2]
            
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
	}*/


	}
	public String toString() {
		StringBuffer s=new StringBuffer("");
		Iterator l;
		l = rules.iterator();
		while(l.hasNext()) s.append( ((Rule)l.next()).toString()+" " );
		return "@" + id + "  " + s;
	}
    
	public void showDetail() {
		Env.p("InterpretedRuleset.showDetail "+this);
		Iterator l;
		l = rules.listIterator();
		while(l.hasNext()) {
			Rule r = ((Rule)l.next());
			r.showDetail();
		} 
	}
}
/* 
 
[�Ǥ��ñ�ʼ�����ˡ]

4����Υǡ������б�����ArrayList���ݻ����롣
�Ȥꤢ�����ǽ�ϥ᥽�å���ζɽ��ѿ��Ȥ��ƤǤ褤��


**********

�᥽�åɤؤ����Ϥ�����Ǥʤ�ArrayList�ˤ��ơ�
�����ɽ��ѿ��Ѥˤ�Ȥ��󤹤褦�ˤ��뤿��˥������ѹ����Ƥ�빽�Ǥ���

*/


