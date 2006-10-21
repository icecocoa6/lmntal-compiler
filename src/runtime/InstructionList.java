/**
 * ������: 2004/4/8
 * @author n-kato
 */
package runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ��٥��դ�̿�����ɽ�����饹��
 * ��Ƭ��̿��Ȥ���ɬ��spec����ġ�
 * 
 * @author n-kato
 */
public class InstructionList implements Cloneable, Serializable {
	/** ��٥������������� */
	private static int nextId = 100;
	/** ��٥� */
	public String label;
	/** �������θĿ� */
	private int formals;
	/** �ɽ��ѿ��θĿ��ʲ������θĿ���ޤ�� */
	private int locals;
	/** ̿���� (Instruction��List) */
	public List<Instruction> insts = new ArrayList<Instruction>();
//	/** ��̿����ޤ���null */
//	public InstructionList parent;
	/** ̤���ѥ᥽�åɡ�*/
	public void setFormals(int formals) {
	}
	/** ̤���ѥ᥽�åɡ��ɽ��ѿ��θĿ��򹹿����롣*/
	public void updateLocals(int locals) {
		if (this.locals < locals) this.locals = locals;
//		if (parent != null) parent.updateLocals(locals);
	}
//	/** ��̿����̵�����ɤ����֤� */
//	public boolean isRoot() {
//		return parent == null;
//	}
	/** �̾�Υ��󥹥ȥ饯�� */
	public InstructionList() {
		label = "L" + nextId++;
//		this.parent = parent;
	}

	/** �ѡ����������Ѥ��륳�󥹥ȥ饯�� */
	public InstructionList(ArrayList insts) {
		this();
		this.insts = insts;
	}
	/** �ѡ����������Ѥ��륳�󥹥ȥ饯�� */
	public InstructionList(int id, ArrayList insts) {
		this(insts);
		setLabel(id);
	}
	public void setLabel(int id) {
		this.label = "L" + id;
		//��äȸ�����ˡ�Ϥʤ���Τ�������
		if (nextId <= id)
			nextId = id+1;
	}
	
	public Object clone() {
		InstructionList c = new InstructionList();
		c.formals = formals;
		c.locals = locals;
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
	/** ̿�����������̿����ɲä��롣*/
	public void add(Instruction inst) {
		insts.add(inst);
	}
	/** ̿����λ���ξ���̿����ɲä��롣*/
	public void add(int index, Instruction inst) {
		insts.add(index, inst);
	}
	public List getInstructions() {
		return insts;
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
