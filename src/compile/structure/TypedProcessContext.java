package compile.structure;

final public class TypedProcessContext extends Context {

	// TODO buddy �� buddy �����򻲾Ȥ���
	public LinkOccurrence freeLink;
	
	/**
	 * TODO ���ͤ�ͤ��롢���饹��¤
	 * @param name
	 */
	public TypedProcessContext(String name, LinkOccurrence freeLink) {
		super(name);
		this.freeLink = freeLink;
	}
		
}
