package compile.structure;

/** 
 * ��������������Υ�󥯤γƽи���ɽ�����饹
 */
public final class LinkOccurrence {
	/**
	 * ���̾
	 */
	public String name;
	
	/**
	 * ��°���륢�ȥ४�֥�������
	 */
	public Atom atom;
	
	/**
	 * ���ȥ�ǤΥ�󥯰���
	 */
	public int pos;
	
	/** 2�󤷤��и����ʤ����ˡ��⤦�����νи����ݻ����� */
	public LinkOccurrence buddy;
	
	/**
	 * ��󥯽и����������롣
	 * @param name ���̾
	 * @param atom ��°���륢�ȥ�
	 * @param pos ��°���륢�ȥ�Ǥξ��
	 */
	LinkOccurrence(String name, Atom atom, int pos) {
		this.name = name;
		this.atom = atom;
		this.pos = pos;
	}
}
