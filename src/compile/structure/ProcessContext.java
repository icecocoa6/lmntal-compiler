package compile.structure;

/** ��������������Υץ���ʸ̮�и���ɽ�����饹 */

final public class ProcessContext extends Context {
	/** �����Υ��« */
	public LinkOccurrence bundle = null;
	/** ���󥹥ȥ饯��
	 * @param mem ��°��
	 * @param qualifiedName ����̾
	 * @param arity ����Ū�ʼ�ͳ��󥯰����θĿ�
	 */
	public ProcessContext(Membrane mem, String qualifiedName, int arity) {
		super(mem,qualifiedName,arity);
	}
	/** ���ꤵ�줿̾���ǥ��«����Ͽ���� */
	public void setBundleName(String bundleName) {
		bundle = new LinkOccurrence(bundleName, this, -1);
	}
	/** $p[A,B|*Z]�Τ褦��ʸ����ɽ�����֤�����ư�䴰���줿$p[...|*p]�ΤȤ���$p���֤��� */
	public String toString() {
		String argstext = "";
		if (bundle == null || bundle.name.matches("\\*[A-Z_].*")) { // todo (buddy!=null)���ɤ�����Ƚ�ꤹ�٤��Ǥ���
			argstext = "[" + java.util.Arrays.asList(args).toString()
				.replaceAll("^.|.$","").replaceAll(", ",",");
			if (bundle != null) argstext += "|" + bundle;
			argstext += "]";		
		}
		return getQualifiedName() + argstext;
	}
}
