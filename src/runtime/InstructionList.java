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
 * ��ʬ�δ֡���Ƭ��̿���spec̿��Ǥ��뤳�Ȥˤ��뤬������������ѿ��ˤ��٤��Ǥ��롣
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
		Iterator it = insts.iterator();
		while (it.hasNext()) {
			c.insts.add(it.next());
		}
		return c;
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
