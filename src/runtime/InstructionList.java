/**
 * ������: 2004/4/8
 * @author n-kato
 */
package runtime;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * ��٥��դ�̿�����ɽ�����饹��
 * <p>
 * ��ʬ�δ���Ƭ��̿���spec̿��Ǥ��뤳�Ȥˤ��Ƥ�����
 * ������spec�ΰ����ͤϤ��Υ��饹�Υ����ѿ��ˤ��٤��Ǥ��롣
 * ���κݡ�InterpretedRuleset.java�Ρ�[0]��spec�ʤΤǥ����åפ���פ��ѻߤ��뤳�ȡ�
 * 
 * @author n-kato
 */
public class InstructionList implements Cloneable {
	/** ��٥������������� */
	private static int nextId = 100;
	/** ��٥� */
	public String label;
	/** ̿���� (Instruction��List) */
	public List insts = new ArrayList();

	public InstructionList() {
		label = "L" + nextId++;
	}
	public InstructionList(String label) {
		this.label = label;
	}
	public Object clone() {
		InstructionList c = new InstructionList();
		c.insts = cloneInstructions(insts);
		return c;
	}
	/** ���ꤵ�줿̿�����Instruction��List�ˤΥ������������롣
	 * @param insts ��������������̿����
	 * @return �������������� */
	public static List cloneInstructions(List insts) {
		List ret = new ArrayList();
		Iterator it = insts.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			ret.add(inst.clone());
		}
		return ret;
	}
	public String toString() {
		return label;
	}
	public String dump() {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < insts.size(); i++){
			buffer.append(insts.get(i));
			buffer.append("\n");
		}
		return buffer.toString();
	}
}
