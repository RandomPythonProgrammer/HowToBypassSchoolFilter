package com.jchen.requestclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class Client {
	
	public static final int HEADER = 4096;
	
	public static void main(String[] args) {
		try {
			Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
			DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
			DataInputStream reader = new DataInputStream(socket.getInputStream());
			BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("$: ");
				String[] userInput = userInputReader.readLine().split(" ");
				String fileName = "HtmlFile";

				sendMessage(writer, userInput[0]);

				if (userInput[0].equalsIgnoreCase("close") || userInput[0].equalsIgnoreCase("shutdown")) {
					writer.close();
					reader.close();
					socket.close();
					userInputReader.close();
					return;
				}

				if (userInput.length == 2) {
					fileName = userInput[1];
				}
				
				File file;
				
				if ((file = new File(fileName)).exists()) {
					int fileNumber;
					for (fileNumber = 1; (file = new File(fileName + "(" + fileNumber + ")" + ".html")).exists(); fileNumber ++);
				}
				
				file.createNewFile();
				System.out.println(file.getAbsolutePath());
				FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
				
				int headerSize = Integer.parseInt(new String(reader.readNBytes(Client.HEADER), "UTF8").replaceAll("[^0-9]", ""));
				String data = new String(reader.readNBytes(headerSize), "UTF8");
				
				fileWriter.write(data);
				fileWriter.close();
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			e.fillInStackTrace();
		}
		
	}
	
	public static void sendMessage(DataOutputStream writer, String message) throws UnsupportedEncodingException, IOException {
		byte[] paddedHeader = new byte[Client.HEADER];
		byte[] header = String.valueOf(message.getBytes("UTF8").length).getBytes("UTF8");
		for (int i = 0; i < header.length; i ++ ) {
			paddedHeader[i] = header[i];
		}
		writer.write(paddedHeader);
		writer.write(message.getBytes("UTF8"));
	}
}
