/**
 * LMNtal Parser �ᥤ�󥯥饹
 * ���ĤΥ����������ɤ�Membrane�Ȥ���ɽ������ޤ���
 */

package compile.parser;

import java_cup.runtime.Scanner;
import java.io.Reader;
//import java.util.Arrays;
import java.util.LinkedList;
//import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import runtime.Functor;
import runtime.Inline;
//import runtime.Env;
import compile.Module;
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
		incorporateSignSymbols(srcProcess);
		expandAtoms(srcProcess);
		correctWorld(srcProcess);
		addProcessToMem(srcProcess, mem);
		HashMap freeLinks = addProxies(mem);
		if (!freeLinks.isEmpty()) closeFreeLinks(mem);
		Inline.makeCode();
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
		}
		return result;
	}

	////////////////////////////////////////////////////////////////
	
	/**
	 * ��ˡ�����̾���ο�������󥯹�ʸ���������
	 * @return ����������󥯹�ʸ
	 * @deprecated
	 */
	private SrcLink createNewSrcLink() {
		return new SrcLink(generateNewLinkName());
	}
	
	/** ��ˡ����ʿ��������̾���������� */
	private String generateNewLinkName() {
		nLinkNumber++;
		return LINK_NAME_PREFIX + nLinkNumber;	
	}
	/** ��ˡ����ʿ������ץ���ʸ̮̾���������� */
	private String generateNewProcessContextName() {
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
		//if (pos >= atom.args.length) error("SYSTEM ERROR: Out of Atom args length:"+pos);
		atom.args[pos] = new LinkOccurrence(link.getName(), atom, pos);
	}
	
	////////////////////////////////////////////////////////////////
	
	/**
	 * ��˥��ȥࡢ���졢�롼��ʤɤ������Ͽ����
	 * @param list ��Ͽ�������ץ����Υꥹ��
	 */
	void addProcessToMem(LinkedList list, Membrane mem) throws ParseException {
		for (int i = 0; i < list.size(); i++) addObjectToMem(list.get(i), mem);
	}
	/**
	 * ��˥��ȥࡢ���졢�롼��ʤɤι�ʸ���֥������Ȥ��ɲ�
	 * @param obj �ɲä��빽ʸ���֥�������
	 * @param mem �ɲ������
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
		// hara
		Module.regMemName(sMem.name, submem);
		submem.name = sMem.name;
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
//				error("SYNTAX ERROR: Untyped process context in an atom argument: " + obj);
//				setLinkToAtomArg(new SrcLink(generateNewLinkName()), atom, i);
//				allbundles = false;
//			}

			// ����¾
			else {
				error("SYNTAX ERROR: Illegal object in an atom argument: " + obj);
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
		expandRuleAbbreviations(sRule);
		// ��¤������
		LinkedList typeConstraints = sRule.getGuard();
		addProcessToMem(sRule.getHead(), rule.leftMem);		
		addProcessToMem(typeConstraints, rule.guardMem);
		addProcessToMem(sRule.getBody(), rule.rightMem);
		// ��󥯰ʳ���̾������³
		resolveContextNames(rule);
		// �ץ������ȥ������������󥯤�Ĥʤ�����μ�ͳ��󥯥ꥹ�Ȥ���ꤹ��
		addProxies(rule.leftMem);
		coupleLinks(rule.guardMem);
		addProxies(rule.rightMem);
		// ���դȺ��դμ�ͳ��󥯤���³����
		coupleInheritedLinks(rule);
		//
		mem.rules.add(rule);
	}
	
	////////////////////////////////////////////////////////////////
	//
	// ��󥯤ȥץ���
	//
	
	/** ������Ф��ƺƵ�Ū�˥ץ������ɲä��롣
	 * @return ������ι������줿��ͳ��󥯥ޥå� mem.freeLinks */
	private HashMap addProxies(Membrane mem) {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			HashMap freeLinks = addProxies(submem);
			// ����μ�ͳ��󥯤��Ф��ƥץ������ɲä���
			HashMap newFreeLinks = new HashMap();
			Iterator it2 = freeLinks.keySet().iterator();
			while (it2.hasNext()) {
				LinkOccurrence freeLink = (LinkOccurrence)freeLinks.get(it2.next());
				String proxyLinkName = PROXY_LINK_NAME_PREFIX + freeLink.name;
				// �����inside_proxy���ɲ�
				ProxyAtom inside = new ProxyAtom(submem, ProxyAtom.INSIDE_PROXY_NAME);
				inside.args[0] = new LinkOccurrence(proxyLinkName, inside, 0); // ��¦
				inside.args[1] = new LinkOccurrence(freeLink.name, inside, 1); // ��¦
				inside.args[1].buddy = freeLink;
				freeLink.buddy = inside.args[1];
				submem.atoms.add(inside);
				// ��������ͳ���̾�򿷤�����ͳ��󥯰������ɲä���
				newFreeLinks.put(proxyLinkName, inside.args[0]);			
				// �������outside_proxy���ɲ�
				ProxyAtom outside = new ProxyAtom(mem, ProxyAtom.OUTSIDE_PROXY_NAME);
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
					if (a.args[j].buddy == null) addLinkOccurrence(links, a.args[j]);
				}
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
	 */
	private void addLinkOccurrence(HashMap links, LinkOccurrence lnk) {
		// 3��ʾ�νи�
		if (links.get(lnk.name) == CLOSED_LINK) {
			error("SYNTAX ERROR: Link " + lnk.name + " appears more than twice.");
			String linkname = lnk.name + generateNewLinkName();
			if (lnk.name.startsWith(SrcLinkBundle.PREFIX_TAG))
				linkname = SrcLinkBundle.PREFIX_TAG + linkname;
			lnk.name = linkname;
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
			warning("WARNING: Global singleton link: " + link.name);
			LinkedList process = new LinkedList();
			process.add(new SrcLink(link.name));
			SrcAtom sAtom = new SrcAtom(link.name, process);
			addSrcAtomToMem(sAtom, mem);
		}
		coupleLinks(mem);
	}

	/** ���դȱ��դμ�ͳ��󥯤�Ĥʤ� */
	void coupleInheritedLinks(RuleStructure rule) {
		HashMap lhsFreeLinks = rule.leftMem.freeLinks;
		HashMap rhsFreeLinks = rule.rightMem.freeLinks;
		HashMap links = new HashMap();
		Iterator it = lhsFreeLinks.keySet().iterator();
		while (it.hasNext()) {
			String linkname = (String)it.next();
			if (lhsFreeLinks.get(linkname) == CLOSED_LINK) continue;
			LinkOccurrence lhsocc = (LinkOccurrence)lhsFreeLinks.get(linkname);
			addLinkOccurrence(links, lhsocc);
		}
		it = rhsFreeLinks.keySet().iterator();
		while (it.hasNext()) {
			String linkname = (String)it.next();
			if (rhsFreeLinks.get(linkname) == CLOSED_LINK) continue;
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
	 * @param names ����ƥ����Ȥθ���̾ (String) ���� ContextDef �ؤμ��� [in,out] */
	private void enumHeadNames(Membrane mem, HashMap names) throws ParseException {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			enumHeadNames(submem, names);
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
				it.remove(); // ���ʤ��Ȥⷿ�ʤ��ץ���ʸ̮�ǤϤʤ����������
				pc.def = (ContextDef)names.get(name);
				if (pc.def.isTyped()) {
					if (pc.def.src != null) {
						// Ÿ���������������פˤʤ�
						error("FEATURE NOT IMPLEMENTED: head contains more than one occurrence of a typed process context name: " + name);
						corrupted();
					}
					if (pc.args.length != 1) {
						error("SYNTAX ERROR: Typed process context occurring in head must have exactly one explicit free link argument: " + pc);
						continue;
					}
					mem.typedProcessContexts.add(pc);
				}
				else {
					// ��¤��Ӥؤ��Ѵ��������������פˤʤ�
					error("FEATURE NOT IMPLEMENTED: untyped process context name appeared more than once in a head: " + name);
					corrupted();
				}
			}
			pc.def.src = pc;	// �������и�����Ͽ
			if (pc.bundle != null) addLinkOccurrence(names, pc.bundle);
		}
		it = mem.ruleContexts.iterator();
		while (it.hasNext()) {
			RuleContext rc = (RuleContext)it.next();
			rc.def = new ContextDef(rc.getQualifiedName());
			rc.def.src = rc;
			names.put(rc.getQualifiedName(), rc.def);
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
			error("SYNTAX ERROR: Head membrane cannot contain more than one untyped process context");
			it = mem.processContexts.iterator();
			while (it.hasNext()) {
				((ProcessContext)it.next()).def.src = null; // �������и�����Ͽ����ä�
				it.remove(); // names�ˤϻĤ�
			}
		}
		if (mem.ruleContexts.size() > 1) {
			error("SYNTAX ERROR: Head membrane cannot contain more than one rule context");
			while (it.hasNext()) {
				((RuleContext)it.next()).def.src = null; // �������и�����Ͽ����ä�
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
				if (pc.def.src != null) {
					if (pc.args.length != pc.def.src.args.length
					 || ((pc.bundle == null) != (((ProcessContext)pc.def.src).bundle == null)) ) {
						error("SYNTAX ERROR: unmatched length of free link list of process context: " + pc);
						it.remove();
						continue;
					}
				}
				if (pc.def.isTyped()) {
					it.remove();
					if (pc.args.length != 1) {
						error("SYNTAX ERROR: Typed process context occurring in body must have exactly one explicit free link argument: " + pc);
						continue;
					}
					mem.typedProcessContexts.add(pc);
				}
				else {
					if (pc.def.src == null) {
						// ��ʸ���顼�ˤ��إåɽи������ä��줿���ʤ�$p�ϡ��ܥǥ��и���̵���Ǽ��������
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

	/** �롼�빽¤���Ф��ơ��ץ���ʸ̮����ӥ롼��ʸ̮����³��Ԥ� */
	private void resolveContextNames(RuleStructure rule) throws ParseException {

		// Ʊ��̾���Υץ���ʸ̮�ΰ����ѥ������Ʊ���ˤ��롣
		// ���դ�������Ū�ʼ�ͳ��󥯤θĿ���1�ˤ��롣

		Iterator it;
		HashMap names = new HashMap();
		enumTypedNames(rule.guardMem, names);
		enumHeadNames(rule.leftMem, names);
		// todo ���«�����դ��Ĥ��Ƥ��ʤ����Ȥ��ǧ����
		
		// - ���եȥåץ�٥�Υץ���ʸ̮��������
		it = rule.leftMem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			error("SYNTAX ERROR: untyped head process context requires an enclosing membrane: " + pc);
			names.remove(pc.def.getName());
			pc.def.src = null;	// �������и�����Ͽ����ä�
			it.remove();
		}
		
		//
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
			else { // ���դ��Ǥʤ���硢src!=null�ȤʤäƤ���
				if (def.src instanceof ProcessContext) {
					rule.processContexts.put(name, def);
				}
				else if (def.src instanceof RuleContext) {
					rule.ruleContexts.put(name, def);
				}
			}
			if (def.rhsOccs.size() == 1) {
				if (def.src != null) {	// �����ɤΤȤ�����̣��̵���Τ�def.src�ϻ����ѹ����ѻߤ�����
					Context rhsocc = ((Context)def.rhsOccs.get(0));
					rhsocc.buddy = def.src;
					def.src.buddy = rhsocc;
				}
			}			
		}
		
		// ���������ץ���ʸ̮�����������ޤǤβ����֤Ȥ��ơ������Ǥʤ���Τ������
		it = rule.processContexts.values().iterator();
		while (it.hasNext()) {
			ContextDef def = (ContextDef)it.next();
			if (def.rhsOccs.size() != 1) {
				error("FEATURE NOT IMPLEMENTED: untyped process context must be linear: " + def.getName());
				corrupted();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////
	//
	// ά��ˡ��Ÿ��
	//

	/** �롼�빽ʸ���Ф���ά��ˡ��Ÿ����Ԥ� */
	private void expandRuleAbbreviations(SrcRule sRule) throws ParseException {

		// todo �����ǥ����ɤ�������������ʬ�ह��ʸ��ߤ����Ʒ�����Ȥ��ư��äƤ����
		LinkedList typeConstraints = sRule.getGuard();

		// - ���ͤ������μ�����
		incorporateSignSymbols(sRule.getHead());
		incorporateSignSymbols(typeConstraints);
		incorporateSignSymbols(sRule.getBody());

		// - ������� = ������
		// todo
		
		// - ���ȥ�Ÿ���ʥ��ȥ�����κƵ�Ū��Ÿ����
		expandAtoms(sRule.getHead());
		expandAtoms(typeConstraints);
		expandAtoms(sRule.getBody());

		// - ������ι�ʸ���顼�������������ȥ�����˥�󥯤��ץ���ʸ̮�Τߤ�¸�ߤ���褦�ˤ���
		correctTypeConstraints(typeConstraints);

		// - ������˽и�������̾X���Ф��ơ��롼��������Ƥ�X��$X���ִ�����
		HashMap typedLinkNameMap = computeTypedLinkNameMap(typeConstraints);
		unabbreviateTypedLinks(sRule.getHead(), typedLinkNameMap);
		unabbreviateTypedLinks(typeConstraints, typedLinkNameMap);
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
		
		// - ���դ��ץ���ʸ̮��ʸ��Ÿ��
		// todo $p����Ū��$p[X]��Ÿ�������$p[X|*V]��Ÿ���Ǥ����ǽ�������¤��Ƥ���Τ򲿤Ȥ�����
		expandTypedProcessContexts(sRule.getHead());
		expandTypedProcessContexts(typeConstraints);
		expandTypedProcessContexts(sRule.getBody());
	}

	/** �ץ�����¤�ʻҥ롼�볰�ˤ˽и�������������ͥ��ȥ�˼����ࡣ
	 * <pre>
	 * '+'(x) �� '+x'
	 * '-'(x) �� '-x'
	 * </pre>
	 */
	private void incorporateSignSymbols(LinkedList process) {
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
		}
	}
	
	/** �ץ�����¤�ʻҥ롼�볰�ˤ򥢥ȥ�Ÿ�����롣
	 * ���ʤ�������ȥ�����˽и��������ƤΥ��ȥ๽¤���칽¤��Ƶ�Ū��Ÿ�����롣
	 * <pre>
	 * f(s1,g(t1,tn),sm) �� f(s1,X,sm), g(t1,tn,X)
	 * f(s1, {t1,tn},sm) �� f(s1,X,sm), {+X,t1,tm}
	 * </pre>
	 */
	private void expandAtoms(LinkedList process) {
		LinkedList srcprocess = (LinkedList)process.clone();
		process.clear();
		Iterator it = srcprocess.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				expandAtom((SrcAtom)obj, process);
			}
			else if (obj instanceof SrcMembrane) {
				expandAtoms(((SrcMembrane)obj).getProcess());
			}
			process.add(obj);
		}
	}
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
			// ����ѻߤ��Ƥ�褤���ºݡ����ߡ���ʸ���ϴ���������Թ�塢��ʸŪ���ѻߤ��Ƥ����
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
		}
	}
	
	/** ���ȥ�Ÿ����Υץ�����¤�ʻҥ롼�볰�ˤΥ��ȥ�����˽и�����ץ���ʸ̮��Ÿ�����롣
	 * <pre> p(s1,$p,sn) �� p(s1,X,sn), $p[X]
	 * </pre>
	 * todo $p[X|*p] ��Ÿ�����٤����⤢��Ϥ�
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
						error("SYNTAX ERROR: Illegal object in guard atom argument: " + subobj);
					}
				}
			}
			else {
				error("SYNTAX ERROR: Illegal object in guard: " + obj);
				it.remove();
			}
		}
	}

	/** ���ȥ�Ÿ����Υץ�����¤�ʥ������ե�����ˡʥ롼�볰�ˤ��Ф��ơ�
	 * �ץ���ʸ̮��롼��ʸ̮����«���и������饳��ѥ��륨�顼�Ȥ��롣
	 * ���ȥ�����Ǥνи���̵̾�Υ�󥯤��ִ����롣*/
	private void correctWorld(LinkedList process) {
		Iterator it = process.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcLink) {}
					else {
						String linkname = generateNewLinkName();
						sAtom.getProcess().set(i, new SrcLink(linkname));
						error("SYNTAX ERROR: Illegal object in an atom argument: " + subobj);
					}
				}
			}
			else if (obj instanceof SrcMembrane) {
				correctWorld(((SrcMembrane)obj).getProcess());
			}
			else if (obj instanceof SrcRule) {}
			else {
				error("SYNTAX ERROR: Illegal object outside a rule: " + obj);
				it.remove();
			}
		}
	}
}

// TODO ( {p($t)} :- ground($t) | end ) �򥳥�ѥ��뤹�뤿�������̿�᤬­��ʤ�
