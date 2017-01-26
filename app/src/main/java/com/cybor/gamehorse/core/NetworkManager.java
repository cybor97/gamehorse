package com.cybor.gamehorse.core;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager
{
    private static final int CURRENT_PLAYER = 0, ENEMY = 1;
    private static NetworkManager instance;
    private HorseGame horseGame;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Thread remotePlayer;
    private int PORT_NUMBER = 12014;
    private List<Integer> coords;
    private boolean isHost;
    private OnConnectedListener onConnectedListener;
    private HorseGame.OnGameOverListener onGameOverListener;
    private HorseGame.OnStateChangeListener onStateChangeListener;

    private NetworkManager()
    {

    }

    public static NetworkManager getInstance()
    {
        if (instance == null)
            instance = new NetworkManager();
        return instance;
    }

    public void createGame()
    {
        if (remotePlayer != null && remotePlayer.isAlive())
            remotePlayer.interrupt();
        (remotePlayer = new Thread(new Runnable() //Ваня, в твоем случае
        {                                         //просто
            @Override                             //вместо new Runnable...
            public void run()                     //()=>{...}
            {                                     //<
                try
                {
                    socket = new ServerSocket(PORT_NUMBER).accept();
                    initStreams();
                    isHost = true;
                    runInteraction();
                } catch (IOException e)
                {
                    Log.e("createGame", e.toString());
                }
            }
        })).start();

    }

    public void connectGame(String host)
    {
        try
        {
            socket = new Socket(host, PORT_NUMBER);
            initStreams();
            runInteraction();
        } catch (IOException e)
        {
            Log.e("connectGame", e.toString());
        }
    }


    public void runInteraction()
    {
        try
        {
            if (onConnectedListener != null)
                onConnectedListener.onConnected();

            horseGame = HorseGame.getInstance(true);
            horseGame.setOnStateChange(new HorseGame.OnStateChangeListener()
            {   //Ваня, в твоем случае... Глянь в createGame.
                @Override
                public void onStateChange(Horse horse)
                {
                    coords = new ArrayList<>();
                    coords.add(horse.getX());
                    coords.add(horse.getY());
                    if (onStateChangeListener != null)
                        onStateChangeListener.onStateChange(horse);
                }
            });

            Horse currentPlayer = horseGame.getHorse(CURRENT_PLAYER);
            Horse enemyPlayer = horseGame.getHorse(ENEMY);
            if (!isHost)
            {
                sendMessage("15_0");
                currentPlayer.setX(0);
                currentPlayer.setY(15);
            }
            while (!Thread.interrupted())
            {
                List<Integer> enemyCoords = Utils.parseCoordinates(receiveMessage());
                if (enemyCoords != null)
                {
                    enemyPlayer.setX(enemyCoords.get(0));
                    enemyPlayer.setY(enemyCoords.get(1));
                }

                if (coords != null)
                {
                    sendMessage(Utils.packCoordinates(coords));
                    coords = null;
                }

            }

        } catch (IOException e)
        {
            Log.e("runInteraction", e.toString());
        }
    }

    public String receiveMessage() throws IOException
    {
        return reader.readLine().replace("\\n", "\n");
    }

    public void sendMessage(String message) throws IOException
    {
        writer.write(message.replace("\n", "\\n") + "\n");
        writer.flush();
    }

    public void initStreams() throws IOException
    {
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void setOnConnectedListener(OnConnectedListener onConnectedListener)
    {
        this.onConnectedListener = onConnectedListener;
    }

    public void setOnGameOverListener(HorseGame.OnGameOverListener onGameOverListener)
    {
        this.onGameOverListener = onGameOverListener;
    }

    public void setOnStateChange(HorseGame.OnStateChangeListener onStateChange)
    {
        this.onStateChangeListener = onStateChange;
    }

    public interface OnConnectedListener
    {
        void onConnected();
    }
}
