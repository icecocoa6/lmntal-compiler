package runtime;

import java.io.*;

public class SpecialFunctor extends Functor {
	private String specialName;
	private int kind;
	SpecialFunctor(String name, int arity) {
		this(name, arity, 0);
	}
	public SpecialFunctor(String name, int arity, int kind) {
		super("", arity);
		this.specialName = name.intern();
		this.kind = kind;
	}
	public int hashCode() {
		return specialName.hashCode() + arity;
	}
	public boolean equals(Object o) {
		if(o instanceof SpecialFunctor){
			SpecialFunctor f = (SpecialFunctor)o;
			return o.getClass().equals(SpecialFunctor.class) && specialName == f.specialName && kind == f.kind;
		}
		return false;
	}
	private static final String OUTSIDE_PROXY_NAME = "$out".intern();
	public boolean isOUTSIDE_PROXY(){
		return this.specialName == OUTSIDE_PROXY_NAME;
	}
	public String getName() {
		return specialName + (kind==0 ? "" : ""+kind); 
	}
	public String toString() {
		return specialName + (kind==0 ? "" : ""+kind) + "_" + arity;
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
		specialName = specialName.intern();
	}

	/** �������ĥ��ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤� */
	public String getQuotedFunctorName() {
		return getAbbrName();
	}
}