package runtime;

import java.io.*;

/**
 * �롼��ν��硣
 * ���ߤϥ롼�������Ȥ���ɽ�����Ƥ��뤬������Ū�ˤ�ʣ���Υ롼��Υޥå��󥰤�
 * ���ĤΥޥå��󥰥ƥ��ȤǹԤ��褦�ˤ��롣
 */
abstract public class Ruleset {
	/** new«�����줿̾���ζ����ͤ��Ǽ�������� */
	protected Functor[] holes;
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
	/** new«�����줿̾���ζ����ͤ���ꤷ�ƿ�����Ruleset��������롣
	 * @return ������Ruleset */
	//abstract
	public Ruleset fillHoles(Functor[] holes) { return null; }
	
	/**
	 * �����Х�롼�륻�å�ID���������
	 * @author nakajima */
	abstract public String getGlobalRulesetID();
	
	////////////////////////////////////////////////////////////////
	
	/** (n-kato)���Υ᥽�åɤ�Ȥ�ʤ��褦�˽񤭴����Ƥ�褤�ʲ���*/
	public byte[] serialize() {
		return new byte[0]; // TODO �����ʿ���� - �������Х��ʥ�ž������β�褬��
	}
	/** (n-kato)���Υ᥽�åɤ�Ȥ�ʤ��褦�˽񤭴����Ƥ�褤�ʲ���*/
	public static Ruleset deserialize(byte[] data) {
		return null; // todo �����ʿ���� - �������Х��ʥ�ž������β�褬��
	}
	
	/**
	 * ���Υ��󥹥��󥹤����Ƥ򥹥ȥ꡼��˽񤭹��ࡣ
	 * �ҥ��饹�ǥ����С��饤�ɤ�����ϡ��ǽ�ˤ��Υ᥽�åɤ��֤��ͤ�Х��������Ƭ���ɲä���ɬ�פ����롣
	 * @return �Х�����
	 */
	public void serialize(ObjectOutputStream out) throws IOException {
		out.writeObject(getClass());
		out.writeObject(holes);
	}
	/**
	 * �Х����󤫤�Ruleset���������롣
	 * @param out �Х�����
	 * @return �����������֥�������
	 */
	public static Ruleset deserialize(ObjectInputStream in) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class c = (Class)in.readObject();
		Ruleset ret = (Ruleset)c.newInstance();
		ret.deserializeInstance(in);
		return ret;
	}
	/**
	 * ���ȥ꡼�फ�顢���Υ��󥹥��󥹤����Ƥ��������롣�ҥ��饹�ǥ����С��饤�ɤ�����ϡ��ǽ�ˤ��Υ᥽�åɤ�ƤӽФ�ɬ�פ����롣
	 * @param out �ɤ߹��ॹ�ȥ꡼��
	 */
	protected void deserializeInstance(ObjectInputStream in) throws IOException, ClassNotFoundException {
		holes = (Functor[])in.readObject();
	}
}
