/**
 * ��ʸ���ϤǤΥ��顼
 */

package compile.parser;

public class ParseException extends Exception {
	public ParseException() {
		super();
	}
	public ParseException(String s) {
		super(s);	
	}
	public ParseException(String s, Throwable e) {
		super(s,e);
	}
}
