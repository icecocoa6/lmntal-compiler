package runtime.functor;


/**
 * �ǡ������ȥ��Ѥ�1�����ե��󥯥���ɽ�����饹
 * @author inui
 */
public abstract class DataFunctor extends Functor {
	public boolean isOutsideProxy() { return false; }
	public boolean isInsideProxy() { return false; }
	public boolean isSymbol() { return false; }
	public boolean isActive() { return false; }
	public int getArity() { return 1; }
}
