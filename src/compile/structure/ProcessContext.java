package compile.structure;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * ��������������Υץ���ʸ̮�и���ɽ�����饹
 */
public final class ProcessContext extends Context
{
	/**
	 * �����Υ��«
	 */
	public LinkOccurrence bundle = null;

	/**
	 * ʬΥ����Ʊ̾���դ��ץ���ʸ̮��̾�����Ǽ
	 */
	public LinkedList sameNameList = null;//seiji

	/**
	 * ���̾
	 */
	public String linkName = null;//seiji

	/**
	 * ���󥹥ȥ饯��
	 * @param mem ��°��
	 * @param qualifiedName ����̾
	 * @param arity ����Ū�ʼ�ͳ��󥯰����θĿ�
	 */
	public ProcessContext(Membrane mem, String qualifiedName, int arity)
	{
		super(mem,qualifiedName,arity);
	}

	/**
	 * ���ꤵ�줿̾���ǥ��«����Ͽ����
	 */
	public void setBundleName(String bundleName)
	{
		bundle = new LinkOccurrence(bundleName, this, -1);
	}

	/**
	 * $p[A,B|*Z]�Τ褦��ʸ����ɽ�����֤�����ư�䴰���줿$p[...|*p]�ΤȤ���$p���֤���
	 */
	public String toString()
	{
		String argstext = "";
		if (bundle == null || bundle.name.matches("\\*[A-Z_].*")) // TODO: (buddy!=null)���ɤ�����Ƚ�ꤹ�٤��Ǥ���
		{
			argstext = "[" + Arrays.asList(args).toString()
				.replaceAll("^.|.$","").replaceAll(", ",",");
			if (bundle != null) argstext += "|" + bundle;
			argstext += "]";
		}
		return getQualifiedName() + argstext;
	}

	/**
	 * Ʊ̾�ץ���ʸ̮��ʬΥ�ˤ�꿷�����������줿̾�����Ǽ���Ƥ���ꥹ�Ȥ��֤�
	 */
	public LinkedList getSameNameList()
	{//seiji
		return sameNameList;
	}

	/**
	 * Ʊ̾�ץ���ʸ̮��ʬΥ��ԤʤäƤ��뤫�ݤ�
	 */
	public boolean hasSameName()
	{//seiji
		return sameNameList != null;
	}

	public String getLinkName()
	{//seiji
		return linkName;
	}
}
