package com.gb.gossip.config.service;

import com.gb.gossip.config.member.Node;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class SocketService {
    private DatagramSocket datagramSocket;
    private byte[] receivedBuffer = new byte[1024];
    private DatagramPacket receivePacket =
            new DatagramPacket(receivedBuffer, receivedBuffer.length);

    public SocketService(int portToListen) {
        try {
            datagramSocket = new DatagramSocket(portToListen);
        } catch (SocketException e) {
            System.out.println("Could not create socket connection");
            e.printStackTrace();
        }
    }

    public void sendGossip(Node node, Node message) {
        byte[] bytesToWrite = getBytesToWrite(message);
        sendGossipMessage(node, bytesToWrite);
    }

    public Node receiveGossip() {
        try {
            datagramSocket.receive(receivePacket);
            ObjectInputStream objectInputStream =
                    new ObjectInputStream(
                            new ByteArrayInputStream(receivePacket.getData()));
            Node message = null;
            try {
                message = (Node) objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                objectInputStream.close();
                return message;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getBytesToWrite(Node message) {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        try {
            ObjectOutput oo = new ObjectOutputStream(bStream);
            oo.writeObject(message);
            oo.close();
        } catch (IOException e) {
            System.out.println
                    ("Could not send " + message.getNetworkMessage() +
                            "] because: " + e.getMessage());
        }

        return bStream.toByteArray();
    }

    private void sendGossipMessage(Node target, byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, target.getInetAddress(), target.getPort());
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Fatal error trying to send: "
                    + packet + " to [" + target.getSocketAddress() + "]");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
