/*
 * �����ע�
 * ���Υե���������Ƥ���������硢Transrator.java �ˤ�Ʊ�ͤν�����ä��뤳�ȡ�
 * �礭���ѹ���ä�����硢TranslatorGenerator �����Ѥ�����ɤ����⤷��ʤ���
 */

package runtime;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Util;
import daemon.IDConverter;

/**
 * compile.RulesetCompiler �ˤ�ä���������롣
 * @author hara, nakajima, n-kato
 */
public final class InterpretedRuleset extends Ruleset implements Serializable {
	/** ���Υ롼�륻�åȤΥ�����ID */
	private int id;
	private static int lastId = 600;

	/** �Ȥꤢ�����롼�������Ȥ��Ƽ��� */
	public List rules;

	/** ���߼¹���Υ롼�� */
	public Rule currentRule;
	
	/**
	 * RuleCompiler �Ǥϡ��ޤ��������Ƥ���ǡ�����������ࡣ
	 * �Τǡ��äˤʤˤ⤷�ʤ�
	 */
	public InterpretedRuleset() {
		rules = new ArrayList();
		id = ++lastId;
	}
	
	/** ���̿�����ѡ���������������Ȥ������Ѥ��륳�󥹥ȥ饯�� */
	public InterpretedRuleset(int id, List rules) {
		this.id = id;
		this.rules = rules;
		if (lastId < id)
			lastId = id;
	}
	
	////////////////////////////////////////////////////////////////

	/** �����Х�롼�륻�å�ID��̤����ξ���null��*/
	private String globalRulesetID;

	/**���Υ롼�륻�åȤΥ�����ID��������롣*/
	public int getId() {
		return id;
	}
	/**�ʲ���*/
	public String getGlobalRulesetID() {
		// todo ��󥿥���ID��ͭ�����֤�ľ��
		if (globalRulesetID == null) {
			globalRulesetID = Env.theRuntime.getRuntimeID() + ":" + id;
			IDConverter.registerRuleset(globalRulesetID, this);
		}
		return globalRulesetID;
	}
	
	////////////////////////////////////////////////////////////////

	public boolean react(Membrane mem, boolean nondeterministic) {
		if (nondeterministic) {
			Env.e("Nondeterministic execution is not supported by interpreter.");
			System.exit(-1);
			return false;
		} else {
			return react(mem);
		}
	}
	
	/**
	 * ����롼��ˤĤ��ƥ��ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	public boolean react(Membrane mem, Atom atom) {
		boolean result = false;
		Iterator it = rules.iterator();
		while (it.hasNext()) {
			Rule r = currentRule = (Rule) it.next();
			if (r.atomMatch.size() == 1) continue; // debugɽ��������

			
			boolean success;
			if(Env.profile){
				long start,stop;
		        start = Util.getTime();
				success = matchTest(mem, atom, r.atomMatch);
		        stop = Util.getTime();
		        synchronized(r){
		        	r.time += (stop>start)?(stop-start):0;
		        	r.apply++;
					if (success)r.succeed ++;
		        }
			} else {
				success = matchTest(mem, atom, r.atomMatch);
			}
			if (success) {
				result = true;
				if (Env.fTrace) Task.trace("-->", "@" + id, r.toString());
				if (Env.debugOption) {//2006.1.26 by inui
					Debug.breakPoint(r.lineno, Debug.ATOM);
					//if (Debug.isBreakPoint()) Task.trace("-->", "@" + id, r.toString());
				}
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
			Rule r = currentRule = (Rule) it.next();
			boolean success;
			if(Env.profile){
				long start,stop;
		        start = Util.getTime();
				success = matchTest(mem, null, r.memMatch);
		        stop = Util.getTime();
		        synchronized(r){
			        r.time += (stop>start)?(stop-start):0;
					r.apply++;
					if(success)r.succeed++;
		        }
			} else {
				success = matchTest(mem, null, r.memMatch);
			}
			if (success) {
				result = true;
				if(Env.fTrace) Task.trace("==>", "@" + id, r.toString());
				if (Env.debugOption) {//2006.1.26 by inui
					Debug.breakPoint(r.lineno, Debug.MEMBRANE);
					//if (Debug.isBreakPoint()) Task.trace("==>", "@" + id, r.toString());
				}
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
		int locals  = spec.getIntArg2();
		if (locals == 0) {
			System.err.println("SYSTEM DEBUG REPORT: an old version of spec instruction was detected");
			locals = formals;
		}
// ArrayIndexOutOfBoundsException ���Ǥ��Τǰ��Ū���ѹ�
//if (formals < 10) formals = 10;
		
		InterpretiveReactor ir = new InterpretiveReactor(locals, this);
		ir.mems[0] = mem;
		if (atom != null) { ir.atoms[1] = atom; }
		return ir.interpret(matchInsts, 0);
	}
	public String toString() {
		String ret = "@" + id;
		if (Env.verbose >= Env.VERBOSE_EXPANDRULES) {
			ret += dumpRules();
		}
		return ret;
	}
	public String dumpRules() {
		StringBuffer s = new StringBuffer("");
		Iterator it = rules.iterator();
		while (it.hasNext()) {
			s.append(" ");
			s.append((Rule)it.next());
		}
		return s.toString();
	}

	public String encode() {
		StringBuffer s = new StringBuffer("");
		Iterator it = rules.iterator();
		while(it.hasNext()) {
			s.append(((Rule)it.next()).getFullText().
					replaceAll("\\n","").replaceAll("\\r",""));
			if(it.hasNext())
				s.append(", ");
		}
		return s.toString();
	}
	
	public void showDetail() {
		if (Env.verbose >= Env.VERBOSE_SHOWRULES || Env.compileonly)
			Env.p("Compiled Ruleset @" + id + dumpRules());
		Iterator l;
		l = rules.listIterator();
		while (l.hasNext()) {
			Rule r = ((Rule) l.next());
			r.showDetail();
		}
	}
	
	public void serialize(ObjectOutputStream out) throws IOException {
		super.serialize(out);
		out.writeObject(rules);
	}
	protected void deserializeInstance(ObjectInputStream in) throws IOException {
		super.deserializeInstance(in);
		// todo id�ο�������ͤ��롣������id�ϼΤƤ���globalid�ΤߤǴ��������
		id = ++lastId;
		try {
			rules = (List)in.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unexpected Error in deserialization");
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

class InterpretiveReactor {
	/** ���ѿ��Υ٥��� */
	AbstractMembrane[] mems;
	/** ���ȥ��ѿ��Υ٥��� */
	Atom[] atoms;
	/** ����¾���ѿ��Υ٥�����mems��atoms���ѻߤ���vars�����礹�롩��*/
	List vars;
	/** ��å�������Υꥹ�ȡ����롼�ײ����˻���**/
	List lockedMemList = new ArrayList();
	
	InterpretedRuleset currentInterpretedRuleset;
	
	InterpretiveReactor(int size, InterpretedRuleset ir) {
		this.currentInterpretedRuleset = ir;
		initVector(size);
	}
	private void initVector(int size) {
		this.mems  = new AbstractMembrane[size];
		this.atoms = new Atom[size];
		this.vars  = new ArrayList();
		//ArrayList newvars = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			this.vars.add(null);
		}
	}
	/** �ѿ��٥������ĥ���� */
	private void extendVector(int size) {
		int oldsize = mems.length;
		if (oldsize >= size) return;
		AbstractMembrane[] oldmems = mems;
		Atom[] oldatoms = atoms;
		
		this.mems  = new AbstractMembrane[size];
		this.atoms = new Atom[size];
		
		for (int i = 0; i < oldsize; i++) {
			mems[i] = oldmems[i];
			atoms[i] = oldatoms[i];
		}
		for (int i = oldsize; i < size; i++) {
			this.vars.add(null);
		}
	}
	
	private void reloadVars(InterpretiveReactor irSrc,
			int size, List memargs, List atomargs, List varargs) {
		AbstractMembrane[] srcmems  = irSrc.mems;
		Atom[]             srcatoms = irSrc.atoms;
		List               srcvars  = irSrc.vars;
		initVector(size);
				
		int memcount  = memargs.size();
		int atomcount = atomargs.size();
		for (int i = 0; i < memcount; i++) {
			mems[i] =
			   srcmems[((Integer) memargs.get(i)).intValue()];
		}
		for (int i = 0; i < atomcount; i++) {
			atoms[i + memcount] =
			   srcatoms[((Integer) atomargs.get(i)).intValue()];
		}
		for (int i = 0; i < varargs.size(); i++) {
			vars.set(i + memcount + atomcount,
				srcvars.get(((Integer) varargs.get(i)).intValue()));
		}
	}

	private void changeVars(InterpretiveReactor irSrc,
			List memargs, List atomargs, List varargs) {
		AbstractMembrane[] srcmems  = irSrc.mems;
		Atom[]             srcatoms = irSrc.atoms;
		List               srcvars  = irSrc.vars;
		int size = memargs.size();
		if (size < atomargs.size()) size = atomargs.size();
		if (size < varargs.size())  size = varargs.size();
		initVector(size);
		for (int i = 0; i < size; i++) {
			if (memargs.get(i) != null)
				mems[i]  = srcmems[((Integer) memargs.get(i)).intValue()];
			else if (atomargs.get(i) != null)
				atoms[i] = srcatoms[((Integer) atomargs.get(i)).intValue()];
			else
				vars.set(i, srcvars.get(((Integer) varargs.get(i)).intValue()));
		}
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
			if (Env.debug >= Env.DEBUG_TRACE) Env.d("Do " + inst);
			switch (inst.getKind()) {

				//��⡧LOCALHOGE��HOGE��Ʊ�������ɤǤ�����
				//nakajima: 2003-12-12
				//��⡧�����Ȥϰ���
				//nakajima: 2003-12-12

				//====����¾====��������====
				case Instruction.DUMMY :
					System.err.println(
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
				case Instruction.DEREFLINK : //[-dstatom, srclink, dstpos]
					link = (Link)vars.get(inst.getIntArg2());
					if (link.getPos() != inst.getIntArg3()) return false;
					atoms[inst.getIntArg1()] = link.getAtom();
					break; //mizuno
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
					//====���ȥ�˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====

					//====��˴ط�������Ϥ�����ܥ�����̿�� ====��������====
				case Instruction.LOCKMEM :
				case Instruction.LOCALLOCKMEM :
					// lockmem [-dstmem, freelinkatom]
					mem = atoms[inst.getIntArg2()].mem;
					if (mem.lock()) {
						lockedMemList.add(mem);
						mems[inst.getIntArg1()] = mem;
						if (interpret(insts, pc))
							return true;
						mem.unlock(true);
					}
					return false; //n-kato

				case Instruction.ANYMEM :
				case Instruction.LOCALANYMEM : // anymem [-dstmem, srcmem] 
					it = mems[inst.getIntArg2()].mems.iterator();
					while (it.hasNext()) {
						AbstractMembrane submem = (AbstractMembrane) it.next();
						if ((submem.kind != inst.getIntArg3())) 
							continue;
						if (submem.lock()) {
							mems[inst.getIntArg1()] = submem;
							lockedMemList.add(submem);
							if (interpret(insts, pc))
								return true;
							submem.unlock();
						}
					}
					return false; //n-kato
				case Instruction.LOCK :
				case Instruction.LOCALLOCK : //[srcmem] 
					mem = mems[inst.getIntArg1()];
					if (mem.lock()) {
						lockedMemList.add(mem);
						if (interpret(insts, pc))
							return true;
						mem.unlock();						
					}
					return false; //n-kato

				case Instruction.GETMEM : //[-dstmem, srcatom]
					if(atoms[inst.getIntArg2()].mem.kind != inst.getIntArg3())
						return false;
					mems[inst.getIntArg1()] = atoms[inst.getIntArg2()].mem;
					break;
				case Instruction.GETPARENT : //[-dstmem, srcmem]
					mem = mems[inst.getIntArg2()].parent;
					if (mem == null) return false;
					mems[inst.getIntArg1()] = mem;
					break; //n-kato

					//====��˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====

					//====��˴ط�������Ϥ��ʤ����ܥ�����̿��====��������====
				case Instruction.TESTMEM : //[dstmem, srcatom]
					if (mems[inst.getIntArg1()] != atoms[inst.getIntArg2()].mem) return false;
					break; //n-kato
				case Instruction.NORULES : //[srcmem] 
					if (mems[inst.getIntArg1()].hasRules()) return false;
					break; //n-kato
				case Instruction.NFREELINKS : //[srcmem, count]
					mem = mems[inst.getIntArg1()];
					if (mem.atoms.getAtomCountOfFunctor(Functor.INSIDE_PROXY) != inst.getIntArg2())
						return false;
					break;
				case Instruction.NATOMS : //[srcmem, count]
					if (mems[inst.getIntArg1()].atoms.getNormalAtomCount() != inst.getIntArg2()) return false;
					break; //n-kato
				case Instruction.NATOMSINDIRECT : //[srcmem, countfunc]
					if (mems[inst.getIntArg1()].atoms.getNormalAtomCount() != ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue()) return false;
					break; //kudo 2004-12-08
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
					if (!((Functor)inst.getArg2()).equals(atoms[inst.getIntArg1()].getFunctor())) 
						return false;
					break; //n-kato
				case Instruction.NOTFUNC : //[srcatom, funcref]
					if (((Functor)inst.getArg2()).equals(atoms[inst.getIntArg1()].getFunctor())) 
						return false;
					break; //n-kato
				case Instruction.EQATOM : //[atom1, atom2]
					if (atoms[inst.getIntArg1()] != atoms[inst.getIntArg2()]) return false;
					break; //n-kato
				case Instruction.NEQATOM : //[atom1, atom2]
					if (atoms[inst.getIntArg1()] == atoms[inst.getIntArg2()]) return false;
					break; //n-kato
				case Instruction.SAMEFUNC: //[atom1, atom2]
					if (!atoms[inst.getIntArg1()].getFunctor().equals(atoms[inst.getIntArg2()].getFunctor())) return false;
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
					atoms[inst.getIntArg1()] = mems[inst.getIntArg2()].newAtom((Functor)(vars.get(inst.getIntArg3())));
					break; //nakajima 2003-12-27, 2004-01-03, n-kato
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
					atom = atoms[inst.getIntArg1()];
					atom.mem.alterAtomFunctor(atom,(Functor)(vars.get(inst.getIntArg2())));
					break; //nakajima 2003-12-27, 2004-01-03, n-kato
					//====���ȥ��������ܥܥǥ�̿��====�����ޤ�====

					//====���ȥ�����뷿�դ���ĥ��̿��====��������====
				case Instruction.ALLOCATOM : //[-dstatom, funcref]
					atoms[inst.getIntArg1()] = new Atom(null, (Functor)inst.getArg2());
					break; //nakajima 2003-12-27, n-kato

				case Instruction.ALLOCATOMINDIRECT : //[-dstatom, func]
					atoms[inst.getIntArg1()] = new Atom(null, (Functor)(vars.get(inst.getIntArg2())));
					break; //nakajima 2003-12-27, 2004-01-03, n-kato

				case Instruction.COPYATOM :
				case Instruction.LOCALCOPYATOM : //[-dstatom, mem, srcatom]
					atoms[inst.getIntArg1()] = mems[inst.getIntArg2()].newAtom(atoms[inst.getIntArg3()].getFunctor());
					break; //nakajima, n-kato

					//case Instruction.ADDATOM:
				case Instruction.LOCALADDATOM : //[dstmem, atom]
					mems[inst.getIntArg1()].addAtom(atoms[inst.getIntArg2()]);
					break; //nakajima 2003-12-27, n-kato
					//====���ȥ�����뷿�դ���ĥ��̿��====�����ޤ�====

					//====���������ܥܥǥ�̿��====��������====
				case Instruction.REMOVEMEM :
				case Instruction.LOCALREMOVEMEM : //[srcmem, parentmem]
					mem = mems[inst.getIntArg1()];
					mem.parent.removeMem(mem);
					break; //n-kato
				case Instruction.NEWMEM: //[-dstmem, srcmem]
					mem = mems[inst.getIntArg2()].newMem(inst.getIntArg3());
					mems[inst.getIntArg1()] = mem;
					break; //n-kato
				case Instruction.LOCALNEWMEM : //[-dstmem, srcmem]
					mem = ((Membrane)mems[inst.getIntArg2()]).newLocalMembrane(inst.getIntArg3());
					mems[inst.getIntArg1()] = mem;
					break; //n-kato
				case Instruction.ALLOCMEM: //[-dstmem]
					mem = ((Task)mems[0].getTask()).createFreeMembrane();
					mems[inst.getIntArg1()] = mem;
					break; //n-kato

				case Instruction.NEWROOT : //[-dstmem, srcmem, nodeatom]
					String nodedesc = atoms[inst.getIntArg3()].getFunctor().getName();
					mems[inst.getIntArg1()] = mems[inst.getIntArg2()].newRoot(nodedesc, inst.getIntArg4());
					break; //n-kato 2004-09-17
				case Instruction.MOVECELLS : //[dstmem, srcmem]
					mems[inst.getIntArg1()].moveCellsFrom(mems[inst.getIntArg2()]);
					break; //nakajima 2004-01-04, n-kato
				case Instruction.ENQUEUEALLATOMS : //[srcmem]
					break;
				case Instruction.FREEMEM : //[srcmem]
					mems[inst.getIntArg1()].free();
					break; //mizuno 2004-10-12, n-kato

				case Instruction.ADDMEM :
				case Instruction.LOCALADDMEM : //[dstmem, srcmem]
					mems[inst.getIntArg2()] = mems[inst.getIntArg2()].moveTo(mems[inst.getIntArg1()]);
					break; //nakajima 2004-01-04, n-kato, n-kato 2004-11-10
				case Instruction.ENQUEUEMEM:
					mems[inst.getIntArg1()].activate();
					break;
				case Instruction.UNLOCKMEM :
				case Instruction.LOCALUNLOCKMEM : //[srcmem]
					mems[inst.getIntArg1()].forceUnlock();
					break; //n-kato

				case Instruction.LOCALSETMEMNAME: //[dstmem, name]
				case Instruction.SETMEMNAME: //[dstmem, name]
					mems[inst.getIntArg1()].setName((String)inst.getArg2());
					break; //n-kato


					//====���������ܥܥǥ�̿��====�����ޤ�====

					//====��󥯤˴ط�������Ϥ��륬����̿��====��������====
				case Instruction.GETLINK : //[-link, atom, pos]
					link = atoms[inst.getIntArg2()].args[inst.getIntArg3()];
					vars.set(inst.getIntArg1(),link); //3->1 by mizuno
					break; //n-kato
				case Instruction.ALLOCLINK : //[-link, atom, pos]
					link = new Link(atoms[inst.getIntArg2()], inst.getIntArg3());
					vars.set(inst.getIntArg1(),link);
					break; //n-kato
					//====��󥯤˴ط�������Ϥ��륬����̿��====�����ޤ�====

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
				case Instruction.UNIFY:		//[atom1, pos1, atom2, pos2, mem]
					// mem = mems[0]; // �ΤΥ�����
					//2005/10/11 mizuno
					//ɬ����ް��������Ѥ���褦�˥���ѥ���������������Υ����ɤ��ѹ�
					mem = mems[inst.getIntArg5()]; // �����Υ�����
					//mem = atoms[inst.getIntArg1()].args[inst.getIntArg2()]
					//		.getAtom().getMem(); // ���ѥ�����
					mem.unifyAtomArgs(
						atoms[inst.getIntArg1()], inst.getIntArg2(),
						atoms[inst.getIntArg3()], inst.getIntArg4() );
					break; //n-kato
				case Instruction.LOCALUNIFY:	//[atom1, pos1, atom2, pos2 (,mem)]
					//2005/10/11 mizuno ������ʤΤǡ������Ȥ�������ʤ��Ϥ�
					mems[0].unifyAtomArgs(
						atoms[inst.getIntArg1()], inst.getIntArg2(),
						atoms[inst.getIntArg3()], inst.getIntArg4() );
					break; //mizuno

				case Instruction.INHERITLINK:		 //[atom1, pos1, link2, mem]
				case Instruction.LOCALINHERITLINK:	 //[atom1, pos1, link2 (,mem)]
					atoms[inst.getIntArg1()].mem.inheritLink(
						atoms[inst.getIntArg1()], inst.getIntArg2(),
						(Link)vars.get(inst.getIntArg3()) );
					break; //n-kato

				case Instruction.UNIFYLINKS:		//[link1, link2, mem]
					//2005/10/11 mizuno
					//ɬ����ް��������Ѥ���褦�˥���ѥ���������������Υ����ɤ��ѹ�
					mem = mems[inst.getIntArg3()]; // �����Υ�����
					//mem = ((Link)vars.get(inst.getIntArg1())).getAtom().getMem(); // ���ѥ�����
					mem.unifyLinkBuddies(
						((Link)vars.get(inst.getIntArg1())),
						((Link)vars.get(inst.getIntArg2())));
					break; //n-kato
				case Instruction.LOCALUNIFYLINKS:	//[link1, link2 (,mem)]
					//2005/10/11 mizuno ������ʤΤǡ������Ȥ�������ʤ��Ϥ�
					mems[0].unifyLinkBuddies(
						((Link)vars.get(inst.getIntArg1())),
						((Link)vars.get(inst.getIntArg2())));
					break; //mizuno

					//====��󥯤�����ܥǥ�̿��====�����ޤ�====

					//====��ͳ��󥯴������ȥ༫ư�����Τ���Υܥǥ�̿��====��������====
				case Instruction.REMOVEPROXIES : //[srcmem]
					mems[inst.getIntArg1()].removeProxies();
					break; //nakajima 2004-01-04, n-kato
				case Instruction.REMOVETOPLEVELPROXIES : //[srcmem]
					mems[inst.getIntArg1()].removeToplevelProxies();
					break; //nakajima 2004-01-04, n-kato
				case Instruction.INSERTPROXIES : //[parentmem,childmem]
					mems[inst.getIntArg1()].insertProxies(mems[inst.getIntArg2()]);
					break;  //nakajima 2004-01-04, n-kato
				case Instruction.REMOVETEMPORARYPROXIES : //[srcmem]
					mems[inst.getIntArg1()].removeTemporaryProxies();
					break; //nakajima 2004-01-04, n-kato
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
				case Instruction.LOADMODULE: //[dstmem, module_name]
					// �⥸�塼����ľ°�Υ롼�륻�åȤ������ɤ߹���
					compile.structure.Membrane m = (compile.structure.Membrane)compile.Module.memNameTable.get(inst.getArg2());
					if(m==null) {
						if (!Env.fUseSourceLibrary) { 
							//�饤�֥��⥸�塼����ɤ߹���
							try {
								Class c = Class.forName("translated.Module_" + inst.getArg2());
								Method method = c.getMethod("getRulesets", null);
								Ruleset[] rulesets = (Ruleset[])method.invoke(null, null);
								for (int i = 0; i < rulesets.length; i++) {
									mems[inst.getIntArg1()].loadRuleset(rulesets[i]);
								}
								break;
							} catch (ClassNotFoundException e) {
							} catch (NoSuchMethodException e) {
							} catch (IllegalAccessException e) {
							} catch (InvocationTargetException e) {	}
							//�㳰ȯ�������ɤ߹��߼���
						}
						Env.e("Undefined module "+inst.getArg2());
					} else {
						//Ʊ�쥽������Υ⥸�塼�� or �������饤�֥��ξ��
						Iterator i = m.rulesets.iterator();
						while (i.hasNext()) {
							mems[inst.getIntArg1()].loadRuleset((Ruleset)i.next() );
						}
					}
					break;
					//====�롼�������ܥǥ�̿��====�����ޤ�====

					//====���դ��Ǥʤ��ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿��====��������====
				case Instruction.RECURSIVELOCK : //[srcmem]
					mems[inst.getIntArg1()].recursiveLock();
				    lockedMemList.add(mems[inst.getIntArg1()]);
					break; //n-kato
				case Instruction.RECURSIVEUNLOCK : //[srcmem]
					mems[inst.getIntArg1()].recursiveUnlock();
				    lockedMemList.add(mems[inst.getIntArg1()]);
					break;//nakajima 2004-01-04, n-kato

				case Instruction.COPYCELLS : //[-dstmap, dstmem, srcmem]
					// <strike>��ͳ��󥯤�����ʤ���ʤ��λ���ȤΥ�󥯤�OK�ˤΤ�</strike>
					vars.set(inst.getIntArg1(), mems[inst.getIntArg2()].copyCellsFrom(mems[inst.getIntArg3()]));
					break; //kudo 2004-09-29
				case Instruction.DROPMEM : //[srcmem]
					mems[inst.getIntArg1()].drop();
					break; //kudo 2004-09-29
				case Instruction.LOOKUPLINK : //[-dstlink, srcmap, srclink]
					HashMap srcmap = (HashMap)vars.get(inst.getIntArg2());
					Link srclink = (Link)vars.get(inst.getIntArg3());
					Atom la = (Atom) srcmap.get(srclink.getAtom());//new Integer(srclink.getAtom().id)); // hashCode()��id���ѹ� (2004-10-12) n-kato
					vars.set(inst.getIntArg1(),new Link(la, srclink.getPos()));
					break; //kudo 2004-10-10
				case Instruction.INSERTCONNECTORS : //[-dstset,linklist,mem]
					Functor FUNC_UNIFY = new Functor("=",2);
					List linklist=(List)inst.getArg2();
					Set insset=new HashSet();
					AbstractMembrane srcmem=mems[inst.getIntArg3()];
					for(int i=0;i<linklist.size();i++)
						for(int j=i+1;j<linklist.size();j++){
							Link a=(Link)vars.get(((Integer)linklist.get(i)).intValue());
							Link b=(Link)vars.get(((Integer)linklist.get(j)).intValue());
							if(a==b.getBuddy()){
								Atom eq=srcmem.newAtom(FUNC_UNIFY);
								srcmem.unifyLinkBuddies(a,new Link(eq,0));
								srcmem.unifyLinkBuddies(b,new Link(eq,1));
//								a.getAtom().args[a.getPos()]=new Link(eq,0);
//								b.getAtom().args[b.getPos()]=new Link(eq,1);
//								eq.args[0]=a;
//								eq.args[1]=b;
								insset.add(eq);
							}
						}
					vars.set(inst.getIntArg1(),insset);
					break; //kudo 2004-12-29
				case Instruction.DELETECONNECTORS : //[srcset,srcmap,srcmem]
					Set delset = (Set)vars.get(inst.getIntArg1());
					Map delmap = (Map)vars.get(inst.getIntArg2());
					srcmem = mems[inst.getIntArg3()];
					it = delset.iterator();
					while(it.hasNext()){
						Atom orig=(Atom)it.next();
						Atom copy=(Atom)delmap.get(orig);//new Integer(orig.id));
						srcmem.unifyLinkBuddies(copy.args[0], copy.args[1]);
//						copy.args[0].getAtom().args[copy.args[0].getPos()]=copy.args[1];
//						copy.args[1].getAtom().args[copy.args[1].getPos()]=copy.args[0];
						srcmem.removeAtom(copy);
					}
					break; //kudo 2004-12-29
					//====���դ��Ǥʤ��ץ���ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿��====�����ޤ�====

					//====����̿��====��������====
				case Instruction.COMMIT :
					// �ȥ졼������
					break;//
				case Instruction.REACT : {
					Rule rule = (Rule) inst.getArg1();
					List bodyInsts = (List) rule.body;
					Instruction spec = (Instruction) bodyInsts.get(0);
					int formals = spec.getIntArg1();
					int locals  = spec.getIntArg2();

// // ArrayIndexOutOfBoundsException ���Ǥ��Τǰ��Ū���ѹ�
// if (locals < 10) locals = 1 ;
					
					InterpretiveReactor ir = new InterpretiveReactor(locals, this.currentInterpretedRuleset);
					ir.reloadVars(this, locals, (List)inst.getArg2(),
						(List)inst.getArg3(), (List)inst.getArg4());
					if (ir.interpret(bodyInsts, 0)) return true;
					if (Env.debug == 9) Env.p("info: body execution failed");
					return false; //n-kato
					}
				case Instruction.JUMP: {
					InstructionList label = (InstructionList) inst.getArg1();
					List bodyInsts = (List) label.insts;
					Instruction spec = (Instruction) bodyInsts.get(0);
					int formals = spec.getIntArg1();
					int locals  = spec.getIntArg2();					
					InterpretiveReactor ir = new InterpretiveReactor(locals, this.currentInterpretedRuleset);
					ir.reloadVars(this, locals, (List)inst.getArg2(),
						(List)inst.getArg3(), (List)inst.getArg4());
					if (ir.interpret(bodyInsts, 0)) return true;
					return false; //n-kato
					}
				case Instruction.RESETVARS :
					reloadVars(this, vars.size(), (List)inst.getArg1(),
							(List)inst.getArg2(), (List)inst.getArg3());
					break;

				case Instruction.CHANGEVARS :
					changeVars(this, (List)inst.getArg1(),
							(List)inst.getArg2(), (List)inst.getArg3());
					break; //n-kato

				case Instruction.PROCEED:
					return true; //n-kato

				case Instruction.SPEC://[formals,locals]
					extendVector(inst.getIntArg2());
					break;//n-kato

				case Instruction.BRANCH :
					List subinsts;
					subinsts = ((InstructionList)inst.getArg1()).insts;
					if (interpret(subinsts, 0))
						return true;
					break; //nakajima, n-kato

				case Instruction.LOOP :
					subinsts = (List) ((List) inst.getArg1()).get(0); // reverted by n-kato: remove ".get(0)" by mizuno
					while (interpret(subinsts, 0)) {
					}
					break; //nakajima, n-kato

				case Instruction.RUN :
					subinsts = (List) ((List) inst.getArg1()).get(0);
					interpret(subinsts, 0);
					break; //nakajima

				case Instruction.NOT :
					subinsts = ((InstructionList)inst.getArg1()).insts;
					if (interpret(subinsts, 0))
						return false;
					break; //n-kato

					//====����̿��====�����ޤ�====

					//====��ʣŬ�Ѥ򥫥åȤ���̿��====��������====
				case Instruction.UNIQ : //[ [link1,link2...] ]
				case Instruction.NOT_UNIQ : //[ [link1,link2...] ]
					Uniq uniq = currentInterpretedRuleset.currentRule.uniq;
					if(uniq==null) {
						uniq = currentInterpretedRuleset.currentRule.uniq = new Uniq();
					}
					ArrayList uniqVars = (ArrayList)inst.getArg(0);
					Link[] hEntry = new Link[uniqVars.size()];
//					for(int i=0;i<atoms.length;i++) System.out.println("atoms "+atoms[i]);
//					System.out.println(vars);
//					for(int i=0;i<mems.length;i++) System.out.println("mems "+mems[i]);
					for(int i=0;i<uniqVars.size();i++) {
						int v = ((Integer)uniqVars.get(i)).intValue();
//						Env.p("var# "+v);
						hEntry[i] = (Link)vars.get(v);
//						Env.p("LINK "+hEntry[i]);
					}
					if(inst.getKind()==Instruction.UNIQ) {
						if(!uniq.check(hEntry)) return false;
					} else {
						if(uniq.check(hEntry)) return false;
					}
					break; //hara 2005-12-02
					//====��ʣŬ�Ѥ򥫥åȤ���̿��====�����ޤ�====
					
					//====���դ��ץ���ʸ̮�򰷤�������ɲ�̿��====��������====
				case Instruction.EQGROUND : //[link1,link2]
					boolean eqground_ret = ((Link)vars.get(inst.getIntArg1())).eqGround((Link)vars.get(inst.getIntArg2()));
					if(!eqground_ret)return false;
					break; //kudo 2004-12-03
				case Instruction.NEQGROUND : //[link1,link2]
					boolean neqground_ret = !((Link)vars.get(inst.getIntArg1())).eqGround((Link)vars.get(inst.getIntArg2()));
					if(!neqground_ret)return false;
					break; //kudo 2006-02-18
				case Instruction.COPYGROUND : //[-dstlink, srclink, dstmem]
					vars.set(inst.getIntArg1(),mems[inst.getIntArg3()].copyGroundFrom((Link)vars.get(inst.getIntArg2())));
					break; //kudo 2004-12-03
				case Instruction.REMOVEGROUND : //[srclink,srcmem]
					mems[inst.getIntArg2()].removeGround((Link)vars.get(inst.getIntArg1()));
					break; //kudo 2004-12-08
				case Instruction.FREEGROUND : //[srclink]
					break; //kudo 2004-12-08
					//====���դ��ץ���ʸ̮�򰷤�������ɲ�̿��====�����ޤ�====

					//====�������Τ���Υ�����̿��====��������====
				case Instruction.ISGROUND : //[-natomsfunc,srclink,srcset]
					int isground_ret = ((Link)vars.get(inst.getIntArg2())).isGround((Set)vars.get(inst.getIntArg3()));
					if(isground_ret == -1)return false;
					vars.set(inst.getIntArg1(),new IntegerFunctor(isground_ret));
					break; //kudo 2004-12-03
					
				case Instruction.ISUNARY: // [atom]
					Functor f = atoms[inst.getIntArg1()].getFunctor();
					if (f.getArity() != 1) return false;
					break; // n-kato
//				case Instruction.ISUNARYFUNC: // [func]
//					break;
					
				case Instruction.ISINT : //[atom]
					if (!(atoms[inst.getIntArg1()].getFunctor() instanceof IntegerFunctor)) return false;
					break; //n-kato
				case Instruction.ISFLOAT : //[atom]
					if (!(atoms[inst.getIntArg1()].getFunctor() instanceof FloatingFunctor)) return false;
					break; //n-kato
				case Instruction.ISSTRING : //[atom] // todo StringFunctor���Ѥ����CONNECTRUNTIME���
					if (!(atoms[inst.getIntArg1()].getFunctor() instanceof StringFunctor)) return false;
//					if (!(atoms[inst.getIntArg1()].getFunctor() instanceof ObjectFunctor)) return false;
//					if (!(((ObjectFunctor)atoms[inst.getIntArg1()].getFunctor()).getObject() instanceof String)) return false;
					break; //n-kato
				case Instruction.ISINTFUNC : //[func]
					if (!(vars.get(inst.getIntArg1()) instanceof IntegerFunctor)) return false;
					break; //n-kato
//				case Instruction.ISFLOATFUNC : //[func]
//					break;
//				case Instruction.ISSTRINGFUNC : //[func]
//					break;

				case Instruction.GETCLASS: //[-stringatom, atom]
					if (!(atoms[inst.getIntArg2()].getFunctor() instanceof ObjectFunctor)) return false;
					Object obj = ((ObjectFunctor)atoms[inst.getIntArg2()].getFunctor()).getObject();
					atoms[inst.getIntArg1()] = new Atom(null, new StringFunctor( obj.getClass().toString().substring(6) ));
					break; //n-kato
					
					//====�������Τ���Υ�����̿��====�����ޤ�====

					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====��������====
				case Instruction.INLINE : //[atom, inlineref]
					Inline.callInline( atoms[inst.getIntArg1()], (String)inst.getArg2(), inst.getIntArg3() );
					/// // ���Υ����ɤ�ȤäƤ�����������TranslatorGenerator����
					///writer.write(tabs + "do{ Atom me = (Atom)var" + inst.getIntArg1() + ";\n");
					///writer.write(tabs + "  mem = (AbstractMembrane)var0;\n");
					///writer.write(tabs + Inline.getCode(inst.getIntArg1(), (String)inst.getArg2(), inst.getIntArg3()));
					///writer.write(tabs + "}while(false);\n"); // ����饤�󥳡��ɤ� switch ����ˤ�������ǽ񤫤�Ƥ��롣
					break; //hara
//				case Instruction.BUILTIN: //[class, atom]
//					add(A,B,C) :- int(A),int(B),$builtin:iadd(A,B,C), 
//					Inline.callInline( atoms[inst.getIntArg1()], inst.getIntArg2() );
//					break;
				case Instruction.GUARD_INLINE : //[obj]
					ArrayList gvars = (ArrayList)inst.getArg2();
					ArrayList gOutVars = (ArrayList)inst.getArg3();
					ArrayList gvars2 = new ArrayList();
					for(int i=0;i<gvars.size();i++) {
						int idx = ((Integer)gvars.get(i)).intValue();
						if(vars.size()>idx && vars.get(idx)!=null) {
							gvars2.add(vars.get(idx));
						} else {
							gvars2.add(atoms[idx]);
						}
					}
					if(! Inline.callGuardInline( (String)inst.getArg1(), (Membrane)mems[0], gvars2 ) ) return false;
					// �����ɤ��ͤ��Ѥ�ä����⤷��ʤ��Τ��᤹
					for(int i=0;i<gvars2.size();i++) {
						int v = ((Integer)gvars.get(i)).intValue();
						if(gOutVars.contains(gvars.get(i))) {
							atoms[v] = (Atom)gvars2.get(i);
						} else {
							vars.set(v, gvars2.get(i));
						}
					}
					break;
					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====�����ޤ�====

					//====ʬ����ĥ�Ѥ�̿��====��������====

				case Instruction.CONNECTRUNTIME: //[srcatom] // todo StringFunctor���Ѥ����ISSTRING���
					func = atoms[inst.getIntArg1()].getFunctor();
					if (!(func instanceof ObjectFunctor)) return false;
					if (!(((ObjectFunctor)func).getObject() instanceof String)) return false;
					if (func.getName().equals("")) break; // ��ʸ����ξ��ϤĤͤ������Ȥ���
					if (LMNtalRuntimeManager.connectRuntime(func.getName()) == null) return false;
					break; //n-kato
				case Instruction.GETRUNTIME: //[-dstatom,srcmem] // todo StringFunctor���Ѥ����ISSTRING���
					String hostname = "";
					if (mems[inst.getIntArg2()].isRoot())
						hostname = mems[inst.getIntArg2()].getTask().getMachine().hostname;
					atoms[inst.getIntArg1()] = new Atom(null, new StringFunctor(hostname));
					break; //n-kato
					//====ʬ����ĥ�Ѥ�̿��====�����ޤ�====
					
					//====���ȥॻ�åȤ����뤿���̿��====��������====
				case Instruction.NEWSET : //[-dstset]
					vars.set(inst.getIntArg1(),new HashSet());
					break; //kudo 2004-12-08
				case Instruction.ADDATOMTOSET : //[srcset,atom]
					((Set)vars.get(inst.getIntArg1())).add(atoms[inst.getIntArg2()]);
					break; //kudo 2004-12-08
					//====���ȥॻ�åȤ����뤿���̿��====�����ޤ�====
					
					//====�����Ѥ��Ȥ߹��ߥܥǥ�̿��====��������====
				case Instruction.IADD : //[-dstintatom, intatom1, intatom2]
					int x,y;
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x+y));
					break; //n-kato
				case Instruction.ISUB : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x-y));	
					break; //nakajima 2004-01-05
				case Instruction.IMUL : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x * y));	
					break; //nakajima 2004-01-05
				case Instruction.IDIV : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					if (y == 0) return false;
					//if (y == 0) func = new Functor("NaN",1);
					else func = new IntegerFunctor(x / y);
					atoms[inst.getIntArg1()] = new Atom(null, func);				
					break; //nakajima 2004-01-05, n-kato
				case Instruction.INEG : //[-dstintatom, intatom]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(-x));				
					break;
				case Instruction.IMOD : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					if (y == 0) return false;
					//if (y == 0) func = new Functor("NaN",1);
					else func = new IntegerFunctor(x % y);
					atoms[inst.getIntArg1()] = new Atom(null, func);						
					break; //nakajima 2004-01-05
				case Instruction.INOT : //[-dstintatom, intatom]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(~x));	
					break; //nakajima 2004-01-21
				case Instruction.IAND : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x & y));	
					break; //nakajima 2004-01-21
				case Instruction.IOR : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x | y));	
					break; //nakajima 2004-01-21
				case Instruction.IXOR : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x ^ y));	
					break; //nakajima 2004-01-21
				case Instruction.ISAL : //[-dstintatom, intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x << y));	
					break; //nakajima 2004-01-21
				case Instruction.ISAR : //[-dstintatom, intatom1, intatom2] 
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x >> y));	
					break; //nakajima 2004-01-21					
				case Instruction.ISHR : //[-dstintatom, intatom1, intatom2] 
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg3()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor(x >>> y));	
					break; //nakajima 2004-01-21	
				
				case Instruction.IADDFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();
					vars.set(inst.getIntArg1(), new IntegerFunctor(x+y));
					break; //n-kato
				case Instruction.ISUBFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();
					vars.set(inst.getIntArg1(), new IntegerFunctor(x-y));
					break; //nakajima 2003-01-05
				case Instruction.IMULFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();
					vars.set(inst.getIntArg1(), new IntegerFunctor(x*y));
					break; //nakajima 2003-01-05
				case Instruction.IDIVFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();
					if (y == 0) return false;
					//if (y == 0) func = new Functor("NaN",1);
					else func = new IntegerFunctor(x / y);
					vars.set(inst.getIntArg1(), func);
					break; //nakajima 2003-01-05
				case Instruction.INEGFUNC : //[-dstintfunc, intfunc]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					vars.set(inst.getIntArg1(), new IntegerFunctor(-x));
					break;
				case Instruction.IMODFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();
					if (y == 0) return false;
					//if (y == 0) func = new Functor("NaN",1);
					else func = new IntegerFunctor(x % y);
					vars.set(inst.getIntArg1(), func);
					break; //nakajima 2003-01-05
				case Instruction.INOTFUNC : //[-dstintfunc, intfunc]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					vars.set(inst.getIntArg1(), new IntegerFunctor(~x));
					break; //nakajima 2003-01-21
				case Instruction.IANDFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();	
					vars.set(inst.getIntArg1(), new IntegerFunctor(x & y));				
					break; //nakajima 2003-01-21
				case Instruction.IORFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();	
					vars.set(inst.getIntArg1(), new IntegerFunctor(x | y));				
					break; //nakajima 2003-01-21
				case Instruction.IXORFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();	
					vars.set(inst.getIntArg1(), new IntegerFunctor(x ^ y));				
					break; //nakajima 2003-01-21
				case Instruction.ISALFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();	
					vars.set(inst.getIntArg1(), new IntegerFunctor(x << y));				
					break; //nakajima 2003-01-21
				case Instruction.ISARFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();	
					vars.set(inst.getIntArg1(), new IntegerFunctor(x >> y));				
					break; //nakajima 2003-01-21
				case Instruction.ISHRFUNC : //[-dstintfunc, intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg3())).intValue();	
					vars.set(inst.getIntArg1(), new IntegerFunctor(x >>> y));				
					break; //nakajima 2003-01-21
					//====�����Ѥ��Ȥ߹��ߥܥǥ�̿��====�����ޤ�====

					//====�����Ѥ��Ȥ߹��ߥ�����̿��====��������====
				case Instruction.ILT : //[intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg1()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();	
					if (!(x < y)) return false;
					break; // n-kato
				case Instruction.ILE : //[intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg1()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();	
					if (!(x <= y)) return false;
					break; // n-kato
				case Instruction.IGT : //[intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg1()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();	
					if (!(x > y)) return false;
					break; // n-kato
				case Instruction.IGE : //[intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg1()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();	
					if (!(x >= y)) return false;
					break; // n-kato
				case Instruction.IEQ : //[intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg1()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();	
					if (!(x == y)) return false;
					break; // n-kato
				case Instruction.INE : //[intatom1, intatom2]
					x = ((IntegerFunctor)atoms[inst.getIntArg1()].getFunctor()).intValue();
					y = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();	
					if (!(x != y)) return false;
					break; // n-kato

				case Instruction.ILTFUNC : //[intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg1())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					if (!(x < y)) return false;
					break; // n-kato
				case Instruction.ILEFUNC : //[intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg1())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					if (!(x <= y)) return false;
					break; // n-kato
				case Instruction.IGTFUNC : //[intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg1())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					if (!(x > y)) return false;
					break; // n-kato
				case Instruction.IGEFUNC : //[intfunc1, intfunc2]
					x = ((IntegerFunctor)vars.get(inst.getIntArg1())).intValue();
					y = ((IntegerFunctor)vars.get(inst.getIntArg2())).intValue();
					if (!(x >= y)) return false;
					break; // n-kato
				// IEQFUNC INEFUNC FEQFUNC FNEFUNC FNEFUNC... INT2FLOATFUNC...
					//====�����Ѥ��Ȥ߹��ߥ�����̿��====�����ޤ�====


					//====��ư���������Ѥ��Ȥ߹��ߥܥǥ�̿��====��������====
				case Instruction.FADD : //[-dstfloatatom, floatatom1, floatatom2]
					double u,v;
					u = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg3()].getFunctor()).floatValue();
					atoms[inst.getIntArg1()] = new Atom(null, new FloatingFunctor(u+v));
					break; //n-kato
				case Instruction.FSUB : //[-dstfloatatom, floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg3()].getFunctor()).floatValue();
					atoms[inst.getIntArg1()] = new Atom(null, new FloatingFunctor(u-v));	
					break; // n-kato
				case Instruction.FMUL : //[-dstfloatatom, floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg3()].getFunctor()).floatValue();
					atoms[inst.getIntArg1()] = new Atom(null, new FloatingFunctor(u * v));	
					break; // n-kato
				case Instruction.FDIV : //[-dstfloatatom, floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg3()].getFunctor()).floatValue();
					//if (v == 0.0) func = new Functor("NaN",1);
					//else
					func = new FloatingFunctor(u / v);
					atoms[inst.getIntArg1()] = new Atom(null, func);				
					break; // n-kato
				case Instruction.FNEG : //[-dstfloatatom, floatatom]
					u = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();
					atoms[inst.getIntArg1()] = new Atom(null, new FloatingFunctor(-u));
					break; //nakajima 2004-01-23
				case Instruction.FADDFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					u = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg3())).floatValue();
					vars.set(inst.getIntArg1(), new FloatingFunctor(u + v));	
					break; //nakajima 2004-01-23			
				case Instruction.FSUBFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					u = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg3())).floatValue();
					vars.set(inst.getIntArg1(), new FloatingFunctor(u - v));		
					break; //nakajima 2004-01-23
				case Instruction.FMULFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					u = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg3())).floatValue();
					vars.set(inst.getIntArg1(), new FloatingFunctor(u * v));	
					break; //nakajima 2004-01-23
				case Instruction.FDIVFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					u = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg3())).floatValue();
					vars.set(inst.getIntArg1(), new FloatingFunctor(u / v));		
					break; //nakajima 2004-01-23
				case Instruction.FNEGFUNC : //[-dstfloatfunc, floatfunc]
					u = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					vars.set(inst.getIntArg1(), new FloatingFunctor(-u));	
					break; //nakajima 2004-01-23
					//====��ư���������Ѥ��Ȥ߹��ߥܥǥ�̿��====�����ޤ�====
					
					//====��ư���������Ѥ��Ȥ߹��ߥ�����̿��====��������====	
				case Instruction.FLT : //[floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg1()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();	
					if (!(u < v)) return false;
					break; // n-kato
				case Instruction.FLE : //[floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg1()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();	
					if (!(u <= v)) return false;
					break; // n-kato
				case Instruction.FGT : //[floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg1()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();	
					if (!(u > v)) return false;
					break; // n-kato
				case Instruction.FGE : //[floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg1()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();	
					if (!(u >= v)) return false;
					break; // n-kato
				case Instruction.FEQ : //[floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg1()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();	
					if (!(u == v)) return false;
					break; // n-kato
				case Instruction.FNE : //[floatatom1, floatatom2]
					u = ((FloatingFunctor)atoms[inst.getIntArg1()].getFunctor()).floatValue();
					v = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();	
					if (!(u != v)) return false;
					break; // n-kato
				case Instruction.FLTFUNC : //[floatfunc1, floatfunc2]
					u = ((FloatingFunctor)vars.get(inst.getIntArg1())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					if (!(u < v)) return false;
					break; //nakajima 2003-01-23
				case Instruction.FLEFUNC : //[floatfunc1, floatfunc2]	
					u = ((FloatingFunctor)vars.get(inst.getIntArg1())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					if (!(u <= v)) return false;		
					break; //nakajima 2003-01-23
				case Instruction.FGTFUNC : //[floatfunc1, floatfunc2]
					u = ((FloatingFunctor)vars.get(inst.getIntArg1())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					if (!(u > v)) return false;		
					break; //nakajima 2003-01-23
				case Instruction.FGEFUNC : //[floatfunc1, floatfunc2]
					u = ((FloatingFunctor)vars.get(inst.getIntArg1())).floatValue();
					v = ((FloatingFunctor)vars.get(inst.getIntArg2())).floatValue();
					if (!(u >= v)) return false;
					break; //nakajima 2003-01-23
					//====��ư���������Ѥ��Ȥ߹��ߥ�����̿��====�����ޤ�====

				case Instruction.FLOAT2INT: //[-intatom, floatatom]
					u = ((FloatingFunctor)atoms[inst.getIntArg2()].getFunctor()).floatValue();
					atoms[inst.getIntArg1()] = new Atom(null, new IntegerFunctor((int)u));
					break; // n-kato
				case Instruction.INT2FLOAT: //[-floatatom, intatom]
					x = ((IntegerFunctor)atoms[inst.getIntArg2()].getFunctor()).intValue();
					atoms[inst.getIntArg1()] = new Atom(null, new FloatingFunctor((double)x));
					break; // n-kato


				case Instruction.GROUP:
					subinsts = ((InstructionList)inst.getArg1()).insts;
					if(!interpret(subinsts, 0)){
						//if(lockedMemList.size() == 0)System.out.println("no!");
						//GROUP���̿��Ǽ��Ԥ�����롼��Ŭ�Ѽ���
						//��������GROUP��Ǽ���������Υ�å���������
						Iterator it2 = lockedMemList.iterator();
						while(it2.hasNext()){
							//System.out.println("test");
							((AbstractMembrane)it2.next()).unlock();
						}
						return false;
					}
					break;
					//sakurai
					



				default :
					System.err.println(
						"SYSTEM ERROR: Invalid instruction: " + inst);
					break;
			}
		}
		return false;
	}
}

