package runtime;

import java.util.*;

class InterpreterReactor {
	AbstractMembrane[] mems;
	Atom[] atoms;
	List vars;
	//List insts;
	InterpreterReactor(
		AbstractMembrane[] mems,
		Atom[] atoms,
		List vars /*, List insts*/
	) {
		Env.n("InterpreterReactor");
		this.mems = mems;
		this.atoms = atoms;
		this.vars = vars;
		//this.insts = insts;
	}

	/** ̿������᤹�롣
	 * @param mems  ���ѿ��Υ٥���
	 * @param atoms ���ȥ��ѿ��Υ٥���
	 * @param vars  ����¾���ѿ��Υ٥�����mems��atoms���ѻߤ���vars�����礹�롩��
	 * @param insts ̿����
	 * @param pc    ̿������Υץ���५����
	 * @return ̿����μ¹Ԥ������������ɤ������֤�
	 */
	boolean interpret(List insts, int pc) {
		Env.p("interpret : " + insts);
		Iterator it;
		Functor func;
		while (pc < insts.size()) {
			Instruction inst = (Instruction) insts.get(pc++);
			Env.p("Do " + inst);
			switch (inst.getKind()) {

				//��⡧LOCALHOGE��HOGE��Ʊ�������ɤǤ�����
				//nakajima: 2003-12-12
				//��⡧�����Ȥϰ���
				//nakajima: 2003-12-12

				//====����¾====��������====
				case Instruction.DUMMY :
					System.out.println(
						"SYSTEM ERROR: dummy instruction remains: " + inst);
					break;
					//case Instruction.UNDEF:
					//	break; //n-kato
					//====����¾====�����ޤ�====

					//====���ȥ�˴ط�������Ϥ�����ܥ�����̿��====��������====
				case Instruction.DEREF : //[-dstatom, srcatom, srcpos, dstpos]
					break;

				case Instruction.DEREFATOM : //[-dstatom, srcatom, srcpos]
					break;

				case Instruction.FINDATOM :
					// findatom [-dstatom, srcmem, funcref]
					func = (Functor) inst.getArg3();
					it = mems[inst.getIntArg2()].atoms.iteratorOfFunctor(func);
					while (it.hasNext()) {
						Atom a = (Atom) it.next();
						atoms[inst.getIntArg1()] = a;
						if (interpret(insts, pc))
							return true;
					}
					return false; //n-kato

				case Instruction.GETLINK : //[-link, atom, pos]
					break;
					//====���ȥ�˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====

					//====��˴ط�������Ϥ�����ܥ�����̿�� ====��������====
				case Instruction.LOCKMEM :
				case Instruction.LOCALLOCKMEM :
					// lockmem [-dstmem, freelinkatom]
					AbstractMembrane mem = atoms[inst.getIntArg2()].mem;
					if (mem.lock(mems[0])) {
						mems[inst.getIntArg1()] = mem;
						if (interpret(insts, pc))
							return true;
						mem.unlock();
					}
					return false; //n-kato

				case Instruction.ANYMEM :
				case Instruction.LOCALANYMEM : // anymem [-dstmem, srcmem] 
					it = mems[inst.getIntArg2()].mems.iterator();
					while (it.hasNext()) {
						AbstractMembrane submem = (AbstractMembrane) it.next();
						if (submem.lock(mems[0])) {
							mems[inst.getIntArg1()] = submem;
							if (interpret(insts, pc))
								return true;
							submem.unlock();
						}
					}
					return false; //n-kato

				case Instruction.GETMEM : //[-dstmem, srcatom]
					break;
				case Instruction.GETPARENT : //[-dstmem, srcmem]
					break;

					//====��˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====

					//====��˴ط�������Ϥ��ʤ����ܥ�����̿��====��������====
				case Instruction.TESTMEM : //[dstmem, srcatom]
					if (mems[inst.getIntArg1()]
						!= atoms[inst.getIntArg2()].mem)
						return false;
					break; //n-kato
				case Instruction.NORULES : //[srcmem] 
					break;
				case Instruction.NATOMS : //[srcmem, count]
					break;
				case Instruction.NFREELINKS : //[srcmem, count]
					break;
				case Instruction.NMEMS : //[srcmem, count]
					break;
				case Instruction.EQMEM : //[mem1, mem2]
					break;
				case Instruction.NEQMEM : //[mem1, mem2]
					break;
				case Instruction.STABLE : //[srcmem] 
					break;
				case Instruction.LOCK :
				case Instruction.LOCALLOCK : //[srcmem] 
					break;
					//====��˴ط�������Ϥ��ʤ����ܥ�����̿��====�����ޤ�====

					//====���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿��====��������====
				case Instruction.FUNC : //[srcatom, funcref]
					break;
				case Instruction.EQATOM : //[atom1, atom2]
					break;
				case Instruction.NEQATOM : //[atom1, atom2]
					break;
					//====���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿��====�����ޤ�====

					//====�ե��󥯥��˴ط�����̿��====��������====
				case Instruction.DEREFFUNC : //[-dstfunc, srcatom, srcpos]
					break;
				case Instruction.GETFUNC : //[-func, atom]
					break;
				case Instruction.LOADFUNC : //[-func, funcref]
					break;
				case Instruction.EQFUNC : //[func1, func2]
					break;
				case Instruction.NEQFUNC : //[func1, func2]
					break;
					//====�ե��󥯥��˴ط�����̿��====�����ޤ�====

					//====���ȥ��������ܥܥǥ�̿��====��������====
				case Instruction.REMOVEATOM :
				case Instruction.LOCALREMOVEATOM : //[srcatom]
					Atom atom;
					Atom a;
					atom = atoms[inst.getIntArg1()];
					atom.mem.removeAtom(atom);
					break;
				case Instruction.NEWATOM :
				case Instruction.LOCALNEWATOM : //[-dstatom, srcmem, funcref]
					func = (Functor) inst.getArg3();
					atoms[inst.getIntArg1()] = mems[inst.getIntArg2()].newAtom(func);

					break;

				case Instruction.NEWATOMINDIRECT :
				case Instruction.LOCALNEWATOMINDIRECT :
					//[-dstatom, srcmem, func]
					break;
				case Instruction.ENQUEUEATOM :
				case Instruction.LOCALENQUEUEATOM : //[srcatom]
					break;
				case Instruction.DEQUEUEATOM : //[srcatom]
					break;
				case Instruction.FREEATOM : //[srcatom]
					break; //n-kato
				case Instruction.ALTERFUNC :
				case Instruction.LOCALALTERFUNC : //[atom, funcref]
					break;
				case Instruction.ALTERFUNCINDIRECT :
				case Instruction.LOCALALTERFUNCINDIRECT : //[atom, func]
					break;
					//====���ȥ��������ܥܥǥ�̿��====�����ޤ�====

					//====���ȥ�����뷿�դ���ĥ��̿��====��������====
				case Instruction.ALLOCATOM : //[-dstatom, funcref]
					break;

				case Instruction.ALLOCATOMINDIRECT : //[-dstatom, func]
					break;

				case Instruction.COPYATOM :
				case Instruction.LOCALCOPYATOM : //[-dstatom, mem, srcatom]
					break;

					//case Instruction.ADDATOM:
				case Instruction.LOCALADDATOM : //[dstmem, atom]
					break;
					//====���ȥ�����뷿�դ���ĥ��̿��====�����ޤ�====

					//====���������ܥܥǥ�̿��====��������====
				case Instruction.REMOVEMEM :
				case Instruction.LOCALREMOVEMEM : //[srcmem]
					break;

				case Instruction.NEWMEM :
				case Instruction.LOCALNEWMEM : //[-dstmem, srcmem]
					break;

				case Instruction.NEWROOT : //[-dstmem, srcmem, node]
					break;
				case Instruction.MOVECELLS : //[dstmem, srcmem]
					break;
				case Instruction.ENQUEUEALLATOMS : //[srcmem]
					break;
				case Instruction.FREEMEM : //[srcmem]
					break; //n-kato

				case Instruction.ADDMEM :
				case Instruction.LOCALADDMEM : //[dstmem, srcmem]
					break;

				case Instruction.UNLOCKMEM :
				case Instruction.LOCALUNLOCKMEM : //[srcmem]
					break;
					//====���������ܥܥǥ�̿��====�����ޤ�====

					//====��󥯤�����ܥǥ�̿��====��������====
				case Instruction.NEWLINK :
				case Instruction.LOCALNEWLINK: //[atom1, pos1, atom2, pos2]
					atoms[inst.getIntArg1()].mem.newLink(
						(Atom)atoms[inst.getIntArg1()], inst.getIntArg2(),
						(Atom)atoms[inst.getIntArg3()], inst.getIntArg4() );
					break;
				case Instruction.RELINK :
				case Instruction.LOCALRELINK : //[atom1, pos1, atom2, pos2]
					atoms[inst.getIntArg1()].mem.relinkAtomArgs(
						(Atom)atoms[inst.getIntArg1()], inst.getIntArg2(),
						(Atom)atoms[inst.getIntArg3()], inst.getIntArg4() );
					break;
				case Instruction.UNIFY :
				case Instruction.LOCALUNIFY : //[atom1, pos1, atom2, pos2]
					atoms[inst.getIntArg1()].mem.unifyAtomArgs(
						(Atom)atoms[inst.getIntArg1()], inst.getIntArg2(),
						(Atom)atoms[inst.getIntArg3()], inst.getIntArg4() );
					break;

				case Instruction.INHERITLINK :
				case Instruction.LOCALINHERITLINK : //[atom1, pos1, link2]
					atoms[inst.getIntArg1()].mem.inheritLink(
						(Atom)atoms[inst.getIntArg1()], inst.getIntArg2(),
						(Link)vars.get(inst.getIntArg3()) );
					break;
					//====��󥯤�����ܥǥ�̿��====�����ޤ�====

					//====��ͳ��󥯴������ȥ༫ư�����Τ���Υܥǥ�̿��====��������====
				case Instruction.REMOVEPROXIES : //[srcmem]
					break;
				case Instruction.REMOVETOPLEVELPROXIES : //[srcmem]
					break;
				case Instruction.INSERTPROXIES : //[parentmem,childmem]
					break;
				case Instruction.REMOVETEMPORARYPROXIES : //[srcmem]
					break;
					//====��ͳ��󥯴������ȥ༫ư�����Τ���Υܥǥ�̿��====�����ޤ�====

					//====�롼�������ܥǥ�̿��====��������====
				case Instruction.LOADRULESET: //[dstmem, ruleset]
					((AbstractMembrane)mems[inst.getIntArg1()]).loadRuleset(
						(Ruleset)inst.getArg2() );
					break;
				case Instruction.COPYRULES:   //[dstmem, srcmem]
					break;
				case Instruction.CLEARRULES:  //[dstmem, srcmem]
					break;
					//====�롼�������ܥǥ�̿��====�����ޤ�====

					//====���դ��Ǥʤ��ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿��====��������====
				case Instruction.RECURSIVELOCK : //[srcmem]
					break;
				case Instruction.RECURSIVEUNLOCK : //[srcmem]
					break;
				case Instruction.COPYMEM : //[-dstmem, srcmem]
					break;
				case Instruction.DROPMEM : //[srcmem]
					break;
					//====���դ��Ǥʤ��ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿��====�����ޤ�====

					//====����̿��====��������====
				case Instruction.REACT :
					Rule rule = (Rule) inst.getArg1();
					List bodyInsts = (List) rule.body;
					Instruction spec = (Instruction) bodyInsts.get(0);
					int formals = spec.getIntArg1();
					int locals = spec.getIntArg2();
					
// // ArrayIndexOutOfBoundsException ���Ǥ��Τǰ��Ū���ѹ�
// if (locals < 10) locals = 10;
					
					AbstractMembrane[] bodymems = new AbstractMembrane[locals];
					Atom[] bodyatoms = new Atom[locals];
					List memformals = (List) inst.getArg2();
					List atomformals = (List) inst.getArg3();
					for (int i = 0; i < memformals.size(); i++) {
						bodymems[i] =
							mems[((Integer) memformals.get(i)).intValue()];
					}
					for (int i = 0; i < atomformals.size(); i++) {
						bodyatoms[i] =
							atoms[((Integer) atomformals.get(i)).intValue()];
					}
					InterpreterReactor ir =
						new InterpreterReactor(
							bodymems,
							bodyatoms,
							new ArrayList());
					ir.interpret(bodyInsts, 0);
					return true; //n-kato

				case Instruction.PROCEED :
					return true;

				case Instruction.SPEC:
					break;

				case Instruction.BRANCH :
					List subinsts;
					subinsts = (List) ((List) inst.getArg1()).get(0);
					if (interpret(subinsts, 0))
						return true;
					break; //nakajima

				case Instruction.LOOP :
					subinsts = (List) ((List) inst.getArg1()).get(0);
					while (interpret(subinsts, 0)) {
					}
					break; //nakajima, n-kato

				case Instruction.RUN :
					subinsts = (List) ((List) inst.getArg1()).get(0);
					interpret(subinsts, 0);
					break; //nakajima

				case Instruction.NOT :
					subinsts = (List) ((List) inst.getArg1()).get(0);
					if (interpret(subinsts, 0))
						return false;
					break; //n-kato

					//====����̿��====�����ޤ�====

					//====�������Τ���Υ�����̿��====��������====
				case Instruction.ISGROUND : //[link]
					break;

				case Instruction.ISINT : //[atom]
					break;
				case Instruction.ISFLOAT : //[atom]
					break;
				case Instruction.ISSTRING : //[atom]
					break;
				case Instruction.ISINTFUNC : //[func]
					break;
				case Instruction.ISFLOATFUNC : //[func]
					break;
				case Instruction.ISSTRINGFUNC : //[func]
					break;
					//====�������Τ���Υ�����̿��====�����ޤ�====

					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====��������====
				case Instruction.INLINE : //[atom, inlineref]
					Inline.callInline( atoms[inst.getIntArg1()], inst.getIntArg2() );
					break;
					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====�����ޤ�====
					
					//====�����Ѥ��Ȥ߹��ߥܥǥ�̿��====��������====
				case Instruction.IADD : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.ISUB : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.IMUL : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.IDIV : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.INEG : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.IMOD : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.INOT : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.IAND : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.IOR : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.IXOR : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.ISHL : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.ISHR : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.ISAR : //[-dstintatom, intatom1, intatom2]
					break;
				case Instruction.IADDFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.ISUBFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.IMULFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.IDIVFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.INEGFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.IMODFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.INOTFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.IANDFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.IORFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.IXORFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.ISHLFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.ISHRFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
				case Instruction.ISARFUNC : //[-dstintfunc, intfunc1, intfunc2]
					break;
					//====�����Ѥ��Ȥ߹��ߥܥǥ�̿��====�����ޤ�====

					//====�����Ѥ��Ȥ߹��ߥ�����̿��====��������====
				case Instruction.ILT : //[intatom1, intatom2]
					break;
				case Instruction.ILE : //[intatom1, intatom2]
					break;
				case Instruction.IGT : //[intatom1, intatom2]
					break;
				case Instruction.IGE : //[intatom1, intatom2]
					break;
				case Instruction.ILTFUNC : //[intfunc1, intfunc2]
					break;
				case Instruction.ILEFUNC : //[intfunc1, intfunc2]
					break;
				case Instruction.IGTFUNC : //[intfunc1, intfunc2]
					break;
				case Instruction.IGEFUNC : //[intfunc1, intfunc2]
					break;
					//====�����Ѥ��Ȥ߹��ߥ�����̿��====�����ޤ�====

				default :
					System.out.println(
						"SYSTEM ERROR: Invalid instruction: " + inst);
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
	private static int lastId = 600;

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
		while (it.hasNext()) {
			Rule r = (Rule) it.next();
			result
				|= matchTest(mem, atom, r.atomMatch, (Instruction) r.body.get(0));
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
		while (it.hasNext()) {
			Rule r = (Rule) it.next();
			result
				|= matchTest(mem, null, r.memMatch, (Instruction) r.body.get(0));
		}
		return result;
	}

	/**
	 * ���Ƴ�����ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	private boolean matchTest(
		Membrane mem,
		Atom atom,
		List matchInsts,
		Instruction spec) {
		int formals = spec.getIntArg1();
		int locals = spec.getIntArg2();
		
// ArrayIndexOutOfBoundsException ���Ǥ��Τǰ��Ū���ѹ�
if (formals < 10) formals = 10;
		
		AbstractMembrane[] mems = new AbstractMembrane[formals];
		Atom[] atoms = new Atom[formals];
		mems[0] = mem;
		atoms[1] = atom;
		InterpreterReactor ir =
			new InterpreterReactor(mems, atoms, new ArrayList());
		return ir.interpret(matchInsts, 0);
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
		StringBuffer s = new StringBuffer("");
		Iterator l;
		l = rules.iterator();
		while (l.hasNext())
			s.append(((Rule) l.next()).toString() + " ");
		return "@" + id + "  " + s;
	}

	public void showDetail() {
		Env.p("InterpretedRuleset.showDetail " + this);
		Iterator l;
		l = rules.listIterator();
		while (l.hasNext()) {
			Rule r = ((Rule) l.next());
			r.showDetail();
		}
	}
}
