/**
 * ��������Υ롼���ɽ���ޤ�
 */

package compile.parser;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class SrcRule {
	
	public String name; // �롼��̾
	public int lineno;	//���ֹ� 2006.1.22 by inui
	public LinkedList head;			// �إåɥץ���
	public LinkedList body;			// �ܥǥ��ץ���
	public LinkedList guard;			// �����ɥץ���
	public LinkedList guardNegatives;	// �����������ﹽʸ�Υꥹ��
	private String text; //�롼��Υƥ�����ɽ��
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȶ��Υ����ɤǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 */
	public SrcRule(String name, LinkedList head, LinkedList body) {
		this(name, head, new LinkedList(), body);
	}
	
	//2006.1.22 by inui
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȶ��Υ����ɤǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 * @param lineno ���ֹ�
	 */
	public SrcRule(String name, LinkedList head, LinkedList body, int lineno) {
		this(name, head, new LinkedList(), body);
		this.lineno = lineno;
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
	
	//2006.1.22 by inui
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȥ����ɤǥ롼����������ޤ�
	 * @param head �إåɤΥꥹ��
	 * @param gurad �����ɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 * @param lineno ���ֹ�
	 */
	public SrcRule(String name, LinkedList head, LinkedList guard, LinkedList body, int lineno) {
		this(name, head, guard, body);
		this.lineno = lineno;
	}
	
	//2006/07/07 by kudo
	/**
	 * ���ꤵ�줿�إåɥ롼��ȥܥǥ��롼��ȥ����ɥ롼��ǽ�������롣
	 * simpagation rule�ι�ʸ���Ф��ƻȤ��륳�󥹥ȥ饯����
	 * @param head �إåɤΥꥹ��
	 * @param head2 �إåɤΥꥹ�� ( \ �θ�� )
	 * @param guard �����ɤΥꥹ��
	 * @param body �ܥǥ��Υꥹ��
	 * @param lineno ���ֹ�
	 */
	public SrcRule(String name, LinkedList head, List head2, LinkedList guard, LinkedList body, int lineno){
		this(name, head, (guard==null?new LinkedList():guard), body, lineno);
		if(head2 != null)unSimpagationize(head2);
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
	
	// by kudo (2006/07/07)
	/**
	 * simpagation rule ���̾�Υ롼��η���ľ�������󥹥ȥ饯������ƤФ�롣
	 * @param head2 �إåɤ�'\'�θ����ʬ�Υꥹ��
	 */
	private void unSimpagationize(List head2){
		// head ������body�إ��ԡ� (�����ɲäΤۤ��������Ѥξ�Ǥ��Թ礬������)
		body.addAll(copySrcs(head));
		// head2��head�θ���Ϣ��
		head.addAll(head2);
	}
	
	/**
	 * ���������֥������ȤΥꥹ�Ȥ򥳥ԡ����롣
	 * @param l
	 * @return
	 */
	private LinkedList copySrcs(List l){
		LinkedList ret = new LinkedList(); // List �����ȳƽ�ǻȤäƤ���getFirst��̵��
		if(l == null)return null;
		Iterator it = l.iterator();
		while(it.hasNext()){
			Object o = it.next();
			if(o instanceof SrcAtom){
				SrcAtom sa = (SrcAtom)o;
				ret.add(new SrcAtom(sa.getName(),copySrcs(sa.getProcess())));
			}
			else if(o instanceof SrcMembrane){
				SrcMembrane sm = (SrcMembrane)o;
				SrcMembrane cpm = new SrcMembrane(copySrcs(sm.getProcess()));
				cpm.name = sm.name;
				cpm.kind = sm.kind;
				cpm.stable = sm.stable;
				cpm.pragma = sm.pragma;
				ret.add(cpm);
			}
			else if(o instanceof SrcProcessContext){
				SrcProcessContext spc = (SrcProcessContext)o;
				SrcProcessContext cppc = new SrcProcessContext(spc.getName());
				cppc.args = copySrcs(spc.args);
				if(spc.bundle != null)cppc.bundle = new SrcLinkBundle(spc.bundle.getName());
				ret.add(cppc);
			}
			else if(o instanceof SrcContext){ // SrcLink, SrcLinkBundle, SrcRuleContext
				SrcLink sl = (SrcLink)o;
				ret.add(new SrcLink(sl.getName()));
			}
			else if(o instanceof SrcRule){ // �����դ˥롼��Ͻи����ʤ�Ȧ
			}
		}
		return ret;
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
	
	/**LMNtal�����������Υƥ�����ɽ����������롣*/
	public String getText() {
		return text;
	}
	void setText() {
		text = SrcDumper.dump(this);
	}
}