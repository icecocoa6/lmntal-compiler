import java.net.*;
import java.io.*;

/*
 * ������: 2004/04/19
 *
 * �����������줿�����Ȥ����������ƥ�ץ졼�Ȥ��ѹ����뤿��
 * ������ɥ� > ���� > Java > ���������� > �����ɤȥ�����
 */

/**
 * @author uedalab
 *
 * �����������줿�����Ȥ����������ƥ�ץ졼�Ȥ��ѹ����뤿��
 * ������ɥ� > ���� > Java > ���������� > �����ɤȥ�����
 */
public class Google {

	public static void main(String[] args) {
		System.out.println(Google.get("http://yahoo.co.jp"));
	}
	public static String get(String u) {
		try {
			URL url = new URL(u);
			HttpURLConnection hc = (HttpURLConnection)url.openConnection();
			hc.setRequestProperty("USER-AGENT", "DoCoMo/1.0/D505i/c10");
			hc.connect();
			BufferedReader br = new BufferedReader(new InputStreamReader(hc.getInputStream(), "JISAutoDetect"));
			String s;
			StringBuffer b=new StringBuffer();
			while((s=br.readLine())!=null) {
				b.append(s);
			}
			System.out.println(b);
			return b.toString()
			.replaceAll("<script>.*?</script>", "")
			.replaceAll("<br>", "\n")
			.replaceAll("<.*?>", "")
			.replaceAll("&#\\d{5}", "")
			.replaceAll("&.*;"," ");
		} catch (Exception e) {
			return null;
		}
	}
}
