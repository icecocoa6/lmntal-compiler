/*
 * �쐬��: 2003/10/21
 *
 * ���̐������ꂽ�R�����g�̑}�������e���v���[�g��ύX���邽��
 * �E�B���h�E > �ݒ� > Java > �R�[�h���� > �R�[�h�ƃR�����g
 */
package test;

import runtime.*;
import junit.framework.TestCase;

/**
 * @author pa
 *
 * ���̐������ꂽ�R�����g�̑}�������e���v���[�g��ύX���邽��
 * �E�B���h�E > �ݒ� > Java > �R�[�h���� > �R�[�h�ƃR�����g
 */
public class InstructionTest extends TestCase {
	Instruction inst;
	/**
	 * Constructor for BodyInstructionTest.
	 * @param arg0
	 */
	public InstructionTest(String arg0) {
		super(arg0);
		inst = new Instruction();
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(InstructionTest.class);
	}

	public void testHoge() {
		System.out.println("test "+inst.data.toString());
		assertTrue(inst.data.toString().equals("[react, [1, 2, 5]]"));
	}
}
