package compile;

import java.util.ArrayList;
import java.util.HashMap;
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
	//���ȥ��Ƴ�ƥ�������̿������Ԥ߾夲��
	//todo?�����Ƴ�ƥ��������Ԥ߾夲
	/** �Ԥ߾夲��Υ롼��μ°��� */
	int maxLocals;
	/** �ե��󥯥���̿����Υޥå� */
	HashMap instsMap;
	
	public Merger(){
		maxLocals = 0;
		instsMap = new HashMap();
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
		if(Optimizer.fGuardMove){
			Set set = instsMap.entrySet();
			Iterator it2 = set.iterator();
			HashMap copymap = new HashMap();
			while(it2.hasNext()){
				Map.Entry mapentry = (Map.Entry)it2.next();
				ArrayList insts = (ArrayList)mapentry.getValue();
				Optimizer.guardMove(insts);
				copymap.put(mapentry.getKey(), insts);
			}
			if(Env.debug >= 1) viewMap(copymap);
			return new MergedBranchMap(copymap);
		}
		if(Env.debug >= 1) viewMap(instsMap);
		return new MergedBranchMap(instsMap);
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
		for(int i=0, j=0; i<insts1.size() &&  j<insts2.size(); i++, j++){
			Instruction inst1 = (Instruction)insts1.get(i);
			Instruction inst2 = (Instruction)insts2.get(j);
			if(i==0 || j==0){
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
							mergedInsts.addAll((ArrayList)tmpinsts);
							return (ArrayList)mergedInsts;
						}
						else continue;
					}
					for(int k=j; k<insts2.size(); k++)
						mergedInsts.add(insts2.get(k));
					for(int k=i; k<insts1.size(); k++)
						branchInsts1.add((Instruction)insts1.get(k));
					if (branchInsts1.size() > 0){
						Instruction spec = (Instruction)branchInsts1.get(0);
						if (spec.getKind() != Instruction.SPEC) branchInsts1.add(0, new Instruction(Instruction.SPEC,0,0));
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
			if (inst1.getKind() != Instruction.SPEC) branchInsts1.add(0, new Instruction(Instruction.SPEC,0,0));
		}
		if (branchInsts2.size() > 0){
			Instruction inst2 = (Instruction)branchInsts2.get(0);
			if (inst2.getKind() != Instruction.SPEC) branchInsts2.add(0, new Instruction(Instruction.SPEC,0,0));
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
		Instruction spec = (Instruction)insts2.get(0);
		if(spec.getKind() != Instruction.SPEC) return null;
		else if (spec.getIntArg2() > maxLocals) maxLocals = spec.getIntArg2();
		
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
							mergedInsts.addAll((ArrayList)tmpinsts);
							return (ArrayList)mergedInsts;
						}
						else continue;
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
			if (inst1.getKind() != Instruction.SPEC) branchInsts1.add(0, new Instruction(Instruction.SPEC,0,0));
		}
		if (branchInsts2.size() > 0){
			Instruction inst2 = (Instruction)branchInsts2.get(0);
			if (inst2.getKind() != Instruction.SPEC) branchInsts2.add(0, new Instruction(Instruction.SPEC,0,0));
		}
		mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)branchInsts2)));
		mergedInsts.add(new Instruction(Instruction.BRANCH, new InstructionList((ArrayList)branchInsts1)));
		
		return (ArrayList)mergedInsts;
	}
}