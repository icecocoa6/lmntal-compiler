package compile.parser.intermediate;

/**
 * �ѡ�����ˡ��롼�륪�֥������Ȥ���������Ѥ��륪�֥������ȡ�
 * �ѡ�����λ��ˡ���������롼�륪�֥������Ȥ��֤������롣
 */
public class LabelRef {
	private Integer id;
	LabelRef(Integer id) {
		this.id = id;
	}
	Integer getId() {
		return id;
	}
}
