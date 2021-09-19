package com.jchen.requestserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.jsoup.Jsoup;

public class Server {

	ServerSocket serverSocket;
	ArrayList<ClientConnection> clients;
	public static final int HEADER = 4096;

	public Server(String[] args) {
		try {
			serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			clients = new ArrayList<>();
			System.out.println("Server online");
			while (true) {
				ClientConnection client = new ClientConnection(this, serverSocket.accept());
				clients.add(client);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Server server = new Server(args);
	}

	public static String getHtml(String url) throws IOException {
		return Jsoup.connect(url).get().html();
	}

	public void shutoff() throws IOException {
		for (ClientConnection client : clients) {
			client.close();
		}
		serverSocket.close();
		System.out.println("Server shutdown");
	}
}

class ClientConnection implements Runnable {

	private Socket socket;
	private DataOutputStream writer;
	private DataInputStream reader;
	private Server server;
	private Thread thread;

	public ClientConnection(Server server, Socket socket) throws IOException {
		this.socket = socket;
		writer = new DataOutputStream(socket.getOutputStream());
		reader = new DataInputStream(socket.getInputStream());
		this.server = server;
		System.out.println("New connetion");
		thread = new Thread(this);
		thread.start();

	}

	public void close() throws IOException {
		System.out.println("New disconnect");
		writer.close();
		reader.close();
		socket.close();
	}

	@Override
	public void run() {
		try {
			String headerString;
			int headerSize;
			String request;
			while ((headerString = new String(reader.readNBytes(Server.HEADER), "UTF8")) != null) {
				headerSize = Integer.parseInt(headerString.replaceAll("[^0-9]", ""));
				request = new String(reader.readNBytes(headerSize), "UTF8");
				if (request.equalsIgnoreCase("close")) {
					close();
					break;
				} else if (request.equalsIgnoreCase("shutdown")) {
					close();
					server.shutoff();
				} else {
					String html = Server.getHtml(request);
					sendMessage(writer, html);
					System.out.println(request);
				}
			}
		} catch (Exception e) {
			System.out.print(e);
		}
	}

	public static void sendMessage(DataOutputStream writer, String message)
			throws UnsupportedEncodingException, IOException {
		byte[] paddedHeader = new byte[Server.HEADER];
		byte[] header = String.valueOf(message.getBytes("UTF8").length).getBytes("UTF8");
		for (int i = 0; i < header.length; i++) {
			paddedHeader[i] = header[i];
		}
		writer.write(paddedHeader);
		writer.write(message.getBytes("UTF8"));
	}
}
