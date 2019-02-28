package cs.tcd.ie;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import tcdIO.Terminal;
public class Publisher extends Node implements Runnable {
	
    static final int DEFAULT_PORT = 50001;
	Terminal terminal;
    InetSocketAddress destinationAddress;
    String pacTopic;
    String message;


    List<DatagramPacket> sentPackets = new ArrayList<>();

    Publisher(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
    
    public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Publisher");
			(new Publisher(terminal, DEFAULT_PORT)).start();
			terminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
    
    public synchronized void start() throws Exception {
    	boolean finished = false;
    	while(!finished) {
    		byte [] data = null;
    		DatagramPacket packet = null;
    		
    		data = (terminal.readString("Publish to a topic by enterting 'publish#<topic>#<message>'\n")).getBytes();
    		terminal.println("sending packet...");
    		terminal.println("Packet Sent");
    		
    		packet= new DatagramPacket(data, data.length, destinationAddress);
			socket.send(packet);
    		this.wait();
    	}
		
	}
    public synchronized void onReceipt(DatagramPacket packet) {
		try {
			StringContent content= new StringContent(packet);

			terminal.println(content.toString() + "\n");
			
			DatagramPacket response;
			response= (new StringContent("OK")).toDatagramPacket();
			response.setSocketAddress(packet.getSocketAddress());
			socket.send(response);
		}
		catch(Exception e) {e.printStackTrace();}
	}

    

	@Override
    public void userInput(String userMessage) {
    	byte[] bytesMessage = message.getBytes();
        byte[] bytesInTopic = pacTopic.getBytes();
        byte[] band = new byte[bytesInTopic.length + bytesMessage.length + 11]; 
        
        band[0] = (byte) 1; 
        band[1] = (byte) bytesInTopic.length; 
        band[2] = (byte) bytesMessage.length; 
        for (int i = 0; i < bytesInTopic.length; i++)
        {
            band[i + 11] = bytesInTopic[i]; 
        }
        for (int j = 0; j < bytesMessage.length; j++) 
        {
            band[j + 11 + bytesInTopic.length] = bytesMessage[j]; 
        }
        System.out.println("Packet is curently sending");
        DatagramPacket topPacket = new DatagramPacket(band, band.length, destinationAddress);
        	sentPackets.add(topPacket);
            try {
				socket.send(topPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("The packet is now sent.");
        
    }

    public synchronized void run() { 
        byte[] bytesInTopic = pacTopic.getBytes();
        byte[] bytesMessage = message.getBytes();
        byte[] buffer = new byte[11 + bytesMessage.length + bytesInTopic.length]; 
        DatagramPacket mesPacket;

        buffer[0] = (byte) bytesMessage.length; 
        buffer[0] = (byte) 1; 
        buffer[1] = (byte) bytesInTopic.length;

        for (int i = 0; i < bytesInTopic.length; i++)
        {
            buffer[i + 11] = bytesInTopic[i]; 
        }
        for (int j = 0; j < bytesMessage.length; j++)
        {
            buffer[j + 11 + bytesInTopic.length] = bytesMessage[j]; 
        }
        System.out.println("Packet is curently sending");
        mesPacket = new DatagramPacket(buffer, buffer.length, destinationAddress);
            try {
				socket.send(mesPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("The packet is now sent.");
            sentPackets.add(mesPacket);
            try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
    }
}