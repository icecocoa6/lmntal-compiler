package runtime;

import java.io.*;

public class SpecialFunctor extends Functor {
	private String name;
	private int kind;
	SpecialFunctor(String name, int arity) {
		this(name, arity, 0);
	}
	public SpecialFunctor(String name, int arity, int kind) {
		super("", arity);
		this.name = name.intern();
		this.kind = kind;
	}
	public int hashCode() {
		return name.hashCode() + arity;
	}
	public boolean equals(Object o) {
		if(o instanceof SpecialFunctor){
			SpecialFunctor f = (SpecialFunctor)o;
			return o.getClass().equals(SpecialFunctor.class) && name == f.name && kind == f.kind;
		}
		return false;
	}
	public boolean isOUTSIDE_PROXY(){
		return this.name == "$out".intern();
	}
	public String getName() {
		return name; 
	}
	public String toString() {
		return name + (kind!=0 ? "" : ""+kind) + "_" + arity;
	}
	public int getKind() {
		return kind;
	}

	/**
	 * ľ���������˸ƤФ�롣
	 * author mizuno
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		name = name.intern();
	}

	/** �������ĥ��ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤� */
	public String getQuotedFunctorName() {
		return getAbbrName();
	}
}