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

import runtime.Functor;
import runtime.Inline;
import runtime.Env;
import compile.Module;
import compile.structure.*;

public class LMNParser {

	private static final String LINK_NAME_PREFIX = "";
	private static final String PROXY_LINK_NAME_PREFIX = "^";
	private static final String PROCESS_CONTEXT_NAME_PREFIX = "_";
	static final LinkOccurrence CLOSED_LINK = new LinkOccurrence("",null,0);

	private int nLinkNumber = 0;
	private Scanner lex = null;
	
	private int nErrors = 0;
	private int nWarnings = 0;
	
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
		expandAtoms(srcProcess);
		correctWorld(srcProcess);
		addProcessToMem(srcProcess, mem);
		HashMap freeLinks = addProxies(mem);
		if (!freeLinks.isEmpty()) {
			closeFreeLinks(mem, "WARNING: Global singleton link: ");
		}
		Inline.makeCode();
		return mem;
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
//		// ���ñ�첽
//		else if (obj instanceof SrcLinkUnify) {
//			addSrcLinkUnifyToMem((SrcLinkUnify)obj, mem);
//			System.out.println("foo");
//		}
		// ����¾ 
		else {
			throw new ParseException("Illegal Object to add to a membrane: "+obj);
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
		// hara
		Module.regMemName(sMem.name, submem);
		submem.name = sMem.name;
		addProcessToMem(sMem.getProcess(), submem);
		mem.mems.add(submem);
	}
	/**
	 * ���ȥ๽ʸ������ɲ�
	 * @param sAtom �ɲä��������ȥ๽ʸ
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addSrcAtomToMem(SrcAtom sAtom, Membrane mem) throws ParseException {
		boolean alllinks   = true;
		boolean allbundles = true;
		LinkedList p = sAtom.getProcess();
		int arity = p.size();
		
		// [1] �ե��󥯥�����������
		// GUI�����ưŪ���������б�������ˤ��ʤ��� FunctorFactory �Τ褦�ʤ�Τ����ä������褤��
		// runtime.*Functor ��¿�������������Լ�������ʪ��롣

		SrcName srcname = sAtom.getName();
		String name = srcname.getName();
		String path = null;
		if (srcname.getType() == SrcName.PATHED) {
			int pos = name.indexOf('.');
			path = name.substring(0, pos);
			name = name.substring(pos + 1);
		}
		Functor func = new runtime.Functor(name, arity, path);
		if (arity == 1 && path == null) {
			if (srcname.getType() == SrcName.PLAIN || srcname.getType() == SrcName.SYMBOL) {
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
			// �ץ���ʸ̮
			else if (obj instanceof SrcProcessContext) {
				throw new ParseException("Untyped process context in an atom argument: " + obj);
			}
			// ����¾
			else {
				throw new ParseException("Illegal object in an atom argument: " + obj);
			}
		}
		
		// [4] ���ȥ�ȥ��ȥླྀ�Ĥ��̤���
		if (arity > 0 && allbundles) 
			mem.aggregates.add(atom);
		else if (arity == 0 || alllinks )
			mem.atoms.add(atom);
		else {
			System.out.println("SYNTAX ERROR: arguments of an atom contain both links and bundles");
		}
	}

	////////////////////////////////////////////////////////////////
	//
	// ��󥯤ȥץ���
	//
	
	/** ������Ф��ƺƵ�Ū�˥ץ������ɲä��롣
	 * @return ������ι������줿��ͳ��󥯥ޥå� mem.freeLinks */
	private HashMap addProxies(Membrane mem) throws ParseException {
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
				if (a.args[j].buddy == null) addLinkOccurrence(links, a.args[j]);
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
	
	/** ��mem�μ�ͳ��󥯤�������Ĥ���ʹ�ʸ���顼����������ѡ� */
	public void closeFreeLinks(Membrane mem, String prefix) throws ParseException {
		Iterator it = mem.freeLinks.keySet().iterator();
		while (it.hasNext()) {
			LinkOccurrence link = (LinkOccurrence)mem.freeLinks.get(it.next());
			System.out.println(prefix  + link.name);
			LinkedList process = new LinkedList();
			process.add(new SrcLink(link.name));
			SrcAtom sAtom = new SrcAtom(link.name, process);
			addSrcAtomToMem(sAtom, mem);
		}
		coupleLinks(mem);
	}


	////////////////////////////////////////////////////////////////
	//
	// �ץ���ʸ̮�ȥ롼��ʸ̮
	//
	
	/** �إåɤΥץ���ʸ̮���롼��ʸ̮�Υޥåפ���� */
	private void enumHeadNames(Membrane mem, HashMap names) throws ParseException {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			enumHeadNames(submem, names);
		}
		//
		if (mem.processContexts.size() > 1) {
			System.out.println("SYNTAX ERROR: Head membrane cannot contain more than one untyped process context");
		}
		it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			names.put(pc.getQualifiedName(), pc);
			pc.src = pc;
			if (pc.bundle != null) addLinkOccurrence(names, pc.bundle);
		}
		//
		if (mem.ruleContexts.size() > 1) {
			System.out.println("SYNTAX ERROR: Head membrane cannot contain more than one rule context");
		}
		it = mem.ruleContexts.iterator();
		while (it.hasNext()) {
			RuleContext rc = (RuleContext)it.next();
			names.put(rc.getQualifiedName(), rc);
			rc.src = rc;
		}
		//
		it = mem.aggregates.iterator();
		while (it.hasNext()) {
			Atom atom = (Atom)it.next();
			for (int i = 0; i < atom.args.length; i++) {
				addLinkOccurrence(names, atom.args[i]);
			}
		}
	}

	/** �ܥǥ��Υץ���ʸ̮���롼��ʸ̮�Υꥹ�Ȥ���� */
	private void enumBodyNames(Membrane mem, HashMap names) throws ParseException {
		Iterator it = mem.mems.iterator();
		while (it.hasNext()) {
			Membrane submem = (Membrane)it.next();
			enumBodyNames(submem, names);
		}
		it = mem.processContexts.iterator();
		while (it.hasNext()) {
			ProcessContext pc = (ProcessContext)it.next();
			if (names.containsKey(pc.getQualifiedName())) {
				ProcessContext pcsrc = (ProcessContext)names.get(pc.getQualifiedName());
				pc.src = pcsrc;
				if (pc.args.length != pcsrc.args.length
				 || ((pc.bundle == null) ^ (pcsrc.bundle == null)) ) {
					System.out.println("Unmatched length of free link list of process context");
					it.remove();
				}
			}
			else {
				System.out.println("process context not appeared in head: " + pc.getQualifiedName());
			}
		}
		it = mem.ruleContexts.iterator();
		while (it.hasNext()) {
			RuleContext rc = (RuleContext)it.next();
			if (names.containsKey(rc.getQualifiedName())) {
				rc.src = (Context)names.get(rc.getQualifiedName());
			}
			else {
				System.out.println("rule context not appeared in head: " + rc.getQualifiedName());
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
	////////////////////////////////////////////////////////////////
		
	/**
	 * �롼�빽ʸ������ɲä���
	 * @param sRule �ɲä������롼�빽ʸ
	 * @param mem �ɲ������
	 * @throws ParseException
	 */
	private void addSrcRuleToMem(SrcRule sRule, Membrane mem) throws ParseException {
		RuleStructure rule = new RuleStructure(mem);
		
		// todo �����ǥ����ɤ�������������ʬ�ह��ʸ��ߤ����Ʒ�����Ȥ��ư��äƤ����
		LinkedList typeConstraints = sRule.getGuard();

		// === ά��ˡ��Ÿ���������� ===

		// - ���ȥ�Ÿ���ʥ��ȥ�����κƵ�Ū��Ÿ����
		expandAtoms(sRule.getHead());
		expandAtoms(typeConstraints);
		expandAtoms(sRule.getBody());

		// - ������ι�ʸ���顼�������������ȥ�����˥�󥯤��ץ���ʸ̮�Τߤ�¸�ߤ���褦�ˤ���
		correctTypeConstraints(typeConstraints);

		// - ������˽и�������̾X���Ф��ơ��롼��������Ƥ�X��$_X���ִ�����
		HashMap typedNames = new HashMap();
		enumNames(typeConstraints, typedNames);

		HashMap typedLinkNames = new HashMap();
		Iterator it = typedNames.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			if (((LinkedList)typedNames.get(name)).getFirst() instanceof SrcLink) {
				typedLinkNames.put(name, new SrcProcessContext("_" + name, true));
			}
		}
		unabbreviateTypedLinks(sRule.getHead(), typedLinkNames);
		unabbreviateTypedLinks(typeConstraints, typedLinkNames);
		unabbreviateTypedLinks(sRule.getBody(), typedLinkNames);

		// - ��¤����
		// ���դ�2��ʾ�$p���и��������ˡ�������̾��$q�ˤ��� $p=$q��������ɲä���
		// todo ��������

		// - ��¤���
		// �������Ʊ�����ȥ��2��ʾ�$p���и��������ˡ�������̾��$q�ˤ��� $p==$q��������ɲä���
		// todo ��������

		// - �����
		// ������˽и�������Body�Ǥνи���1��Ǥʤ�$p���Ф��ƥ����ɤ�ground($p)���ɲä���
		// todo ��������
		

		// - ���դ��ץ���ʸ̮��ʸ��Ÿ��
		typedNames = new HashMap();
		enumNames(typeConstraints, typedNames);
		expandTypedProcessContexts(sRule.getHead(), typedNames);
		expandTypedProcessContexts(sRule.getBody(), typedNames);

		// === ά��ˡ��Ÿ�������ޤ� ===

		// ��¤������
		addProcessToMem(sRule.getHead(), rule.leftMem);		
		addProcessToMem(typeConstraints, rule.guardMem);
		addProcessToMem(sRule.getBody(), rule.rightMem);

		// �ץ������ȥ������������󥯤�Ĥʤ�����μ�ͳ��󥯥ꥹ�Ȥ���ꤹ��
		addProxies(rule.leftMem);
		addProxies(rule.rightMem);
		
		// ���դȺ��դμ�ͳ��󥯤���³����
		coupleInheritedLinks(rule);

		// ���⤷�ʤ�
		correctHead(rule.leftMem);
		correctBody(rule.rightMem);
		
		// �ץ���ʸ̮����ӥ롼��ʸ̮����³����
		HashMap names = new HashMap();
		enumHeadNames(rule.leftMem, names);
		enumBodyNames(rule.rightMem, names);
		
		// todo rule.processContexts ����������
		// todo rule.ruleContexts ����������
		// todo rule.typedProcessContexts ����������
		// todo bundle ����³����
		
		mem.rules.add(rule);
	}
	
	/** ���դȱ��դμ�ͳ��󥯤�Ĥʤ� */
	void coupleInheritedLinks(RuleStructure rule) throws ParseException {
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
			closeFreeLinks(rule.leftMem, "SYNTAX ERROR: rule head contains free variable: ");
			closeFreeLinks(rule.rightMem,"SYNTAX ERROR: rule body contains free variable: ");
		}
	}

	/**
	 * �ץ���ʸ̮��ʸ������ɲ�
	 * @param sProc �ɲä������ץ���ʸ̮��ʸ
	 * @param mem �ɲ������
	 */
	private void addSrcProcessContextToMem(SrcProcessContext sProc, Membrane mem) {
		ProcessContext pc;
		if (sProc.args == null) {
			pc = new ProcessContext(mem, sProc.getQualifiedName(), 0);
			pc.setBundleName("*" + sProc.getName());
		}
		else {
			int length = sProc.args.size();
			pc = new ProcessContext(mem, sProc.getQualifiedName(), length);
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
	
//	private void addSrcLinkUnifyToMem(SrcLinkUnify sUnify, Membrane mem) throws ParseException {
//		Atom unify = new Atom(mem,"=",2);
//		setLinkToAtomArg((SrcLink)sUnify.getProcess().get(0), unify, 0);
//		setLinkToAtomArg((SrcLink)sUnify.getProcess().get(1), unify, 1);
//	}
	
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
	 * @throws ParseException ���å���ξ�꤬���ȥ��¸�ߤ��ʤ����
	 */
	private void setLinkToAtomArg(SrcLink link, Atom atom, int pos) throws ParseException {
		if (pos >= atom.args.length) throw new ParseException("Out of Atom args length:"+pos);
		atom.args[pos] = new LinkOccurrence(link.getName(), atom, pos);
	}
	
	
	////////////////////////////////////////////////////////////////
	//
	// ά��ˡ��Ÿ��
	//
	
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

	/** ���ȥ�Ÿ����Υץ�����¤�ʻҥ롼�볰�ˤ˽и��������Ƥ�typedLinkNames��Υ��̾��
	 * �ץ���ʸ̮��ʸ���ִ����롣
	 * @param typedLinkNames ���դ����̾ X (String) ���顢
	 * �б����뷿�դ��ץ���ʸ̮��ʸ $p_X (SrcProcessContext) �ؤμ���
	 * <pre> p(s1,X,sn) �� p(s1,$p_X,sn)
	 * </pre>*/
	private void unabbreviateTypedLinks(LinkedList process, HashMap typedLinkNames) {
		Iterator it = process.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcLink) {
						SrcLink srcLink = (SrcLink)subobj;
						String name = srcLink.getName();
						if (typedLinkNames.containsKey(name)) {
							sAtom.getProcess().set(i, typedLinkNames.get(name));
						}
					}
				}
			}
			else if (obj instanceof SrcMembrane) {
				unabbreviateTypedLinks(((SrcMembrane)obj).getProcess(), typedLinkNames);
			}
		}
	}
	
	/** ���ȥ�Ÿ����Υץ�����¤�ʻҥ롼�볰�ˤΥ��ȥ�����˽и����뷿�դ��ץ���ʸ̮��Ÿ�����롣
	 * <pre> p(s1,$p,sn) �� p(s1,X,sn), $p[X]
	 * </pre> */
	private void expandTypedProcessContexts(LinkedList process, HashMap typedNames) {
		Iterator it = process.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof SrcAtom) {
				SrcAtom sAtom = (SrcAtom)obj;
				for (int i = 0; i < sAtom.getProcess().size(); i++) {
					Object subobj = sAtom.getProcess().get(i);
					if (subobj instanceof SrcProcessContext) {
						SrcProcessContext srcProcessContext = (SrcProcessContext)subobj;
						String name = srcProcessContext.getName();
						if (typedNames.containsKey(name)) {							
							String newlinkname = generateNewLinkName();
							sAtom.getProcess().set(i, new SrcLink(newlinkname));
							((SrcAtom)obj).process.add(new SrcLink(newlinkname));
							process.add(srcProcessContext);
							srcProcessContext.args.add(new SrcLink(newlinkname));
							sAtom.getProcess().set(i, typedNames.get(name));
						}
					}
				}
			}
			else if (obj instanceof SrcMembrane) {
				expandTypedProcessContexts(((SrcMembrane)obj).getProcess(), typedNames);
			}
		}
	}
	
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
						System.out.println("Illegal object in guard atom argument: " + obj);
					}
				}
			}
			else {
				System.out.println("Illegal object in guard: " + obj);
				it.remove();
			}
		}
	}
	
	private void correctHead(Membrane mem) {}
	private void correctBody(Membrane mem) {}

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
						System.out.println("Illegal object in an atom argument: " + obj);
					}
				}
			}
			else if (obj instanceof SrcMembrane) {
				correctWorld(((SrcMembrane)obj).getProcess());
			}
			else if (obj instanceof SrcRule) {}
			else {
				System.out.println("Illegal object outside a rule: " + obj);
				it.remove();
			}
		}
	}
}

// TODO ( {p($t)} :- ground($t) | end ) �򥳥�ѥ��뤹�뤿�������̿�᤬­��ʤ�
