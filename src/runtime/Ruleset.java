package runtime;

/**
 * �롼��ν��硣
 * ���ߤϥ롼�������Ȥ���ɽ�����Ƥ��뤬������Ū�ˤ�ʣ���Υ롼��Υޥå��󥰤�
 * ���ĤΥޥå��󥰥ƥ��ȤǹԤ��褦�ˤ��롣
 */
abstract public class Ruleset {
	abstract public String toString();
	/**
	 * ���ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	abstract public boolean react(Membrane mem, Atom atom);
	/**
	 * ���Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	abstract public boolean react(Membrane mem);
	/**
	 * �롼�륻�åȤ�ID���֤�
	 * @author nakajima
	 * @return �롼�륻�å�ID
	 * 
	 * 
	 */
	abstract public String getGlobalRulesetID();
}
