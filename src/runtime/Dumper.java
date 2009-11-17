package runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


final class Unlexer {
	private StringBuffer buf = new StringBuffer();

	private String last = " ";

	public void append(String text) {
		if (!last.matches(".*['\"\\[\\]|,(){} ]")
				&& Env.profile != Env.PROFILE_ALL
				&& !text.matches("['\"\\[\\]|,(){} ].*")
				&& last.matches(".*[0-9A-Za-z_]") == text
						.matches("[0-9A-Za-z_].*")) {
			// if (! (last.matches(".*=") && text.matches("\\$.*")) )
			buf.append(" ");
		}
		buf.append(text);
		last = text;
	}

	public String toString() {
		return buf.toString();
	}
}

public class Dumper {
	static HashMap<String,int[]> binops = new HashMap<String, int[]>();

	private static final int xfy = 0;

	private static final int yfx = 1;

	private static final int xfx = 2;
	static {
		binops.put("^", new int[] { xfy, 200 });
		binops.put("**", new int[] { xfy, 300 });
		binops.put("mod", new int[] { xfx, 300 });
		binops.put("*", new int[] { yfx, 400 });
		binops.put("/", new int[] { yfx, 400 });
		binops.put("*.", new int[] { yfx, 400 });
		binops.put("/.", new int[] { yfx, 400 });
		binops.put("+", new int[] { yfx, 500 });
		binops.put("-", new int[] { yfx, 500 });
		binops.put("+.", new int[] { yfx, 500 });
		binops.put("-.", new int[] { yfx, 500 });
		binops.put("=", new int[] { xfx, 700 });
		binops.put("==", new int[] { xfx, 700 });
		binops.put("=:=", new int[] { xfx, 700 });
		binops.put("=\\=", new int[] { xfx, 700 });
		binops.put(">", new int[] { xfx, 700 });
		binops.put(">=", new int[] { xfx, 700 });
		binops.put("<", new int[] { xfx, 700 });
		binops.put("=<", new int[] { xfx, 700 });
		binops.put("=:=.", new int[] { xfx, 700 });
		binops.put("=\\=.", new int[] { xfx, 700 });
		binops.put(">.", new int[] { xfx, 700 });
		binops.put(">=.", new int[] { xfx, 700 });
		binops.put("<.", new int[] { xfx, 700 });
		binops.put("=<.", new int[] { xfx, 700 });
		binops.put(":", new int[] { xfy, 800 }); // ������ : �κ��դ�
		// [a-z][A-Za-z0-9_]* �ΤȤ��Τ�
	}

	static boolean isInfixOperator(String name) {
		return binops.containsKey(name);
	}

	static int getBinopType(String name) {
		return ((int[]) binops.get(name))[0];
	}

	static int getBinopPrio(String name) {
		return ((int[]) binops.get(name))[1];
	}
	
	static String PROFILE_TABS  = "RuleName,\tThreadID,\tAtomDrivenTime,\tMembraneDrivenTime,\t" + 
									"AtomDrivenSucceed,\tMembraneDrivenSucceed,\t" +
									"AtomDrivenApply,\tMembraneDrivenApply,\t" +
									"BackTracks,\tLockFailures\n";

	/** �����Ȥ���Ϥ��롣���Ϸ����λ���Ϥޤ��Ǥ��ʤ��� */
	public static String dump(Membrane mem) {
		return dump(mem, true);
	}

	public static String dump(Membrane mem, boolean doLock) {
		boolean locked = false;
		if (doLock) {
			if (mem.getLockThread() != Thread.currentThread()) {
				if (!mem.lock()) {
					return "...";
				}
				locked = true;
			}
		}

		Unlexer buf = new Unlexer();
		Unlexer pbuf = new Unlexer();
		
		boolean commaFlag = false;

		// #1 - ���ȥ�ν���

		// Set atoms = new HashSet(mem.getAtomCount());
		Set<Atom> atoms = new HashSet<Atom>(mem.atoms.size()); // ����proxy��ɽ�����Ƥ��뤿�ᡣ���������᤹

		Iterator<Atom> it_a = mem.atomIterator();
		while (it_a.hasNext()) {
			Atom a = it_a.next();
			if (Env.hideProxy && !a.isVisible()) {
				// PROXY��ɽ�������ʤ� 2005/02/03 T.Nagata
				continue;
			}
			atoms.add(a);
		}
		if (Env.verbose >= Env.VERBOSE_EXPANDATOMS) {
			it_a = mem.atomIterator();
			while (it_a.hasNext()) {
				Atom a = it_a.next();
				if(!atoms.contains(a))continue;
				if (commaFlag)
					buf.append(", ");
				else
					commaFlag = true;
				buf.append(dumpAtomGroup(a, atoms, false));
			}
		} else {
			List predAtoms[] = { new LinkedList(), new LinkedList(),
					new LinkedList(), new LinkedList(), new LinkedList() };

			// �����ˤ��륢�ȥ�Ȥ���ͥ����:
			// 0. �����ʤ��Υ��ȥࡢ����Ӻǽ�������������ʳ��ؤΥ�󥯤Ǥ��륢�ȥ�
			// 1. ����٤� = �ʲ���2�����黻�ҤΥ��ȥ���׻��������inline���ȥ��
			// 2. �̾�Υ���ܥ�̾�ǥ���褬�ǽ�������1�������ȥ�
			// 3. �̾�Υ���ܥ�̾�Ǻǽ������Υ���褬�ǽ�������2�����ʾ�Υ��ȥ�
			// 4. ��3�����Υ���褬�ǽ�������cons���ȥ�

			// �̾�Ǥʤ����ȥ�̾�ʵ����ˤ��ʤ����ȥ��̾����:
			// - $in,$out,[],����,�¿�,�����A-Z�ǻϤޤ륢�ȥ�

			it_a = mem.atomIterator();
			while (it_a.hasNext()) {
				Atom a = it_a.next();
				if (a.getArity() == 0
						|| a.getLastArg().getAtom().getMem() != mem) {
					predAtoms[0].add(a);
				} else if (a.getArity() == 2 && isInfixOperator(a.getName())
						&& getBinopPrio(a.getName()) >= 700
						|| a.getName().startsWith("/*inline*/")) {
					predAtoms[1].add(a);
				} else if (a.getLastArg().isFuncRef()) {
					// todo �����ɤ������������ΤǤʤ�Ȥ����� (1)
					if (!a.getFunctor().isSymbol())
						continue; // �̾�Υե��󥯥������ˤ�����
					if (a.getName().matches("^[A-Z].*"))
						continue; // �䴰���줿��ͳ��󥯤ϰ������֤�����
					if (a.getFunctor().equals(Functor.INSIDE_PROXY))
						continue;
					if (a.getFunctor().isOutsideProxy())
						continue;
					if (a.getFunctor().equals(Functor.NIL))
						continue; // []��������Ʊ��ɽ��Ū�ʰ���
					if (a.getArity() == 1) {
						predAtoms[2].add(a);
					} else if (!a.getFunctor().equals(Functor.CONS)) {
						predAtoms[3].add(a);
					} else { // cons�ϤǤ�������ǡ����Ȥ��ư���
						predAtoms[4].add(a);
					}
				}
			}

			// predAtoms��Υ��ȥ�����˽���
			for (int phase = 0; phase < predAtoms.length; phase++) {
				it_a = predAtoms[phase].iterator();
				while (it_a.hasNext()) {
					Atom a = it_a.next();
					if (atoms.contains(a)) { // �ޤ����Ϥ���Ƥ��ʤ����
						if (commaFlag)
							buf.append(", ");
						else
							commaFlag = true;
						// 3�����黻�Ҥζ��� s=t ɽ���ϡ��黻��Ÿ��ɽ�����ʤ��Ȥ��Τ߹Ԥ�
						if (Env.verbose < Env.VERBOSE_EXPANDOPS) {
							// cons�ϱ黻�Ҥ�Ʊ��ɽ��Ū�ʰ���
							if (a.getFunctor().equals(Functor.CONS)
									|| (a.getArity() == 3 && isInfixOperator(a
											.getName()))) {
								buf
										.append(dumpLink(a.getLastArg(), atoms,
												700));
								buf.append("=");
								buf.append(dumpAtomGroupWithoutLastArg(a,
										atoms, 700, false));
								continue;
							}
						}
						buf.append(dumpAtomGroup(a, atoms, false));
					}
				}
			}

			// todo ����changed�롼�פ�predAtoms�����礹��

			// ��ϩ��������ˤϤޤ��ĤäƤ���Τǡ�Ŭ���ʽ꤫����ϡ�
			// ��ϩ����ʬ��õ�����������������Ȥꤢ�������Τޤޡ�
			boolean changed;
			do {
				changed = false;
				it_a = atoms.iterator();
				while (it_a.hasNext()) {
					Atom a = it_a.next();
					// �黻��ɽ���Ǥ���Ȥ��ϡ��ǡ������Ǥ���������������褦�ˤ���
					if (Env.verbose < Env.VERBOSE_EXPANDOPS) {
						// todo �����ɤ������������ΤǤʤ�Ȥ����� (2)
						if (!a.getFunctor().isSymbol())
							continue;
						if (a.getName().matches("^[A-Z].*"))
							continue;
						if (a.getFunctor().equals(Functor.INSIDE_PROXY))
							continue;
						if (a.getFunctor().isOutsideProxy())
							continue;
						if (a.getFunctor().equals(Functor.NIL))
							continue;
					}
					// �ץ������ά�Ǥ���Ȥ��ϡ��ץ������Ǥ���������������褦�ˤ���
					if (Env.hideProxy/*Env.verbose < Env.VERBOSE_EXPANDPROXIES*/) {
						if (a.getFunctor().isInsideProxy())
							continue;
						if (a.getFunctor().isOutsideProxy())
							continue;
					}
					// �����ޤǻĤä�1�����Υ��ȥ�ϥǡ����β�ǽ�����⤤�Τǡ��Ǥ���������������褦�ˤ���
					if (a.getArity() == 1)
						continue;
					//
					if (commaFlag)
						buf.append(", ");
					else
						commaFlag = true;
					buf.append(dumpAtomGroup(a, atoms, false));
					changed = true;
					break;
				}
			} while (changed);

			// �Ĥä�1�����Υ��ȥ�ʥǡ������Ȼפä���α���Ƥ��������ʤɡˤ����ˤ��ƽ��Ϥ��롣
			// ����������褬��ͳ��󥯴������ȥ�ΤȤ��˸¤�
			do {
				changed = false;
				it_a = atoms.iterator();
				while (it_a.hasNext()) {
					Atom a = it_a.next();
					if (a.getArity() == 1) {
						if (a.getLastArg().getAtom().getFunctor() == Functor.INSIDE_PROXY
								|| a.getLastArg().getAtom().getFunctor()
										.isOutsideProxy()) {
							if (commaFlag)
								buf.append(", ");
							else
								commaFlag = true;
							buf.append(dumpAtomGroup(a, atoms, false));
							changed = true;
							break;
						}
					}
				}
			} while (changed);

			// �Ĥä����ȥ�� s=t �η����ǽ��Ϥ��롣
			while (!atoms.isEmpty()) {
				it_a = atoms.iterator();
				Atom a = it_a.next();
				if (commaFlag)
					buf.append(", ");
				else
					commaFlag = true;
				buf.append(dumpAtomGroupWithoutLastArg(a, atoms, 700, false));
				buf.append("=");
				buf.append(dumpAtomGroupWithoutLastArg(
						a.getLastArg().getAtom(), atoms, 700, false));
			}
		}

		// #2 - ����ν���
		Iterator<Membrane> it_m = mem.memIterator();
		while (it_m.hasNext()) {
			Membrane m = it_m.next();
			if (commaFlag) {
				buf.append(", ");
				if (Env.getExtendedOption("dump").equals("1"))
					buf.append("\n");
			} else
				commaFlag = true;
			if (Env.getExtendedOption("dump").equals("1"))
				Env.indent++;
			if (Env.getExtendedOption("dump").equals("1"))
				for (int k = 0; k < Env.indent; k++)
					buf.append("  ");
			if (m.name != null)
				buf.append(m.name /*+ ":"*/); // ��̾�ι�ʸ��':'��Ϥ��ޤʤ����Ȥˤ��� by kudo (2006/06/26)
			buf.append("{");
			buf.append(dump(m));
			if(Env.profile == Env.PROFILE_ALL)
				pbuf.append(dump(m));
			buf.append("}");
			if (m.kind == 1)
				buf.append("_");
			else if (m.kind == Membrane.KIND_ND)
				buf.append("*");
			if (Env.getExtendedOption("dump").equals("1"))
				Env.indent--;
		}

		Iterator<Ruleset> it_r;
		// #3 - �롼��ν���
		if(Env.showruleset){
			it_r = mem.rulesetIterator();
			while (it_r.hasNext()) {
				if (commaFlag)
					buf.append(", ");
				else
					commaFlag = true;
					buf.append(it_r.next().toString());
			}
		}
		if(Env.showrule){
			it_r = mem.rulesetIterator();
			while (it_r.hasNext()) {
				Ruleset rs = it_r.next();
				List<Rule> rules;
				if(rs instanceof InterpretedRuleset)
					rules = ((InterpretedRuleset)rs).rules;
				else
					rules = rs.compiledRules;
				if(rules != null){
					Iterator<Rule> it2 = rules.iterator();
					while (it2.hasNext()) {
						Rule r = it2.next();
						if (r.name != null) {
							if (commaFlag)
								buf.append(", ");
							else
								commaFlag = true;
							buf.append("@" + r.toString() + "@");
							if (Env.profile == Env.PROFILE_BYRULE) {
								long times = (Env.majorVersion == 1 && Env.minorVersion > 4) ? (r.atomtime+r.memtime)/1000000 : r.atomtime+r.memtime;
								buf.append("_" + (r.atomsucceed+r.memsucceed) + "/" + (r.atomapply+r.memapply) + "(" + times + "msec)");
							} else if (Env.profile == Env.PROFILE_BYRULEDETAIL) {
								long times = (Env.majorVersion == 1 && Env.minorVersion > 4) ? (r.atomtime+r.memtime)/1000000 : r.atomtime+r.memtime;
								buf.append("_" + (r.atomsucceed+r.memsucceed) + "/" + (r.atomapply+r.memapply) +
										"(" + r.backtracks + "," + r.lockfailure + ")" + "(" + times + "msec)");
							} else if (Env.profile == Env.PROFILE_ALL) {
								Iterator<Benchmark> its = r.bench.values().iterator();
								while(its.hasNext()) {
									Benchmark b = its.next();
									long atomtimes = (Env.majorVersion == 1 && Env.minorVersion > 4) ? b.atomtime/1000000 : b.atomtime;
									long memtimes = (Env.majorVersion == 1 && Env.minorVersion > 4) ? b.memtime/1000000 : b.memtime;
									pbuf.append(r.toString() + ",\t" + b.threadid + ",\t" + 
											atomtimes + ",\t" + memtimes + ",\t" + 
											b.atomsucceed + ",\t" + b.memsucceed + ",\t" +
											b.atomapply + ",\t" + b.memapply + ",\t" +
											b.backtracks + ",\t" + b.lockfailure + "\n");								
								}
							}
						}
					}					
				}
			}
		}

		if (locked) {
			mem.quietUnlock();
		}

		if(Env.profile != Env.PROFILE_ALL)
			return buf.toString();
		else
			return pbuf.toString();
	}

	/**
	 * @param AbstractMembrane
	 *            mem
	 * @param boolean
	 *            doLock
	 * @param int
	 *            mode
	 * @return String ��Υ���ѥ����ǽ��ʸ����ɽ��
	 */
	public static String encode(Membrane mem, boolean doLock, int mode) {
		boolean locked = false;
		if (doLock) {
			if (mem.getLockThread() != Thread.currentThread()) {
				if (!mem.lock()) {
					return "";
				}
				locked = true;
			}
		}

		Unlexer buf = new Unlexer();
		boolean commaFlag = false;

		if(mode==0 | mode==2) {
		// #1 - ���ȥ�ν���

			// proxy��ɽ�����ʤ��ΤǤ���ǽ�ʬ
			Set<Atom> atoms = new HashSet<Atom>(mem.getAtomCount());

			Iterator<Atom> it_a = mem.atomIterator();
			while (it_a.hasNext()) {
				Atom a = it_a.next();
				if (!a.isVisible()) {
					// PROXY��ɽ�������ʤ�
					continue;
				}
				atoms.add(a);
			}

			List predAtoms[] = { new LinkedList(), new LinkedList(),
					new LinkedList(), new LinkedList(), new LinkedList() };

			// �����ˤ��륢�ȥ�Ȥ���ͥ����:
			// 0. �����ʤ��Υ��ȥࡢ����Ӻǽ�������������ʳ��ؤΥ�󥯤Ǥ��륢�ȥ�
			// 1. ����٤� = �ʲ���2�����黻�ҤΥ��ȥ���׻��������inline���ȥ��
			// 2. �̾�Υ���ܥ�̾�ǥ���褬�ǽ�������1�������ȥ�
			// 3. �̾�Υ���ܥ�̾�Ǻǽ������Υ���褬�ǽ�������2�����ʾ�Υ��ȥ�
			// 4. ��3�����Υ���褬�ǽ�������cons���ȥ�

			// �̾�Ǥʤ����ȥ�̾�ʵ����ˤ��ʤ����ȥ��̾����:
			// - $in,$out,[],����,�¿�,�����A-Z�ǻϤޤ륢�ȥ�

			it_a = mem.atomIterator();
			while (it_a.hasNext()) {
				Atom a = it_a.next();
				if (a.getArity() == 0
						|| a.getLastArg().getAtom().getMem() != mem) {
					predAtoms[0].add(a);
				} else if (a.getArity() == 2 && isInfixOperator(a.getName())
						&& getBinopPrio(a.getName()) >= 700
						|| a.getName().startsWith("/*inline*/")) {
					predAtoms[1].add(a);
				} else if (a.getLastArg().isFuncRef()) {
					// todo �����ɤ������������ΤǤʤ�Ȥ����� (1)
					if (!a.getFunctor().isSymbol())
						continue; // �̾�Υե��󥯥������ˤ�����
					if (a.getName().matches("^[A-Z].*"))
						continue; // �䴰���줿��ͳ��󥯤ϰ������֤�����
					if (a.getFunctor().equals(Functor.INSIDE_PROXY))
						continue;
					if (a.getFunctor().isOutsideProxy())
						continue;
					if (a.getFunctor().equals(Functor.NIL))
						continue; // []��������Ʊ��ɽ��Ū�ʰ���
					if (a.getArity() == 1) {
						predAtoms[2].add(a);
					} else if (!a.getFunctor().equals(Functor.CONS)) {
						predAtoms[3].add(a);
					} else { // cons�ϤǤ�������ǡ����Ȥ��ư���
						predAtoms[4].add(a);
					}
				}
			}

			// predAtoms��Υ��ȥ�����˽���
			for (int phase = 0; phase < predAtoms.length; phase++) {
				it_a = predAtoms[phase].iterator();
				while (it_a.hasNext()) {
					Atom a = it_a.next();
					if (atoms.contains(a)) { // �ޤ����Ϥ���Ƥ��ʤ����
						if (commaFlag)
							buf.append(", ");
						else
							commaFlag = true;
						// 3�����黻�Ҥζ��� s=t ɽ���ϡ��黻��Ÿ��ɽ�����ʤ��Ȥ��Τ߹Ԥ�
						// cons�ϱ黻�Ҥ�Ʊ��ɽ��Ū�ʰ���
						if (a.getFunctor().equals(Functor.CONS)
								|| (a.getArity() == 3 && isInfixOperator(a
										.getName()))) {
							buf.append(dumpLink(a.getLastArg(), atoms, 700));
							buf.append("=");
							buf.append(dumpAtomGroupWithoutLastArg(a, atoms,
									700, true));
							continue;
						}
						buf.append(dumpAtomGroup(a, atoms, true));
					}
				}
			}

			// todo ����changed�롼�פ�predAtoms�����礹��

			// ��ϩ��������ˤϤޤ��ĤäƤ���Τǡ�Ŭ���ʽ꤫����ϡ�
			// ��ϩ����ʬ��õ�����������������Ȥꤢ�������Τޤޡ�
			boolean changed;
			do {
				changed = false;
				it_a = atoms.iterator();
				while (it_a.hasNext()) {
					Atom a = it_a.next();
					// todo �����ɤ������������ΤǤʤ�Ȥ����� (2)
					if (!a.getFunctor().isSymbol())
						continue;
					if (a.getName().matches("^[A-Z].*"))
						continue;
					if (a.getFunctor().equals(Functor.INSIDE_PROXY))
						continue;
					if (a.getFunctor().isOutsideProxy())
						continue;
					if (a.getFunctor().equals(Functor.NIL))
						continue;
					// �����ޤǻĤä�1�����Υ��ȥ�ϥǡ����β�ǽ�����⤤�Τǡ��Ǥ���������������褦�ˤ���
					if (a.getArity() == 1)
						continue;
					if (commaFlag)
						buf.append(", ");
					else
						commaFlag = true;
					buf.append(dumpAtomGroup(a, atoms, true));
					changed = true;
					break;
				}
			} while (changed);

			// �Ĥä�1�����Υ��ȥ�ʥǡ������Ȼפä���α���Ƥ��������ʤɡˤ����ˤ��ƽ��Ϥ��롣
			// ����������褬��ͳ��󥯴������ȥ�ΤȤ��˸¤�
			do {
				changed = false;
				it_a = atoms.iterator();
				while (it_a.hasNext()) {
					Atom a = it_a.next();
					if (a.getArity() == 1) {
						if (a.getLastArg().getAtom().getFunctor() == Functor.INSIDE_PROXY
								|| a.getLastArg().getAtom().getFunctor()
										.isOutsideProxy()) {
							if (commaFlag)
								buf.append(", ");
							else
								commaFlag = true;
							buf.append(dumpAtomGroup(a, atoms, true));
							changed = true;
							break;
						}
					}
				}
			} while (changed);

			// �Ĥä����ȥ�� s=t �η����ǽ��Ϥ��롣
			while (!atoms.isEmpty()) {
				it_a = atoms.iterator();
				Atom a = it_a.next();
				if (commaFlag)
					buf.append(", ");
				else
					commaFlag = true;
				buf.append(dumpAtomGroupWithoutLastArg(a, atoms, 700, true));
				buf.append("=");
				buf.append(dumpAtomGroupWithoutLastArg(
						a.getLastArg().getAtom(), atoms, 700, true));
			}
		}

		// #2 ����ν��� 
			Iterator<Membrane> it_m = mem.memIterator();
			while (it_m.hasNext()) {
				Membrane m = it_m.next();
				if (commaFlag) {
					buf.append(", ");
				} else
					commaFlag = true;
				if (m.name != null)
					buf.append(m.name);
				buf.append("{");
				buf.append(encode(m, true, mode));
				buf.append("}");
				if (m.kind == 1)
					buf.append("_");
			}

		if(mode <= 1) {
		// #3 �롼��ν���
			Iterator<Ruleset> it_r = mem.rulesetIterator();
			while (it_r.hasNext()) {
				if (commaFlag)
					buf.append(",");
				else
					commaFlag = true;
				// Translator �����������ե������encode()�᥽�åɤ�ƤӽФ�
				buf.append(it_r.next().encode());
			}
		}

		if (locked) {
			mem.quietUnlock();
		}

		return buf.toString();
	}

	private static String dumpAtomGroup(Atom a, Set atoms, boolean fully) {
		return dumpAtomGroup(a, atoms, 0, 999, fully);
	}

	private static String dumpAtomGroupWithoutLastArg(Atom a, Set atoms,
			int outerprio, boolean fully) {
		return dumpAtomGroup(a, atoms, 1, outerprio, fully);
	}

	/**
	 * ���ȥ�ΰ�����Ÿ�����ʤ���ʸ������Ѵ����롣 �����������ȥ�a�κǸ��reducedArgCount�Ĥΰ����Ͻ��Ϥ��ʤ���
	 * <p>
	 * ���Ϥ������ȥ��atoms��������롣 ���Ϥ��륢�ȥ��atoms�����ǤǤʤ���Фʤ�ʤ���
	 * 
	 * @param a
	 *            ���Ϥ��륢�ȥ�
	 * @param atoms
	 *            �ޤ����Ϥ��Ƥ��ʤ����ȥ�ν��� [in,out]
	 * @param reducedArgCount
	 *            a�Τ������Ϥ��ʤ��Ǹ�ΰ�����Ĺ��
	 * @param fully
	 *            true�ʤ�ե��󥯥�̾�䥢�ȥ�̾���ά���ʤ�
	 */
	private static String dumpAtomGroup(Atom a, Set atoms, int reducedArgCount,
			int outerprio, boolean fully) {
		atoms.remove(a);
		Functor func = a.getFunctor();
		int arity = func.getArity() - reducedArgCount;
		if (arity == 0) {
			if (!fully)
				return func.getQuotedAtomName();
			return func.getQuotedFullyAtomName();
		}
		if (Env.hideProxy //Env.verbose < Env.VERBOSE_EXPANDPROXIES
				&& arity == 1
				&& (func.isInsideProxy() || func.isOutsideProxy())) {
			return dumpLink(a.args[0], atoms, outerprio);
		}
		Unlexer buf = new Unlexer();
		if (Env.verbose < Env.VERBOSE_EXPANDOPS) {
			if (arity == 2 && isInfixOperator(func.getName())) {
				if (func.getName().equals(":")
						&& (a.args[0].getAtom().getArity() != 1 || !a.args[0]
								.getAtom().getName().matches(
										"[a-z][A-Za-z0-9_]*"))) {
				} else {
					int type = getBinopType(func.getName());
					int prio = getBinopPrio(func.getName());
					int innerleftprio = prio + (type == yfx ? 1 : 0);
					int innerrightprio = prio + (type == xfy ? 1 : 0);
					boolean needpar = (outerprio < innerleftprio || outerprio < innerrightprio);
					if (needpar)
						buf.append("(");
					buf.append(dumpLink(a.args[0], atoms, innerleftprio));
					buf.append(func.getName());
					buf.append(dumpLink(a.args[1], atoms, innerrightprio));
					if (needpar)
						buf.append(")");
					return buf.toString();
				}
			}
			if (arity == 2 && func.getName().equals(".")) {
				buf.append("[");
				buf.append(dumpLink(a.args[0], atoms, outerprio));
				buf.append(dumpListCdr(a.args[1], atoms));
				buf.append("]");
				return buf.toString();
			}
		}
		if (!fully)
			buf.append(func.getQuotedFunctorName());
		else
			buf.append(func.getQuotedFullyFunctorName());
		if (Env.verbose > Env.VERBOSE_SIMPLELINK || !func.getName().matches("[-\\+]")
				|| !(arity==1) || !(a.args[0].getAtom().getFunctor() instanceof SpecialFunctor) )
			buf.append("(");
		buf.append(dumpLink(a.args[0], atoms));
		for (int i = 1; i < arity; i++) {
			buf.append(",");
			buf.append(dumpLink(a.args[i], atoms));
		}
		if (Env.verbose > Env.VERBOSE_SIMPLELINK || !func.getName().matches("[-\\+]") 
				|| !(arity==1) || !(a.args[0].getAtom().getFunctor() instanceof SpecialFunctor) )
			buf.append(")");
		return buf.toString();
	}

	private static String dumpListCdr(Link l, Set atoms) {
		Unlexer buf = new Unlexer();
		while (true) {
			if (!(l.isFuncRef() && atoms.contains(l.getAtom())))
				break;
			Atom a = l.getAtom();
			if (!a.getFunctor().equals(Functor.CONS))
				break;
			atoms.remove(a);
			buf.append(",");
			buf.append(dumpLink(a.args[0], atoms));
			l = a.args[1];
		}
		if (l.getAtom().getFunctor().equals(Functor.NIL)) {
			atoms.remove(l.getAtom());
		} else {
			buf.append("|");
			buf.append(dumpLink(l, atoms));
		}
		return buf.toString();
	}

	private static String dumpLink(Link l, Set atoms) {
		return dumpLink(l, atoms, 999);
	}

	private static String dumpLink(Link l, Set atoms, int outerprio) {
		// PROXY��ɽ�����ʤ� 2005/02/03 T.Nagata
		if (Env.hideProxy && !l.getAtom().isVisible()) {
			Atom tmp_a = l.getAtom();
			Link tmp_l = l;
			while (!tmp_a.isVisible()) {
				tmp_l = tmp_a.args[tmp_l.getPos() == 0 ? 1 : 0];
				tmp_a = tmp_l.getAtom();
			}
			return l.toString().compareTo(tmp_l.getBuddy().toString()) >= 0 ? l
					.toString() : tmp_l.getBuddy().toString();
		}
		if (Env.verbose < Env.VERBOSE_EXPANDATOMS && l.isFuncRef()
				&& atoms.contains(l.getAtom())) {
			return dumpAtomGroupWithoutLastArg(l.getAtom(), atoms, outerprio,
					false);
		} else {
			return l.toString();
		}
	}
}
