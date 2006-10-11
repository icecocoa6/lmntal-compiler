package type.quantity;

import type.ConstraintSet;

import compile.structure.Membrane;
import compile.structure.RuleStructure;

/**
 * ��Ū����(?)
 * @author kudo
 *
 */
public class QuantityInferrer {
	
	private ConstraintSet constraints;
	
	private final Quantities quantities;
	
	public QuantityInferrer(ConstraintSet constraints){
		this.constraints = constraints;
		this.quantities = new Quantities();
	}

	public void infer(Membrane root){
		inferGeneratedMembrane(root);
	}
	
	/**
	 * 
	 * @param rule
	 */
	private void inferRule(RuleStructure rule){
		inferInheritedMembranes(rule.leftMem, rule.rightMem);
	}
	/**
	 * ���դ��鱦�դ˼����Ѥ��줿��Ʊ����פǤ��롢�Ȥ��Ʋ��Ϥ��롣
	 * @param lhs
	 * @param rhs
	 */
	private void inferInheritedMembranes(Membrane lhs, Membrane rhs){
		
	}
	/**
	 * ñ�Ȥ��������줿��Ȥ��Ʋ��Ϥ��롣
	 * @param mem
	 */
	private void inferGeneratedMembrane(Membrane mem){
		
	}
}
