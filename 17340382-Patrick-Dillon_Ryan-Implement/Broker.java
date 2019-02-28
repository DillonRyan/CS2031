package cs.tcd.ie;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import tcdIO.Terminal;


public class Broker extends Node {
	static final int DEFAULT_DST_PORT = 50001;
	
    HashMap<String, List<SocketAddress>> listOfSubscriber = new HashMap<>();
    HashMap<String, List<SocketAddress>> listOfPublisher = new HashMap<>();

    SocketAddress newestSubscriber;
  
    
    @Override
    public void userInput(String message) {

    }
    public synchronized void run() {
        System.out.println("Establishing Contact ");
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            Broker broker = new Broker(DEFAULT_DST_PORT); 
           // broker.onReceipt(packet);
            broker.run();
            System.out.println("Program finished");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    
    Broker(int port) {
      
            try {
				socket = new DatagramSocket(port);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            listener.go();
        
    }

    public void dealWithPublication(DatagramPacket packet, String topic, byte[] message) throws IOException { 
        if (listOfSubscriber.get(topic) != null) 
        {
            byte[] sendTopic = (topic + ": ").getBytes(); // topics that getting sent
            byte[] mesToBeSent = new byte[message.length + sendTopic.length]; // message to be sent
            for (int i = 0; i < sendTopic.length; i++) 
            {
            	mesToBeSent[i] = sendTopic[i];
            }
            for (int z = 0; z < message.length; z++) 
            {
            	mesToBeSent[sendTopic.length + z] = message[z];
            }
            DatagramPacket mesPac = new DatagramPacket(mesToBeSent, mesToBeSent.length); // message packet
            List<SocketAddress> subscribers = listOfSubscriber.get(topic);
            for (int p = 0; p < subscribers.size(); p++) {
            	mesPac.setSocketAddress(subscribers.get(p));
                socket.send(mesPac);
            }
        } else {
            System.out.println("There are no subscribers for this topic");
        }
        if (!listOfPublisher.containsKey(topic))
        {
            List<SocketAddress> publisher = new ArrayList<>();
            publisher.add(packet.getSocketAddress());
            listOfPublisher.put(topic, publisher);
        }
        else if (!listOfPublisher.containsValue(packet.getSocketAddress())) 
        {
            List<SocketAddress> pubAndTop = listOfPublisher.get(topic); //temporay list
            pubAndTop.add(packet.getSocketAddress());
            listOfPublisher.put(topic, pubAndTop);
        }
    
    }
    
    public void dealWithSubscription(DatagramPacket packet, String topic) throws IOException { 
        newestSubscriber = packet.getSocketAddress(); // new subscriber
        if (listOfPublisher.containsKey(topic))
        {
            List<SocketAddress> tmpList = listOfPublisher.get(topic);
            byte[] send = new byte[4];
            send[3] = 3; /// send publications position
            DatagramPacket reqAllPub = new DatagramPacket(send, send.length); // getting all publications
            for (int i = 0; i < tmpList.size(); i++)
            {
            	reqAllPub.setSocketAddress(tmpList.get(i));
                socket.send(reqAllPub);
            }
        }
        if (!listOfSubscriber.containsKey(topic))
        {
            List<SocketAddress> newSubs = new ArrayList<>();
            newSubs.add(packet.getSocketAddress());
            listOfSubscriber.put(topic, newSubs);
        } else {
            List<SocketAddress> tmpList = listOfSubscriber.get(topic);
            tmpList.add(packet.getSocketAddress());
            listOfSubscriber.put(topic, tmpList);
        }
        
    }
    
    public synchronized void onReceipt(DatagramPacket packet) {
   
        	boolean correctlyReceived = false;
            byte[] data = packet.getData();
            byte[] buffTopic = new byte[data[1]]; 
            switch (data[0]) { 
                case 0:
                    for (int i = 0; i < buffTopic.length; i++) 
                    {
                    	buffTopic[i] = data[11 + i];
                    }
                    String topic = new String(buffTopic);
				try {
					dealWithSubscription(packet, topic);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    System.out.println("The new subscription is : " + topic);
                    correctlyReceived = true;
                    break;
                case 1: 
                    for (int j = 0; j < buffTopic.length; j++)
                    {
                    	buffTopic[j] = data[11 + j]; //[10 + j + 1]
                    }
                    topic = new String(buffTopic);
                    byte[] bufferMessage = new byte[data[2]]; // message length position
                    for (int k = 0; k < bufferMessage.length; k++)
                    {
                    	bufferMessage[k] = data[11 + buffTopic.length + k];
                    }
				try {
					dealWithPublication(packet, topic, bufferMessage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    System.out.println("The new publication is : " + topic);
                    correctlyReceived = true;
                    break;
                case 2:
                    for (int l = 0; l < buffTopic.length; l++) 
                    {
                    	buffTopic[l] = data[11 + l];
                    }
                    topic = new String(buffTopic);
                    List<SocketAddress> curSubs = listOfSubscriber.get(topic);
                    for (int m = 0; m < curSubs.size(); m++) 
                    {
                        if (curSubs.get(m).equals(packet.getSocketAddress())) 
                        {
                        	curSubs.remove(curSubs.get(m));
                            break;
                        }
                    }
                    correctlyReceived = true;
                    break;
                case 3:
                    for (int n = 0; n < buffTopic.length; n++) 
                    {
                    	buffTopic[n] = data[11 + n];
                    }
                    topic = new String(buffTopic);
                    bufferMessage = new byte[data[2]]; // message length position
                    for (int o = 0; o < bufferMessage.length; o++) 
                    {
                    	bufferMessage[o] = data[11 + buffTopic.length + o];
                    }
                    byte[] sendTopic = (topic + ": ").getBytes();
                    byte[] sendMessage = new byte[sendTopic.length + bufferMessage.length];
                    for (int p = 0; p < sendTopic.length; p++)
                    {
                    	sendMessage[p] = sendTopic[p];
                    }
                    for (int q = 0; q < bufferMessage.length; q++) 
                    {
                    	sendMessage[sendTopic.length + q] = bufferMessage[q];
                    }
                    DatagramPacket messagePacket = new DatagramPacket(sendMessage, sendMessage.length);
                    messagePacket.setSocketAddress(newestSubscriber);
				try {
					socket.send(messagePacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    correctlyReceived = true;
                    break;
            }
            if (!correctlyReceived) 
            {
            	DatagramPacket reply;
                String acknowledgemnet = "Packet received incorectly";
                byte [] response = acknowledgemnet.getBytes();
                reply = new DatagramPacket(response, response.length);
                reply.setSocketAddress(packet.getSocketAddress());
                try {
					socket.send(reply);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            } else {
                DatagramPacket reply;
                String acknowledgemnet = "received correctly";
                byte [] response = acknowledgemnet.getBytes();
                reply = new DatagramPacket(response, response.length);
                reply.setSocketAddress(packet.getSocketAddress());
                try {
					socket.send(reply);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        
    }
   
}

    

