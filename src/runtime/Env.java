/*
 * �쐬��: 2003/10/24
 *
 */
package runtime;

/**
 * ���B�f�o�b�O�p�B
 * @author hara
 */
public final class Env {
	public static Machine machine = new Machine();
	public static Membrane rootMembrane = new Membrane(machine);
	
	// General dumper
	public static void p(Object o) {
		System.out.println(o);
	}
	// Called method
	public static void c(Object o) {
		p(">>> "+o);
	}
	// New object
	public static void n(Object o) {
		p(">>> new "+o);
	}
}
