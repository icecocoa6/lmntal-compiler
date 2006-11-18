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
	 * ���Υ�����Java�η�
	 * test.JavaTypeChcker�ǻȤ���
	 * test.JavaTypeChcker���ʤ��ʤ�Ф��Υե�����ɤ�ä�
	 */
	public Class type; //2006.11.12 inui
	
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
}
