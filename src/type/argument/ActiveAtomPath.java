package type.argument;

import runtime.functor.Functor;

public class ActiveAtomPath implements Path {

	/**
	 * �����ƥ��֥��ȥ�ν�°��
	 */
	private String memname;
	public String getMemName(){
		return memname;
	}

	/**
	 * �����ƥ��֥��ȥ�Υե��󥯥�
	 */
	private Functor functor;
	public Functor getFunctor(){
		return functor;
	}

	/**
	 * ���Υѥ��ΰ�������
	 */
	private int pos;
	public int getPos(){
		return pos;
	}

	/**
	 * @param memname nullʸ�����Ϥ���뤳�ȤϤʤ�
	 * @param functor
	 * @param pos
	 */
	public ActiveAtomPath(String memname, Functor functor, int pos) {
		this.memname = memname;
		this.functor = functor;
		this.pos = pos;
	}

	public String toString() {
		return "<" + memname + ":" + functor + "," + pos + ">";
	}
	
	public String toStringWithOutAnonMem(){
		return "<" + (memname=="??"?"":(memname + ":")) + functor + "," + pos + ">";
	}

	public int hashCode(){
		return memname.hashCode() + functor.hashCode() + pos;
	}
	
	public boolean equals(Object o){
		if(o instanceof ActiveAtomPath){
			ActiveAtomPath aap = (ActiveAtomPath)o;
			return memname.equals(aap.memname) && functor.equals(aap.functor) && pos == aap.pos;
		}
		else return false;
	}
}
