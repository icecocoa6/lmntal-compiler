package compile.parser;

import util.Util;

/**
 * �������ե��������̾���ȡ�����ʥ��ȥ��̾���ε��Ҥ˻��Ѥ����ʸ����ˤ�ɽ�����饹
 * <p>���Υ��饹�ϸ��ߡ��ץ���ʸ̮̾����ӥ롼��ʸ̮̾�Ǥϻ��Ѥ���ʤ���
 * <p>
 * '-1'(X)�Ƚ񤭤������Ȥ����뤿�ᡢ�����Ϥ����ˤȤ��ơ�
 * 12��'12'�϶��̤�����1�����ʤ�Фɤ���������Ȥߤʤ��褦�˲����ꤷ����
 * <p>
 * 12 �Ȥ���̾����1�����Υ���ܥ�� [[12]] �ȵ��Ҥ��롣
 * 12]]33 �Ȥ���̾���Υ���ܥ�� '12]]33' �ȵ��Ҥ��롣
 * <p>
 * runtime.Functor���黲�Ȥ��뤿���public���饹���ѹ����Ƥߤ���
 */
public class SrcName {
	/** ̾���ȡ�����ɽ��ʸ���� */
	protected String name;	
	/** ̾���ȡ�����μ��� */
	protected int type;

	// type����
	public static final int PLAIN  = 0; // aaa 12 -12 3.14 -3.14e-1 �㥯�������ʤ���̾���ȡ������
	public static final int SYMBOL = 1; // 'aaa' 'AAA' '12' '-12' '3.14' '-3.14e-1'
	public static final int STRING = 2; // "aaa" "AAA" "12" "-12" "3.14" "-3.14e-1"
	public static final int QUOTED = 3; // [:aaa:] [:AAA:] [:12:] [:-12:] [:3.14:] [:-3.14e-1:]
	public static final int PATHED = 4; // module.p module:p

	/** ɸ���̾���ȡ������ɽ�����������롣
	 * @param name ̾�� */
	public SrcName(String name) {
		this.name = name;
		this.type = PLAIN;
	}
	/** ���ꤵ�줿�����̾���ȡ������ɽ�����������롣*/
	public SrcName(String name, int type) {
		this.name = name;
		this.type = type;
	}
	/** ����̾���ȡ�����ɽ��ʸ������������ */
	public String getName() {
		return name;
	}
	/** �ȡ�����μ�����֤� */
	public int getType() {
		return type;
	}
	/** ���������������ɽ����������롣*/
	public String getSourceName() {
		switch (type) {
		case SYMBOL:
			return Util.quoteString(name, '\'');
		case STRING:
			return Util.quoteString(name, '"');
		case QUOTED:
			return "[:" + name + ":]";
		default:
			//���ֵ�ˡ�Υ��ȥ���̾�ε�ˡ�ˤʤäƤ��ޤ��Τǡ��ü�̾�ϥ������Ȥ��Ƥ�����
			String path, n;
			int pos = name.indexOf('.');
			if (pos > 0) {
				path = name.substring(0, pos);
				n = name.substring(pos+1);
			} else {
				path = null;
				n = name;
			}
			if (!n.matches("^([a-z0-9][A-Za-z0-9_]*)$"))
				n = Util.quoteString(name, '\'');
			return (path == null ? "" : path + ".") + n;
		}
	}
}