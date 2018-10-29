package maas;

import java.util.List;
import java.util.Vector;

public class Start {
	public static void main(String[] args) {

		int num_buyers = 20;
		int num_sellers = 3;

		List<String> buyers = new Vector<>();
		for (int i = 0; i < num_buyers; i++) {
			buyers.add("Buyer" + Integer.toString(i) + ":maas.tutorials.BookBuyerAgent");
		}
	
		List<String> sellers = new Vector<>();
		for (int i = 0; i < num_buyers; i++) {
			sellers.add("Seller" + Integer.toString(i) + ":maas.tutorials.BookSellerAgent");
		}

		List<String> cmd = new Vector<>();
		cmd.add("-agents");
		StringBuilder sb = new StringBuilder();
		for (String a : buyers) {
			sb.append(a);
			sb.append(";");
		}
		for (String a : sellers) {
			sb.append(a);
			sb.append(";");
		}
    	cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
}
