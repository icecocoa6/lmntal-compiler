package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ���󥿡��ץ꥿���Ρ�̿����μ���˴ؤ��� switch ʸ����ʬ���ɤ߹��ߡ�
 * Translator �Ѥ� switch ʸ���������롣
 * �Ȥ��� : java util.TranslatorGenerator < InterpretedRuleset.java > hoge.java
 * 
 * ��ư�����塢�ʲ��κ�Ȥ��ư�ǹԤ�ɬ�פ����롣
 * 
 * FINDATOM
 * ANYMEM
 * LOCKMEM
 * LOCK
 *   �Ƶ��ƤӽФ������ѹ�
 *   break; ���ɲ�
 * DEREF
 * DEREFLINK
 * GETPARENT
 * ISGROUND
 * ISSTRING
 * GETCLASS
 * IDIV     IMOD
 * IDIVFUNC IMODFUNC
 *   ����ʸ��ifʸ����˰�ư
 *   GETCLASS�ϡ��������ѿ�������Τǥ֥�å��ǰϤ�
 *   DIV/MOD�Ρ�else�פϺ��
 * LOADMODULE
 * INLINE
 * CONNECTRUNTIME
 * GETRUNTIME
 * GROUP
 *   �����ȥ����ȡ�̤�б���
 * LOADRULESET
 * REACT
 * JUMP
 * ...
 * NOT
 *   ��ư�񤭴���
 * PROCEED
 *   return true; ���ɲ�
 * FINDATOM
 *   Atom a --> atom
 */
public class TranslatorGenerator {
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "eucJP"));
		System.setOut(new PrintStream(System.out, true, "eucJP"));
		
		while (true) {
			String line = reader.readLine();
			if (line == null)
				return;
			if (line.trim().equals("switch (inst.getKind()) {"))
				break;
		}
		Pattern piarg = Pattern.compile("inst\\.getIntArg[0-9]\\(\\)");
		Pattern pfarg = Pattern.compile("\\(Functor\\) *(inst\\.getArg[0-9]\\(\\))");
		Pattern plvar = Pattern.compile("(atoms|mems)\\[([^\\]]*)\\] *= ");
		Pattern patom = Pattern.compile("atoms\\[([^\\]]*)\\]");
		Pattern pmem  = Pattern.compile("mems\\[([^\\]]*)\\]");
		Pattern pvarget = Pattern.compile("vars\\.get\\(((\\(\\)|[^\\)])*)\\)");
		Pattern plink = Pattern.compile("\\.args\\[([^\\]]*)\\]");
		while (true) {
			String line = reader.readLine();
			if (line == null)
				break;
			String trimed = line.trim();
			if (trimed.startsWith("//")) {
				System.out.println(line);
			} else if (trimed.startsWith("case")) {
				System.out.println(line);
			} else if (trimed.startsWith("break;")) {
				System.out.println(line);
			} else if (trimed.startsWith("default:")) {
				break;
			} else if (trimed.equals("if (interpret(insts, pc))")) {
				String line2 = reader.readLine();
				if (line2.trim().equals("return true;")) {
					System.out.println("\t\t\t\t\ttranslate(it, tabs, iteratorNo, varnum);"); //��Ǽ�ư�ǽ���
				} else {
					//��Ǽ�ư�ǽ���
					System.out.println("**" + line);
					System.out.println("**" + line2);
				}
			} else if (trimed.startsWith("return false;")) {
			} else if (!trimed.equals("")) {
				String data = line;
				if (data.startsWith("\t\t\t\t\t")) {
					data = data.substring(5);
				}
				data = data.replaceAll("\"", "\\\\\""); // ��"�� -> ��\"��
				data = replace(piarg, data, 0, "\" + ", " + \"");
				data = replace(pfarg, data, 1, "\" + getFuncVarName((Functor)", ") + \"");
				data = replace(plvar, data, 2, "var", " = ");
				data = replace(patom, data, 1, "((Atom)var", ")");
				data = replace(pmem , data, 1, "((AbstractMembrane)var", ")");
				data = replace(pvarget, data, 1, "var", "");
				data = replace(plink, data, 1, ".getArg(", ")");
				data = data.replaceAll("\\.mems\\.iterator\\(\\)", ".memIterator()");
				data = data.replaceAll("\\.mems\\.size\\(\\)", ".getMemCount()");
				data = data.replaceAll("\\.mem\\.", ".getMem().");
				data = data.replaceAll("\\.mem;", ".getMem();");
				data = data.replaceAll("\\.mem\\)", ".getMem())");
				data = data.replaceAll("\\.parent", ".getParent()");
				data = data.replaceAll("\\.atoms\\.iteratorOfFunctor\\(", ".atomIteratorOfFunctor(");
				data = data.replaceAll("\\.atoms\\.getAtomCountOfFunctor\\(", ".getAtomCountOfFunctor(");
				data = data.replaceAll("\\.atoms\\.getNormalAtomCount\\(", ".getAtomCount(");
				data = data.replaceAll("^it = ", "Iterator it\" + iteratorNo + \" = ");
				data = data.replaceAll("it.hasNext\\(\\)", "it\" + iteratorNo + \".hasNext()");
				data = data.replaceAll("it.next\\(\\)", "it\" + iteratorNo + \".next()");
				
				if (data.trim().startsWith("vars.set(")) {
					trimed = data.trim();
					int pos = trimed.indexOf(',');
					int pos2 = trimed.lastIndexOf(')');
					System.out.println("\t\t\t\t\twriter.write(tabs + \"var" + trimed.substring(9, pos)
							+ " = " + trimed.substring(pos+1, pos2) + ";\\n\");");
					continue;
				}
				if (data.startsWith("if (") || data.startsWith("if(")) {
					int pos = data.indexOf('(') + 1;
					if (data.trim().endsWith(")")) {
						String line2 = reader.readLine();
						if (line2.trim().equals("return false;")) {
							data = "if (!(" + data.substring(pos, data.lastIndexOf(')')) + ")) {";
							System.out.println("\t\t\t\t\twriter.write(tabs + \"" + data + "\\n\");");
							System.out.println("\t\t\t\t\ttranslate(it, tabs + \"\t\", iteratorNo, varnum);");
							System.out.println("\t\t\t\t\twriter.write(tabs + \"}\\n\");");
						} else {
							//��Ǽ�ư�ǽ���
							System.out.println("**" + line);
							System.out.println("**" + line2);
						}
						continue;
					} else if (data.endsWith("return false;")) {
						int pos2 = data.lastIndexOf(')');
						data = "if (!(" + data.substring(pos, pos2) + ")) {";
						System.out.println("\t\t\t\t\twriter.write(tabs + \"" + data + "\\n\");");
						System.out.println("\t\t\t\t\ttranslate(it, tabs + \"\t\", iteratorNo, varnum);");
						System.out.println("\t\t\t\t\twriter.write(tabs + \"}\\n\");");
						continue;
					}
				}
				System.out.println("\t\t\t\t\twriter.write(tabs + \"" + data + "\\n\");");
			}
		}
	}
	private static String replace(Pattern p, String data, int group, String prefix, String suffix) {
		StringBuffer buf = new StringBuffer();
		Matcher m = p.matcher(data);
		while (m.find()) {
			m.appendReplacement(buf, prefix + m.group(group) + suffix);
		}
		m.appendTail(buf);
		return buf.toString();
	}
}
