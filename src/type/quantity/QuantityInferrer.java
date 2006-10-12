package type.quantity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import type.ConstraintSet;

import compile.structure.Atom;
import compile.structure.Membrane;
import compile.structure.ProcessContext;
import compile.structure.RuleStructure;

/**
 * ��Ū����(?)
 * @author kudo
 *
 */
public class QuantityInferrer {
	
	private ConstraintSet constraints;
	
	private final CountsOfMemSet countsset;
	
	public QuantityInferrer(ConstraintSet constraints){
		this.constraints = constraints;
		countsset = new CountsOfMemSet();
	}
	
	/**
	 * ��Ū���Ϥ�Ԥ�
	 * @param root
	 */
	public void infer(Membrane root){
		// 1��Ŭ�Ѥ��줿�롼��α��դȤ��Ƹ�������
		inferRHSMembrane(root, new NumCount(1));
		
		countsset.printAll();
	}
	
	/**
	 * �롼��ˤĤ�����Ū���Ϥ�Ԥ�
	 * @param rule
	 */
	private void inferRule(RuleStructure rule){
		Count count = new VarCount();
		// ������ȱ������Ʊ����Ȥ��ư���
		countsset.add(inferInheritedMembrane(rule.leftMem, rule.rightMem, count));
		Iterator<Membrane> itm = rule.rightMem.mems.iterator();
		// ���ջ��������
		while(itm.hasNext()){
			inferRHSMembrane(itm.next(),count);
		}
		// ���սи��롼��θ���
		Iterator<RuleStructure> itr = rule.rightMem.rules.iterator();
		while(itr.hasNext()){
			inferRule(itr.next());
		}
	}

	/**
	 * ���դ����Ƶ�Ū����������
	 * 1. �ץ���ʸ̮���и����ʤ���
	 * 2. �ץ���ʸ̮��1�Ľи�������
	 * 3. �ץ���ʸ̮��2�İʾ�и�������
	 * �ˤ櫓��
	 * @param rhs
	 */
	private void inferRHSMembrane(Membrane rhs, Count count){
		int pcsize = rhs.processContexts.size();
		switch(pcsize){
		case 0 : // �ץ���ʸ̮���и����ʤ���
			countsset.add(inferGeneratedMembrane(rhs, count));
			break;
		case 1 : // �ץ���ʸ̮��1�Ľи�������
			countsset.add(inferInheritedMembrane(((ProcessContext)rhs.processContexts.get(0)).def.lhsOcc.mem,rhs, count));
			break;
		default: // �ץ���ʸ̮��2�İʾ�и�������
			Set<Membrane> lhss = new HashSet<Membrane>();
			Iterator<ProcessContext> itp = rhs.processContexts.iterator();
			while(itp.hasNext()){
				ProcessContext lhsOcc = (ProcessContext)itp.next().def.lhsOcc;
				if(!lhss.contains(lhsOcc.mem))lhss.add(lhsOcc.mem);
			}
			countsset.add(inferMultiInheritedMembrane(lhss,rhs, count));
		}
		// ����θ���
		Iterator<Membrane> itm = rhs.mems.iterator();
		while(itm.hasNext()){
			inferRHSMembrane(itm.next(), count);
		}
		// �롼��θ���
		Iterator<RuleStructure> itr = rhs.rules.iterator();
		while(itr.hasNext()){
			inferRule(itr.next());
		}
	}

	/**
	 * ���դ��鱦�դ˼����Ѥ��줿��Ʊ����פǤ��롢�Ȥ��Ʋ��Ϥ��롣
	 * @param lhs
	 * @param rhs
	 */
	private CountsOfMem inferInheritedMembrane(Membrane lhs, Membrane rhs, Count count){
		//���դ��麸�դ򸺻�(���Ϸ�̤�û�)
		CountsOfMem rhsCounts = getCountsOfMem(1,rhs,count,1);
		rhsCounts.addAllCounts(getCountsOfMem(-1,lhs,count,1));
		return rhsCounts;
	}

	/**
	 * ����ʣ�����줫�鱦�դ˼����Ѥ��줿�֥ޡ������줿��פȤ��Ʋ��Ϥ��롣
	 * TODO inferInheritedMembrane�Ϥ�����������ǽ
	 * @param lhss
	 * @param rhs
	 */
	private CountsOfMem inferMultiInheritedMembrane(Set<Membrane> lhss, Membrane rhs, Count count){
		Iterator<Membrane> itl = lhss.iterator();
		CountsOfMem rhsCounts = getCountsOfMem(1,rhs,count,lhss.size());
		while(itl.hasNext()){
			Membrane lhs = itl.next();
			rhsCounts.addAllCounts(getCountsOfMem(-1,lhs,count,lhss.size()));
		}
		return rhsCounts;
	}
	
	/**
	 * ñ�Ȥ��������줿��Ȥ��Ʋ��Ϥ��롣
	 * @param mem
	 */
	private CountsOfMem inferGeneratedMembrane(Membrane mem, Count count){
		return getCountsOfMem(1,mem,count, 0);
	}

	
	private CountsOfMem getCountsOfMem(int sign, Membrane mem, Count count, int multiple){
		CountsOfMem quantities = new CountsOfMem(mem, multiple);
		//���ȥ�β��Ϸ��
		Iterator<Atom> ita = mem.atoms.iterator();
		while(ita.hasNext()){
			// <-R, p/n>
			quantities.addAtomCount(ita.next(),(sign==1?count:new MinCount(count)));
		}
		//����β��Ϸ��
		Iterator<Membrane> itm = mem.mems.iterator();
		while(itm.hasNext()){
			// <-R, m>
			quantities.addMemCount(itm.next(),(sign==1?count:new MinCount(count)));
		}
		return quantities;
	}
	
}
