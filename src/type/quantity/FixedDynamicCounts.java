package type.quantity;

public class FixedDynamicCounts {
	public final int multiple;
	
	/** �������ץ��� */
	public final FixedCounts removeCounts;
	/** ��������ץ��� */
	public final FixedCounts generateCounts;
	public FixedDynamicCounts(DynamicCounts dom) {
		removeCounts = dom.removeCounts.solve();
		generateCounts = dom.generateCounts.solve();
		multiple = dom.multiple;
	}

}
