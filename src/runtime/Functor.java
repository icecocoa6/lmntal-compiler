package runtime;

/**
 * String�̖��O�ƃ����N���̑g����Ȃ�A�g����Functor�B
 */
public class Functor{
	private String name;
	private int arity;
	/** �e�탁�\�b�h�Ŏg�����߂ɕێ����Ă����B�������v���� */
	private String strFunctor;
	public Functor(String name, int arity) {
		this.name = name;
		this.arity = arity;
		// == �Ŕ�r�ł���悤�ɂ��邽�߂�intern���Ă����B
		strFunctor = (name + "_" + arity).intern();
	}
	public String getName() {
		return name;
	}
	public int getArity() {
		return arity;
	}
	public String toString() {
		return strFunctor;
	}
	public int hashCode() {
		return strFunctor.hashCode();
	}
	public boolean equals(Object o) {
		// �R���X�g���N�^��intern���Ă���̂ŁA==�Ŕ�r�ł���B
		return ((Functor)o).strFunctor == this.strFunctor;
	}
}
