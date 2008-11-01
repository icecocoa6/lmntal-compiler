package compile.parser.intermediate;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import runtime.Env;
import runtime.Inline;
import runtime.InlineUnit;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.InterpretedRuleset;
import runtime.Rule;
import runtime.Ruleset;

import compile.Module;
import compile.parser.ParseException;
import compile.structure.Membrane;

public class RulesetParser {
	/** 
	 * ���̿������ɤ߹��ߡ��ɤ߹�����ǽ�Υ롼�륻�åȡʽ���ǡ��������ѥ롼�륻�åȡˤ��֤���
	 * @param reader ���̿������ɤ߹��� Reader
	 * @throws ParseException �ɤ߹��ߤ˼��Ԥ�����硣I/O ���顼��ޤ�
	 */
	public static Ruleset parse(Reader reader) throws ParseException {
		Lexer lexer = new Lexer(reader);
		parser parser = new parser(lexer);
		List<InterpretedRuleset> rulesets;
		List modules;
		List<String> inlines;	

		try {
			Object[] t = (Object[])parser.parse().value;
			rulesets = (ArrayList)t[0];
			modules = (ArrayList)t[1];
			inlines = (ArrayList)t[2];
		} catch (IOException e) {
			Env.error("ERROR: failed to read input data.");
			throw new ParseException();
		} catch (Exception e) {
			Env.error("ERROR: " + e.getMessage());
			throw new ParseException();
		}

		// id -> ���ΤΥޥå�����
		HashMap<Integer, InterpretedRuleset> rulesetMap = new HashMap<Integer, InterpretedRuleset>();
		for(InterpretedRuleset rs : rulesets){
			rulesetMap.put(rs.getId(), rs);
		}

		// RulesetRef �򡢼ºݤΥ롼�륻�åȤ��֤�������
		for(InterpretedRuleset rs : rulesets){
			for(Rule rule : rs.rules){
				updateRef(rule.atomMatch, rulesetMap, rule.guardLabel, rule.bodyLabel);
				updateRef(rule.memMatch, rulesetMap, rule.guardLabel, rule.bodyLabel);
				updateRef(rule.guard, rulesetMap, rule.guardLabel, rule.bodyLabel);
				updateRef(rule.body, rulesetMap, rule.guardLabel, rule.bodyLabel);
			}
		}

		// �⥸�塼��ν��� //���㥹�ȥ��顼�����������ʤ�Ǥ��������� 2008.6.27 iwasawa
		Iterator it = modules.iterator();
		while (it.hasNext()) {
			Membrane mem = (Membrane)it.next();
			ListIterator lit = mem.rulesets.listIterator();
			while (lit.hasNext()) {
				Integer id = (Integer)lit.next();
				lit.set(rulesetMap.get(id));
			}
			Module.regMemName(mem.name, mem);
		}

		// ����饤��ν���
		for(String name : inlines){
			InlineUnit iu = new InlineUnit(name);
			iu.attach();
			Inline.inlineSet.put(name, iu);
		}

		return rulesets.get(0);
	}

	private static void updateRef(List<Instruction> insts, Map<Integer, InterpretedRuleset> map, InstructionList guard, InstructionList body) {
		if (insts == null) return;
		Integer guardLabel, bodyLabel;
		guardLabel = (guard == null) ? null : Integer.valueOf(guard.label.substring(1));
		bodyLabel = (body == null) ? null : Integer.valueOf(body.label.substring(1));

		for(Instruction inst : insts){
			for (int i = 0; i < inst.data.size(); i++) {
				Object data = inst.data.get(i);
				if (data instanceof RulesetRef) {
					inst.data.set(i, map.get(((RulesetRef)data).getId()));
				} else if (data instanceof LabelRef) {
					Integer id = ((LabelRef)data).getId();
					if (id.equals(guardLabel)) {
						inst.data.set(i, guard);
					} else if (id.equals(bodyLabel)) {
						inst.data.set(i, body);
					} else {
						Env.error("ERROR : invalid label L" + id);
					}
				} else if (data instanceof InstructionList) {
					updateRef(((InstructionList)data).insts, map, guard, body);
				}
			}
		}
	}
}
