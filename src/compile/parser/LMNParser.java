/**
 * LMNtal Parser �ᥤ�󥯥饹
 * ���ĤΥ����������ɤϥꥹ�ȤȤ���ɽ������ޤ���
 * �ꥹ�Ȥγƥ��֥������Ȥϡ�Src*�Υ��֥������ȤǤ���
 */

package compile.parser;

import java_cup.runtime.Scanner;
import java.io.Reader;
import java.util.LinkedList;
import compile.structure.*;

public class LMNParser {
	
	private Scanner lex = null;
	
	private static int nLinkNumber = 0;
	
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
		TODO: �夫��ץ饤�١��ȥ᥽�åɤ��ѹ�
		@return ���Ϥ��줿�����������ɤΥꥹ��
		@throws ParseException 
	*/
	public LinkedList parseSrc() throws ParseException {
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
	 * @return �������ե���������
	 * @throws ParseException
	 */
	public Membrane parse() throws ParseException {
		LinkedList src = parseSrc();
		Membrane mem = new Membrane(null);
		addProcessToMem(src, mem);
		return mem;
	}
	
	private void addProcessToMem(LinkedList list, Membrane mem) throws ParseException {
		for (int i=0;i<list.size();i++) {
			Object obj = list.get(i);
			addObjectToMem(obj,mem);
		}
		// TODO: ��󥯤�Ž���դ� �ץ�����������
	}
	
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
	
	private void addAtomToMem(SrcAtom sAtom, Membrane mem) throws ParseException {
		addAtomToMem(sAtom, mem, null);
	}
	
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
				addAtomToMem((SrcAtom)obj, mem, link);
				addLinkToAtom(link, atom, i);
			}
			// ����¾
			else {
				throw new ParseException("Unknown Object to add Link:"+obj);
			}
		}
		mem.atoms.add(atom);
	}
	
	private SrcLink createNewLink() {
		nLinkNumber++;
		return new SrcLink("L_"+nLinkNumber);
	}
	
	private void addLinkToAtom(SrcLink link, Atom atom, int pos) throws ParseException {
		if (pos >= atom.args.length) throw new ParseException("Out of Atom args length:"+pos);
		atom.args[pos] = new LinkOccurrence(link.getName(), atom, pos);
	}

	private void addRuleToMem(SrcRule sRule, Membrane mem) throws ParseException {
		RuleStructure rule = new RuleStructure();
		addProcessToMem(sRule.getHead(), rule.leftMem);
		addProcessToMem(sRule.getBody(), rule.rightMem);
		mem.rules.add(rule);
	}

	private void addProcessContextToMem(SrcProcessContext sProc, Membrane mem) {
		ProcessContext p = new ProcessContext(sProc.getName());
		mem.processContexts.add(p);
	}
	
	private void addRuleContextToMem(SrcRuleContext sRule, Membrane mem) {
		RuleContext p = new RuleContext(sRule.getName());
		mem.ruleContexts.add(p);
	}
	
	private void addLinkUnifyToMem(SrcLinkUnify sUnify, Membrane mem) throws ParseException {
		LinkUnify unify = new LinkUnify(mem);
		addLinkToAtom((SrcLink)sUnify.getProcess().get(0), unify, 0);
		addLinkToAtom((SrcLink)sUnify.getProcess().get(1), unify, 1);
	}
}