/**
 * LMNtal Parser �ᥤ�󥯥饹
 * ���ĤΥ����������ɤ�Membrane�Ȥ���ɽ������ޤ���
 */

package compile.parser;

import java_cup.runtime.Scanner;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;

//import java_cup.runtime.Symbol;
import runtime.Inline;
import runtime.Env;
import compile.structure.*;

public class LMNParser {

	private static final String PREFIX_LINK_NAME = "L::";
	private static final String PREFIX_PROXY_LINK_NAME = "P::";
	static final LinkOccurrence CLOSED_LINK = new LinkOccurrence("",null,0);

	private /*static*/ int nLinkNumber = 0;
	private Scanner lex = null;
	
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
	 * @return �������ե��������Τ���������롼�뤬���Ĥ����ޤޤ����
	 * @throws ParseException
	 */
	public Membrane parse() throws ParseException {
		LinkedList srcProcess = parseSrc();
		Membrane mem = new Membrane(null);
		addProcessToMem(srcProcess, mem);
		HashMap freeLinks = coupleLinks(mem);
		if (!freeLinks.isEmpty()) {
			Iterator it = freeLinks.keySet().iterator();
			while (it.hasNext()) {
				LinkOccurrence link = (LinkOccurrence)freeLinks.get(it.next());
				System.out.println("WARNING: Global singleton link: " + link.name);
				LinkedList process = new LinkedList();
				process.add(new SrcLink(link.name));
				SrcAtom sAtom = new SrcAtom(link.name, process);
				addSrcAtomToMem(sAtom, mem);
			}
			coupleLinks(mem);
		}
		Inline.makeCode();
		return mem;
	}

	////////////////////////////////////////////////////////////////
	
	/**
	 * ���ꤵ�줿��ˤ��륢�ȥ�ΰ������Ф��ơ���󥯤η���Ԥ�����ͳ��󥯤�HashMap���֤���
	 * <p>������Ф��ƥ�󥯤η�礪��ӥץ����κ������Ԥ�줿��ǸƤӽФ���롣
	 * <p>�����ѤȤ��ơ��᥽�åɤ�����ͤ� mem.freeLinks �˥��åȤ��롣
	 * @throws ParseException
	 * @return ���̾���鼫ͳ��󥯽и��ؤ�HashMap
	 */
	private static HashMap coupleLinks(Membrane mem) throws ParseException {
		HashMap links = new HashMap();
		// Ʊ�����٥�Υ�󥯷���Ԥ�
		for (int i = 0; i < mem.atoms.size(); i++) {
			Atom a = (Atom)mem.atoms.get(i);
			// ��󥯤μ��Ф�
			for (int j = 0; j < a.args.length; j++) {
				addLinkOccurrence(links, a.args[j]);
			}
		}
		removeClosedLinks(links);
		mem.freeLinks = links;
		return links;
	}
	
	/** �Ĥ�����󥯤�links�������� */
	private static void removeClosedLinks(HashMap links) {
		Iterator it = links.keySet().iterator();
		while (it.hasNext()) {
			String linkName = (String)it.next();
			if (links.get(linkName) == CLOSED_LINK) it.remove();
		}
	}
	
	/**
	 * ���ꤵ�줿��󥯽и���Ͽ���롣Ʊ��̾����2���ܤνи��ʤ�Х�󥯤η���Ԥ���
	 * @param lnk ��Ͽ�����󥯽и�
	 * @throws ParseException 2����¿�����̾���и��������
	 */
	private static void addLinkOccurrence(HashMap links, LinkOccurrence lnk) throws ParseException {
		// 3��ʾ�νи�
		if (links.get(lnk.name) == CLOSED_LINK) {
			throw new ParseException("Link " + lnk.name + " appears more than twice.");
		}
		// 1���ܤνи�
		else if (links.get(lnk.name) == null) {
			links.put(lnk.name, lnk);
		}
		// 2���ܤνи�
		else {
			LinkOccurrence buddy = (LinkOccurrence)links.get(lnk.name);
			lnk.buddy = buddy;
			buddy.buddy = lnk;
			links.put(lnk.name, CLOSED_LINK);
		}
	}

	////////////////////////////////////////////////////////////////
	
	/**
	 * ��˥��ȥࡢ���졢�롼��ʤɤ������Ͽ����
	 * @param list ��Ͽ�������ץ����Υꥹ��
	 * @throws ParseException
	 */
	void addProcessToMem(LinkedList list, Membrane mem) throws ParseException {
		for (int i = 0; i < list.size(); i++) addObjectToMem(list.get(i), mem);
	}
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
			addSrcMemToMem((SrcMembrane)obj, mem);
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
		HashMap freeLinks = coupleLinks(submem);
		
		// ����μ�ͳ��󥯤��Ф��ƥץ������ɲä���
		HashMap newFreeLinks = new HashMap();
		Iterator it = freeLinks.keySet().iterator();
		while (it.hasNext()) {
			LinkOccurrence freeLink = (LinkOccurrence)freeLinks.get(it.next());
			String proxyLinkName = PREFIX_PROXY_LINK_NAME + freeLink.name;
			// �����inside_proxy���ɲ�
			ProxyAtom inside = new ProxyAtom(ProxyAtom.INSIDE_PROXY, submem);
			inside.args[0] = new LinkOccurrence(proxyLinkName, inside, 0); // ��¦
			inside.args[1] = new LinkOccurrence(freeLink.name, inside, 1); // ��¦
			inside.args[1].buddy = freeLink;
			freeLink.buddy = inside.args[1];
			submem.atoms.add(inside);
			// ��������ͳ���̾�򿷤�����ͳ��󥯰������ɲä���
			newFreeLinks.put(proxyLinkName, inside.args[0]);			
			// �������outside_proxy���ɲ�
			ProxyAtom outside = new ProxyAtom(ProxyAtom.OUTSIDE_PROXY, mem);
			outside.args[0] = new LinkOccurrence(proxyLinkName, outside, 0); // ��¦
			outside.args[1] = new LinkOccurrence(freeLink.name, outside, 1); // ��¦
			outside.args[0].buddy = inside.args[0];
			inside.args[0].buddy = outside.args[0];
			mem.atoms.add(outside);
		}
		submem.freeLinks = newFreeLinks;
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
				throw new ParseException("Unknown object in an atom argument: "+obj);
			}
		}
		mem.atoms.add(atom);
	}

	/**
	 * �롼�빽ʸ������ɲä���
	 * @param sRule �ɲä������롼�빽ʸ
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addSrcRuleToMem(SrcRule sRule, Membrane mem) throws ParseException {
		RuleStructure rule = new RuleStructure(mem);
		HashMap ruleLinks = new HashMap();

		// TODO ��ά��ˡ��Ÿ�� sRule����Ȥ��֤�����
		
		// �إå�
		addProcessToMem(sRule.getHead(), rule.leftMem);
		HashMap lhsFreeLinks = coupleLinks(rule.leftMem);
		
		// ������
		addProcessToMem(sRule.getGuard(), rule.guardMem);

		// �ܥǥ�
		addProcessToMem(sRule.getBody(), rule.rightMem);
		HashMap rhsFreeLinks = coupleLinks(rule.rightMem);
		
		// ���դȺ��դμ�ͳ��󥯤���³����
		coupleInheritedLinks(ruleLinks, lhsFreeLinks, rhsFreeLinks);
		
		// todo �ץ���ʸ̮����³����
		
		mem.rules.add(rule);
	}
	
	/** ���դȱ��դμ�ͳ��󥯤�Ĥʤ� */
	static void coupleInheritedLinks(HashMap links, HashMap lhsfreelinks, HashMap rhsfreelinks) throws ParseException {
		HashMap linkNameTable = new HashMap();
		Iterator it = lhsfreelinks.keySet().iterator();
		while (it.hasNext()) {
			String linkname = (String)it.next();
			if (lhsfreelinks.get(linkname) == CLOSED_LINK) continue;
			LinkOccurrence lhsocc = (LinkOccurrence)lhsfreelinks.get(linkname);
			addLinkOccurrence(links, lhsocc);
		}
		it = rhsfreelinks.keySet().iterator();
		while (it.hasNext()) {
			String linkname = (String)it.next();
			if (rhsfreelinks.get(linkname) == CLOSED_LINK) continue;
			LinkOccurrence rhsocc = (LinkOccurrence)rhsfreelinks.get(linkname);
			addLinkOccurrence(links, rhsocc);
		}
		// TODO �����ˤ����и����ʤ���ͳ��󥯤򥨥顼���Ȥ���
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
	
	////////////////////////////////////////////////////////////////
	
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
}