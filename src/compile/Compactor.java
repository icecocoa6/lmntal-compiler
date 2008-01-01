/*
 * ������: 2004/10/25
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;
//import runtime.Functor;
import runtime.Env;

import runtime.Rule;

/**
 * ̿�����Ĺ���˴ؤ����Ŭ����Ԥ����饹�᥽�åɤ���ĥ��饹����ȯ���ѡ�
 * RISC�����줿̿����β���������뤿��˻Ȥ����⤷��ʤ���
 * RISC���ƥ����Ѥ�expand�᥽�åɤ���ġ�
 * ̿�᥻�åȤ�RISC���˰����㤦ͽ�ꡣ
 * @author n-kato
 */
public class Compactor {
	/** �롼�륪�֥������Ȥ��Ŭ�������ͽ��� */
	public static void compactRule(Rule rule) {		
		// todo compactInstructionList(rule.atomMatchLabel);
		List atomMatch = rule.atomMatch;
		for(int i=1; i<atomMatch.size(); i++){
			Instruction inst = (Instruction)atomMatch.get(i);
			if(inst.getKind() == Instruction.BRANCH)
				compactInstructionList(((InstructionList)inst.getArg1()).insts);
			else continue;
		}
		if(Env.findatom2)
			compactInsts(rule.memMatch);
		else
			compactInstructionList(rule.memMatch);
		compactInstructionList(rule.guard);
		compactInstructionList(rule.body);
	}
	private static void compactInsts(List insts) {		
		// todo compactInstructionList(rule.atomMatchLabel);
		compactInstructionListForSlimCode(insts);
		for(int i=0; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.BRANCH)
				compactInsts(((InstructionList)inst.getArg1()).insts);
			else if(inst.getKind() == Instruction.GROUP)
				compactInsts(((InstructionList)inst.getArg1()).insts);
			else continue;
		}
	}
	/** ̿������Ŭ�������ͽ���*/
	public static void compactInstructionList(List insts) {
		//if (true) return;
		//List insts = label.insts;
		Instruction spec = (Instruction)insts.get(0);
		int formals = spec.getIntArg1();
		int varcount = spec.getIntArg2();
		varcount = expandBody(insts, varcount);	// Ÿ����RISC����
		boolean changed = true;
		while (changed) {
			changed = false;
//			if (liftUpTestInstructions(insts))  changed = true;
			if (eliminateCommonSubexpressions(insts))  changed = true;
			if (eliminateRedundantInstructions(insts))  changed = true;
		}
		packUnifyLinks(insts);
		varcount = compactBody(insts, varcount);	// ���� (CISC����
		varcount = renumberLocals(insts, varcount);	// �ɽ��ѿ��򿶤�ʤ���
		spec.updateSpec(formals,varcount);
	}
	
	public static void compactInstructionListForSlimCode(List insts) {
		//if (true) return;
		//List insts = label.insts;
//		Instruction spec = (Instruction)insts.get(0);
//		int formals = spec.getIntArg1();
//		int varcount = spec.getIntArg2();
		int varcount = 0;
		varcount = expandBody(insts, varcount);	// Ÿ����RISC����
		boolean changed = true;
		while (changed) {
			changed = false;
//			if (liftUpTestInstructions(insts))  changed = true;
			if (eliminateCommonSubexpressions(insts))  changed = true;
			if (eliminateRedundantInstructions(insts))  changed = true;
		}
		packUnifyLinks(insts);
		varcount = compactBody(insts, varcount);	// ���� (CISC����
//		varcount = renumberLocals(insts, varcount);	// �ɽ��ѿ��򿶤�ʤ���
//		spec.updateSpec(formals,varcount);
	}

	//�ʥƥ���̿���������ѡ� for f(X,Y):-X=Y
	static void genTest(Rule rule) {		
		 if (rule.body.size() > 6) {
			 HashMap map = new HashMap();
			 map.put(new Integer(2), new Integer(4));
			 Instruction.applyVarRewriteMap(rule.body, map);
			 map.clear();
			 map.put(new Integer(3), new Integer(2));
			 Instruction.applyVarRewriteMap(rule.body, map);
			 map.clear();
			 map.put(new Integer(4), new Integer(3));
			 Instruction.applyVarRewriteMap(rule.body, map);
		 }
	}
	
	/** ̿�����RISC������ */
	public static int expandBody(List insts, int varcount) {
		int size = insts.size();
		for (int i = 0; i < size; i++) {
			Instruction inst = (Instruction)insts.get(i);
//060831okabe			int modify = (inst.getKind() >= Instruction.LOCAL ? Instruction.LOCAL : 0);
			switch (inst.getKind()) {
			// inheritlink [atom1,pos1,link2,mem1]
			// ==> alloclink[link1,atom1,pos1];unifylinks[link1,link2,mem1]
			case Instruction.INHERITLINK:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.ALLOCLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.UNIFYLINKS,
							varcount, inst.getIntArg3() ));
				if (inst.data.size() == 4) ((Instruction)insts.get(i + 1)).data.add(inst.getArg4());
				varcount += 1;
				size += 1;
				i += 1;
				continue;

			// unify[atom1,pos1,atom2,pos2,mem1]
			// ==> getlink[link1,atom1,pos1];getlink[link2,atom2,pos2];unifylinks[link1,link2,mem1]
			case Instruction.UNIFY:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.GETLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.GETLINK,
								varcount + 1, inst.getIntArg3(), inst.getIntArg4() ));
				insts.add(i + 2, new Instruction(Instruction.UNIFYLINKS,
								varcount, varcount + 1 ));
				if (inst.data.size() == 5) ((Instruction)insts.get(i + 2)).data.add(inst.getArg5());
				varcount += 2;
				size += 2;
				i += 2;
				continue;

			// newlink[atom1,pos1,atom2,pos2,mem1]
			// ==> alloclink[link1,atom1,pos1];alloclink[link2,atom2,pos2];unifylinks[link1,link2,mem1]
			case Instruction.NEWLINK:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.ALLOCLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.ALLOCLINK,
								varcount + 1, inst.getIntArg3(), inst.getIntArg4() ));
				insts.add(i + 2, new Instruction(Instruction.UNIFYLINKS,
								varcount, varcount + 1 ));
				if (inst.data.size() == 5) ((Instruction)insts.get(i + 2)).data.add(inst.getArg5());
				varcount += 2;
				size += 2;
				i += 2;
				continue;

			// relink[atom1,pos1,atom2,pos2,mem1]
			// ==> alloclink[link1,atom1,pos1];getlink[link2,atom2,pos2];unifylinks[link1,link2,mem1]
			case Instruction.RELINK:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.ALLOCLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.GETLINK,
								varcount + 1, inst.getIntArg3(), inst.getIntArg4() ));
				insts.add(i + 2, new Instruction(Instruction.UNIFYLINKS,
								varcount, varcount + 1 ));
				if (inst.data.size() == 5) ((Instruction)insts.get(i + 2)).data.add(inst.getArg5());
				varcount += 2;
				size += 2;
				i += 2;
				continue;

			// samefunc[atom1,atom2]
			// ==> getfunc[func1,atom1];getfunc[func2,atom2];eqfunc[func1,func2]
			case Instruction.SAMEFUNC:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.GETFUNC,varcount,     inst.getIntArg1() ));
				insts.add(i + 1, new Instruction(Instruction.GETFUNC,varcount + 1, inst.getIntArg2() ));
				insts.add(i + 2, new Instruction(Instruction.EQFUNC,  varcount,     varcount + 1 ));
				varcount += 2;
				size += 2;
				i += 2;
				continue;
			}
		}
		return varcount;
	}
	/** ̿�����CISC������ */
	public static int compactBody(List insts, int varcount) {
		int size = insts.size() - 2;
		for (int i = 0; i < size; i++) {
			Instruction inst;
			Instruction inst0 = (Instruction)insts.get(i);
			Instruction inst1 = (Instruction)insts.get(i + 1);
			Instruction inst2 = (Instruction)insts.get(i + 2);

			// getlink[!link1,atom1,pos1];getlink[!link2,atom2,pos2];unifylinks[!link1,!link2(,mem1)]
			// ==> unify[atom1,pos1,atom2,pos2(,mem1)]
			if (inst0.getKind() == Instruction.GETLINK
			 && inst1.getKind() == Instruction.GETLINK
			 && inst2.getKind() == Instruction.UNIFYLINKS
			 && inst0.getIntArg1() == inst2.getIntArg1() && inst1.getIntArg1() == inst2.getIntArg2()
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst0.getArg1(), i + 3) == 0
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst1.getArg1(), i + 3) == 0) {
				insts.remove(i); insts.remove(i); insts.remove(i);
				inst = new Instruction(Instruction.UNIFY,
								inst0.getIntArg2(), inst0.getIntArg3(),
								inst1.getIntArg2(), inst1.getIntArg3(), inst2.getIntArg3() );
				insts.add(i,inst);
				size -= 2;
				continue;
			}

			// alloclink[!link1,atom1,pos1];alloclink[!link2,atom2,pos2];unifylinks[!link1,!link2(,mem1)]
			// ==> newlink[atom1,pos1,atom2,pos2(,mem1)]
			if (inst0.getKind() == Instruction.ALLOCLINK
			 && inst1.getKind() == Instruction.ALLOCLINK
			 && inst2.getKind() == Instruction.UNIFYLINKS
			 && inst0.getIntArg1() == inst2.getIntArg1() && inst1.getIntArg1() == inst2.getIntArg2()
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst0.getArg1(), i + 3) == 0
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst1.getArg1(), i + 3) == 0) {
				insts.remove(i); insts.remove(i); insts.remove(i);
				inst = new Instruction(Instruction.NEWLINK,
								inst0.getIntArg2(), inst0.getIntArg3(),
								inst1.getIntArg2(), inst1.getIntArg3(), inst2.getIntArg3() );
				insts.add(i,inst);
				size -= 2;
				continue;
			}

			// alloclink[!link1,atom1,pos1];getlink[!link2,atom2,pos2];unifylinks[!link1,!link2(,mem1)]
			// ==> relink[atom1,pos1,atom2,pos2(,mem1)]
			if (inst0.getKind() == Instruction.ALLOCLINK
			 && inst1.getKind() == Instruction.GETLINK
			 && inst2.getKind() == Instruction.UNIFYLINKS
			 && inst0.getIntArg1() == inst2.getIntArg1() && inst1.getIntArg1() == inst2.getIntArg2()
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst0.getArg1(), i + 3) == 0
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst1.getArg1(), i + 3) == 0) {
				insts.remove(i); insts.remove(i); insts.remove(i);
				inst = new Instruction(Instruction.RELINK,
								inst0.getIntArg2(), inst0.getIntArg3(),
								inst1.getIntArg2(), inst1.getIntArg3(), inst2.getIntArg3() );
				insts.add(i,inst);
				size -= 2;
				continue;
			}

			// getlink[!link1,atom1,pos1];alloclink[!link2,atom2,pos2];unifylinks[!link2,!link1(,mem2)]
			// ==> relink[atom2,pos2,atom1,pos1(,mem2)]
			if (inst0.getKind() == Instruction.GETLINK
			 && inst1.getKind() == Instruction.ALLOCLINK
			 && inst2.getKind() == Instruction.UNIFYLINKS
			 && inst0.getIntArg1() == inst2.getIntArg2() && inst1.getIntArg1() == inst2.getIntArg1() 
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst0.getArg1(), i + 3) == 0
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst1.getArg1(), i + 3) == 0) {
				insts.remove(i); insts.remove(i); insts.remove(i);
				inst = new Instruction(Instruction.RELINK,
								inst1.getIntArg2(), inst1.getIntArg3(),
								inst0.getIntArg2(), inst0.getIntArg3(), inst2.getIntArg3() );
				insts.add(i,inst);
				size -= 2;
				continue;
			}
			
			// getfunc[!func1,atom1];getfunc[!func2,atom2];eqfunc[!func1,!func2]
			// ==> samefunc[atom1,atom2]
			if (inst0.getKind() == Instruction.GETFUNC
			 && inst1.getKind() == Instruction.GETFUNC
			 && inst2.getKind() == Instruction.EQFUNC
			 && inst0.getIntArg1() == inst2.getIntArg1() && inst1.getIntArg1() == inst2.getIntArg2()
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst0.getArg1(), i + 3) == 0
			 && Instruction.getVarUseCountFrom(insts, (Integer)inst1.getArg1(), i + 3) == 0) {
				insts.remove(i); insts.remove(i); insts.remove(i);
				inst = new Instruction(Instruction.SAMEFUNC,
								inst0.getIntArg2(), inst1.getIntArg2() );
				insts.add(i,inst);
				size -= 2;
				continue;
			}
		}
		return varcount;
	}
	/** ̿������ѿ��ֹ�򿶤�ʤ��� */
	public static int renumberLocals(List insts, int varcount) {
		int size = insts.size();
		Instruction spec = (Instruction)insts.get(0);
		int locals = spec.getIntArg1();	// �ǽ�ζɽ��ѿ����ֹ�ϡ��������θĿ��ˤ���
//		for (int i = 1; i < size; i++) {
//			Instruction inst = (Instruction)insts.get(i);
//			if (inst.getOutputType() == -1) continue;
//			if (inst.getIntArg1() != locals) {
//				Integer src = (Integer)inst.getArg1();
//				Integer dst = new Integer(locals);
//				Integer tmp = new Integer(varcount);
//				HashMap map1 = new HashMap();
//				HashMap map2 = new HashMap();
//				HashMap map3 = new HashMap();
//				map1.put(src, tmp);
//				map2.put(dst, src);
//				map3.put(tmp, dst);
//				Instruction.applyVarRewriteMapFrom(insts,map1,i);
//				Instruction.applyVarRewriteMapFrom(insts,map2,i);
//				Instruction.applyVarRewriteMapFrom(insts,map3,i);
//			}
//			locals++;
//		}
//		return locals;
		return renumberLocalsSub(insts.subList(1,size),locals,varcount);
	}
	
	public static int renumberLocalsSub(List insts,int locals,int varcount){
		int max = 0;
		for(int i = 0; i < insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.NOT){
				List subinsts = ((InstructionList)inst.getArg1()).insts;
				int sub = renumberLocalsSub(subinsts, locals, varcount);
				if(sub > max)max = sub;
			}
			if(inst.getKind()==Instruction.GUARD_INLINE) {
				// �����ɥ���饤��ξ��ϡ������ѿ���ʣ�������礬���롣hara
				ArrayList out = (ArrayList)inst.getArg3();
				for(int j=0;j<out.size();j++) {
					renumberLocalsSub2(((Integer)out.get(j)).intValue(), locals, varcount, insts, i);
					// �롼�פκǸ�������󥯥���Ȥ��ʤ�(9�Բ��Ǥ���Τ�)
					// 2006/09/22 kudo
					if(j<out.size()-1)locals++;
				}
			} else {
				if (inst.getOutputType() == -1) continue;
				if (inst.getIntArg1() != locals) {
					renumberLocalsSub2(inst.getIntArg1(), locals, varcount, insts, i);
				}
			}
			locals++;
		}
		if(locals > max)max = locals;
		return max;
	}
	
	/**
	 * ̿���� insts �� begin ���ϰʹߤˤĤ��ơ��ѿ��ֹ� isrc �� locals �νи��򤹤٤Ƹ򴹤��롣
	 * @param isrc
	 * @param locals
	 * @param varcount
	 * @param insts
	 * @param begin
	 */
	static void renumberLocalsSub2(int isrc, int locals, int varcount, List insts, int begin) {
		Integer src = new Integer(isrc);
		Integer dst = new Integer(locals);
		Integer tmp = new Integer(varcount);
		HashMap map1 = new HashMap();
		HashMap map2 = new HashMap();
		HashMap map3 = new HashMap();
		map1.put(src, tmp);
		map2.put(dst, src);
		map3.put(tmp, dst);
		Instruction.applyVarRewriteMapFrom(insts,map1,begin);
		Instruction.applyVarRewriteMapFrom(insts,map2,begin);
		Instruction.applyVarRewriteMapFrom(insts,map3,begin);
	}
	
	////////////////////////////////////////////////////////////////
	// CISC����

	/** UnifyLinks̿��ΰ������Ϥ�����󥯤μ�����Ǥ�������ٱ䤹��ʲ���
	 * <p>�Ȥꤢ��������̿�᤬ALLOCLINK�ޤ���LOOKUPLINK�ΤȤ��Τߵ�ǽ���롣*/
	public static void packUnifyLinks(List insts) {
		for (int i = insts.size(); --i >= 1; ) {
			Instruction inst = (Instruction)insts.get(i);
			if (inst.getKind() == Instruction.UNIFYLINKS) {
				int stopper = i;
				for (int k = i; --k >= 1; ) {
					Instruction inst2 = (Instruction)insts.get(k);
					switch (inst2.getKind()) {
					case Instruction.ALLOCLINK:
					case Instruction.LOOKUPLINK:
						if ( inst2.getIntArg1() == inst.getIntArg1()
						  || inst2.getIntArg1() == inst.getIntArg2() ) {
							delayAllocLinkInstruction(insts,k,stopper);
							// 1���ܤ�inst2��inst����2������������Ǥ��ꡢinstľ���˰�ư�Ǥ�����硢
							// 2���ܤ�inst2��1���ܤ�inst2���ɤ��ۤ��ʤ��褦�ˤ���
							if (inst2.getIntArg1() == inst.getIntArg2() && insts.get(stopper - 1) == inst2)
								stopper--;
						}
					}	
				}
			}
		}
	}
	/** insts[i]��ALLOCLINK/LOOKUPLINK̿���Ǥ�������ٱ䤹�롣������insts[stopper]���ɤ��ۤ��ʤ���*/
	public static void delayAllocLinkInstruction(List insts, int i, int stopper) {
		Instruction inst = (Instruction)insts.get(i);
		while (++i < stopper) {
			// insts[i - 1]��ALLOCLINK/LOOKUPLINK̿��
			Instruction inst2 = (Instruction)insts.get(i);
			
			//     ALLOCLINK[link0,...];alloclink[link1,atom1,pos1]
			// ==> alloclink[link1,atom1,pos1];ALLOCLINK[link0,...]
			if (inst2.getKind() == Instruction.ALLOCLINK) {}
			//	   ALLOCLINK[link0,...];unifylinks[link1,link2(,mem1)]
			// ==> unifylinks[link1,link2(,mem1)];ALLOCLINK[link0,...]
			// if link0 != link1 && link0 != link2
			else if (inst2.getKind() == Instruction.UNIFYLINKS
			 && inst.getIntArg1() != inst2.getIntArg1()
			 && inst.getIntArg1() != inst2.getIntArg2() ) {}
			//	   ALLOCLINK[link0,...];getlink[link1,atom1,pos1]
			// ==> getlink[link1,atom1,pos1];ALLOCLINK[link0,...]
			else if (inst2.getKind() == Instruction.GETLINK) {}
			//	   ALLOCLINK[link0,...];lookuplink[link2,set1,link1]
			// ==> lookuplink[link2,set1,link1];ALLOCLINK[link0,...]
			// if link0 != link1
			else if (inst2.getKind() == Instruction.LOOKUPLINK
			 && inst.getIntArg1() != inst2.getIntArg3() ) {}
			else break;
			
			insts.remove(i - 1);
			insts.add(i, inst);
		}
	}
	
	////////////////////////////////////////////////////////////////

	/** ������ʬ��������
	 * @return changed */
	public static boolean eliminateCommonSubexpressions(List insts) {
		boolean changed = false;
		for (int i1 = insts.size() - 1; i1 >= 0; i1--) {
			Instruction inst1 = (Instruction)insts.get(i1);
			if (!inst1.hasSideEffect() && !inst1.hasControlEffect()) {
				//����̿��
				for (int i2 = i1 + 1; i2 < insts.size(); i2++) {
					Instruction inst2 = (Instruction)insts.get(i2);
					if (inst2.hasSideEffect()) {
						break;
					}
					if (sameTypeAndSameInputArgs(inst1, inst2, true)) {
						HashMap varChangeMap = new HashMap();
						varChangeMap.put(inst2.getArg1(), inst1.getArg1());
						insts.remove(i2);
						i2--;
						Instruction.applyVarRewriteMap(insts, varChangeMap);
						changed = true;
					}
				}
			} else {
				switch (inst1.getKind()) {
				//����̿��
				case Instruction.EQATOM:
				case Instruction.EQMEM:
				case Instruction.EQFUNC:
				case Instruction.SAMEFUNC:
				case Instruction.ISINT: case Instruction.ISUNARY:
				case Instruction.IEQ: case Instruction.ILT: case Instruction.ILE:
				case Instruction.IGT: case Instruction.IGE: case Instruction.INE:
				case Instruction.FEQ: case Instruction.FLT: case Instruction.FLE:
				case Instruction.FGT: case Instruction.FGE: case Instruction.FNE:
					for (int i2 = i1 + 1; i2 < insts.size(); i2++) {
						Instruction inst2 = (Instruction)insts.get(i2);
						if (inst2.hasSideEffect()) {
							break; //for
						}
						if (sameTypeAndSameInputArgs(inst1, inst2, false)) {
						 	insts.remove(i2);
							i2--;
							changed = true;
						}
					}
					break; //switch
				}
			}
		}
		return changed;
	}
	/**
	 * ���Ĥ�̿�᤬��Ʊ������ǡ�Ʊ�����ϰ�������Ĥ��ɤ����򸡺����롣
	 */
	private static boolean sameTypeAndSameInputArgs(Instruction inst1, Instruction inst2, boolean hasOutputArg) {
		if (inst1.getKind() != inst2.getKind()) {
			return false;
		}
		for (int i = (hasOutputArg ? 1 : 0); i < inst1.data.size(); i++) {
			if (!inst1.getArg(i).equals(inst2.getArg(i))) {
				return false;
			}
		}
		return true;
	}
	
	/** ��Ĺ��̿�������
	 * @return changed */
	public static boolean eliminateRedundantInstructions(List insts) {
		boolean changed = false;
		for (int i = insts.size(); --i >= 1; ) {
			Instruction inst = (Instruction)insts.get(i);
			// getlink[!link,atom,pos] ==> ; �ʤ�
			if (!inst.hasSideEffect() && !inst.hasControlEffect()) {
				if (Instruction.getVarUseCountFrom(insts, (Integer)inst.getArg1(), i + 1) == 0) {
					insts.remove(i);
					i++;
					changed = true;
					continue;
				}
			}
			switch (inst.getKind()) {
			// eqfunc[func,func] ==> ; �ʤ�
			case Instruction.EQATOM:
			case Instruction.EQMEM:
			case Instruction.EQFUNC:
			case Instruction.SAMEFUNC:
			case Instruction.IEQ:
			case Instruction.FEQ:
				if (inst.getIntArg1() == inst.getIntArg2()) {
				 	insts.remove(i);
					i++;
					changed = true;
					continue;
				}
			}
		}
		return changed;
	}
}