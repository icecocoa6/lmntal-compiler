package compile.parser.intermediate;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import runtime.Env;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.InterpretedRuleset;
import runtime.Rule;
import runtime.Ruleset;

import compile.parser.ParseException;

public class RulesetParser {
	/** 
	 * ���̿������ɤ߹��ߡ��ɤ߹�����ǽ�Υ롼�륻�åȡʽ���ǡ��������ѥ롼�륻�åȡˤ��֤���
	 * @param reader ���̿������ɤ߹��� Reader
	 * @throws ParseException �ɤ߹��ߤ˼��Ԥ�����硣I/O ���顼��ޤ�
	 */
	public static Ruleset parse(Reader reader) throws ParseException {
		Lexer lexer = new Lexer(reader);
		parser parser = new parser(lexer);
		ArrayList list;
		
		try {
			list = (ArrayList)parser.parse().value;
		} catch (IOException e) {
			Env.error("ERROR: failed to read input data.");
			throw new ParseException();
		} catch (Exception e) {
			Env.error("ERROR: " + e.getMessage());
			throw new ParseException();
		}

		// id -> ���ΤΥޥå�����
		HashMap rulesetMap = new HashMap();
		Iterator rsIt = list.iterator();
		while (rsIt.hasNext()) {
			InterpretedRuleset rs = (InterpretedRuleset)rsIt.next();
			rulesetMap.put(new Integer(rs.getId()), rs);
		}
		
		// RulesetRef �򡢼ºݤΥ롼�륻�åȤ��֤�������
		rsIt = list.iterator();
		while (rsIt.hasNext()) {
			InterpretedRuleset rs = (InterpretedRuleset)rsIt.next();
			Iterator ruleIt = rs.rules.iterator();
			while (ruleIt.hasNext()) {
				Rule rule = (Rule)ruleIt.next();
				updateRef(rule.atomMatch, rulesetMap, rule.guardLabel, rule.bodyLabel);
				updateRef(rule.memMatch, rulesetMap, rule.guardLabel, rule.bodyLabel);
				updateRef(rule.guard, rulesetMap, rule.guardLabel, rule.bodyLabel);
				updateRef(rule.body, rulesetMap, rule.guardLabel, rule.bodyLabel);
			}
		}
		
		return (Ruleset)list.get(0);
	}
	
	private static void updateRef(List insts, Map map, InstructionList guard, InstructionList body) {
		Integer guardLabel, bodyLabel;
		guardLabel = (guard == null) ? null : Integer.valueOf(guard.label.substring(1));
		bodyLabel = (body == null) ? null : Integer.valueOf(body.label.substring(1));

		Iterator it = insts.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
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
