/**
 * LMNtal Parser �ᥤ�󥯥饹
 * ���ĤΥ����������ɤ�Membrane�Ȥ���ɽ������ޤ���
 */

package compile.parser;

import java_cup.runtime.Scanner;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Enumeration;

import compile.structure.*;

public class LMNParser {
	
	private Scanner lex = null;
	
	private static int nLinkNumber = 0;
	
	private static final String PREFIX_LINK_NAME = "L::";
	private static final String PREFIX_PROXY_LINK_NAME = "P::";
	
	/**
	   ������ϴ�����Ϥ���ꤷ�ƽ����
	   @param lex ���Ѥ��������ϴ�
	*/
	protected LMNParser(Scanner lex) {
		this.lex = lex;
	}
	
	/**
		�ǥե���Ȥλ�����ϴ�Ȼ��ꤵ�줿���ȥ꡼��ǽ����
	*/
	public LMNParser(Reader in) {
		this(new Lexer(in));
	}

	/**	
		���Ϥη�̤� LinkedList �Ȥ�������ڤȤ����֤��ޤ���
		@return ���Ϥ��줿�����������ɤΥꥹ��
		@throws ParseException 
	*/
	protected LinkedList parseSrc() throws ParseException {
		parser p = new parser(lex);
		LinkedList result = null;
		try {
			result = (LinkedList)p.parse().value;
		} catch (Exception e) {
			throw new ParseException(e.getMessage());	
		}
		return result;
	}
	
	/**
	 * �������ե��������Ϥ��ޤ�
	 * ���ϸ�ϥ�󥯤�Ž���դ����ץ������κ������Ԥ��Ƥ��ޤ�
	 * @return �������ե��������Ρʤ���������롼�뤬���Ĥ����ޤޤ�����
	 * @throws ParseException
	 */
	public Membrane parse() throws ParseException {
		LinkedList src = parseSrc();
		Membrane mem = new Membrane(null);
		addProcessToMem(src, mem);
		createProxy(mem);
		return mem;
	}
	
	/**
	 * ��˥��ȥࡢ���졢�롼��ʤɤ������Ͽ����
	 * @param list ��Ͽ�������ץ����Υꥹ��
	 * @param mem ��Ͽ�����
	 * @throws ParseException
	 */
	private void addProcessToMem(LinkedList list, Membrane mem) throws ParseException {
		for (int i=0;i<list.size();i++) addObjectToMem(list.get(i), mem);
	}
	
	/**
	 * �ץ������κ����ȥ�󥯤η��
	 * ��������ƥ�󥯤η�硢�ץ����κ����ϹԤ��Ƥ���Ȥ���
	 * @param mem ��󥯽�����Ԥ�������
	 * @throws ParseException
	 */
	private void createProxy(Membrane mem) throws ParseException {
		Hashtable linkNameTable = new Hashtable();
		// Ʊ�����٥�Υ�󥯷���Ԥ�
		for (int i=0;i<mem.atoms.size();i++) {
			Atom a = (Atom)mem.atoms.get(i);
			// ��󥯤μ��Ф�
			for (int j=0;j<a.args.length;j++) {
				connectLink(a.args[j], linkNameTable);
			}
		}
		// ���줫�鼫ͳ��󥯤μ��Ф������
		for (int i=0;i<mem.mems.size();i++) {
			Membrane childMem = (Membrane)mem.mems.get(i);
			// ��ͳ��󥯤μ��Ф�
			for (int j=0;j<childMem.freeLinks.size();j++) {
				connectLink((LinkOccurrence)childMem.freeLinks.get(i), linkNameTable, true, mem);
			}
		}

		Enumeration enumLinkName = linkNameTable.keys();
		// ���줬���ꡢ�б����ʤ���Τϼ�ͳ��󥯤Ȥ�����Ͽ
		if (mem.mem != null) {
			while (enumLinkName.hasMoreElements()) {
				// �ץ������̤�����Υ�󥯤����
				LinkOccurrence freeLink = 
					addProxyToMem((LinkOccurrence)linkNameTable.get(enumLinkName.nextElement()), mem);
				mem.freeLinks.add(freeLink);
			}
		}
		// ���줬�ʤ��Ƽ�ͳ��󥯤�������
		else {
			
		}
	}
	
	/**
	 * ���ȥ�˥ץ��������ɲ�
	 * @param freeLink �ץ��������̤��Ƴ��˽Ф뼫ͳ���
	 * @param mem �ɲ������
	 * @return �ץ���������Υ�󥯥��֥�������
	 */
	private LinkOccurrence addProxyToMem(LinkOccurrence freeLink, Membrane mem) {
		ProxyAtom proxy = new ProxyAtom(mem);
		proxy.args[0] = new LinkOccurrence(freeLink.name, proxy, 0); // ��¦
		proxy.args[1] = new LinkOccurrence(PREFIX_PROXY_LINK_NAME+freeLink.name, proxy, 1); // ��¦
		// ��¦�η��
		proxy.args[1].buddy = freeLink;
		freeLink.buddy = proxy.args[1];
		freeLink.name = proxy.args[1].name;
		// �ץ������ɲ�
		mem.atoms.add(proxy);
		
		return proxy.args[0];
	}
	
	
	/**
	 * ��󥯤η���Ԥ�(Ʊ�����¸�ߤ�����)
	 * @param lnk ����Ԥ�����
	 * @param linkNameTable ���̾�˥�󥯥��֥������Ȥ��б��Ť����ơ��֥�
	 * @throws ParseException 2����¿�����̾���и��������
	 */
	private void connectLink(LinkOccurrence lnk, Hashtable linkNameTable) throws ParseException {
		connectLink(lnk, linkNameTable, false, null);
	}
	
	/**
	 * ��󥯤η���Ԥ�
	 * @param lnk ����Ԥ�����
	 * @param linkNameTable ���̾�˥�󥯥��֥������Ȥ��б��Ť����ơ��֥�
	 * @param isOverMembrane ����̲᤹���󥯤�
	 * @param mem �ɲ������
	 * @throws ParseException 2����¿�����̾���и��������
	 */
	private void connectLink(LinkOccurrence lnk, Hashtable linkNameTable, boolean isOverMembrane, Membrane mem) throws ParseException {
		// 3��ʾ�νи�
		if (linkNameTable.get(lnk.name) == Boolean.TRUE) {
			throw new ParseException("Link Name '" + lnk.name + "' appear more than 3.");
		}
		// 1���ܤνи�
		else if (linkNameTable.get(lnk.name) == null) {
			linkNameTable.put(lnk.name, lnk);
		}
		// 2���ܤνи�
		else {
			LinkOccurrence buddy = (LinkOccurrence)linkNameTable.get(lnk.name);
			if (isOverMembrane) buddy = addProxyToMem(buddy, mem);
			lnk.buddy = buddy;
			buddy.buddy = lnk;
			linkNameTable.put(lnk.name, Boolean.TRUE);
		}
	}
	
	/**
	 * ��˥��ȥࡢ���졢�롼��ʤɤ��ɲ�
	 * @param obj ���ȥࡢ���졢�롼��ʤɤΥ��֥�������
	 * @param mem �ɲä�������
	 * @throws ParseException obj��̤�Τʥ��֥������Ȥξ��
	 */
	private void addObjectToMem(Object obj, Membrane mem) throws ParseException {
		// ���ȥ�
		if (obj instanceof SrcAtom) {
			addAtomToMem((SrcAtom)obj, mem);
		}
		// ��
		else if (obj instanceof SrcMembrane) {
			SrcMembrane sMem = (SrcMembrane)obj;
			Membrane p = new Membrane(mem);
			addProcessToMem(sMem.getProcess(), p);
			// ��󥯤�Ž���դ� �ץ�����������
			createProxy(p);
			mem.mems.add(p); 
		}
		// �롼��
		else if (obj instanceof SrcRule) {
			addRuleToMem((SrcRule)obj, mem);
		}
		// �ץ�������ƥ�����
		else if (obj instanceof SrcProcessContext) {
			addProcessContextToMem((SrcProcessContext)obj, mem);
		}
		// �롼�륳��ƥ�����
		else if (obj instanceof SrcRuleContext) {
			addRuleContextToMem((SrcRuleContext)obj, mem);
		}
		// ���ñ�첽
		else if (obj instanceof SrcLinkUnify) {
			addLinkUnifyToMem((SrcLinkUnify)obj, mem);
		}
		// ����¾ 
		else {
			throw new ParseException("Unknown Object to add membrane:"+obj);
		}
	}
	
	/**
	 * ���ȥ������ɲ�
	 * @param sAtom �ɲä��������ȥ�
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addAtomToMem(SrcAtom sAtom, Membrane mem) throws ParseException {
		addAtomToMem(sAtom, mem, null);
	}
	
	/**
	 * ���ȥ������ɲ�
	 * @param sAtom �ɲä��������ȥ�
	 * @param mem �ɲ������
	 * @param lastLink ��ά������Ƥ�����
	 * @throws ParseException ���ȥ�Υ�󥯤�̤�Τʥ��֥������Ȥ�������
	 */
	private void addAtomToMem(SrcAtom sAtom, Membrane mem, SrcLink lastLink) throws ParseException {
		if (lastLink != null) sAtom.process.add(lastLink);
		LinkedList p = sAtom.getProcess();
		Atom atom = new Atom(mem, sAtom.getName(), p.size());
		// ��󥯤��Խ�
		for (int i=0;i<p.size();i++) {
			Object obj = p.get(i);
			// �̾���
			if (obj instanceof SrcLink) {
				addLinkToAtom((SrcLink)obj, atom, i);
			}
			// ���ȥ�
			else if (obj instanceof SrcAtom) {
				SrcLink link = createNewLink();
				addAtomToMem((SrcAtom)obj, mem, new SrcLink(link.getName()));
				addLinkToAtom(link, atom, i);
			}
			// ����¾
			else {
				throw new ParseException("Unknown Object to add Link:"+obj);
			}
		}
		mem.atoms.add(atom);
	}
	
	/**
	 * ��ά������Ƥ����󥯤�̾����Ĥ��ƺ���
	 * @return ��ˡ����ʥ��̾
	 */
	private SrcLink createNewLink() {
		nLinkNumber++;
		return new SrcLink(PREFIX_LINK_NAME+nLinkNumber);
	}
	
	/**
	 * ���ȥ�˥�󥯤��ɲä���
	 * @param link �ɲä��������
	 * @param atom �ɲ���Υ��ȥ�
	 * @param pos �ɲ���Υ��ȥ�Ǥξ��
	 * @throws ParseException �ɲ���ξ�꤬���ȥ��¸�ߤ��ʤ����
	 */
	private void addLinkToAtom(SrcLink link, Atom atom, int pos) throws ParseException {
		if (pos >= atom.args.length) throw new ParseException("Out of Atom args length:"+pos);
		atom.args[pos] = new LinkOccurrence(link.getName(), atom, pos);
	}

	/**
	 * �롼�������ɲä���
	 * @param sRule �ɲä������롼��
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addRuleToMem(SrcRule sRule, Membrane mem) throws ParseException {
		RuleStructure rule = new RuleStructure();
		// TODO ��ά��ˡ��Ÿ�� sRule����Ȥ��֤�����
		
		addProcessToMem(sRule.getHead(), rule.leftMem);
		createProxy(rule.leftMem);
		addProcessToMem(sRule.getBody(), rule.rightMem);
		createProxy(rule.rightMem);
		
		mem.rules.add(rule);
		rule.parent = mem;       // RuleStructure.parent ���ɲä����Τ��ɲ� by Hara
	}

	/**
	 * �ץ����ѿ�������ɲ�
	 * @param sProc �ɲä������ץ����ѿ�
	 * @param mem �ɲ������
	 */
	private void addProcessContextToMem(SrcProcessContext sProc, Membrane mem) {
		ProcessContext p = new ProcessContext(sProc.getName());
		mem.processContexts.add(p);
	}
	
	/**
	 * �롼���ѿ�������ɲ�
	 * @param sRule �ɲä������롼���ѿ�
	 * @param mem �ɲ������
	 */
	private void addRuleContextToMem(SrcRuleContext sRule, Membrane mem) {
		RuleContext p = new RuleContext(sRule.getName());
		mem.ruleContexts.add(p);
	}
	
	/**
	 * ���ñ�첽������ɲ�
	 * @param sUnify �ɲä������롼��ñ�첽
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addLinkUnifyToMem(SrcLinkUnify sUnify, Membrane mem) throws ParseException {
		LinkUnify unify = new LinkUnify(mem);
		addLinkToAtom((SrcLink)sUnify.getProcess().get(0), unify, 0);
		addLinkToAtom((SrcLink)sUnify.getProcess().get(1), unify, 1);
	}
}