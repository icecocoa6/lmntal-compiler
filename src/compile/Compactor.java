/*
 * ������: 2004/10/25
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;
//import runtime.Functor;
//import runtime.Env;

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
		compactInstructionList(rule.memMatchLabel);
		compactInstructionList(rule.guardLabel);
		compactInstructionList(rule.bodyLabel);
	}
	/** ̿������Ŭ�������ͽ���*/
	public static void compactInstructionList(InstructionList label) {
		//if (true) return;
		List insts = label.insts;
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
		varcount = compactBody(insts, varcount);	// ���� (CISC����
		varcount = renumberLocals(insts, varcount);	// �ɽ��ѿ��򿶤�ʤ���
		spec.updateSpec(formals,varcount);
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
			int modify = (inst.getKind() >= Instruction.LOCAL ? Instruction.LOCAL : 0);
			switch (inst.getKind()) {
			// inheritlink [atom1,pos1,link2,mem1]
			// ==> alloclink[link1,atom1,pos1];unifylinks[link1,link2,mem1]
			case Instruction.LOCALINHERITLINK:
			case Instruction.INHERITLINK:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.ALLOCLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.UNIFYLINKS + modify,
								varcount, inst.getIntArg3() ));
				if (inst.data.size() == 4) ((Instruction)insts.get(i + 1)).data.add(inst.getArg4());
				varcount += 1;
				size += 1;
				i += 1;
				continue;

			// unify[atom1,pos1,atom2,pos2,mem1]
			// ==> getlink[link1,atom1,pos1];getlink[link2,atom2,pos2];unifylinks[link1,link2,mem1]
			case Instruction.LOCALUNIFY:
			case Instruction.UNIFY:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.GETLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.GETLINK,
								varcount + 1, inst.getIntArg3(), inst.getIntArg4() ));
				insts.add(i + 2, new Instruction(Instruction.UNIFYLINKS + modify,
								varcount, varcount + 1 ));
				if (inst.data.size() == 5) ((Instruction)insts.get(i + 2)).data.add(inst.getArg5());
				varcount += 2;
				size += 2;
				i += 2;
				continue;

			// newlink[atom1,pos1,atom2,pos2,mem1]
			// ==> alloclink[link1,atom1,pos1];alloclink[link2,atom2,pos2];alloclinks[link1,link2,mem1]
			case Instruction.LOCALNEWLINK:
			case Instruction.NEWLINK:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.ALLOCLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.ALLOCLINK,
								varcount + 1, inst.getIntArg3(), inst.getIntArg4() ));
				insts.add(i + 2, new Instruction(Instruction.UNIFYLINKS + modify,
								varcount, varcount + 1 ));
				if (inst.data.size() == 5) ((Instruction)insts.get(i + 2)).data.add(inst.getArg5());
				varcount += 2;
				size += 2;
				i += 2;
				continue;

			// relink[atom1,pos1,atom2,pos2,mem1]
			// ==> alloclink[link1,atom1,pos1];getlink[link2,atom2,pos2];unifylinks[link1,link2,mem1]
			case Instruction.LOCALRELINK:
			case Instruction.RELINK:
				insts.remove(i);
				insts.add(i,     new Instruction(Instruction.ALLOCLINK,
								varcount, inst.getIntArg1(), inst.getIntArg2() ));
				insts.add(i + 1, new Instruction(Instruction.GETLINK,
								varcount + 1, inst.getIntArg3(), inst.getIntArg4() ));
				insts.add(i + 2, new Instruction(Instruction.UNIFYLINKS + modify,
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
								inst1.getIntArg2(), inst1.getIntArg3() );
				insts.add(i,inst);
				if (inst2.data.size() == 3) inst.add(inst2.getArg3());
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
								inst1.getIntArg2(), inst1.getIntArg3() );
				insts.add(i,inst);
				if (inst2.data.size() == 3) inst.add(inst2.getArg3());
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
								inst1.getIntArg2(), inst1.getIntArg3() );
				insts.add(i,inst);
				if (inst2.data.size() == 3) inst.add(inst2.getArg3());
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
		for (int i = 1; i < size; i++) {
			Instruction inst = (Instruction)insts.get(i);
			if (inst.getOutputType() == -1) continue;
			if (inst.getIntArg1() != locals) {
				Integer src = (Integer)inst.getArg1();
				Integer dst = new Integer(locals);
				Integer tmp = new Integer(varcount);
				HashMap map1 = new HashMap();
				HashMap map2 = new HashMap();
				HashMap map3 = new HashMap();
				map1.put(src, tmp);
				map2.put(dst, src);
				map3.put(tmp, dst);
				Instruction.applyVarRewriteMapFrom(insts,map1,i);
				Instruction.applyVarRewriteMapFrom(insts,map2,i);
				Instruction.applyVarRewriteMapFrom(insts,map3,i);
			}
			locals++;
		}
		return locals;
	}
	
	////////////////////////////////////////////////////////////////
	
	/** ������ʬ��������
	 * @return changed */
	public static boolean eliminateCommonSubexpressions(List insts) {
		return false;
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