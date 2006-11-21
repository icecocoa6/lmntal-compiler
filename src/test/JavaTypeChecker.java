package test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import runtime.Env;
import runtime.SymbolFunctor;

import compile.structure.Atom;
import compile.structure.LinkOccurrence;
import compile.structure.Membrane;
import compile.structure.RuleStructure;

public class JavaTypeChecker {
	public static boolean enabled = false;
	
	private static boolean updateJavaType(LinkOccurrence l, Class type) {
		if (l.type != null && l.buddy.type != null) return false;

		l.type = l.buddy.type = type;
//		System.out.println(l.atom+":"+l.type.getName());
		return true;
	}
	
	private static boolean checkJavaType(Membrane mem) {
		boolean updated = false;
		for (compile.structure.Atom a : mem.atoms) {
//			System.out.println(a);
			if (a.args.length == 0) continue;
			String path = a.functor.getPath();
			String name = a.functor.getName();
			
			if (path != null) {//�⥸�塼��̾�Ĥ����ȥ�̾
				String className = path.replaceAll("_", ".");
				LinkOccurrence l = a.args[a.args.length-1];
				try {
					Class c = Class.forName(className);
					if (name.equals("new")) {//���󥹥ȥ饯��
						updated = updateJavaType(l, c);
					} else {//static final ����β�ǽ��
						String finalName = name.toUpperCase();
						Field field = c.getField(finalName);
						Class type = field.getType();
						updated = updateJavaType(l, type);
					}
				} catch (ClassNotFoundException e) {
				} catch (NoSuchFieldException e) {	
				}
			} else if (a.functor instanceof SymbolFunctor) {
				if (name.equals("true") || name.equals("false"))
					updated = updateJavaType(a.args[0], Boolean.TYPE);
			} else {
				updated = updateJavaType(a.args[0], a.functor.getValue().getClass());
			}
			
			//'='�η���Ʊ�����Ǥ���
			if (a.getName().equals("=")) {
				if (a.args[0].type != null) {
					updated = updateJavaType(a.args[1], a.args[0].type);
				} else if (a.args[1].type != null) {
					updated = updateJavaType(a.args[0], a.args[1].type);
				}
			}

			//��1���������֥������Ȥʤ�ǽ������⥪�֥������ȤǤ���
			if (a.args[0].type != null) {
				LinkOccurrence l = a.args[a.args.length-1];
				updated = updateJavaType(l, a.args[0].type);
			}
		}
		for (Membrane m : mem.mems) {
			if (checkJavaType(m)) updated = true;
		}
		for (RuleStructure rs : mem.rules) {
			if (checkJavaType(rs.leftMem)) updated = true;
			if (checkJavaType(rs.rightMem)) updated = true;
		}
		return updated;
	}
	
	private static void dump(Membrane mem) {
		for (Atom a : mem.atoms)
			System.out.println(a);
		for (Membrane m : mem.mems)
			dump(m);
		for (RuleStructure rs : mem.rules) {
			dump(rs.leftMem);
			dump(rs.guardMem);
			dump(rs.rightMem);
		}
	}
	
	/**
	 * ��åѡ����饹������ܷ����Ѵ�����
	 * @param type
	 * @return
	 */
	private static Class toPrimitiveType(Class type) {
		if (type.equals(Integer.class)) return Integer.TYPE;
		if (type.equals(Double.class)) return Double.TYPE;
		return type;
	}
	
	/**
	 * Atom����᥽�åɤΰ���������������
	 * @param a
	 * @return
	 */
	private static Class[] makeJavaParams(Atom a) {
		List<Class> paramList = new ArrayList<Class>();
		
		for (int i = 1; i < a.getArity()-2; i++) {
			Class type = a.args[i].type;
			if (type == null) return null;
			paramList.add(toPrimitiveType(type));
		}
		//�ǽ������˷����ʤ��ä��顤���������ͤ�����������Ǥ���
		Class type = a.args[a.getArity()-2].type;
		if (type != null) paramList.add(toPrimitiveType(type));
		
		Class[] params = new Class[paramList.size()];
		return paramList.toArray(params);
	}
	
	private static boolean toSuperClass(Class[] params) {
		if (params == null) return false;
		
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null || params[i].equals(Object.class)) continue;
			params[i] = params[i].getSuperclass();
			return true;
		}
		return false;
	}
	
	public static boolean containsJavaTypeError2(Membrane mem) {
		boolean error = false;
		for (Atom a : mem.atoms) {
			String name = a.getName();
			if (name.equals("cp") || name.equals("gc")) continue;//�Ȥꤢ�����������뤳�Ȥǲ��
			if (a.getPath() != null) continue;//static�᥽�åɤϸ������ʤ�
			if (a.args.length <= 1) continue;
			Class type = a.args[0].type;
			if (type == null) continue;
			if (name.equals("new") || name.equals("=") || !a.functor.getClass().equals(SymbolFunctor.class)) continue;
			
			Class[] params = makeJavaParams(a);
			while (true) {
				try {
					type.getMethod(name, params);
					break;
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
				
				//�����ѡ����饹�ΰ�����
				if (!toSuperClass(params)) {
					System.out.println(Env.srcs.get(0)+":"+a.line+","+a.column+": "+type.getName()+"#"+name+" ��¸�ߤ��ޤ���");
					System.out.println("\t"+a);
					error = true;
					break;
				}
			}
		}
		for (Membrane m : mem.mems) {
			if (containsJavaTypeError2(m)) error = true;
		}
		for (RuleStructure rs : mem.rules) {
			if (containsJavaTypeError2(rs.leftMem)) error = true;
			if (containsJavaTypeError2(rs.rightMem)) error = true;
		}
		return error;
	}

	/**
	 * ���ꤵ�줿�줫����ꤵ�줿̾������ĥ�󥯤��֤�
	 * @param mem
	 * @param name
	 * @return
	 */
	private static LinkOccurrence findLink(Membrane mem, String name) {
		for (Atom a : mem.atoms) {
			for (LinkOccurrence link : a.args) {
				if (link.buddy.atom.getName().equals(name))
					return link;
			}
		}
		return null;
	}
	
	/**
	 * ���ꤵ�줿�줫����ꤵ�줿̾������ĥ�󥯤Υꥹ�Ȥ��֤�
	 * @param mem
	 * @param name
	 * @return
	 */
	private static List<LinkOccurrence> findLinks(Membrane mem, String name) {
		List<LinkOccurrence> links = new ArrayList<LinkOccurrence>();
		for (Atom a : mem.atoms) {
			for (LinkOccurrence link : a.args) {
				if (link.buddy.atom.getName().equals(name))
					links.add(link);
			}
		}
		for (Membrane m : mem.mems) {
			links.add(findLink(m, name));
		}
		for (RuleStructure rs : mem.rules) {
			for (Membrane m : rs.leftMem.mems)
				links.add(findLink(m, name));
			for (Membrane m : rs.rightMem.mems)
				links.add(findLink(m, name));
		}
		return links;
	}
	
	/**
	 * class���������󤫤鷿��Ĥ���
	 * @param mem
	 */
	private static boolean checkJavaTypeByClassGuard(Membrane mem) {
		boolean updated = false;
		for (RuleStructure rs : mem.rules) {
			for (Atom a : rs.guardMem.atoms) {
				if (a.getName().equals("class")) {
					LinkOccurrence headLink = findLink(rs.leftMem, a.args[0].buddy.atom.getName());
					LinkOccurrence link1 = findLink(rs.guardMem, a.args[1].buddy.atom.getName());
					List<LinkOccurrence> bodyLinks = findLinks(rs.rightMem, a.args[0].buddy.atom.getName());
//					System.out.println(a.args[0].buddy.atom+" <=> "+link0);
//					System.out.println(a.args[1].buddy.atom+" <=> "+link1.atom.getName());
//					System.out.println(a.args[0].buddy.atom+" <=> "+link2);
					try {
						if (updateJavaType(headLink, Class.forName(link1.atom.getName())))
							updated = true;
						for (LinkOccurrence link : bodyLinks) {
							if (link != null && updateJavaType(link, Class.forName(link1.atom.getName())))
								updated = true;
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			for (Membrane m : rs.rightMem.mems) {
				if (checkJavaTypeByClassGuard(m))
					updated = true;
			}
			for (Membrane m : rs.leftMem.mems) {
				if (checkJavaTypeByClassGuard(m))
					updated = true;
			}
		}
		//����
		for (Membrane m : mem.mems) {
			if (checkJavaTypeByClassGuard(m))
				updated = true;
		}
		return updated;
	}
	
	/**
	 * �������Java�η����顼��¸�ߤ��뤫Ĵ�٤�
	 * @param mem
	 * @return
	 */
	public static boolean containsJavaTypeError(Membrane mem) {
		while (checkJavaTypeByClassGuard(mem));
		while (checkJavaType(mem));
		dump(mem);
		return containsJavaTypeError2(mem);
//		return true;
	}
}
