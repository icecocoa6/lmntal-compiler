package runtime;

import java.util.*;

/**
 * compile.RuleCompiler �ˤ�ä���������롣
 * @author hara, nakajima, n-kato
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
			if (matchTest(mem, atom, r.atomMatch)) {
				result = true;
				//if (!mem.isCurrent()) return true;
				return true;
			}
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
			if (matchTest(mem, null, r.memMatch)) {
				result = true;
				return true;
				//if (!mem.isCurrent()) return true;
			}
		}
		return result;
	}

	/**
	 * ���Ƴ�����ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	private boolean matchTest(Membrane mem, Atom atom, List matchInsts) {
		Instruction spec = (Instruction)matchInsts.get(0);
		int formals = spec.getIntArg1();
		
// ArrayIndexOutOfBoundsException ���Ǥ��Τǰ��Ū���ѹ�
//if (formals < 10) formals = 10;
		
		AbstractMembrane[] mems = new AbstractMembrane[formals];
		Atom[] atoms = new Atom[formals];
		List vars = new ArrayList();
		mems[0] = mem;
		if (atom != null) { atoms[1] = atom; }
		InterpreterReactor ir = new InterpreterReactor(mems, atoms, vars);
		return ir.interpret(matchInsts, 0);
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
		Env.d("InterpretedRuleset.showDetail " + this);
		Iterator l;
		l = rules.listIterator();
		while (l.hasNext()) {
			Rule r = ((Rule) l.next());
			r.showDetail();
		}
	}
}

////////////////////////////////////////////////////////////////

/**
 * ���󥿥ץ꥿���Ȥ��ѿ��٥�����ɽ�����饹��
 * <p>
 * ���ߤϡ�2�Ĥ������1�ĤΥꥹ�Ȥ����������ߤ����ѿ��ֹ椬��ʣ���ʤ��褦�˻��Ѥ��Ƥ��롣
 * @author hara,nakajima,n-kato
 */

class InterpreterReactor {
	/** ���ѿ��Υ٥��� */
	AbstractMembrane[] mems;
	/** ���ȥ��ѿ��Υ٥��� */
	Atom[] atoms;
	/** ����¾���ѿ��Υ٥�����mems��atoms���ѻߤ���vars�����礹�롩��*/
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

	/** ������Ϳ����줿̿������᤹�롣
	 * @param insts ̿����
	 * @param pc    ̿������Υץ���५����
	 * @return ̿����μ¹Ԥ������������ɤ������֤�
	 */
	boolean interpret(List insts, int pc) {
		//Env.p("interpret : " + insts);
		Iterator it;
		Atom atom;
		AbstractMembrane mem;
		Link link;
		Functor func;
		while (pc < insts.size()) {
			Instruction inst = (Instruction) insts.get(pc++);
			Env.d("Do " + inst);
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
					link = atoms[inst.getIntArg2()].args[inst.getIntArg3()];
					if (link.getPos() != inst.getIntArg4()) return false;
					atoms[inst.getIntArg1()] = link.getAtom();
					break; //n-kato
				case Instruction.DEREFATOM : // [-dstatom, srcatom, srcpos]
					link = atoms[inst.getIntArg2()].args[inst.getIntArg3()];
					atoms[inst.getIntArg1()] = link.getAtom();
					break; //n-kato
				case Instruction.FINDATOM : // [-dstatom, srcmem, funcref]
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
					link = atoms[inst.getIntArg2()].args[inst.getIntArg3()];
					vars.set(inst.getIntArg3(),link);
					break; //n-kato
					//====���ȥ�˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====

					//====��˴ط�������Ϥ�����ܥ�����̿�� ====��������====
				case Instruction.LOCKMEM :
				case Instruction.LOCALLOCKMEM :
					// lockmem [-dstmem, freelinkatom]
					mem = atoms[inst.getIntArg2()].mem;
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
				case Instruction.LOCK :
				case Instruction.LOCALLOCK : //[srcmem] 
					mem = mems[inst.getIntArg1()];
					if (mem.lock(mems[0])) {
						if (interpret(insts, pc))
							return true;
						mem.unlock();						
					}
					break; //n-kato

				case Instruction.GETMEM : //[-dstmem, srcatom]
					mems[inst.getIntArg1()] = atoms[inst.getIntArg2()].mem;
					break; //n-kato
				case Instruction.GETPARENT : //[-dstmem, srcmem]
					mems[inst.getIntArg1()] = mems[inst.getIntArg2()].parent;
					break; //n-kato

					//====��˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====

					//====��˴ط�������Ϥ��ʤ����ܥ�����̿��====��������====
				case Instruction.TESTMEM : //[dstmem, srcatom]
					if (mems[inst.getIntArg1()] != atoms[inst.getIntArg2()].mem) return false;
					break; //n-kato
				case Instruction.NORULES : //[srcmem] 
					if (mems[inst.getIntArg1()].hasRules()) return false;
					break; //n-kato
				case Instruction.NATOMS : //[srcmem, count]
					if (mems[inst.getIntArg1()].atoms.size() != inst.getIntArg2()) return false;
					break; //n-kato
				case Instruction.NFREELINKS : //[srcmem, count]
					// TODO ������
					mem = mems[inst.getIntArg1()];
					if (mem.atoms.size() - mem.atoms.getNormalAtomCount() != inst.getIntArg2())
						return false;
					break;
				case Instruction.NMEMS : //[srcmem, count]
					if (mems[inst.getIntArg1()].mems.size() != inst.getIntArg2()) return false;
					break; //n-kato
				case Instruction.EQMEM : //[mem1, mem2]
					if (mems[inst.getIntArg1()] != mems[inst.getIntArg2()]) return false;
					break; //n-kato
				case Instruction.NEQMEM : //[mem1, mem2]
					if (mems[inst.getIntArg1()] == mems[inst.getIntArg2()]) return false;
					break; //n-kato
				case Instruction.STABLE : //[srcmem] 
					if (!mems[inst.getIntArg1()].isStable()) return false;
					break; //n-kato
					//====��˴ط�������Ϥ��ʤ����ܥ�����̿��====�����ޤ�====

					//====���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿��====��������====
				case Instruction.FUNC : //[srcatom, funcref]
					if (!atoms[inst.getIntArg1()].getFunctor().equals((Functor)inst.getArg2()))
						return false;
					break; //n-kato
				case Instruction.EQATOM : //[atom1, atom2]
					if (atoms[inst.getIntArg1()] != atoms[inst.getIntArg2()]) return false;
					break; //n-kato
				case Instruction.NEQATOM : //[atom1, atom2]
					if (atoms[inst.getIntArg1()] == atoms[inst.getIntArg2()]) return false;
					break; //n-kato
					//====���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿��====�����ޤ�====

					//====�ե��󥯥��˴ط�����̿��====��������====
				case Instruction.DEREFFUNC : //[-dstfunc, srcatom, srcpos]
					vars.set(inst.getIntArg1(), atoms[inst.getIntArg2()].args[inst.getIntArg3()].getAtom().getFunctor());
					break; //nakajima 2003-12-21, n-kato
				case Instruction.GETFUNC : //[-func, atom]
					vars.set(inst.getIntArg1(), atoms[inst.getIntArg2()].getFunctor());
					break; //nakajima 2003-12-21, n-kato
				case Instruction.LOADFUNC : //[-func, funcref]
					vars.set(inst.getIntArg1(), (Functor)inst.getArg2());
					break;//nakajima 2003-12-21, n-kato
				case Instruction.EQFUNC : //[func1, func2]
					if (!vars.get(inst.getIntArg1()).equals(vars.get(inst.getIntArg2()))) return false;
					break; //nakajima, n-kato
				case Instruction.NEQFUNC : //[func1, func2]
					if (vars.get(inst.getIntArg1()).equals(vars.get(inst.getIntArg2()))) return false;
					break; //nakajima, n-kato
					//====�ե��󥯥��˴ط�����̿��====�����ޤ�====

					//====���ȥ��������ܥܥǥ�̿��====��������====
				case Instruction.REMOVEATOM :
				case Instruction.LOCALREMOVEATOM : //[srcatom, srcmem, funcref]
					atom = atoms[inst.getIntArg1()];
					atom.mem.removeAtom(atom);
					break; //n-kato
				case Instruction.NEWATOM :
				case Instruction.LOCALNEWATOM : //[-dstatom, srcmem, funcref]
					func = (Functor) inst.getArg3();
					atoms[inst.getIntArg1()] = mems[inst.getIntArg2()].newAtom(func);
					break; //n-kato
				case Instruction.NEWATOMINDIRECT :
				case Instruction.LOCALNEWATOMINDIRECT : //[-dstatom, srcmem, func]
					//TODO NEWATOM�Ȥۤ�Ʊ�������ɤˤʤä���ä����ɤ����Ρ�func��funcref�äư㤦���ġ�����ᡣ���ͤ�褯�ɤळ�ȡ�
					atoms[inst.getIntArg1()] = mems[inst.getIntArg2()].newAtom((Functor)(vars.get(inst.getIntArg3())));
					break; //nakajima 2003-12-27, 2004-01-03
				case Instruction.ENQUEUEATOM :
				case Instruction.LOCALENQUEUEATOM : //[srcatom]
					atom = atoms[inst.getIntArg1()];
					atom.mem.enqueueAtom(atom);
					break; //n-kato
				case Instruction.DEQUEUEATOM : //[srcatom]
					atom = atoms[inst.getIntArg1()];
					atom.dequeue();
					break; //n-kato
				case Instruction.FREEATOM : //[srcatom]
					break; //n-kato
				case Instruction.ALTERFUNC :
				case Instruction.LOCALALTERFUNC : //[atom, funcref]
					atom = atoms[inst.getIntArg1()];
					atom.mem.alterAtomFunctor(atom,(Functor)inst.getArg2());
					break; //n-kato
				case Instruction.ALTERFUNCINDIRECT :
				case Instruction.LOCALALTERFUNCINDIRECT : //[atom, func]
					//TODO funcref��$func�äƲ����㤦�ΤǤ�������func��int��funcref��Functor��
					atom = atoms[inst.getIntArg1()];
					atom.mem.alterAtomFunctor(atom,(Functor)(vars.get(inst.getIntArg2())));
					break; //nakajima 2003-12-27, 2004-01-03
					//====���ȥ��������ܥܥǥ�̿��====�����ޤ�====

					//====���ȥ�����뷿�դ���ĥ��̿��====��������====
				case Instruction.ALLOCATOM : //[-dstatom, funcref]
					atoms[inst.getIntArg1()] = new Atom(null, (Functor)inst.getArg2());
					break; //nakajima 2003-12-27

				case Instruction.ALLOCATOMINDIRECT : //[-dstatom, func]
					// TODO funcref��$func�äƲ����㤦�ΤǤ���������
					atoms[inst.getIntArg1()] = new Atom(null, (Functor)(vars.get(inst.getIntArg2())));
					break; //nakajima 2003-12-27, 2004-01-03

				case Instruction.COPYATOM :
				case Instruction.LOCALCOPYATOM : //[-dstatom, mem, srcatom]
					//TODO Functor��arity��2�Ǥ����Τ��� �����Ǥ���̿��λ��ͤ�褯�ɤळ�ȡ�
					atoms[inst.getIntArg1()] = mems[inst.getIntArg2()].newAtom(atoms[inst.getIntArg1()].getFunctor());
					break; //nakajima 2003-12-27, 2004-01-03

					//case Instruction.ADDATOM:
				case Instruction.LOCALADDATOM : //[dstmem, atom]
					mems[inst.getIntArg1()].addAtom(atoms[inst.getIntArg2()]);
					break; //nakajima 2003-12-27
					//====���ȥ�����뷿�դ���ĥ��̿��====�����ޤ�====

					//====���������ܥܥǥ�̿��====��������====
				case Instruction.REMOVEMEM :
				case Instruction.LOCALREMOVEMEM : //[srcmem, parentmem]
					mem = mems[inst.getIntArg1()];
					mem.parent.removeMem(mem);
					break; //n-kato
				case Instruction.NEWMEM: //[-dstmem, srcmem]
					mem = mems[inst.getIntArg2()].newMem();
					mems[inst.getIntArg1()] = mem;
					break; //n-kato
				case Instruction.LOCALNEWMEM : //[-dstmem, srcmem]
					mem = ((Membrane)mems[inst.getIntArg2()]).newLocalMembrane();
					mems[inst.getIntArg1()] = mem;
					break; //n-kato
				case Instruction.NEWROOT : //[-dstmem, srcmem, node]
					//TODO AbstactMachine���饹��¸�ߤ��ʤ���̿��λ��ͤ�褯�ɤळ�ȡ�
					//AbstractMachine remotenode = new AbstractMachine(  );
					//mems[inst.getIntArg2()].newRoot(    )
					break; //nakajima 2003-12-27
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
					mems[inst.getIntArg1()].unlock();
					break; //n-kato
					//====���������ܥܥǥ�̿��====�����ޤ�====

					//====��󥯤�����ܥǥ�̿��====��������====
				case Instruction.NEWLINK:		 //[atom1, pos1, atom2, pos2, mem1]
				case Instruction.LOCALNEWLINK:	 //[atom1, pos1, atom2, pos2 (,mem1)]
					atoms[inst.getIntArg1()].mem.newLink(
						atoms[inst.getIntArg1()], inst.getIntArg2(),
						atoms[inst.getIntArg3()], inst.getIntArg4() );
					break; //n-kato
				case Instruction.RELINK:		 //[atom1, pos1, atom2, pos2, mem]
				case Instruction.LOCALRELINK:	 //[atom1, pos1, atom2, pos2 (,mem)]
					atoms[inst.getIntArg1()].mem.relinkAtomArgs(
						atoms[inst.getIntArg1()], inst.getIntArg2(),
						atoms[inst.getIntArg3()], inst.getIntArg4() );
					break; //n-kato
				case Instruction.UNIFY: //[atom1, pos1, atom2, pos2]
					mems[0].unifyAtomArgs(
						atoms[inst.getIntArg1()], inst.getIntArg2(),
						atoms[inst.getIntArg3()], inst.getIntArg4() );
					break; //n-kato

				case Instruction.INHERITLINK:		 //[atom1, pos1, link2, mem]
				case Instruction.LOCALINHERITLINK:	 //[atom1, pos1, link2 (,mem)]
					atoms[inst.getIntArg1()].mem.inheritLink(
						atoms[inst.getIntArg1()], inst.getIntArg2(),
						(Link)vars.get(inst.getIntArg3()) );
					break; //n-kato
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
				case Instruction.LOADRULESET:
				case Instruction.LOCALLOADRULESET: //[dstmem, ruleset]
					mems[inst.getIntArg1()].loadRuleset((Ruleset)inst.getArg2() );
					break; //n-kato
				case Instruction.COPYRULES:
				case Instruction.LOCALCOPYRULES:   //[dstmem, srcmem]
					mems[inst.getIntArg1()].copyRulesFrom(mems[inst.getIntArg2()]);
					break; //n-kato
				case Instruction.CLEARRULES:
				case Instruction.LOCALCLEARRULES:  //[dstmem]
					mems[inst.getIntArg1()].clearRules();
					break; //n-kato
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
						bodyatoms[i + memformals.size()] =
							atoms[((Integer) atomformals.get(i)).intValue()];
					}
					InterpreterReactor ir =
						new InterpreterReactor(
							bodymems,
							bodyatoms,
							new ArrayList());
					ir.interpret(bodyInsts, 0);
					return true; //n-kato

				case Instruction.PROCEED:
					return true; //n-kato

				case Instruction.SPEC:
					break;//n-kato

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

					//====���դ��ץ���ʸ̮�򰷤�������ɲ�̿��====��������====
				case Instruction.EQGROUND : //[groundlink1,groundlink2]
					break;
					//====���դ��ץ���ʸ̮�򰷤�������ɲ�̿��====�����ޤ�====

					//====�������Τ���Υ�����̿��====��������====
				case Instruction.ISGROUND : //[link]
					break;

				case Instruction.ISINT : //[atom]
					if (!(atoms[inst.getIntArg1()].getFunctor() instanceof IntegerFunctor)) return false;
					break; //n-kato
				case Instruction.ISFLOAT : //[atom]
					break;
				case Instruction.ISSTRING : //[atom]
					break;
				case Instruction.ISINTFUNC : //[func]
					if (!(vars.get(inst.getIntArg1()) instanceof IntegerFunctor)) return false;
					break; //n-kato
				case Instruction.ISFLOATFUNC : //[func]
					break;
				case Instruction.ISSTRINGFUNC : //[func]
					break;
					//====�������Τ���Υ�����̿��====�����ޤ�====

					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====��������====
				case Instruction.INLINE : //[atom, inlineref]
					Inline.callInline( atoms[inst.getIntArg1()], inst.getIntArg2() );
					break; //hara
					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====�����ޤ�====
					
					//====�����Ѥ��Ȥ߹��ߥܥǥ�̿��====��������====
				case Instruction.IADD : //[-dstintatom, intatom1, intatom2]
					int x,y;
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x+y));
					break; //n-kato
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
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();
					vars.set(inst.getIntArg1(), new IntegerFunctor(x+y));
					break; //n-kato
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

