/**
 * ��������Υ롼���ɽ���ޤ�
 */

package compile.parser;
import java.util.LinkedList;

class SrcRule {
	
	public String name; // �롼��̾
	public LinkedList head;			// �إåɥץ���
	public LinkedList body;			// �ܥǥ��ץ���
	public LinkedList guard;			// �����ɥץ���
	public LinkedList guardNegatives;	// �����������ﹽʸ�Υꥹ��
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȶ��Υ����ɤǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 */
	public SrcRule(String name, LinkedList head, LinkedList body) {
		this(name, head, new LinkedList(), body);
	}
	
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȥ����ɤǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param gurad �����ɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 */
	public SrcRule(String name, LinkedList head, LinkedList guard, LinkedList body) {
		
		this.name = name;
		this.head = head;
		this.guard = guard;
		this.guardNegatives = new LinkedList();
		this.body = body;
		addTypeConstraint(head);
		
	}
	
	/**
	 * ���̾��_IX�ʤɡ�Ƭ��_I���Ĥ��ȼ�ư�ǥ����ɤ�int(_IX)��ä���.
	 * hara. nakano.
	 * */
	public void addTypeConstraint(LinkedList l){
		if(l==null)return;
		for(int i = 0; i < l.size(); i++){
			Object o = l.get(i);
			if(o instanceof SrcLink){
				SrcLink sl = (SrcLink)o;
				if(sl.name.matches("^_I.*")){
					/*intȯ��*/
					LinkedList newl = new LinkedList();
					newl.add(new SrcLink(sl.name));
					SrcAtom newg = new SrcAtom("int",newl);
					this.guard.add(newg);
				}
				else if(sl.name.matches("^_G.*")){
					/*groundȯ��*/
					LinkedList newl = new LinkedList();
					newl.add(new SrcLink(sl.name));
					SrcAtom newg = new SrcAtom("ground",newl);
					this.guard.add(newg);
				}
				else if(sl.name.matches("^_S.*")){
					/*stringȯ��*/
					LinkedList newl = new LinkedList();
					newl.add(new SrcLink(sl.name));
					SrcAtom newg = new SrcAtom("string",newl);
					this.guard.add(newg);
				}
				else if(sl.name.matches("^_U.*")){
					/*unaryȯ��*/
					LinkedList newl = new LinkedList();
					newl.add(new SrcLink(sl.name));
					SrcAtom newg = new SrcAtom("unary",newl);
					this.guard.add(newg);
				}
			}
			else if(o instanceof SrcAtom){
				SrcAtom sa = (SrcAtom)o;
					addTypeConstraint(sa.process);
			}else if(o instanceof SrcMembrane){
				SrcMembrane sm = (SrcMembrane)o;
					addTypeConstraint(sm.process);
			}
		}
	}
	

	/**
	 * �إåɤ����ꤹ��
	 */
	public void setHead(LinkedList head) {
		this.head = head;
	}
	
	/**
	 * �롼��Υإåɤ�������ޤ�
	 * @return �إåɤΥꥹ��
	 */
	public LinkedList getHead() {
		return this.head;
	}
	
	/**
	 * �롼��Υ����ɤ����ޤ�
	 * @return �����ɤΥꥹ��
	 */
	public LinkedList getGuard() {
		return this.guard;
	}
	/**
	 * ��������������������
	 */
	public LinkedList getGuardNegatives() {
		return this.guardNegatives;
	}
	
	/**
	 * �롼��Υܥǥ���������ޤ�
	 * @return �ܥǥ��Υꥹ��
	 */
	public LinkedList getBody() {
		return this.body;
	}
	
	public String toString() {
		return "(rule:"+name+")";
	}
}