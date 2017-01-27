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
        (remotePlayer = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    isHost = true;
                    socket = new ServerSocket(PORT_NUMBER).accept();
                    initStreams();
                    runInteraction();
                } catch (IOException e)
                {
                    Log.e("createGame", e.toString());
                }
            }
        })).start();

    }

    public void connectGame(final String host)
    {
        if (remotePlayer != null && remotePlayer.isAlive())
            remotePlayer.interrupt();
        (remotePlayer = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    socket = new Socket(host, PORT_NUMBER);
                    initStreams();
                    runInteraction();
                } catch (IOException e)
                {
                    Log.e("createGame", e.toString());
                }
            }
        })).start();
    }

    public void runInteraction()
    {
        try
        {
            if (onConnectedListener != null)
                onConnectedListener.onConnected();

            horseGame = HorseGame.getInstance(true);
            horseGame.setOnStateChange(onStateChangeListener);

            if (!isHost)
            {
                sendMessage("0_15");
                horseGame.tryStep(CURRENT_PLAYER, 0, 15, true);
                horseGame.tryStep(ENEMY, 15, 0, true);
            } else horseGame.tryStep(CURRENT_PLAYER, 15, 0, true);
            while (!Thread.interrupted())
                for (Horse current : horseGame.getHistory().get(CURRENT_PLAYER))
                {
                    receiveStep();
                    sendStep(current);
                }

        } catch (IOException e)
        {
            Log.e("runInteraction", e.toString());
        }
    }

    public void receiveStep() throws IOException
    {
        List<Integer> enemyCoords = Utils.parseCoordinates(receiveMessage());
        if (enemyCoords != null)
            horseGame.tryStep(ENEMY, enemyCoords.get(0), enemyCoords.get(1));
    }

    public void sendStep(Horse horse) throws IOException
    {
        List<Integer> coords = new ArrayList<>();
        coords.add(horse.getX());
        coords.add(horse.getY());
        sendMessage(Utils.packCoordinates(coords));
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
