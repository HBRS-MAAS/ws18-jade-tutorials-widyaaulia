package maas.tutorials;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class BookSellerAgent extends Agent {
	// The catalogue of books for sale (maps the title of a book to its price)
	private Hashtable ebook;
	private Hashtable paperback;
	private Hashtable paperback_amount;	

	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
		ebook = new Hashtable();
		paperback = new Hashtable();
		paperback_amount = new Hashtable();

		updateCatalogue();

		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-selling");
		sd.setName("maas-book-trading");

		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving queries from buyer agents
		addBehaviour(new OfferRequestsServer());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");
	}

	/**
     Add books to catalogue
	 */
	//public void updateCatalogue(final String title, final int price) {
		//addBehaviour(new OneShotBehaviour() {
		//	public void action() {
		//		catalogue.put(title, new Integer(price));
		//		System.out.println(title+" inserted into catalogue. Price = "+price);
		//	}
		//} );
	public void updateCatalogue() {
		Integer num = 4;
		String[] book_title = {"aku ngantuk","mau bobo","kapan pulang?","bosan"};
		Integer[] book_price = {10000, 20000, 30000, 40000};
		Integer[] book_amount = {5, 2, 10, 3};
		Integer discount = 5000;
		for (Integer i = 0; i < num; i++) {
			ebook.put(book_title[i], book_price[i] - discount);
			paperback.put(book_title[i], book_price[i]);
			paperback_amount.put(book_title[i], book_amount[i]);
			System.out.println(book_title[i] + " inserted into catalogue. Price = " + book_price[i]);
		}		
	}			
	//}

	/**
	   Inner class OfferRequestsServer.
	   This is the behaviour used by Book-seller agents to serve incoming requests 
	   for offer from buyer agents.
	   If the requested book is in the local catalogue the seller agent replies 
	   with a PROPOSE message specifying the price. Otherwise a REFUSE message is
	   sent back.
	 */
	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String[] title_type = msg.getContent().split(":");
        			String title = title_type[0];
				
				ACLMessage reply = msg.createReply();

				Integer price;
				
				if (title_type[1].toLowerCase() == "ebook") {
					price = (Integer) ebook.get(title);
				} else {
					price = (Integer) paperback.get(title);
				}
				
				if (price != null) {
					// The requested book is available for sale. Reply with the price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				}
				else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	/**
	   Inner class PurchaseOrdersServer.
	   This is the behaviour used by Book-seller agents to serve incoming 
	   offer acceptances (i.e. purchase orders) from buyer agents.
	   The seller agent removes the purchased book from its catalogue 
	   and replies with an INFORM message to notify the buyer that the
	   purchase has been sucesfully completed.
	 */
	private class PurchaseOrdersServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String[] title_type = msg.getContent().split(":");
        			String title = title_type[0];

				ACLMessage reply = msg.createReply();

				Integer temp = 0;
				Integer price = (Integer) paperback.get(title);

				if (price != null) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(title+" sold to agent "+msg.getSender().getName());
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}

				//Reduce the amount of books or remove the book from the catalogue
				if (title_type[1].toLowerCase() == "paperback") {
					Integer amount = (Integer) paperback.get(title);
					if (amount != null) {
						if (amount > 1) {
							paperback_amount.put(title, amount - 1);
						} else {
							temp = (Integer) paperback_amount.remove(title);
							temp = (Integer) paperback.remove(title);
						}
					}
				}

				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
}
