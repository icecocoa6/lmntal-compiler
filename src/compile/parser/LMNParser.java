/**
 * LMNtal Parser �ᥤ�󥯥饹
 * ���ĤΥ����������ɤ�Membrane�Ȥ���ɽ������ޤ���
 */

package compile.parser;

import java_cup.runtime.Scanner;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Enumeration;

//import java_cup.runtime.Symbol;
import runtime.Inline;
import runtime.Env;
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
			throw new ParseException(e.getMessage()+" "+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));	
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
		Inline.makeCode();
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
	 * @return ���̾���鼫ͳ��󥯽и��ؤΥϥå���
	 */
	private Hashtable createProxy(Membrane mem) throws ParseException {
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
				LinkOccurrence freeLink = 
					addProxyToMem((LinkOccurrence)childMem.freeLinks.get(j), mem, ProxyAtom.OUTSIDE_PROXY);
				connectLink(freeLink, linkNameTable);
			}
		}

		Enumeration enumLinkName = linkNameTable.keys();
		// ���줬���ꡢ�б����ʤ���Τϼ�ͳ��󥯤Ȥ�����Ͽ
		if (mem.mem != null) {
			while (enumLinkName.hasMoreElements()) {
				// �ץ������̤�����Υ�󥯤����
				LinkOccurrence freeLink = 
					addProxyToMem((LinkOccurrence)linkNameTable.get(enumLinkName.nextElement()), mem, ProxyAtom.INSIDE_PROXY);
				mem.freeLinks.add(freeLink);
			}
		}
		// ���줬�ʤ��Ƽ�ͳ��󥯤�������
		else {
			
		}
		return linkNameTable;
	}
	/** ���դȱ��դμ�ͳ��󥯤�Ĥʤ���n-kato�ˤ�벾�Υ����ɡ� */
	private void coupleInheritedLinks(Hashtable lhsfreelinks, Hashtable rhsfreelinks) throws ParseException {
		Hashtable linkNameTable = new Hashtable();
		Enumeration lhsenum = lhsfreelinks.keys();
		while (lhsenum.hasMoreElements()) {
			String linkname = (String)lhsenum.nextElement();
			if (lhsfreelinks.get(linkname) == Boolean.TRUE) continue;
			LinkOccurrence lhsocc = (LinkOccurrence)lhsfreelinks.get(linkname);
			connectLink(lhsocc, linkNameTable);
		}
		Enumeration rhsenum = rhsfreelinks.keys();
		while (rhsenum.hasMoreElements()) {
			String linkname = (String)rhsenum.nextElement();
			if (rhsfreelinks.get(linkname) == Boolean.TRUE) continue;
			LinkOccurrence rhsocc = (LinkOccurrence)rhsfreelinks.get(linkname);
			connectLink(rhsocc, linkNameTable);
		}
		// TODO �����ˤ����и����ʤ���ͳ��󥯤򥨥顼���Ȥ���
	}
	
	/**
	 * ���ȥ�˥ץ��������ɲ�
	 * @param freeLink �ץ��������̤��Ƴ��˽Ф뼫ͳ���
	 * @param mem �ɲ������
	 * @param �ץ����Υ�����
	 * @return �ץ���������Υ�󥯥��֥�������
	 */
	private LinkOccurrence addProxyToMem(LinkOccurrence freeLink, Membrane mem, int type) {
		ProxyAtom proxy = new ProxyAtom(type, mem);
		if (type == ProxyAtom.INSIDE_PROXY) {
			proxy.args[0] = new LinkOccurrence(freeLink.name, proxy, 0); // ��¦
			proxy.args[1] = new LinkOccurrence(PREFIX_PROXY_LINK_NAME+freeLink.name, proxy, 1); // ��¦
			// ��¦�η��
			proxy.args[1].buddy = freeLink;
			freeLink.buddy = proxy.args[1];
			freeLink.name = proxy.args[1].name;
			// �ץ������ɲ�
			mem.atoms.add(proxy);
			return proxy.args[0];
		} else if (type == ProxyAtom.OUTSIDE_PROXY) {
			proxy.args[0] = new LinkOccurrence(PREFIX_PROXY_LINK_NAME+freeLink.name, proxy, 0); // ��¦
			proxy.args[1] = new LinkOccurrence(freeLink.name, proxy, 1); // ��¦
			// ��¦�η��
			proxy.args[0].buddy = freeLink;
			freeLink.buddy = proxy.args[0];
			freeLink.name = proxy.args[0].name;
			// �ץ������ɲ�
			mem.atoms.add(proxy);
			return proxy.args[1];
		} else {
			return null;
		}
	}
	
	
	/**
	 * ��󥯤η���Ԥ�(Ʊ�����¸�ߤ�����)
	 * @param lnk ����Ԥ�����
	 * @param linkNameTable ���̾�˥�󥯥��֥������Ȥ��б��Ť����ơ��֥�
	 * @throws ParseException 2����¿�����̾���и��������
	 */
	private void connectLink(LinkOccurrence lnk, Hashtable linkNameTable) throws ParseException {
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
			lnk.buddy = buddy;
			buddy.buddy = lnk;
			linkNameTable.put(lnk.name, Boolean.TRUE);
		}
	}
	
	/**
	 * ��󥯤η���Ԥ�
	 * @param lnk ����Ԥ�����
	 * @param linkNameTable ���̾�˥�󥯥��֥������Ȥ��б��Ť����ơ��֥�
	 * @param isOverMembrane ����̲᤹���󥯤�
	 * @param mem �ɲ������
	 * @throws ParseException 2����¿�����̾���и��������
	 */
/*	private void connectLink(LinkOccurrence lnk, Hashtable linkNameTable, boolean isOverMembrane, Membrane mem) throws ParseException {
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
//			if (isOverMembrane) buddy = addProxyToMem(buddy, mem, ProxyAtom.OUTSIDE_PROXY);
			lnk.buddy = buddy;
			buddy.buddy = lnk;
			linkNameTable.put(lnk.name, Boolean.TRUE);
		}
	}
*/
	
	/**
	 * ��˥��ȥࡢ���졢�롼��ʤɤι�ʸ���֥������Ȥ��ɲ�
	 * @param obj �ɲä��빽ʸ���֥�������
	 * @param mem �ɲ������
	 * @throws ParseException obj��̤�Τʥ��֥������Ȥξ��ʤ�
	 */
	private void addObjectToMem(Object obj, Membrane mem) throws ParseException {
		// ���ȥ�
		if (obj instanceof SrcAtom) {
			addSrcAtomToMem((SrcAtom)obj, mem);
		}
		// ��
		else if (obj instanceof SrcMembrane) {
			addSrcMemToMem((SrcMembrane)obj, mem);; 
		}
		// �롼��
		else if (obj instanceof SrcRule) {
			addSrcRuleToMem((SrcRule)obj, mem);
		}
		// �ץ�������ƥ�����
		else if (obj instanceof SrcProcessContext) {
			addSrcProcessContextToMem((SrcProcessContext)obj, mem);
		}
		// �롼�륳��ƥ�����
		else if (obj instanceof SrcRuleContext) {
			addSrcRuleContextToMem((SrcRuleContext)obj, mem);
		}
		// ���ñ�첽
		else if (obj instanceof SrcLinkUnify) {
			addSrcLinkUnifyToMem((SrcLinkUnify)obj, mem);
		}
		// ����¾ 
		else {
			throw new ParseException("Unknown Object to add membrane:"+obj);
		}
	}

	/**
	 * �칽ʸ������ɲ�
	 * @param sMem �ɲä����칽ʸ
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addSrcMemToMem(SrcMembrane sMem, Membrane mem) throws ParseException {
		Membrane submem = new Membrane(mem);
		addProcessToMem(sMem.getProcess(), submem);
		createProxy(submem); // ��󥯤�Ž���դ� �ץ�����������
		mem.mems.add(submem);
	}
	
	/**
	 * ���ȥ๽ʸ������ɲ�
	 * @param sAtom �ɲä��������ȥ๽ʸ
	 * @param mem �ɲ������
	 * @throws ParseException ���ȥ�Υ�󥯤�̤�Τʥ��֥������Ȥ�������ʤ�
	 */
	private void addSrcAtomToMem(SrcAtom sAtom, Membrane mem) throws ParseException {
		LinkedList p = sAtom.getProcess();
		Atom atom = new Atom(mem, sAtom.getName(), p.size(), sAtom.line, sAtom.column);
		for (int i = 0; i < p.size(); i++) {
			Object obj = p.get(i);
			// �̾���
			if (obj instanceof SrcLink) {
				setLinkToAtomArg((SrcLink)obj, atom, i);
			}
			// ���ȥ�
			else if (obj instanceof SrcAtom) {
				SrcLink link = createNewSrcLink();
				((SrcAtom)obj).process.add(new SrcLink(link.getName()));
				addSrcAtomToMem((SrcAtom)obj, mem);
				setLinkToAtomArg(link, atom, i);
			}
			// ����¾
			else {
				throw new ParseException("Unknown Object to add Link:"+obj);
			}
		}
		mem.atoms.add(atom);
	}
	
	/**
	 * ��ˡ�����̾���ο�������󥯹�ʸ���������
	 * @return ����������󥯹�ʸ
	 */
	private SrcLink createNewSrcLink() {
		nLinkNumber++;
		return new SrcLink(PREFIX_LINK_NAME + nLinkNumber);
	}
	
	/**
	 * ���ȥ�ΰ����˥�󥯤򥻥åȤ���
	 * @param link ���åȤ��������
	 * @param atom ���å���Υ��ȥ�
	 * @param pos ���å���Υ��ȥ�Ǥξ��
	 * @throws ParseException ���å���ξ�꤬���ȥ��¸�ߤ��ʤ����
	 */
	private void setLinkToAtomArg(SrcLink link, Atom atom, int pos) throws ParseException {
		if (pos >= atom.args.length) throw new ParseException("Out of Atom args length:"+pos);
		atom.args[pos] = new LinkOccurrence(link.getName(), atom, pos);
	}

	/**
	 * �롼�빽ʸ������ɲä���
	 * @param sRule �ɲä������롼�빽ʸ
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addSrcRuleToMem(SrcRule sRule, Membrane mem) throws ParseException {
		RuleStructure rule = new RuleStructure(mem);
		// TODO ��ά��ˡ��Ÿ�� sRule����Ȥ��֤�����
		
		// �إå�
		addProcessToMem(sRule.getHead(), rule.leftMem);
		Hashtable lhsfreelinks = createProxy(rule.leftMem);
		
		// ������
		addProcessToMem(sRule.getGuard(), rule.guardMem);
		createProxy(rule.guardMem);

		// �ܥǥ�
		addProcessToMem(sRule.getBody(), rule.rightMem);
		Hashtable rhsfreelinks = createProxy(rule.rightMem);
		
		// ���դȺ��դμ�ͳ��󥯤���³����
		coupleInheritedLinks(lhsfreelinks, rhsfreelinks);	
		
		mem.rules.add(rule);
	}

	/**
	 * �ץ���ʸ̮��ʸ������ɲ�
	 * @param sProc �ɲä������ץ���ʸ̮��ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcProcessContextToMem(SrcProcessContext sProc, Membrane mem) {
		ProcessContext p = new ProcessContext(sProc.getName());
		mem.processContexts.add(p);
	}
	
	/**
	 * �롼��ʸ̮��ʸ������ɲ�
	 * @param sRule �ɲä������롼��ʸ̮��ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcRuleContextToMem(SrcRuleContext sRule, Membrane mem) {
		RuleContext p = new RuleContext(sRule.getName());
		mem.ruleContexts.add(p);
	}
	
	/**
	 * ���ñ�첽������ɲ�
	 * @param sUnify �ɲä������롼��ñ�첽
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addSrcLinkUnifyToMem(SrcLinkUnify sUnify, Membrane mem) throws ParseException {
		LinkUnify unify = new LinkUnify(mem);
		setLinkToAtomArg((SrcLink)sUnify.getProcess().get(0), unify, 0);
		setLinkToAtomArg((SrcLink)sUnify.getProcess().get(1), unify, 1);
	}
}