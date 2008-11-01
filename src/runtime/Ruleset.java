package runtime;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * �롼��ν��硣
 * ���ߤϥ롼�������Ȥ���ɽ�����Ƥ��뤬������Ū�ˤ�ʣ���Υ롼��Υޥå��󥰤�
 * ���ĤΥޥå��󥰥ƥ��ȤǹԤ��褦�ˤ��롣
 */
abstract public class Ruleset {
	/** new«�����줿̾���ζ����ͤ��Ǽ�������� */
	protected Functor[] holes;
	public List<Rule> compiledRules = new ArrayList<Rule>();
	public boolean isRulesSetted = false;
	public boolean isSystemRuleset = false;
	abstract public String toString();
	abstract public String encode();
	public String[] encodeRulesIndividually(){ return null; }
	/**
	 * ���ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @param mem ����
	 * @param atom ��Ƴ���륢�ȥ�
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	abstract public boolean react(Membrane mem, Atom atom);
	/**
	 * ���Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @param mem ����
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	abstract public boolean react(Membrane mem);
	/**
	 * ���Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @param mem ����
	 * @param nondeterministic �����Ū�¹Ԥ�Ŭ�Ѹ�����Ԥ�����true
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	abstract public boolean react(Membrane mem, boolean nondeterministic);
	/** new«�����줿̾���ζ����ͤ���ꤷ�ƿ�����Ruleset��������롣
	 * @return ������Ruleset */
	//abstract
	public Ruleset fillHoles(Functor[] holes) { return null; }
	
	// 061129 okabe runtimeid �ѻߤˤ��
//	/**
//	 * �����Х�롼�륻�å�ID���������
//	 * @author nakajima */
//	abstract public String getGlobalRulesetID();
	
	////////////////////////////////////////////////////////////////
	
	/** (n-kato)���Υ᥽�åɤ�Ȥ�ʤ��褦�˽񤭴����Ƥ�褤�ʲ���*/
	public byte[] serialize() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			serialize(out);
			out.close();
			return bout.toByteArray();
		} catch (IOException e) {
			//ByteArrayOutputStream�ʤΤ����Ф�ȯ�����ʤ���
			throw new RuntimeException("Unexpected Exception", e);
		}
	}
	/** (n-kato)���Υ᥽�åɤ�Ȥ�ʤ��褦�˽񤭴����Ƥ�褤�ʲ���*/
	public static Ruleset deserialize(byte[] data) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(data);
			ObjectInputStream in = new ObjectInputStream(bin);
			Ruleset ret = Ruleset.deserialize(in);
			in.close();
			return ret;
		} catch (IOException e) {
			//ByteArrayInputStream�ʤΤ����Ф�ȯ�����ʤ���
			throw new RuntimeException("Unexpected Exception", e);
		}
	}
	
	/**
	 * ���Υ��󥹥��󥹤����Ƥ򥹥ȥ꡼��˽񤭹��ࡣ
	 * �ҥ��饹�ǥ����С��饤�ɤ�����ϡ��ǽ�ˤ��Υ᥽�åɤ�ƤӽФ�ɬ�פ����롣
	 * @param out �񤭹��ॹ�ȥ꡼��
	 */
	public void serialize(ObjectOutputStream out) throws IOException {
		out.writeObject(getClass());
		out.writeObject(holes);
	}
	/**
	 * �Х����󤫤�Ruleset���������롣
	 * @param in �ɤ߹��ॹ�ȥ꡼��
	 * @return �����������֥�������
	 */
	public static Ruleset deserialize(ObjectInputStream in) throws IOException {
		Class c;
		Ruleset ret;
		try {
			c = (Class)in.readObject();
			ret = (Ruleset)c.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unexpected Error in deserialization");
		} catch (InstantiationException e) {
			throw new RuntimeException("Unexpected Error in deserialization");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected Error in deserialization");
		}
		ret.deserializeInstance(in);
		return ret;
	}
	/**
	 * ���ȥ꡼�फ�顢���Υ��󥹥��󥹤����Ƥ��������롣�ҥ��饹�ǥ����С��饤�ɤ�����ϡ��ǽ�ˤ��Υ᥽�åɤ�ƤӽФ�ɬ�פ����롣
	 * @param in �ɤ߹��ॹ�ȥ꡼��
	 */
	protected void deserializeInstance(ObjectInputStream in) throws IOException {
		try {
			holes = (Functor[])in.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unexpected Error in deserialization");
		}
	}
}
