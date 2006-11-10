package type.quantity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import type.TypeEnv;

import compile.structure.Atom;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.RuleContext;
import compile.structure.RuleStructure;

/**
 * ��Ū����(?)
 * @author kudo
 *
 */
public class QuantityInferer {
	
	private final CountsSet countsset;
	
	public Map<String, FixedCounts> getMemNameToFixedCountsSet(){
		return countsset.getMemNameToFixedCountsSet();
	}
	
	private Membrane root;
	
	public QuantityInferer(Membrane root){
		this.countsset = new CountsSet();
		this.root = root;
	}
	
	/**
	 * ��Ū���Ϥ�Ԥ�
	 * @param root
	 */
	public void infer(){
		inferRHSMembrane(root);
		
		if(TypeEnv.countLevel >= TypeEnv.COUNT_APPLY){
			// �ġ��ζ�����ˤĤ��Ƥ��줾��˸��̤�Ŭ�Ѥ���
			countsset.applyIndividual();
		}
		// Ŭ�Ѳ���ѿ���[0,̵��]�������Ƥ�
		countsset.assignInfinityToVar();
		if(TypeEnv.countLevel >= TypeEnv.COUNT_APPLY){
			// ���ȥ�Ŀ��β��¤�0�Ȥ���Ŭ�Ѳ�����
			countsset.solveByCounts();
		}
		// �����ͤ�׻�������
		countsset.solveIndividuals();
		countsset.solveDynamics();
		// �������ޡ�������
		countsset.mergeFixeds();
		//�ץ�������Ω�������줿��˸��̤�Ŭ�Ѥ���
		countsset.applyCollapseds();
		
		// �����¤�0��ľ����[0,0]��0�ˤ���
		countsset.assignZeroToMinimum();
	}
	
	public void printAll(){
		countsset.printAll();
	}
	
	/**
	 * �롼��ˤĤ�����Ū���Ϥ�Ԥ�
	 * @param rule
	 */
	private void inferRule(RuleStructure rule){
		// ������ȱ������Ʊ����Ȥ��ư���
		inferRuleRootMembrane(rule);
		// ���ջ��������
		for(Membrane rhsmem : ((List<Membrane>)rule.rightMem.mems))
			inferRHSMembrane(rhsmem);
		// ���սи��롼��θ���
		for(RuleStructure rhsrule : ((List<RuleStructure>)rule.rightMem.rules))
			inferRule(rhsrule);
	}

	/**
	 * ���դ����Ƶ�Ū����������
	 * 1. �ץ���ʸ̮���и����ʤ���
	 * 2. �ץ���ʸ̮��1�Ľи�������
	 * 3. �ץ���ʸ̮��2�İʾ�и�������
	 * �ˤ櫓��
	 * @param rhs
	 */
	private void inferRHSMembrane(Membrane rhs){
		
		Set<Membrane> lhss = new HashSet<Membrane>();
		/* �ץ���ʸ̮�����դ��ץ���ʸ̮�ˤĤ��ƺ��սи���ν�������� */
		for(ProcessContext rhsOcc : ((List<ProcessContext>)rhs.processContexts)){
			ProcessContext lhsOcc = (ProcessContext)rhsOcc.def.lhsOcc;
			if(!lhss.contains(lhsOcc.mem))lhss.add(lhsOcc.mem);
		}
		for(ProcessContext rhsOcc : ((List<ProcessContext>)rhs.typedProcessContexts)){
			ProcessContext lhsOcc = (ProcessContext)rhsOcc.def.lhsOcc;
			if(!lhss.contains(lhsOcc.mem))lhss.add(lhsOcc.mem);
		}
		
		// �ץ���ʸ̮��ʣ�����줫����Ƥ����硢�ץ�������Ω�����䤿���
		if(lhss.size() > 1)countsset.collapseProcessIndependency(TypeEnv.getMemName(rhs));

		switch(lhss.size()){
		case 0 : // �ץ���ʸ̮���и����ʤ���
			inferGeneratedMembrane(rhs);
			break;
		default:
			inferMultiInheritedMembrane(lhss,rhs);
//		case 1 : // �ץ���ʸ̮��1�Ľи�������
//			countsset.add(inferInheritedMembrane(((ProcessContext)rhs.processContexts.get(0)).def.lhsOcc.mem,rhs));
//			break;
//		default: // �ץ���ʸ̮��2�İʾ�и�������
//			Set<Membrane> lhss = new HashSet<Membrane>();
//			for(ProcessContext rhsOcc : ((List<ProcessContext>)rhs.processContexts)){
//				ProcessContext lhsOcc = (ProcessContext)rhsOcc.def.lhsOcc;
//				if(!lhss.contains(lhsOcc.mem))lhss.add(lhsOcc.mem);
//			}
//			countsset.add(inferMultiInheritedMembrane(lhss,rhs));
		}

		/** �ץ���ʸ̮��ʬ����̵�����Ȥ��ǧ���� */
		if(!checkIndependency((List<ProcessContext>)rhs.processContexts, rhs) ||
			!checkIndependency((List<ProcessContext>)rhs.typedProcessContexts, rhs) )
			countsset.collapseProcessUnderBounds(TypeEnv.getMemName(rhs));
		
		/** �롼��ʸ̮�ΰ�ư��̵ͭ���ǧ���� */
		for(RuleContext rc : ((List<RuleContext>)rhs.ruleContexts)){
			if(lhss.contains(rc.mem))continue;
			else{
				if(lhss.size() == 0)
					countsset.collapseRuleIndependency(rhs);
				else
					countsset.collapseRulesIndependency(TypeEnv.getMemName(rhs));
			}
		}
		
		// ����θ���
		for(Membrane child : ((List<Membrane>)rhs.mems))
			inferRHSMembrane(child);
		// �롼��θ���
		for(RuleStructure rule : ((List<RuleStructure>)rhs.rules))
			inferRule(rule);
	}

	/**
	 * �錄���줿ʸ̮��Τ����줫��������˽и����뤳�Ȥ�Τ����
	 * �������и����Ƥ��ʤ���硢false���֤�
	 */
	public boolean checkOccurrence(List<ProcessContext> rhsOccs, Membrane rhs){
		boolean okflg = false;
		for(ProcessContext pcRhsOcc : rhsOccs){
			if(pcRhsOcc.mem == rhs){
				okflg = true;
				break;
			}
		}
		return okflg;
	}
	/**�ץ���ʸ̮��ʬ��������å�����
	 * �Ϥ��줿ʸ̮��Τ��줾��κ��սи���Υץ���������͢������Ƥ��뤳�Ȥ��ǧ����
	 * ��ǧ�Ǥ�����true�򡢳�ǧ�Ǥ��ʤ����false���֤� */
	public boolean checkIndependency(List<ProcessContext> rhsOccs, Membrane rhs){
		for(ProcessContext rhsOcc : rhsOccs){
			Membrane lhsmem = ((ProcessContext)rhsOcc.def.lhsOcc).mem;
			if(lhsmem.processContexts.size() > 0){
				boolean ok = checkOccurrence((List<ProcessContext>)((ProcessContext)lhsmem.processContexts.get(0)).def.rhsOccs, rhs);
				if(!ok)return false;
			}
			boolean okflg = true;
			for(ProcessContext lhsOcc : ((List<ProcessContext>)lhsmem.typedProcessContexts)){
				boolean ok = checkOccurrence((List<ProcessContext>)lhsOcc.def.rhsOccs, rhs);
				if(!ok){
					okflg = false;
					break;
				}
			}
			if(!okflg)return false;
		}
		return true;
	}

//	/**
//	 * ���դ��鱦�դ˼����Ѥ��줿��Ʊ����פǤ��롢�Ȥ��Ʋ��Ϥ��롣
//	 * @param lhs
//	 * @param rhs
//	 */
//	private DynamicCounts inferInheritedMembrane(Membrane lhs, Membrane rhs){
//		VarCount vc = new VarCount();
//		Count count = new Count(vc);
//		//���դ��麸�դ򸺻�(���Ϸ�̤�û�)
//		StaticCounts rhsCounts = getCountsOfMem(1,rhs,count);
//		StaticCounts lhsCounts = getCountsOfMem(-1,lhs,count);
//		return new DynamicCounts(lhsCounts, 1, rhsCounts,vc);
//	}
	
	/**
	 * �롼�뱦�դȺ��դ�����ˤĤ��Ƽ����Ѥ��줿��ΤȤ��Ʋ��Ϥ��롣
	 * @param rule
	 * @param count
	 * @return
	 */
	private void inferRuleRootMembrane(RuleStructure rule){
		//���դ��麸�դ򸺻�(���Ϸ�̤�û�)
		VarCount vc = new VarCount();
		Count count = new Count(vc);
		StaticCounts rhsCounts = getCountsOfMem(1,rule.rightMem,count);
		StaticCounts lhsCounts = getCountsOfMem(-1,rule.leftMem,count);
		
		rhsCounts.mem = rule.parent;
		lhsCounts.mem = rule.parent;
		
		countsset.add(new DynamicCounts(lhsCounts, 1, rhsCounts, vc), false);
	}

	/**
	 * ����ʣ�����줫�鱦�դ˼����Ѥ��줿�֥ޡ������줿��פȤ��Ʋ��Ϥ��롣
	 * @param lhss
	 * @param rhs
	 */
	private void inferMultiInheritedMembrane(Set<Membrane> lhss, Membrane rhs){
		VarCount vc = new VarCount();
		Count count = new Count(vc);
		StaticCounts rhsCounts = getCountsOfMem(1,rhs,count);
		StaticCounts lhsCounts = new StaticCounts(rhs);
		for(Membrane lhs : lhss)
			lhsCounts.addAllCounts(getCountsOfMem(-1,lhs,count));
		countsset.add(new DynamicCounts(lhsCounts, lhss.size(), rhsCounts, vc), true);
	}
	
	/**
	 * ñ�Ȥ��������줿��Ȥ��Ʋ��Ϥ��롣
	 * @param mem
	 */
	private void inferGeneratedMembrane(Membrane mem){
		VarCount vc = new VarCount();
		vc.bind(new NumCount(1));
		Count count = new Count(vc);
		countsset.add(getCountsOfMem(1,mem,count));
	}
	
	private StaticCounts getCountsOfMem(int sign, Membrane mem, Count count){
		StaticCounts quantities = new StaticCounts(mem);
		//���ȥ�β��Ϸ��
		for(Atom atom : ((List<Atom>)mem.atoms))
			quantities.addAtomCount(atom,(Count.mul(sign, count)));
		//����β��Ϸ��
		for(Membrane child : ((List<Membrane>)mem.mems))
			quantities.addMemCount(child,(Count.mul(sign,count)));
		return quantities;
	}
	
//	/**
//	 * �롼��Ŭ�Ѳ��������̵�¤Ȥ��Ʋ��Ϸ�̤��
//	 * 
//	 */
//	private boolean solveRVAsInfinity(){
//		return true;
//	}
}
