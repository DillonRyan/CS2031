package cs.tcd.ie;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import tcdIO.*;
public class Subscriber extends Node implements Runnable {
	
	static Terminal terminal;
    boolean finished = false;
    InetSocketAddress destinationAddress;
    static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "localhost";
    List<String> packetTopics = new ArrayList<>();

    public static void main(String[] args) throws BindException {
        System.out.println("Enter the topic you want to subscribe to?");
        try {					
			Terminal terminal= new Terminal("Subscriber");		
			(new Subscriber(terminal, DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
			terminal.println("\nProgram completed");
		} catch(java.lang.Exception e) {e.printStackTrace();
		}
    }
    public synchronized void start() throws Exception {
		byte[] data= null;
		DatagramPacket packet= null;
		
			data= (terminal.readString("Subscribe to a topic by enterting 'subscribe#<topic>'\n")).getBytes();
			
			terminal.println("Sending packet...");
			packet= new DatagramPacket(data, data.length, destinationAddress);
			socket.send(packet);
			terminal.println("Packet sent");
			this.wait();
	}
    

    @Override
    public void userInput(String userMessage) {
        if (!finished) 
        {
            if (userMessage.equals("3"))
            {
                if (packetTopics.size() != 0) 
                {
                	for (int i = 0; i < packetTopics.size(); i++) {
                        System.out.println(packetTopics.get(i));
                	}
                } else {
                        System.out.println("You're currently subscribed to no topics");
                }
            }
             else if (userMessage.equals("2")) 
            {
                System.out.println("Please enter the topic you would like to subscribe too");
                Scanner userInput = new Scanner(System.in);
                if (userInput.hasNext()) 
                {
                    String subscribeTopic = userInput.next();
                    subscribeToANewTopic(subscribeTopic.getBytes());
                }
            } else if (userMessage.equals("1")) 
            {
                System.out.println("Please enter the topic you would like to unsubscribe from");
                Scanner userInput = new Scanner(System.in);
                if (userInput.hasNext()) 
                {
                	userMessage = userInput.next();
                	unSubscribe(userMessage);
                }
            }
           
        }
    }
    Subscriber(Terminal terminal,String dstHost, int dstPort, int srcPort) {
		try {
			this.terminal= terminal;
			destinationAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
    public synchronized void onReceipt(DatagramPacket packet) {
     
    	StringContent content= new StringContent(packet);
		this.notify();
		terminal.println(content.toString());
    }

    
    public void subscribeToANewTopic(byte[] topicBytes) {
    	packetTopics.add(new String(topicBytes));
        byte[] packetBuff = new byte[10 + topicBytes.length + 1]; 
        packetBuff[0] = (byte) 0; 
        packetBuff[1] = (byte) topicBytes.length; 
        for (int i = 0; i < topicBytes.length; i++) 
        {
        	packetBuff[10 + i + 1] = topicBytes[i]; 
        }
        System.out.println("Packet currently sending");
        DatagramPacket messagePacket = new DatagramPacket(packetBuff, packetBuff.length, destinationAddress);
      
            try {
				socket.send(messagePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("The packet has now been sent");
        
    }

    
    

    public synchronized void unSubscribe(String topic) {
    	packetTopics.remove(topic);
        byte[] byteTopic = topic.getBytes();
            byte[] unSub = new byte[11 + byteTopic.length];
            unSub[0] = 2;
            unSub[1] = (byte) byteTopic.length; 
            for (int i = 0; i < byteTopic.length; i++) {
                unSub[11 + i] = byteTopic[i]; 
            }
            DatagramPacket packetMes = new DatagramPacket(unSub, unSub.length, destinationAddress);
            try {
				socket.send(packetMes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        System.out.println("You unsubscribed from: " + topic + "if you want to unsubscribe from everything type end or else press 0");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext("end")) 
        {
            finished = true;
            this.notify();
        }
    }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

    
    
    
}