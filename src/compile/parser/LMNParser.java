/**
 * LMNtal Parser �ᥤ�󥯥饹
 * ���ĤΥ����������ɤ�Membrane�Ȥ���ɽ������ޤ���
 */

package compile.parser;

import java_cup.runtime.Scanner;
import java.io.Reader;
//import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import runtime.Functor;
//import runtime.Env;
import compile.structure.*;

public class LMNParser {

	private static final String            LINK_NAME_PREFIX = "~"; //         [A-Za-z0-9_]* �ʳ�
	private static final String      PROXY_LINK_NAME_PREFIX = "^"; //   [A-Z_][A-Za-z0-9_]* �ʳ�
	private static final String PROCESS_CONTEXT_NAME_PREFIX = "_"; // [a-z0-9][A-Za-z0-9_]* �ʳ�
	static final LinkOccurrence CLOSED_LINK = new LinkOccurrence("",null,0);

	private int nLinkNumber = 0;
	private Scanner lex = null;
	
	private int nErrors = 0;
	private int nWarnings = 0;
	
	private SyntaxExpander expander = new SyntaxExpander(this);
	
	public void corrupted() throws ParseException {
		error("SYSTEM ERROR: error recovery for the previous error is not implemented");
		throw new ParseException("Rule compilation aborted");
	}
	public void error(String text) {
		System.out.println(text);
		nErrors++;
	}
	public void warning(String text) {
		System.out.println(text);
		nWarnings++;
	}
	
	
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
	 * �ᥤ��᥽�åɡ��������ե��������Ϥ����ץ�����¤�����ä��칽¤���������롣
	 * ���ϸ�Ϲ�ʸ���顼���������졢��󥯤䥳��ƥ�����̾�β�衢����ӥץ����κ������Ԥ��Ƥ��롣
	 * @return �������ե��������Τ�ɽ���ץ�����¤�����ä��칽¤
	 * @throws ParseException
	 */
	public Membrane parse() throws ParseException {
		LinkedList srcProcess = parseSrc();
		Membrane mem = new Membrane(null);
		expander.incorporateSignSymbols(srcProcess);
		expander.incorporateModuleNames(srcProcess);
		expander.expandAtoms(srcProcess);
		expander.correctWorld(srcProcess);
		addProcessToMem(srcProcess, mem);
		HashMap freeLinks = addProxies(mem);
		if (!freeLinks.isEmpty()) closeFreeLinks(mem);
		return mem;
	}
	
	/**	
		���Ϥη�̤� LinkedList �Ȥ�������ڤȤ����֤��ޤ���
		@return ���Ϥ��줿�����������ɤΥꥹ��
		@throws ParseException 
	*/
	protected LinkedList parseSrc() { // throws ParseException {
		parser p = new parser(lex);
		LinkedList result = null;
		try {
			result = (LinkedList)p.parse().value;
		} catch (Exception e) {
//			throw new ParseException(e.getMessage()+" "+runtime.Env.parray(java.util.Arrays.asList(e.getStackTrace()), "\n"));	
//			error("PARSE ERROR: " + p.error_sym());
			result = new LinkedList();
		} catch (Error e) {
			error("ERROR: " + e.getMessage());
			result = new LinkedList();
		}
		return result;
	}

	////////////////////////////////////////////////////////////////

	/** ��ˡ����ʿ��������̾���������� */
	String generateNewLinkName() {
		nLinkNumber++;
		return LINK_NAME_PREFIX + nLinkNumber;	
	}
	/** ��ˡ����ʿ������ץ���ʸ̮̾���������� */
	String generateNewProcessContextName() {
		nLinkNumber++;
		return PROCESS_CONTEXT_NAME_PREFIX + nLinkNumber;	
	}
	
	/**
	 * ���ȥ�ΰ����˥�󥯤򥻥åȤ���
	 * @param link ���åȤ��������
	 * @param atom ���å���Υ��ȥ�
	 * @param pos ���å���Υ��ȥ�Ǥξ��
	 */
	private void setLinkToAtomArg(SrcLink link, Atom atom, int pos) {
		//if (pos >= atom.args.length) error("SYSTEM ERROR: out of Atom arg length:"+pos);
		atom.args[pos] = new LinkOccurrence(link.getName(), atom, pos);
	}
	
	////////////////////////////////////////////////////////////////
	//
	// ��ʸ���֥������Ȥ��칽¤���֥������Ȥ��ɲä���᥽�åɷ�
	//
	
	/**
	 * ��˥ꥹ����ι�ʸ���֥������Ȥ��ɲä���
	 * @param list ��Ͽ���빽ʸ���֥������ȤΥꥹ��
	 * @param mem �ɲ������
	 */
	void addProcessToMem(LinkedList list, Membrane mem) throws ParseException {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			addObjectToMem(it.next(), mem);
		}
	}
	/**
	 * ��˥��ȥࡢ���졢�롼��ʤɤι�ʸ���֥������Ȥ��ɲ�
	 * @param obj �ɲä��빽ʸ���֥�������
	 * @param mem �ɲ������
	 */
	private void addObjectToMem(Object obj, Membrane mem) throws ParseException {
		// �ꥹ��
		if (obj instanceof LinkedList) {
			Iterator it = ((LinkedList)obj).iterator();
			while (it.hasNext()) {
				addObjectToMem(it.next(), mem);
			}
		}
		// ���ȥ�
		else if (obj instanceof SrcAtom) {
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
		// ����¾ 
		else {
			throw new ParseException("SYSTEM ERROR: Illegal Object to add to a membrane: "+obj);
		}
	}

	/**
	 * �칽ʸ������ɲ�
	 * @param sMem �ɲä����칽ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcMemToMem(SrcMembrane sMem, Membrane mem) throws ParseException {
		Membrane submem = new Membrane(mem);
		submem.stable = sMem.stable;
		addProcessToMem(sMem.getProcess(), submem);
		mem.mems.add(submem);
	}
	/**
	 * ���ȥ๽ʸ������ɲ�
	 * @param sAtom �ɲä��������ȥ๽ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcAtomToMem(SrcAtom sAtom, Membrane mem) {
		boolean alllinks   = true;
		boolean allbundles = true;
		LinkedList p = sAtom.getProcess();
		int arity = p.size();
		
		// [1] �ե��󥯥�����������
		// GUI�����ưŪ���������б�������ˤ��ʤ��� FunctorFactory �Τ褦�ʤ�Τ����ä������褤��
		// runtime.*Functor ��¿�������������Լ�������ʪ��롣

		int nametype = sAtom.getNameType();
		String name = sAtom.getName();
		String path = null;
		if (nametype == SrcName.PATHED) {
			int pos = name.indexOf('.');
			path = name.substring(0, pos);
			name = name.substring(pos + 1);
		}
		Functor func = new runtime.Functor(name, arity, path);
		if (arity == 1 && path == null) {
			if (nametype == SrcName.PLAIN || nametype == SrcName.SYMBOL) {
				try {
					func = new runtime.IntegerFunctor(Integer.parseInt(name));
				}
				catch (NumberFormatException e) {
					try {
						func = new runtime.FloatingFunctor(Double.parseDouble(name));
					}
					catch (NumberFormatException e2) {
						//
					}
				}
			}
			else if (nametype == SrcName.STRING || nametype == SrcName.QUOTED) {
				func = new runtime.StringFunctor(name); // new runtime.ObjectFunctor(name);
			}
		}
		
		// [2] ���ȥ๽¤����������
		Atom atom = new Atom(mem, func);
		atom.setSourceLocation(sAtom.line, sAtom.column);
		
		// [3] �����ι�¤����������		
		for (int i = 0; i < arity; i++) {
			Object obj = p.get(i);
			// ���
			if (obj instanceof SrcLink) {
				setLinkToAtomArg((SrcLink)obj, atom, i);
				if (obj instanceof SrcLinkBundle) { alllinks = false; }
				else { allbundles = false; }
			}
//			// ���ȥ�
//			else if (obj instanceof SrcAtom) {
//				String newlinkname = generateNewLinkName();
//				((SrcAtom)obj).process.add(new SrcLink(newlinkname));
//				addSrcAtomToMem((SrcAtom)obj, mem);
//				setLinkToAtomArg(new SrcLink(newlinkname), atom, i);
//			}

//			// �ץ���ʸ̮
//			else if (obj instanceof SrcProcessContext) {
//				error("SYNTAX ERROR: untyped process context in an atom argument: " + obj);
//				setLinkToAtomArg(new SrcLink(generateNewLinkName()), atom, i);
//				allbundles = false;
//			}

			// ����¾
			else {
				error("SYNTAX ERROR: illegal object in an atom argument: " + obj);
				setLinkToAtomArg(new SrcLink(generateNewLinkName()), atom, i);
				allbundles = false;
			}
		}
		
		// [4] ���ȥ�ȥ��ȥླྀ�Ĥ��̤���
		if (arity > 0 && allbundles) 
			mem.aggregates.add(atom);
		else if (arity == 0 || alllinks )
			mem.atoms.add(atom);
		else {
			error("SYNTAX ERROR: arguments of an atom contain both links and bundles");
		}
	}

	/**
	 * �ץ���ʸ̮��ʸ������ɲ�
	 * <p>�����ʤ���$p��$p[|*p]�Ȥ�������̾*p��Ȥä���¤�˼�ưŪ���ִ������
	 * @param sProc �ɲä������ץ���ʸ̮��ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcProcessContextToMem(SrcProcessContext sProc, Membrane mem) {
		ProcessContext pc;
		String name = sProc.getQualifiedName();
		if (sProc.args == null) {
			pc = new ProcessContext(mem, name, 0);
			pc.setBundleName(SrcLinkBundle.PREFIX_TAG + sProc.getName());
		} else {
			int length = sProc.args.size();
			pc = new ProcessContext(mem, name, length);
			for (int i = 0; i < length; i++) {
				String linkname = ((SrcLink)sProc.args.get(i)).getName();
				pc.args[i] = new LinkOccurrence(linkname,pc,i);
			}
			if (sProc.bundle != null) pc.setBundleName(sProc.bundle.getQualifiedName());
		}
		mem.processContexts.add(pc);
	}
	
	/**
	 * �롼��ʸ̮��ʸ������ɲ�
	 * @param sRule �ɲä������롼��ʸ̮��ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcRuleContextToMem(SrcRuleContext sRule, Membrane mem) {
		RuleContext p = new RuleContext(mem, sRule.getQualifiedName());
		mem.ruleContexts.add(p);
	}
	
	/**
	 * �롼�빽ʸ������ɲä���
	 * @param sRule �ɲä������롼�빽ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcRuleToMem(SrcRule sRule, Membrane mem) throws ParseException {
		RuleStructure rule = new RuleStructure(mem);
		// ά��ˡ��Ÿ��		
		expander.expandRuleAbbreviations(sRule);
		// todo ���դΥ롼���ʸ���顼�Ȥ��ƽ����
		
		// ���դ���ӥ����ɷ�������Ф��ơ���¤������������󥯰ʳ���̾�����褹��
		addProcessToMem(sRule.getHead(), rule.leftMem);		
		addProcessToMem(sRule.getGuard(), rule.guardMem);
		HashMap names = resolveHeadContextNames(rule);
		// �����������浪��ӱ��դ��Ф��ơ���¤������������󥯰ʳ���̾�����褹��
		addGuardNegatives(sRule.getGuardNegatives(), rule, names);
		addProcessToMem(sRule.getBody(), rule.rightMem);
		resolveContextNames(rule, names);
		// �ץ������ȥ������������󥯤�Ĥʤ�����μ�ͳ��󥯥ꥹ�Ȥ���ꤹ��
		addProxies(rule.leftMem);
		coupleLinks(rule.guardMem);
		addProxies(rule.rightMem);
		addProxiesToGuardNegatives(rule);
		coupleGuardNegativeLinks(rule);		// ������������Υ�󥯤���³����
		coupleInheritedLinks(rule);			// ���դȺ��դμ�ͳ��󥯤���³����
		//
		mem.rules.add(rule);
	}

	/** ���������������ַ������б����빽¤����������
	 *  @param sNegatives ���������������ַ���[$p,[Q]]�Υꥹ��[in]
	 *  @param rule �롼�빽¤[in,out]
	 *  @param names ���դ���ӥ����ɷ�����˽и�����$p�ʤ�*X�ˤ��餽������ʤȽи��ˤؤΥޥå�[in] */
	private void addGuardNegatives(LinkedList sNegatives, RuleStructure rule, HashMap names) throws ParseException {
		Iterator it = sNegatives.iterator();
		while (it.hasNext()) {
			LinkedList neg = new LinkedList();
			ListIterator it2 = ((LinkedList)it.next()).listIterator();
			while (it2.hasNext()) {
				LinkedList sPair = (LinkedList)it2.next();
				String cxtname = ((SrcProcessContext)sPair.getFirst()).getQualifiedName();
				if (!names.containsKey(cxtname)) {
					error("SYNTAX ERROR: fresh process context constrained in a negative condition: " + cxtname);
				}
				else {
					ContextDef def = (ContextDef)names.get(cxtname);
					if (def.typed) {
						error("SYNTAX ERROR: typed process context constrained in a negative condition: " + cxtname);
					}
					else if (def.lhsOcc != null) {
						Membrane mem = new Membrane(null);
						addProcessToMem((LinkedList)sPair.getLast(),mem);
						neg.add(new ProcessContextEquation(def,mem));
					}
				}
			}
			rule.guardNegatives.add(neg);
		}
	}

	////////////////////////////////////////////////////////////////
	//
	// ��󥯤ȥץ���
	//
	
	/** ������Ф��ƺƵ�Ū�˥ץ������ɲä��롣
	 * @return ������ι������줿��ͳ��󥯥ޥå� mem.freeLinks */
	private HashMap addProxies(Membrane mem) {
		HashSet proxyLinkNames = new HashSet();	// mem�Ȥ��λ���δ֤˺���������֥��̾�ν���
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			HashMap freeLinks = addProxies(submem);
			// ����μ�ͳ��󥯤��Ф��ƥץ������ɲä���
			HashMap newFreeLinks = new HashMap();
			Iterator it2 = freeLinks.keySet().iterator();
			while (it2.hasNext()) {
				LinkOccurrence freeLink = (LinkOccurrence)freeLinks.get(it2.next());
				// ����μ�ͳ���̾ freeLink.name ���Ф��ơ���֥��̾ proxyLinkName ����ꤹ�롣
				// �̾��X���Ф��ơ�1^X�Ȥ��롣
				// X��mem�ζɽ��󥯤Ǥ��ꡢ1^X��mem��Ǥ��Ǥ˻��Ѥ������ϡ�1^^X�Ȥ��롣
				// X��submem�λ���ؤ�ľ�̥�󥯤Ǥ��ꡢ�����Ǥ���֥��̾��1^X�ξ��ϡ�2^X�Ȥ��롣
				String index = "1";
				if (freeLink.atom.functor.equals(ProxyAtom.OUTSIDE_PROXY)
				 && freeLink.atom.args[0].name.startsWith("1") ) {
				 	index = "2";
				}
				String proxyLinkName = index + PROXY_LINK_NAME_PREFIX + freeLink.name;
				if (proxyLinkNames.contains(proxyLinkName)) {
					proxyLinkName = index + PROXY_LINK_NAME_PREFIX
						+ PROXY_LINK_NAME_PREFIX + freeLink.name;
				}
				proxyLinkNames.add(proxyLinkName);
				// �����inside_proxy���ɲ�
				ProxyAtom inside = new ProxyAtom(submem, ProxyAtom.INSIDE_PROXY);
				inside.args[0] = new LinkOccurrence(proxyLinkName, inside, 0); // ��¦
				inside.args[1] = new LinkOccurrence(freeLink.name, inside, 1); // ��¦
				inside.args[1].buddy = freeLink;
				freeLink.buddy = inside.args[1];
				submem.atoms.add(inside);
				// ��������ͳ���̾�򿷤�����ͳ��󥯰������ɲä���
				newFreeLinks.put(proxyLinkName, inside.args[0]);			
				// �������outside_proxy���ɲ�
				ProxyAtom outside = new ProxyAtom(mem, ProxyAtom.OUTSIDE_PROXY);
				outside.args[0] = new LinkOccurrence(proxyLinkName, outside, 0); // ��¦
				outside.args[1] = new LinkOccurrence(freeLink.name, outside, 1); // ��¦
				outside.args[0].buddy = inside.args[0];
				inside.args[0].buddy = outside.args[0];
				mem.atoms.add(outside);
			}
			submem.freeLinks = newFreeLinks;
		}
		return coupleLinks(mem);
	}
	/** ��������������Ф���addProxies��Ƥ� */
	private void addProxiesToGuardNegatives(RuleStructure rule) {
		Iterator it = rule.guardNegatives.iterator();
		while (it.hasNext()) {
			Iterator it2 = ((LinkedList)it.next()).iterator();
			while (it2.hasNext()) {
				ProcessContextEquation eq = (ProcessContextEquation)it2.next();
				addProxies(eq.mem);
			}
		}
	}
	/**
	 * ���ꤵ�줿��ˤ��륢�ȥ�ΰ������Ф��ơ���󥯤η���Ԥ�����ͳ��󥯤�HashMap���֤���
	 * <p>������Ф��ƥ�󥯤η�礪��ӥץ����κ������Ԥ�줿��ǸƤӽФ���롣
	 * <p>�����ѤȤ��ơ��᥽�åɤ�����ͤ� mem.freeLinks �˥��åȤ��롣
	 * @return ���̾���鼫ͳ��󥯽и��ؤ�HashMap
	 */
	private HashMap coupleLinks(Membrane mem) {
		// Ʊ�����٥�Υ�󥯷���Ԥ�
		HashMap links = new HashMap();
		List[] lists = {mem.atoms, mem.processContexts, mem.typedProcessContexts};
		for (int i = 0; i < lists.length; i++) {
			Iterator it = lists[i].iterator();
			while (it.hasNext()) {
				Atom a = (Atom)it.next();
				for (int j = 0; j < a.args.length; j++) {
					if (a.args[j].buddy == null) { // outside_proxy����1�����Ϥ��Ǥ���null�ˤʤäƤ���
						addLinkOccurrence(links, a.args[j]);
					}
				}
			}
		}
		removeClosedLinks(links);
		mem.freeLinks = links;
		return links;
	}

	private HashMap enumGuardNegativeLinks(RuleStructure rule ) {
//		
//		Iterator it = rule.guardNegatives.iterator();
//		while (it.hasNext()) {
//			Iterator it2 = ((LinkedList)it.next()).iterator();
//			while (it2.hasNext()) {
//				
//				eq.mem
//			}
//			removeClosedLinks(links);
//			//mem.freeLinks = links;
//		}
//		return links;
		return null;
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
	 */
	private void addLinkOccurrence(HashMap links, LinkOccurrence lnk) {
		// 3��ʾ�νи�
		if (links.get(lnk.name) == CLOSED_LINK) {
			error("SYNTAX ERROR: link " + lnk.name + " appears more than twice.");
			String linkname = lnk.name + generateNewLinkName();
			if (lnk.name.startsWith(SrcLinkBundle.PREFIX_TAG))
				linkname = SrcLinkBundle.PREFIX_TAG + linkname;
			lnk.name = linkname;
			links.put(lnk.name, lnk);
		}
		// 1���ܤνи�
		if (links.get(lnk.name) == null) {
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
	
	/** ��mem�μ�ͳ��󥯤�������Ĥ���ʹ�ʸ���顼����������ѡ� */
	public void closeFreeLinks(Membrane mem) {
		Iterator it = mem.freeLinks.keySet().iterator();
		while (it.hasNext()) {
			LinkOccurrence link = (LinkOccurrence)mem.freeLinks.get(it.next());
			warning("WARNING: global singleton link: " + link.name);
			LinkedList process = new LinkedList();
			process.add(new SrcLink(link.name));
			SrcAtom sAtom = new SrcAtom(link.name, process);
			addSrcAtomToMem(sAtom, mem);
		}
		coupleLinks(mem);
	}
	/** �롼��Υ�����������Υȥåץ�٥�Υ�󥯤�Ĥʤ���
	 * <p>�����freeLinks�η׻��塢coupleInheritedLinks(rule)�����˸Ƥ֤��ȡ�
	 * <p>�ܥ᥽�åɽ�λ�塢���������������Υ�󥯽и��ϼ��Τ����줫�ˤʤ롧
	 * <ol>
	 * <li>Ʊ���������դؤζɽ��󥯡��̾����������󥯡�- �ܥ᥽�åɸƤӽФ����ˤ��Ǥ��Ĥ��Ƥ���
	 * <li>Ʊ�����������¾���������դؤΡ������֥�󥯡ס�������ľ�ܥ�󥯡�
	 * <li>������������$p���롼�뺸�դǽи�������ˤ��륢�ȥ�/���դ�$p�ؤΡ־�񤭥�󥯡ס���������󥯡�
	 *     �ʤ����������ǤϷ��դ����ȥब1�����ΤߤǤ��뤿�ᡢ�ѥå��ַ����¤ˤ����뤿�᷿�դ��ؤΥ�󥯤�̵����
	 * <li>null��ؤ������ɡ�ƿ̾��󥯡ס����Τˤ�$pp������Ū�ʥ�󥯰����Ȥ��̾����������󥯡�
	 * </ul>
	 * <p>�����ɥ���ѥ���Ǽºݤ˻Ȥ��Ȥ��ˤϡ������֥�󥯤��Ф��ơ���ͳ��󥯴������ȥ�κ���Ŭ���䤦���ȡ�*/
	void coupleGuardNegativeLinks(RuleStructure rule) {
		Iterator it = rule.guardNegatives.iterator();
		while (it.hasNext()) {
			HashMap interlinks = new HashMap();	// �����֥�󥯤���ӥ�����ƿ̾��󥯤ΰ���
			Iterator it2 = ((LinkedList)it.next()).iterator();
			while (it2.hasNext()) {
				ProcessContextEquation eq = (ProcessContextEquation)it2.next();
				// �������դμ�ͳ��󥯽и��ΰ������������
				Membrane mem = eq.mem;
				HashMap rhsfreelinks = mem.freeLinks;
				// �������դμ�ͳ��󥯽и��ΰ���������������դΰ������б�����
				ProcessContext a = (ProcessContext)eq.def.lhsOcc;
				HashMap rhscxtfreelinks = new HashMap();	// �����������եȥåץ�٥�$pp�μ�ͳ��󥯽���
				for (int i = 0; i < a.args.length; i++) {
					LinkOccurrence lhslnk = a.args[i];
					String linkname = lhslnk.name;
					if (rhsfreelinks.containsKey(lhslnk.name)) {
						// ξ�դ˽и�������: ( {$p[X]} :- \+($p=(a(X),$pp)) | ... )
						LinkOccurrence rhslnk = (LinkOccurrence)rhsfreelinks.get(lhslnk.name);
						rhslnk.buddy = lhslnk.buddy;	// �������Τߤ�buddy�����Ԥ�
						rhsfreelinks.put(lhslnk.name, CLOSED_LINK);
					}
					else {
						// ���դˤΤ߽и�������: ( {$p[X]} :- \+($p=(a,$pp)) | ... )
						rhscxtfreelinks.put(lhslnk.name, lhslnk);
					}
				}
				removeClosedLinks(rhsfreelinks);
				Iterator it3 = rhsfreelinks.keySet().iterator();
				while (it3.hasNext()) {
					String linkname = (String)it3.next();
					LinkOccurrence lnk = (LinkOccurrence)rhsfreelinks.get(linkname);
					// ���դˤΤ߽и�������:
					// ( ... :- \+($p=a(X),$q=b(X)) | ... ) => �����֥�󥯤ϡ�2���ܤνи��ΤȤ��Ĥ�����
					// ( ... :- \+($p=(a(X),$pp)  ) | ... ) => ������ƿ̾��󥯡ʥȥåץ�٥�$pp�μ�ͳ��󥯡�
					addLinkOccurrence(interlinks, lnk);
					// todo lnk��3���ܰʹߤνи��ΤȤ������̾���Ѥ�ä����ᡢrhsfreelinks�ν�����ɬ��
				}
			}
			removeClosedLinks(interlinks);

			// ������ƿ̾��󥯤���������$pp�ξ�;�ब[]�Ǥʤ��¤���פǤϤʤ���
			Iterator it3 = interlinks.keySet().iterator();
			anonymouslink:
			while (it3.hasNext()) {
				String linkname = (String)it3.next();
				LinkOccurrence lnk = (LinkOccurrence)interlinks.get(linkname);
				if (lnk.atom.mem.processContexts.isEmpty()) {
					warning("WARNING: unsatisfiable negative condition because of free link: " + lnk.name);
				}
				else {
					ProcessContext pc = (ProcessContext)lnk.atom.mem.processContexts.get(0);
					LinkOccurrence[] newargs = new LinkOccurrence[pc.args.length + 1];
					for (int i = 0; i < pc.args.length; i++) {
						if (pc.args[i].name.equals(lnk.name)) continue anonymouslink;
						newargs[i] = pc.args[i];
					}
					newargs[pc.args.length] = new LinkOccurrence(lnk.name, pc, pc.args.length);
					pc.args = newargs;
				}
			}
		}
	}
	/** ���դȱ��դμ�ͳ��󥯤�Ĥʤ� */
	void coupleInheritedLinks(RuleStructure rule) {
		HashMap lhsFreeLinks = rule.leftMem.freeLinks;
		HashMap rhsFreeLinks = rule.rightMem.freeLinks;
		HashMap links = new HashMap();
		Iterator it = lhsFreeLinks.keySet().iterator();
		while (it.hasNext()) {
			String linkname = (String)it.next();
			LinkOccurrence lhsocc = (LinkOccurrence)lhsFreeLinks.get(linkname);
			addLinkOccurrence(links, lhsocc);
		}
		it = rhsFreeLinks.keySet().iterator();
		while (it.hasNext()) {
			String linkname = (String)it.next();
			LinkOccurrence rhsocc = (LinkOccurrence)rhsFreeLinks.get(linkname);
			addLinkOccurrence(links, rhsocc);
		}
		removeClosedLinks(links);
		if (!links.isEmpty()) {
			it = links.keySet().iterator();
			while (it.hasNext()) {
				LinkOccurrence link = (LinkOccurrence)links.get(it.next());
				error("SYNTAX ERROR: rule with free variable: "+ link.name);
				LinkedList process = new LinkedList();
				process.add(new SrcLink(link.name));
				SrcAtom sAtom = new SrcAtom(link.name, process);
				addSrcAtomToMem(sAtom, link.atom.mem);
			}
			coupleLinks(rule.leftMem);
			coupleLinks(rule.rightMem);
		}
	}

	
	////////////////////////////////////////////////////////////////
	//
	// �ץ���ʸ̮�����դ��ץ���ʸ̮���롼��ʸ̮�����«
	//

	/** �����ɷ�����η��դ��ץ���ʸ̮�Υꥹ�Ȥ�������롣
	 * @param names ����ƥ����Ȥθ���̾ (String) ���� ContextDef �ؤμ��� [in,out] */
	private void enumTypedNames(Membrane mem, HashMap names) {
		Iterator it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			String name = pc.getQualifiedName();
			if (!names.containsKey(name)) {
				pc.def = new ContextDef(pc.getQualifiedName());
				pc.def.typed = true;
				names.put(name, pc.def);
			}
			else pc.def = (ContextDef)names.get(name);
			it.remove();
			mem.typedProcessContexts.add(pc);
			if (pc.bundle != null) addLinkOccurrence(names, pc.bundle);
		}
	}
	
	/** �إåɤΥץ���ʸ̮�����դ��ץ���ʸ̮���롼��ʸ̮�����«�Υꥹ�Ȥ�������롣
	 * ���ʤ��ץ���ʸ̮������Ū�ʰ������ߤ��˰ۤʤ뤳�Ȥ��ǧ���롣
	 * @param mem �����졢�ޤ��ϥ����������������������դι�¤���ݻ�������
	 * @param names ����ƥ����Ȥθ���̾ (String) ���� ContextDef �ؤμ��� [in,out]
	 * @param isLHS ���դ��ɤ�����def.lhsOcc���ɲä��뤫�ɤ�����Ƚ��˻��Ѥ�����*/
	private void enumHeadNames(Membrane mem, HashMap names, boolean isLHS) throws ParseException {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			enumHeadNames(submem, names, isLHS);
		}
		//
		it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			String name = pc.getQualifiedName();
			if (!names.containsKey(name)) {
				pc.def = new ContextDef(name);
				names.put(name, pc.def);
			}
			else {
				it.remove(); // ���ʤ��Ȥⷿ�ʤ��ץ���ʸ̮�ǤϤʤ��ʷ��դ��ޤ��ϥ��顼�Ȥʤ�ˤ��������
				pc.def = (ContextDef)names.get(name);
				if (pc.def.isTyped()) {
					if (pc.def.lhsOcc != null) {
						// Ÿ���������������פˤʤ�ʥ�����������ΤȤ��Ϥɤ����Ƥ�񤱤ʤ������֡�
						error("FEATURE NOT IMPLEMENTED: head contains more than one occurrence of a typed process context name: " + name);
						continue;
					}
					if (pc.args.length != 1) {
						error("SYNTAX ERROR: typed process context occurring in head must have exactly one explicit free link argument: " + pc);
						continue;
					}
					mem.typedProcessContexts.add(pc);
				}
				else {
					// ��¤��Ӥؤ��Ѵ��������������פˤʤ�ʥ�����������ΤȤ��Ϥɤ����Ƥ�񤱤ʤ������֡�
					error("FEATURE NOT IMPLEMENTED: untyped process context name appeared more than once in a head: " + name);
					continue;
				}
			}
			if (isLHS)  pc.def.lhsOcc = pc;	// ���դǤνи�����Ͽ
			if (pc.bundle != null) addLinkOccurrence(names, pc.bundle);
			//
			if (!pc.def.isTyped()) {
				HashSet explicitfreelinks = new HashSet();
				for (int i = 0; i < pc.args.length; i++) {
					LinkOccurrence lnk = pc.args[i];
					if (explicitfreelinks.contains(lnk.name)) {
						error("SYNTAX ERROR: explicit arguments of a process context in head must be pairwise disjoint: " + pc.def);
						lnk.name = lnk.name + generateNewLinkName();
					}
					else {
						explicitfreelinks.add(lnk.name);
					}
				}			
			}
		}
		it = mem.ruleContexts.iterator();
		while (it.hasNext()) {
			RuleContext rc = (RuleContext)it.next();
			String name = rc.getQualifiedName();
			if (!names.containsKey(name)) {
				rc.def = new ContextDef(name);
				if (isLHS)  rc.def.lhsOcc = rc;
				names.put(name, rc.def);
			}
			else {
				error("SYNTAX ERROR: head contains more than one occurrence of a rule context: " + name);
				it.remove();
			}
		}
		it = mem.aggregates.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			for (int i = 0; i < atom.args.length; i++) {
				addLinkOccurrence(names, atom.args[i]);
			}
		}
		//
		if (mem.processContexts.size() > 1) {
			error("SYNTAX ERROR: head membrane cannot contain more than one untyped process context");
			it = mem.processContexts.iterator();
			while (it.hasNext()) {
				ProcessContext pc = (ProcessContext)it.next();
				if (pc.def.lhsOcc == pc)  pc.def.lhsOcc = null; // ���դǤνи�����Ͽ����ä�
				it.remove(); // names�ˤϻĤ�
			}
		}
		if (mem.ruleContexts.size() > 1) {
			error("SYNTAX ERROR: head membrane cannot contain more than one rule context");
			while (it.hasNext()) {
				RuleContext rc = (RuleContext)it.next();
				if (rc.def.lhsOcc == rc)  rc.def.lhsOcc = null; // ���դǤνи�����Ͽ����ä�
				it.remove(); // names�ˤϻĤ�
			}
		}
	}
	/** �ܥǥ��Υץ���ʸ̮�����դ��ץ���ʸ̮���롼��ʸ̮�����«�Υꥹ�Ȥ�������롣
	 * @param names ����ƥ����Ȥθ���̾ (String) ���� ContextDef �ؤμ��� [in] */
	private void enumBodyNames(Membrane mem, HashMap names) throws ParseException {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			enumBodyNames(submem, names);
		}
		//
		it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			String name = pc.getQualifiedName();
			if (!names.containsKey(name)) {
				error("SYNTAX ERROR: untyped process context not appeared in head: " + pc.getQualifiedName());
				it.remove();
				continue;
			}
			else {
				pc.def = (ContextDef)names.get(name);
				if (pc.def.lhsOcc != null) {
					if (pc.args.length != pc.def.lhsOcc.args.length
					 || ((pc.bundle == null) != (((ProcessContext)pc.def.lhsOcc).bundle == null)) ) {
						error("SYNTAX ERROR: unmatched length of free link list of process context: " + pc);
						it.remove();
						continue;
					}
				}
				if (pc.def.isTyped()) {
					it.remove();
					if (pc.args.length != 1) {
						error("SYNTAX ERROR: typed process context occurring in body must have exactly one explicit free link argument: " + pc);
						continue;
					}
					mem.typedProcessContexts.add(pc);
				}
				else {
					if (pc.def.lhsOcc == null) {
						// ��ʸ���顼�ˤ��إåɽи������ä��줿���ʤ�$p�ϡ��ܥǥ��и���̵���Ǽ�������롣
						it.remove();
						continue;
					}
				}
				pc.def.rhsOccs.add(pc);
			}
		}
		it = mem.ruleContexts.iterator();
		while (it.hasNext()) {
			RuleContext rc = (RuleContext)it.next();
			String name = (String)rc.getQualifiedName();
			if (names.containsKey(name)) {
				rc.def = (ContextDef)names.get(name);
				rc.def.rhsOccs.add(rc);
			}
			else {
				error("SYNTAX ERROR: rule context not appeared in head: " + rc);
				it.remove();
			}
		}
		it = mem.aggregates.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			for (int i = 0; i < atom.args.length; i++) {
				addLinkOccurrence(names, atom.args[i]);
			}
		}
	}

	/** ���դ���ӥ����ɷ�������Ф��ơ��ץ���ʸ̮����ӥ롼��ʸ̮��̾������Ԥ���
	 *  ̾�����ˤ��ȯ�����줿��ʸ���顼���������롣
	 *  @return ���դ���ӥ����ɤ˽и��������̾(String) -> ContextDef / LinkOccurrence(Bundles) */
	private HashMap resolveHeadContextNames(RuleStructure rule) throws ParseException {
		HashMap names = new HashMap();
		enumTypedNames(rule.guardMem, names);
		enumHeadNames(rule.leftMem, names, true);
		// todo ���«�����դ��Ĥ��Ƥ��ʤ����Ȥ��ǧ����
		// ---���«��2��и��������ɤ�����Ĵ�٤�Ф褤������
		
		// ���եȥåץ�٥�Υץ���ʸ̮��������
		Iterator it = rule.leftMem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			error("SYNTAX ERROR: untyped head process context requires an enclosing membrane: " + pc);
			names.remove(pc.def.getName());
			pc.def.lhsOcc = null;	// ���դǤνи�����Ͽ����ä�
			it.remove();
		}
		return names;
	}

	/** �����������浪��ӱ��դ��Ф��ơ��ץ���ʸ̮����ӥ롼��ʸ̮��̾������Ԥ���
	 *  ̾�����ˤ��ȯ�����줿��ʸ���顼���������롣*/
	private void resolveContextNames(RuleStructure rule, HashMap names) throws ParseException {

		// Ʊ��̾���Υץ���ʸ̮�ΰ����ѥ������Ʊ���ˤ��롣
		// ���դ�������Ū�ʼ�ͳ��󥯤θĿ���1�ˤ��롣

		Iterator it;
		
		// ������������
		it = rule.guardNegatives.iterator();
		while (it.hasNext()) {
			Iterator it2 = ((LinkedList)it.next()).iterator();
			HashMap tmpnames = (HashMap)names.clone();	// ¾�ξ���ܥǥ��Ȥϴط��ʤ�����
			HashSet cxtnames = new HashSet();
			while (it2.hasNext()) {
				ProcessContextEquation eq = (ProcessContextEquation)it2.next();
				String cxtname = eq.def.getName();
				if (cxtnames.contains(cxtname)) {
					error("SYNTAX ERROR: process context constrained more than once in a negative condition: " + cxtname);
					it2.remove();
				}
				else {
					cxtnames.add(cxtname);
					enumHeadNames(eq.mem, tmpnames, false);
				}
			}
		}
				
		// ����
		enumBodyNames(rule.rightMem, names);
		
		// todo ���«���Ĥ���
		
		// todo �ץ���ʸ̮�֤ǷѾ����줿���«��Ʊ��̾���Ǥ��뤳�Ȥ��ǧ����
		// todo ���դΥ��ȥླྀ�ĤΥ���褬����Ʊ���ץ���ʸ̮̾����Ĥ��Ȥ��ǧ����
		
		// rule.processContexts/ruleContexts/typedProcessContexts ����������
		it = names.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			Object obj = names.get(name);
			if (obj instanceof LinkOccurrence) continue;	// ���«�ΤȤ���̵��
			ContextDef def = (ContextDef)obj;
			if (def.isTyped()) {
				rule.typedProcessContexts.put(name, def);
			}
			else { // ���դ��Ǥʤ���硢lhsOcc!=null�ȤʤäƤ���
				if (def.lhsOcc instanceof ProcessContext) {
					rule.processContexts.put(name, def);
				}
				else if (def.lhsOcc instanceof RuleContext) {
					rule.ruleContexts.put(name, def);
				}
			}
			if (def.rhsOccs.size() == 1) {
				if (def.lhsOcc != null) {	// �����ɤǤʤ��Ȥ�
					Context rhsocc = ((Context)def.rhsOccs.get(0));
					rhsocc.buddy = def.lhsOcc;
					def.lhsOcc.buddy = rhsocc;
				}
			}
		}
		
		// ���������ץ���ʸ̮�����������ޤǤβ����֤Ȥ��ơ������Ǥʤ����ʤ�$p�������
		it = rule.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.rhsOccs.size() != 1) {
				error("FEATURE NOT IMPLEMENTED: untyped process context must be linear: " + def.getName());
				int len = def.lhsOcc.args.length;
				Iterator it2 = def.rhsOccs.iterator();
				while (it2.hasNext()) {
					ProcessContext pc = (ProcessContext)it2.next();
					pc.mem.processContexts.remove(pc);
					Atom atom = new Atom(pc.mem, def.getName(), len);
					for (int i = 0; i < len; i++) {
						atom.args[i] = new LinkOccurrence(pc.args[i].name, atom, i);
					}
					// if (pc.bundle != null) { remove from names; }
					pc.mem.atoms.add(atom);
					it2.remove();
				}
				ProcessContext rhsocc = new ProcessContext(rule.rightMem, def.getName(), len);
				rule.rightMem.processContexts.add(rhsocc);
				for (int i = 0; i < len; i++) {
					String linkname = generateNewLinkName();
					rhsocc.args[i] = new LinkOccurrence(linkname, rhsocc, i);
					Atom atom = new Atom(rule.rightMem, linkname, 1);
					atom.args[0] = new LinkOccurrence(linkname, atom, 0);
					rule.rightMem.atoms.add(atom);
				}
				if (((ProcessContext)def.lhsOcc).bundle != null) {
					rhsocc.setBundleName(SrcLinkBundle.PREFIX_TAG + generateNewLinkName());
					// add to names;
				}
				rhsocc.buddy = def.lhsOcc;
				def.lhsOcc.buddy = rhsocc;
				rhsocc.def = def;
				def.rhsOccs.add(rhsocc);
			}
		}
	}
}

////////////////////////////////////////////////////////////////
//
// ��ʸŪ�ʽ񤭴�����Ԥ��᥽�åɤ��ݻ����륯�饹
//

class SyntaxExpander {
	private LMNParser parser;
	SyntaxExpander(LMNParser parser) {
		this.parser = parser;
	}	
	
	////////////////////////////////////////////////////////////////
	//
	// ά��ˡ��Ÿ��
	//

	/** �롼�빽ʸ���Ф���ά��ˡ��Ÿ����Ԥ� */
	void expandRuleAbbreviations(SrcRule sRule) throws ParseException {

		// �����ɤ�������������ʬ�ह��
		flatten(sRule.getGuard());
		ListIterator lit = sRule.getGuard().listIterator();
		while (lit.hasNext()) {
			Object obj = lit.next();
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				if (sAtom.getName().equals("\\+") && sAtom.getProcess().size() == 1) {
					lit.remove();
					sRule.getGuardNegatives().add(sAtom.getProcess().getFirst());
				}
			}
		}
		LinkedList typeConstraints = sRule.getGuard();
		LinkedList guardNegatives  = sRule.getGuardNegatives();

		// - ������������κ���Ū�ʹ�ʸ���顼�������������������[$p,[Q]]�Υꥹ�ȤȤ������ɽ�����Ѵ�����
		correctGuardNegatives(guardNegatives);
		
		// - ���ͤ������μ�����
		incorporateSignSymbols(sRule.getHead());
		incorporateSignSymbols(typeConstraints);
		incorporateSignSymbols(guardNegatives);
		incorporateSignSymbols(sRule.getBody());
		
		// - �⥸�塼��̾�Υ��ȥ�ե��󥯥��ؤμ�����
		incorporateModuleNames(sRule.getHead());
		incorporateModuleNames(typeConstraints);
		incorporateModuleNames(guardNegatives);
		incorporateModuleNames(sRule.getBody());
		
		// - ������ξ�Ĺ�� = ������
		shrinkUnificationConstraints(typeConstraints);
		
		// - ���ȥ�Ÿ���ʥ��ȥ�����κƵ�Ū��Ÿ����
		expandAtoms(sRule.getHead());
		expandAtoms(typeConstraints);
		expandAtoms(guardNegatives);
		expandAtoms(sRule.getBody());

		// - ������ι�ʸ���顼�������������ȥ�����˥�󥯤��ץ���ʸ̮�Τߤ�¸�ߤ���褦�ˤ���
		correctTypeConstraints(typeConstraints);

		// - ������˽и�������̾X���Ф��ơ��롼��������Ƥ�X��$X���ִ�����
		HashMap typedLinkNameMap = computeTypedLinkNameMap(typeConstraints);
		unabbreviateTypedLinks(sRule.getHead(), typedLinkNameMap);
		unabbreviateTypedLinks(typeConstraints, typedLinkNameMap);
		unabbreviateTypedLinks(guardNegatives,  typedLinkNameMap);
		unabbreviateTypedLinks(sRule.getBody(), typedLinkNameMap);

		// - ��¤����
		// ���դ�2��ʾ�$p���и��������ˡ�������̾��$q�ˤ��� $p=$q��������ɲä���
		// todo ��������

		// - ��¤���
		// �������Ʊ�����ȥ��2��ʾ�$p���и��������ˡ�������̾��$q�ˤ��� $p==$q��������ɲä���
		// ������ѻߡ�

		// - �����
		// ������˽и�������Body�Ǥνи���1��Ǥʤ�$p���Ф��ƥ����ɤ�ground($p)���ɲä���
		// todo ��������
		
		// - ���ȥ�����˥ץ���ʸ̮���񤱤빽ʸ�ʤ�����ַ��դ��ץ���ʸ̮��ʸ�סˤ�Ÿ��
		// todo $p����Ū��$p[X]��Ÿ�������$p[X|*V]��Ÿ���Ǥ����ǽ�������¤��Ƥ���Τ򲿤Ȥ�����
		expandTypedProcessContexts(sRule.getHead());
		expandTypedProcessContexts(typeConstraints);
		expandTypedProcessContexts(guardNegatives);
		expandTypedProcessContexts(sRule.getBody());
	}

	/** ������������κ���Ū�ʹ�ʸ���顼�������������������[$p,[Q]]�Υꥹ�ȤȤ�����ַ������Ѵ����롣
	 *  ������ַ����ϡ����ȥ�Ÿ���ʤɤ�Ʃ��Ū�˹Ԥ�����˺��Ѥ��줿��*/
	private void correctGuardNegatives(LinkedList guardNegatives) {
		ListIterator lit = guardNegatives.listIterator();
		while (lit.hasNext()) {
			Object obj = lit.next();
			LinkedList eqlist;
			// \+�ΰ�����ꥹ�Ȥ˺ƹ�������
			if (obj instanceof LinkedList) {
				eqlist = (LinkedList)obj;
				flatten(eqlist);
			}
			else {
				eqlist = new LinkedList();
				eqlist.add(obj);
			}
			lit.remove();
			lit.add(eqlist);
			// �ꥹ�Ȥ����ǤΤ�����$p=Q �Τߤ�[$p,[Q]]�Ȥ��ƻĤ���
			ListIterator lit2 = eqlist.listIterator();
			while (lit2.hasNext()) {
				Object obj2 = lit2.next();
				lit2.remove();
				if (obj2 instanceof SrcAtom) {
					SrcAtom sAtom = (SrcAtom)obj2;
					if (sAtom.getName().equals("=") && sAtom.getProcess().size() == 2) {
						Object lhs = sAtom.getProcess().getFirst();
						if (lhs instanceof SrcProcessContext) {
							if (((SrcProcessContext)lhs).args != null) {
								warning("WARNING: argument of constrained process context is ignored: "
									+ SrcDumper.dump(lhs,0).replaceAll("\n",""));
								((SrcProcessContext)lhs).args = null;
							}
							Object rhs = sAtom.getProcess().get(1);
							LinkedList list = new LinkedList();
							LinkedList rhslist = new LinkedList();
							list.add(lhs);
							list.add(rhslist);
							rhslist.add(rhs);
							lit2.add(list);
							continue;
						}
					}
				}
				error("SYNTAX ERROR: process context equation expected rather than: "
					+ SrcDumper.dump(obj2,0).replaceAll("\n",""));
			}
		}
	}

	/** �ץ�����¤�ʻҥ롼�볰�ˤ˽и�������������ͥ��ȥ�˼����ࡣ
	 * <pre>
	 * '+'(x) �� '+x'
	 * '-'(x) �� '-x'
	 * </pre>
	 */
	void incorporateSignSymbols(LinkedList process) {
		ListIterator it = process.listIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom atom = (SrcAtom)obj;
				if (atom.getProcess().size() == 1
				 && (atom.getName().equals("+") || atom.getName().equals("-"))
				 && atom.getProcess().get(0) instanceof SrcAtom) {
					SrcAtom inneratom = (SrcAtom)atom.getProcess().get(0);
					if (inneratom.getProcess().size() == 0
					 && inneratom.getName().matches("([0-9]+|[0-9]*\\.[0-9]*)([Ee][+-]?[0-9]+)?")) {
						it.remove();
						it.add(new SrcAtom( atom.getName()
							+ ((SrcAtom)atom.getProcess().get(0)).getName() ));
					}
				}
				incorporateSignSymbols(atom.getProcess());
			}
			else if (obj instanceof SrcMembrane) {
				incorporateSignSymbols(((SrcMembrane)obj).getProcess());
			}
			else if (obj instanceof LinkedList) {
				incorporateSignSymbols((LinkedList)obj);
			}
		}
	}
	/** �ץ�����¤�ʻҥ롼�볰�ˤ˽и�����⥸�塼��̾��ե��󥯥��˼����ࡣ
	 * <pre>
	 * ':'(m,p(t1..tn)) �� 'm.p'(t1..tn)
	 * </pre>
	 */
	void incorporateModuleNames(LinkedList process) {
		ListIterator it = process.listIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom atom = (SrcAtom)obj;
				if (atom.getProcess().size() == 2
				 && atom.getName().equals(":")
				 && atom.getProcess().get(0) instanceof SrcAtom
				 && atom.getProcess().get(1) instanceof SrcAtom ) {
					SrcAtom pathatom = (SrcAtom)atom.getProcess().get(0);
					SrcAtom bodyatom = (SrcAtom)atom.getProcess().get(1);
					if (pathatom.getProcess().size() == 0
					 && pathatom.getNameType() == SrcName.PLAIN) {
						it.remove();
						it.add(bodyatom);
						bodyatom.srcname = new SrcName(pathatom.getName() + "." + bodyatom.getName(), SrcName.PATHED);
						incorporateModuleNames(bodyatom.getProcess());
						continue;
					}
				}
				incorporateModuleNames(atom.getProcess());
			}
			else if (obj instanceof SrcMembrane) {
				incorporateModuleNames(((SrcMembrane)obj).getProcess());
			}
			else if (obj instanceof LinkedList) {
				incorporateModuleNames((LinkedList)obj);
			}
		}
	}
	/** �ʥ����ɷ�����Ρ˥ץ�����¤�Υȥåץ�٥�˽и������Ĺ�� = �����롣
	 * <pre>
	 * $p = f(t1..tn) �� f(t1..tn,$p)
	 * f(t1..tn) = $p �� f(t1..tn,$p)
	 * </pre>
	 */
	private void shrinkUnificationConstraints(LinkedList process) {
		ListIterator it = process.listIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom atom = (SrcAtom)obj;
				if (!atom.getName().equals("=")) continue;
				if (atom.getProcess().size() != 2) continue;
				for (int atomarg = 0; atomarg < 2; atomarg++) {
					if (!(atom.getProcess().get(1 - atomarg) instanceof SrcProcessContext)) continue;
					if (!(atom.getProcess().get(atomarg) instanceof SrcAtom)) continue;
					SrcAtom subatom = (SrcAtom)atom.getProcess().get(atomarg);
					it.remove();
					it.add(subatom);
					subatom.getProcess().add(atom.getProcess().get(1 - atomarg));
					break;
				}
			}
		}
	}
	/** �ץ�����¤�Υ롼�Ȥ��餿�ɤ���ϰϤΥꥹ�Ȥ�Ƶ�Ū��Ÿ�����롣
	 * <pre>
	 * (t1,,tn) �� t1,,tn
	 * </pre>
	 */
	private void flatten(LinkedList process) {
		LinkedList srcprocess = (LinkedList)process.clone();
		process.clear();
		ListIterator it = srcprocess.listIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof LinkedList) {
				LinkedList list = (LinkedList)obj;
				flatten(list);
				process.addAll(list);
			}
			else process.add(obj);
		}
	}
	/** �ץ�����¤�ʻҥ롼�볰�ˤ򥢥ȥ�Ÿ�����롣
	 * ���ʤ�������ȥ�����˽и��������ƤΥ��ȥ๽¤���칽¤��Ƶ�Ū��Ÿ�����롣
	 * <pre>
	 * f(s1,g(t1,,tn),sm) �� f(s1,X,sm), g(t1,,tn,X)
	 * f(s1, {t1,,tn},sm) �� f(s1,X,sm), {+X,t1,,tm}
	 * f(s1, (t1,,tn),sm) �� f(s1,X,sm), ','(t1,(t2,,tn),X)
	 * </pre>
	 */
	void expandAtoms(LinkedList process) {
		LinkedList srcprocess = (LinkedList)process.clone();
		process.clear();
		ListIterator it = srcprocess.listIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				expandAtom((SrcAtom)obj, process);
			}
			else if (obj instanceof SrcMembrane) {
				expandAtoms(((SrcMembrane)obj).getProcess());
			}
			else if (obj instanceof LinkedList) {
				expandAtoms((LinkedList)obj);
			}
			process.add(obj);
		}
	}
	/** ���ȥ�γư������Ф��ƥ��ȥ�Ÿ����Ԥ���
	 * @param sAtom ���ȥ�Ÿ�����륢�ȥࡣ���Ȥ��ˤ��˲�����롣
	 * @param result ���ȥ�Ÿ����̤Υ��֥�����������ɲä���ꥹ�ȥ��֥������ȡʥץ�����¤��
	 */
	private void expandAtom(SrcAtom sAtom, LinkedList result) {
		LinkedList process = sAtom.getProcess();
		for (int i = 0; i < process.size(); i++) {
			Object obj = process.get(i);
			// ���ȥ�
			if (obj instanceof SrcAtom) {
				SrcAtom subatom = (SrcAtom)obj;
				//
				String newlinkname = generateNewLinkName();
				process.set(i, new SrcLink(newlinkname));
				subatom.getProcess().add(new SrcLink(newlinkname));
				//
				expandAtom(subatom, result);
				result.add(subatom);
			}
			// ��
			else if (obj instanceof SrcMembrane) {
				SrcMembrane submem = (SrcMembrane)obj;
				SrcAtom subatom = new SrcAtom("+");
				//
				String newlinkname = generateNewLinkName();
				process.set(i, new SrcLink(newlinkname));
				subatom.getProcess().add(new SrcLink(newlinkname));
				//
				submem.getProcess().add(subatom);
				expandAtoms(submem.getProcess());
				result.add(submem);
			}
			// ���ȡʲ���
			else if (obj instanceof LinkedList) {
				 LinkedList list = (LinkedList)obj;
				 if (list.isEmpty()) {				
					 SrcAtom subatom = new SrcAtom("()");
					 //
					 String newlinkname = generateNewLinkName();
					 process.set(i, new SrcLink(newlinkname));
					 subatom.getProcess().add(new SrcLink(newlinkname));
					 //
					 expandAtom(subatom, result);
					 result.add(subatom);
				 }
				 else {
					 SrcAtom subatom = new SrcAtom(",");
					 //
					 String newlinkname = generateNewLinkName();
					 process.set(i, new SrcLink(newlinkname));
					 subatom.getProcess().add(list.removeFirst());
					 if (list.size() == 1) {
						 subatom.getProcess().add(list.getFirst());
					 }
					 else {
						 subatom.getProcess().add(list);
					 }
					 subatom.getProcess().add(new SrcLink(newlinkname));
					 //
					 expandAtom(subatom, result);
					 result.add(subatom);
				 }
			 }
		}
	}
	/** ���ȥ�Ÿ����Υץ�����¤�ʻҥ롼�볰�ˤ˽и�������̾����ӥ���ƥ�����̾����󤹤롣
	 * @param names ����̾ (String) ���饳��ƥ����Ƚи���LinkedList�ؤμ��� [in,out] */
	private void enumNames(LinkedList process, HashMap names) {
		Iterator it = process.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			// ���ȥ�
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcContext) {
						addNameOccurrence((SrcContext)subobj, names);
					}
				}
			}
			// ��
			else if (obj instanceof SrcMembrane) {
				enumNames(((SrcMembrane)obj).getProcess(), names);
			}
			// �ץ���ʸ̮���롼��ʸ̮
			else if (obj instanceof SrcProcessContext || obj instanceof SrcRuleContext) {
				addNameOccurrence((SrcContext)obj, names);
			}
		}
	}
	private void addNameOccurrence(SrcContext sContext, HashMap names) {
		String name = sContext.getQualifiedName();
		if (!names.containsKey(name)) {
			names.put(name, new LinkedList());
		}
		((LinkedList)names.get(name)).add(sContext);
	}
		
	/** unabbreviateTypedLinks�ǻȤ�����μ������������롣
	 * @return ���դ���󥯤θ���̾ " X" (String) ���顢
	 * �б����뷿�դ��ץ���ʸ̮̾�ƥ����� "X" (String) �ؤμ���
	 * <p>todo ��Ϥ����ס�ñ�˥��̾�ƥ����� "X" ������������褦�˽������٤��Ǥ��롣
	 */
	HashMap computeTypedLinkNameMap(LinkedList typeConstraints) {	
		HashMap typedLinkNameMap = new HashMap();
		HashMap typedNames = new HashMap();
		enumNames(typeConstraints, typedNames);
		Iterator it = typedNames.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			Object obj = ((LinkedList)typedNames.get(name)).getFirst();
			if (obj instanceof SrcLink) {
				typedLinkNameMap.put(name, ((SrcLink)obj).getName());
			}
		}
		return typedLinkNameMap;
	}

	/** ���ȥ�Ÿ����Υץ�����¤�ʻҥ롼�볰�ˤ˽и��������Ƥ�typedLinkNameMap��Υ��̾��
	 * �ץ���ʸ̮��ʸ���ִ����롣
	 * @param typedLinkNameMap ���դ���󥯤θ���̾ " X" (String) ���顢
	 * �б����뷿�դ��ץ���ʸ̮̾�ƥ����� "X" (String) �ؤμ���
	 * <pre> p(s1,X,sn) �� p(s1,$X,sn)
	 * </pre>*/
	private void unabbreviateTypedLinks(LinkedList process, HashMap typedLinkNameMap) {
		Iterator it = process.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcLink) {
						SrcLink srcLink = (SrcLink)subobj;
						String name = srcLink.getQualifiedName();
						if (typedLinkNameMap.containsKey(name)) {
							sAtom.getProcess().set(i,
								new SrcProcessContext((String)typedLinkNameMap.get(name),true));
						}
					}
				}
			}
			else if (obj instanceof SrcMembrane) {
				unabbreviateTypedLinks(((SrcMembrane)obj).getProcess(), typedLinkNameMap);
			}
			else if (obj instanceof LinkedList) {
				unabbreviateTypedLinks((LinkedList)obj, typedLinkNameMap);
			}
		}
	}
	
	/** ���ȥ�Ÿ����Υץ�����¤�ʻҥ롼�볰�ˤΥ��ȥ�����˽и�����ץ���ʸ̮��Ÿ�����롣
	 * <pre> p(s1,$p,sn) �� p(s1,X,sn), $p[X]
	 * </pre>
	 * <p>�᥽�åɤ�̾���Ȥϰۤʤꡢ���դ��Ǥʤ��ץ���ʸ̮��Ÿ��������ͤˤʤäƤ��롣
	 * <p>todo $p[X|*p] ��Ÿ�����٤����⤢��Ϥ�
	 */
	private void expandTypedProcessContexts(LinkedList process) {
		ListIterator it = process.listIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcProcessContext) {
						SrcProcessContext srcProcessContext = (SrcProcessContext)subobj;
						String name = srcProcessContext.getQualifiedName();
						String newlinkname = generateNewLinkName();
						sAtom.getProcess().set(i, new SrcLink(newlinkname));
						it.add(srcProcessContext);
						// ���ȥ������$p[...]������褦�˹�ʸ��ĥ���줿���Τ� args!=null �Ȥʤ�
						if (srcProcessContext.args == null)
							srcProcessContext.args = new LinkedList();
						srcProcessContext.args.add(new SrcLink(newlinkname));
					}
				}
			}
			else if (obj instanceof SrcMembrane) {
				expandTypedProcessContexts(((SrcMembrane)obj).getProcess());
			}
		}
	}
	
	
	/* ���ȥ�Ÿ����Υץ�����¤�ʻҥ롼�볰�ˤ˽и����뷿�դ��ץ���ʸ̮��typed�ޡ�����Ԥ���
	 * @param typedNames ���դ��ץ���ʸ̮�θ���̾ "$p" (String) �򥭡��Ȥ������
	 * <pre> $p[X] �� $p[X]
	 * </pre> *
	private void markAsTyped(LinkedList process, HashMap typedNames) {
		ListIterator it = process.listIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcProcessContext) {
				SrcProcessContext sProc = (SrcProcessContext)obj;
				if (typedNames.containsKey(sProc.getQualifiedName())) {
					sProc.typed = true;
				}
			}
			else if (obj instanceof SrcMembrane) {
				markAsTyped(((SrcMembrane)obj).getProcess(), typedNames);
			}
		}
	}*/
	
	////////////////////////////////////////////////////////////////
	//
	// ��ʸ���顼���Ф����������Ԥ��᥽�å�
	//
	
	/** ���ȥ�Ÿ����Υץ�����¤�ʥ����ɤη�����ˤ��Ф��ơ�
	 * ���롼��ʸ̮��롼�����«��ȥåץ�٥�Υץ���ʸ̮��¸�ߤ�����
	 * ����ѥ��륨�顼�Ȥ��롣���ȥ�����Ǥνи���̵̾�Υץ����ѿ����ִ����롣*/
	private void correctTypeConstraints(LinkedList process) {
		Iterator it = process.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcLink) {}
					else if (subobj instanceof SrcProcessContext) {}
					else {
						String proccxtname = generateNewProcessContextName();
						sAtom.getProcess().set(i, new SrcProcessContext(proccxtname, true));
						error("SYNTAX ERROR: illegal object in guard atom argument: " + subobj);
					}
				}
			}
			else {
				error("SYNTAX ERROR: illegal object in guard: " + obj);
				it.remove();
			}
		}
	}

	/** ���ȥ�Ÿ����Υץ�����¤�ʥ������ե�����ˡʥ롼�볰�ˤ��Ф��ơ�
	 * �ץ���ʸ̮��롼��ʸ̮����«���и������饳��ѥ��륨�顼�Ȥ��롣
	 * ���ȥ�����Ǥνи���̵̾�Υ�󥯤��ִ����롣*/
	void correctWorld(LinkedList process) {
		Iterator it = process.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof LinkedList) {
				LinkedList list = (LinkedList)obj;
				correctWorld(list);				
			}
			else if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcLink) {}
					else {
						String linkname = generateNewLinkName();
						sAtom.getProcess().set(i, new SrcLink(linkname));
						error("SYNTAX ERROR: illegal object in an atom argument: " + subobj);
					}
				}
			}
			else if (obj instanceof SrcMembrane) {
				correctWorld(((SrcMembrane)obj).getProcess());
			}
			else if (obj instanceof SrcRule) {}
			else {
				error("SYNTAX ERROR: illegal object outside a rule: " + obj);
				it.remove();
			}
		}
	}
	private void error(String text) {
		parser.error(text);
	}
	private void warning(String text) {
		parser.warning(text);
	}
	/** ��ˡ����ʿ��������̾���������� */
	private String generateNewLinkName() {
		return parser.generateNewLinkName();
	}
	/** ��ˡ����ʿ������ץ���ʸ̮̾���������� */
	private String generateNewProcessContextName() {
		return parser.generateNewProcessContextName();
	}
}

// TODO ( {p($t)} :- ground($t) | end ) �򥳥�ѥ��뤹�뤿�������̿�᤬­��ʤ�
