/**
 * ��������˽и�������ӥ��ȥ�
 */
package compile.parser;

import java.util.LinkedList;

class SrcCompareAtom extends SrcAtom {

	public static final int LT = 0x0001; // <
	public static final int LE = 0x0002; // <=
	public static final int GT = 0x0011; // >
	public static final int GE = 0x0012; // <=
	public static final int EQ = 0x0021; // ==
	public static final int NE = 0x0022; // !=
	
	private int type;
	
	/**
	 * ��ӥ��ȥ���������ޤ�
	 * @param type ��Ӥμ���
	 * @param left ���ץ���
	 * @param right ���ץ���
	 */
	public SrcCompareAtom(int type, Object left, Object right) {
		super(getTypeString(type));
		this.type = type;
		
		process = new LinkedList();
		process.add(left);
		process.add(right);
	}
	
	/**
	 * ��Ӥμ��फ��̾�������ޤ�
	 * @param type ��Ӥμ���
	 * @return ���ꤵ�줿������б�����ʸ���󡢤ʤ����null
	 */
	public static String getTypeString(int type) {
		switch (type) {
			case LT: return "<";
			case LE: return "<=";
			case GT: return ">";
			case GE: return ">=";
			case EQ: return "==";
			case NE: return "!=";
		}
		return null;
	}
}