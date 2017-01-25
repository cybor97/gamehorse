package com.cybor.gamehorse.core;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager
{
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Map<String, String> games;
    private Thread finder;
    private Thread remotePlayer;
    private int PORT_NUMBER = 12014;

    public void createGame(String gameName)
    {
        try
        {
            socket = new ServerSocket(PORT_NUMBER).accept();
            initStreams();
            sendMessage(gameName);
            if (!reader.readLine().equals("skip"))
                runInteraction();
        } catch (IOException e)
        {
            Log.e("createGame", e.toString());
        }
    }

    public String connectGame(String host, boolean skip)
    {
        try
        {
            socket = new Socket(host, PORT_NUMBER);
            initStreams();
            String message = reader.readLine().replace("\\n", "\n");
            sendMessage(skip ? "skip" : "ok");
            return message;
        } catch (IOException e)
        {
            Log.e("createGame", e.toString());
            return null;
        }
    }

    public void findGames()
    {
        games = new HashMap<>();
        if (finder != null && finder.isAlive())
            finder.interrupt();
        (finder = new Thread(new Runnable() //В твоем случае
        {                                   //просто
            @Override                       //вместо new Runnable...
            public void run()               //()=>{...}
            {                               //<

            }
        })).start();
    }

    public void runInteraction()
    {
        if (remotePlayer != null && remotePlayer.isAlive())
            remotePlayer.interrupt();
        (remotePlayer = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

            }
        })).start();
    }

    public void sendMessage(String message) throws IOException
    {
        writer.write(message + "\n");
        writer.flush();
    }

    public void initStreams() throws IOException
    {
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    interface OnMessageAcceptedListener
    {
        void onMessageAccepted();
    }

    interface OnConnectedListener
    {
        void onConnected();
    }
}
