package compile.structure;

/** 
 * ��������������Υ�󥯤ޤ��ϥ��«�γƽи���ɽ�����饹��<br>
 * runtime.Link �Ȱ�äơ�LinkOccurrence.atom �Ϥ�����¦�Υ��ȥ४�֥������Ȥ����äƤ��롣
 * 
 */
public final class LinkOccurrence {
	/**
	 * ���̾
	 */
	public String name;
	
	/**
	 * ��°���륢�ȥ४�֥������ȡʤ�����¦��
	 */
	public Atomic atom;
	
	/**
	 * �ʤ�����¦�Ρ˥��ȥ�ǤΥ�󥯰���
	 */
	public int pos;
	
	/** 2�󤷤��и����ʤ����ˡ��⤦�����νи����ݻ����� */
	public LinkOccurrence buddy = null;
	
	/**
	 * ��󥯽и����������롣
	 * @param name ���̾
	 * @param atom ��°���륢�ȥ�
	 * @param pos ��°���륢�ȥ�Ǥξ��
	 */
	public LinkOccurrence(String name, Atomic atom, int pos) {
		this.name = name;
		this.atom = atom;
		this.pos = pos;
	}
	
	public String toString() {
		return name.replaceAll("~", "_");
	}

	public boolean equals(Object o)
	{
		return o == this
			|| o instanceof LinkOccurrence && eq((LinkOccurrence)o);
	}

	public boolean eq(LinkOccurrence l)
	{
		return atom == l.atom && pos == l.pos;
	}

	public int hashCode()
	{
		return atom.hashCode() ^ (17 * pos);
	}
}
