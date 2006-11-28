package compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import runtime.Env;
import runtime.Functor;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.MergedBranchMap;
import runtime.Rule;

/**
 * �Ԥ߾夲��Ԥ����饹
 * @author sakurai
 */
public class Merger {
	//���ȥ��Ƴ�ƥ�������̿������Ԥ߾夲��f
	//todo?�����Ƴ�ƥ��������Ԥ߾夲
	/** �Ԥ߾夲��Υ롼��μ°��� */
	/** �ե��󥯥���̿����Υޥå� */
	HashMap instsMap;
	/** �ѿ��ֹ�ͥե��󥯥��Υޥå�*/
	HashMap var2funcMap;
	int maxLocals;
	
	public Merger(){
		maxLocals = 0;
		instsMap = new HashMap();
		var2funcMap = new HashMap();
	}
	
	/**
	 * �ƥ롼����Υ��ȥ��Ƴ�ƥ������˽и�����branch̿����Ԥ߾夲��
	 * �ե��󥯥���̿����Υޥåפ���������
	 * @param rules �롼�륻�å���Υ롼�뷲
	 * @return �ե��󥯥���̿����Υޥå�
	 */
	public MergedBranchMap Merging(ArrayList rules){
		Iterator it = rules.iterator();
		while(it.hasNext()){
			Rule rule = (Rule)it.next();
			List atomMatch = (ArrayList)rule.atomMatch;
			List guard = (ArrayList)rule.guard;
			//if(Env.fTrace || Env.debugOption)(rule.body).add(1, new Instruction(Instruction.GETCURRENTRULE, rule));
			if(guard != null){
				//uniq̿�᤬��������Ԥ߾夲��ߡ�todo �ʤ�Ȥ�����
				for(int i=0; i<guard.size(); i++) {
					Instruction inst = (Instruction)guard.get(i);
					if(inst.getKind() == Instruction.UNIQ
						|| inst.getKind() == Instruction.NOT_UNIQ) return null;
				}
			}
			for(int i=0; i<atomMatch.size(); i++){
				Instruction inst = (Instruction)atomMatch.get(i);
				switch(inst.getKind()){
				case Instruction.SPEC:
					if(inst.getIntArg2() > maxLocals) maxLocals = inst.getIntArg2();
					break;
				case Instruction.BRANCH:
					InstructionList label = (InstructionList)inst.getArg1();
					List branchInsts = label.insts;
					maxLocals = 0;
					//uniq�ط��α��޽���
					for(int u=0; u<branchInsts.size(); u++) {
						Instruction uniq = (Instruction)branchInsts.get(u);
						if(uniq.getKind() == Instruction.UNIQ
							|| uniq.getKind() == Instruction.NOT_UNIQ) return null;
					}
					Functor func = null;
					for(int j=1; j<branchInsts.size(); j++){
						Instruction funcInst = (Instruction)branchInsts.get(j);
						if(funcInst.getKind() == Instruction.FUNC){
							func = (Functor)funcInst.getArg2();
							break;
						}
					}
					if(instsMap.containsKey(func)){ //��Ƭ��func̿�᤬Ʊ��branch̿���õ��
						List existInsts = (ArrayList)instsMap.get(func);
						List mergedInsts = new ArrayList();
						mergedInsts = mergeInsts(branchInsts, existInsts);
						Instruction spec = Instruction.spec(2, maxLocals);
						mergedInsts.add(0, spec);
						instsMap.put(func, mergedInsts);
					}
					else {
						instsMap.put(func, branchInsts);
					}
					break;
				default: break;
				}
			}
		}
		Set set = instsMap.entrySet();
		Iterator it2 = set.iterator();
		HashMap optimizedmap= new HashMap();
		while(it2.hasNext()){
			Map.Entry mapentry = (Map.Entry)it2.next();
			ArrayList insts = (ArrayList)mapentry.getValue();
			if(Optimizer.fGuardMove) Optimizer.guardMove(insts);
			//stackOrderChange(insts);
			optimizedmap.put(mapentry.getKey(), insts);
		}
		if(Env.debug >= 1) 	viewMap(optimizedmap);
		return new MergedBranchMap(optimizedmap);
	}
	
	/**
	 * ���������ޥåפ�ɽ�����ǥХå���
	 * param map �ޥå�
	 */
	private void viewMap(HashMap map){
		Set set = map.entrySet();
		Iterator it1 = set.iterator();
		while(it1.hasNext()){
			Map.Entry mapentry = (Map.Entry)it1.next();
			ArrayList branchinststest = (ArrayList)mapentry.getValue();

			System.out.println(mapentry.getKey() + " �� ");
			viewInsts(branchinststest, 1);
			System.out.println("");
		}
	}
	
	/**
	 * �ޥåפ�value�Ǥ���̿�����ɽ�� �ǥХå���
	 * @param insts ̿����
	 * @param tabs ɽ������ݤΥ���
	 */
	private void viewInsts(ArrayList insts, int tabs){
		for(int i=0; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			for(int j=0; j<tabs; j++) System.out.print("    ");
			//������̿��������̿��
			if(inst.getKind() == Instruction.BRANCH){
				System.out.println("branch\t[");
				InstructionList label = (InstructionList) inst.getArg1();
				viewInsts((ArrayList)label.insts, tabs+1);
				for(int j=0; j<tabs; j++) System.out.print("    ");
				System.out.println("]");
			}
			else System.out.println(inst);
		}
	}
	
	/**
	 * 2�Ĥ�̿�������Ӥ���������ʬ��ޡ�������
	 * @param insts1 1���ܤ�̿����
	 * @param insts2 2���ܤ�̿����
	 * @return 2�Ĥ�̿����ζ�����ʬ
	 */
	private ArrayList mergeInsts(List insts1, List insts2){
		List mergedInsts = new ArrayList();
		int differenceIndex = insts1.size()+insts2.size();
		List branchInsts1 = new ArrayList();
		List branchInsts2 = new ArrayList();
		List branchInsts3 = new ArrayList();
		int formal = 0;
		int local = 0;
		for(int i=0, j=0; i<insts1.size() &&  j<insts2.size(); i++, j++){
			Instruction inst1 = (Instruction)insts1.get(i);
			Instruction inst2 = (Instruction)insts2.get(j);
			if(i==0 || j==0){
				if(inst1.getIntArg1() > inst2.getIntArg1())formal = inst1.getIntArg1();
				else formal = inst2.getIntArg1();
				if(inst1.getIntArg2() > inst2.getIntArg2())local = inst1.getIntArg2();
				else local = inst2.getIntArg2();
				if(inst1.getKind() == Instruction.SPEC && inst1.getIntArg2() > maxLocals){
					maxLocals = inst1.getIntArg2();
					continue;
				}
				if(inst2.getKind() == Instruction.SPEC && inst2.getIntArg2() > maxLocals){
					maxLocals = inst2.getIntArg2();
					continue;
				}
			}
			else if (inst1.equalsInst(inst2)){
				mergedInsts.add(inst1);
				continue;
			}
			else {
				if(inst2.getKind() == Instruction.BRANCH){
					//insts2��j���ܰʹߤ�BRANNCH̿��
					for(int k=j; k<insts2.size(); k++){
						Instruction instb = (Instruction)insts2.get(k);
						InstructionList label = (InstructionList)instb.getArg1();
						List instsb = label.insts;
						List tmpinsts = mergeInBranchInsts(insts1, i, instsb);
						if (tmpinsts != null){
							mergedInsts.addAll(branchInsts3);
							mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)tmpinsts)));
							//mergedInsts.addAll((ArrayList)tmpinsts);
							return (ArrayList)mergedInsts;
						}
						else {branchInsts3.add(instb); continue;}
					}
					for(int k=j; k<insts2.size(); k++)
						mergedInsts.add(insts2.get(k));
					for(int k=i; k<insts1.size(); k++)
						branchInsts1.add((Instruction)insts1.get(k));
					if (branchInsts1.size() > 0){
						Instruction spec = (Instruction)branchInsts1.get(0);
						if (spec.getKind() != Instruction.SPEC) branchInsts1.add(0, new Instruction(Instruction.SPEC, formal, local));
					}
					mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)branchInsts1)));
					return (ArrayList)mergedInsts;
				}
				else {
					differenceIndex = i;
					break;
				}
			}
		}
		
		for(int i=differenceIndex; i<insts1.size(); i++)
			branchInsts1.add((Instruction)insts1.get(i));
		for(int i=differenceIndex; i<insts2.size(); i++)
			branchInsts2.add((Instruction)insts2.get(i));
		if (branchInsts1.size() > 0){
			Instruction inst1 = (Instruction)branchInsts1.get(0);
			if (inst1.getKind() != Instruction.SPEC) branchInsts1.add(0, new Instruction(Instruction.SPEC,formal, local));
		}
		if (branchInsts2.size() > 0){
			Instruction inst2 = (Instruction)branchInsts2.get(0);
			if (inst2.getKind() != Instruction.SPEC) branchInsts2.add(0, new Instruction(Instruction.SPEC,formal,local));
		}
		mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)branchInsts2)));
		mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)branchInsts1)));
		return (ArrayList)mergedInsts;
	}
	
	/**
	 * BRANCH̿�����̿����ȤΥޡ���
	 * @param insts1 1���ܤ�̿����
	 * @param insts2 2���ܤ�̿����
	 * @return 2�Ĥ�̿����ζ�����ʬ
	 */
	private ArrayList mergeInBranchInsts(List insts1, int index, List insts2){
		List mergedInsts = new ArrayList();
		int differenceIndex1 = insts1.size()+insts2.size();
		int differenceIndex2 = insts1.size()+insts2.size();
		List branchInsts1 = new ArrayList();
		List branchInsts2 = new ArrayList();
		List branchInsts3 = new ArrayList();
		Instruction spec = (Instruction)insts2.get(0);
		int formal = 0;
		int local = 0;
		if(spec.getKind() != Instruction.SPEC) return null;
		else {
			formal = spec.getIntArg1();
			local = spec.getIntArg2();
			if (spec.getIntArg2() > maxLocals) 	maxLocals = spec.getIntArg2();
		}
		
		int startsize = mergedInsts.size();	
		for(int i=index, j=1; i<insts1.size() && j<insts2.size(); i++, j++){
			Instruction inst1 = (Instruction)insts1.get(i);
			Instruction inst2 = (Instruction)insts2.get(j);
			if (inst1.equalsInst(inst2)){
				mergedInsts.add(inst1);
				continue;
			}
			else {
				if(inst2.getKind() == Instruction.BRANCH){
					//insts2��j���ܰʹߤ�BRANNCH̿��
					for(int k=j; k<insts2.size(); k++){
						Instruction instb = (Instruction)insts2.get(k);
						InstructionList label = (InstructionList)instb.getArg1();
						List instsb = label.insts;
						List tmpinsts = mergeInBranchInsts(insts1, i, instsb);
						if (tmpinsts != null){
							mergedInsts.addAll(branchInsts3);
							mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)tmpinsts)));
							//mergedInsts.addAll((ArrayList)tmpinsts);
							return (ArrayList)mergedInsts;
						}
						else {branchInsts3.add(instb); continue;}
					}
					differenceIndex1 = i;
					differenceIndex2 = j;
					break;
				}
				else {
					differenceIndex1 = i;
					differenceIndex2 = j;
					break;
				}
			}
		}
		int endsize = mergedInsts.size();
		if (startsize == endsize) {
			return null;
		}
		for(int i=differenceIndex1; i<insts1.size(); i++)
			branchInsts1.add((Instruction)insts1.get(i));
		for(int i=differenceIndex2; i<insts2.size(); i++)
			branchInsts2.add((Instruction)insts2.get(i));
		if (branchInsts1.size() > 0){
			Instruction inst1 = (Instruction)branchInsts1.get(0);
			if (inst1.getKind() != Instruction.SPEC) branchInsts1.add(0, new Instruction(Instruction.SPEC, formal, local));
		}
		if (branchInsts2.size() > 0){
			Instruction inst2 = (Instruction)branchInsts2.get(0);
			if (inst2.getKind() != Instruction.SPEC) branchInsts2.add(0, new Instruction(Instruction.SPEC, formal, local));
		}
		mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)branchInsts2)));
		mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)branchInsts1)));
		
		return (ArrayList)mergedInsts;
	}
	
	private void stackOrderChange(List insts){
		var2funcMap.clear();
		for(int i=0; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			switch(inst.getKind()){
			case Instruction.BRANCH:
			case Instruction.JUMP:
				InstructionList label = (InstructionList)inst.getArg1();
				List subinsts = label.insts;
				stackOrderChange(subinsts);
				break;
			case Instruction.NEWATOM:
				Functor func = (Functor)inst.getArg3();
				if(!var2funcMap.containsKey(func)) var2funcMap.put(new Integer(inst.getIntArg1()), func);
				break;
			case Instruction.ENQUEUEATOM:
				Integer var = (Integer)inst.getArg1();
				if(var2funcMap.containsKey(var)){ //�����Ѥ��ʤ����ȥ�
					Functor func2 = (Functor)var2funcMap.get(var);
					if(!instsMap.containsKey(func2)){
						//�롼��Ŭ�Ѥ����Ѥ���ʤ����ȥ�ϥ����å��β��������Ѥ�褦�ˤ���
						for(int j=i-1; j>0; j--){
							Instruction inst2 = (Instruction)insts.get(j);
							if(inst2.getKind() == Instruction.ENQUEUEATOM) continue;
							else {
								insts.remove(i);
								insts.add(j+1, inst);
								break;
							} 
						}
					} else continue;
				} else {
					//�����Ѥ��륢�ȥ�ϥ����å��ξ�������Ѥ�
					for(int j=i+1; j<insts.size(); j++){
						Instruction inst2 = (Instruction)insts.get(j);
						if(inst2.getKind() == Instruction.ENQUEUEATOM) continue;
						else {
							insts.add(j-1, inst);
							insts.remove(i);
							break;
						} 
					}
				}
				break;
			default: 
				break;
			}
		}
	}
	
}
