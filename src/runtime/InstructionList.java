package runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * ��٥��դ�̿�����ɽ�����饹��
 * ��Ƭ��̿��Ȥ���ɬ��spec����ġ�
 * 
 * @author n-kato
 */
public class InstructionList implements Cloneable
{
	/**
	 * ��٥�������������
	 */
	private static int nextId = 100;

	/**
	 * ��٥�
	 */
	public String label;

	/**
	 * �������θĿ�
	 */
	private int formals;

	/**
	 * �ɽ��ѿ��θĿ��ʲ������θĿ���ޤ��
	 */
	private int locals;

	/**
	 * ̿���� (Instruction��List)
	 */
	public List<Instruction> insts = new ArrayList<Instruction>();

	/**
	 * ��̿����ޤ���null
	 */
	public InstructionList parent;

	/**
	 * ̤���ѥ᥽�åɡ�
	 */
	public void setFormals(int formals)
	{
	}

	/**
	 * ̤���ѥ᥽�åɡ��ɽ��ѿ��θĿ��򹹿����롣
	 */
	public void updateLocals(int locals)
	{
		if (this.locals < locals) this.locals = locals;
//		if (parent != null) parent.updateLocals(locals);
	}

	/**
	 * ��̿����̵�����ɤ����֤�
	 */
	public boolean isRoot()
	{
		return parent == null;
	}

	/** �̾�Υ��󥹥ȥ饯�� */
	public InstructionList()
	{
		label = "L" + nextId++;
		this.parent = null;
	}

	public InstructionList(InstructionList parent)
	{
		label = "L" + nextId++;
		this.parent = parent;
	}

	/**
	 * �ѡ����������Ѥ��륳�󥹥ȥ饯��
	 */
	public InstructionList(List<Instruction> insts)
	{
		this();
		this.insts = insts;
	}

	/**
	 * �ѡ����������Ѥ��륳�󥹥ȥ饯��
	 */
	public InstructionList(int id, List<Instruction> insts)
	{
		this(insts);
		setLabel(id);
	}

	public void setLabel(int id)
	{
		this.label = "L" + id;
		//��äȸ�����ˡ�Ϥʤ���Τ�������
		if (nextId <= id)
			nextId = id + 1;
	}

	public Object clone()
	{
		InstructionList c = new InstructionList();
		c.formals = formals;
		c.locals = locals;
		c.insts = cloneInstructions(insts);
		return c;
	}

	/**
	 * ���ꤵ�줿̿�����Instruction��List�ˤΥ������������롣
	 * @param insts ��������������̿����
	 * @return ��������������
	 */
	public static List<Instruction> cloneInstructions(List<Instruction> insts)
	{
		List<Instruction> ret = new ArrayList<Instruction>();
		for (Instruction inst : insts)
		{
			ret.add((Instruction)inst.clone());
		}
		return ret;
	}

	/**
	 * ̿�����������̿����ɲä��롣
	 */
	public void add(Instruction inst)
	{
		insts.add(inst);
	}

	/**
	 * ̿����λ���ξ���̿����ɲä��롣
	 */
	public void add(int index, Instruction inst)
	{
		insts.add(index, inst);
	}

	public List<Instruction> getInstructions()
	{
		return insts;
	}

	public String toString()
	{
		return label;
	}

	public String dump()
	{
		StringBuilder buffer = new StringBuilder();
		for (Instruction inst : insts)
		{
			buffer.append(inst);
			buffer.append("\n");
		}
		return buffer.toString();
	}
}
