package com.cybor.gamehorse.ui;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cybor.gamehorse.R;
import com.cybor.gamehorse.core.GameMap;
import com.cybor.gamehorse.core.Horse;
import com.cybor.gamehorse.core.HorseGame;
import com.cybor.gamehorse.core.NetworkManager;
import com.cybor.gamehorse.core.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_NULL;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.cybor.gamehorse.core.GameMap.EMPTY;
import static com.cybor.gamehorse.core.GameMap.HORSE;
import static com.cybor.gamehorse.core.GameMap.STEP;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        HorseGame.OnStateChangeListener,
        HorseGame.OnGameOverListener,
        NetworkManager.OnConnectedListener
{
    private static final int NONE = 0;
    private static final int LOCAL_MODE = 1, NETWORK_MODE = 2;
    private static final int HOST = 1, CLIENT = 2;
    NetworkManager networkManager;
    private int networkMode = NONE;
    private int gameMode = NONE;
    private View menuContainer;
    private TextView localModeButton, networkModeButton;
    private EditText hostET;
    private FrameLayout gameContainer;
    private HorseGame game;
    private Map<String, ImageView> viewMap;
    private TextView dashboardTV;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        gameContainer = (FrameLayout) findViewById(R.id.game_field_container);
        menuContainer = findViewById(R.id.menu_container);

        findViewById(R.id.title_tv).setOnClickListener(this);
        localModeButton = (TextView) findViewById(R.id.local_mode_button);
        localModeButton.setOnClickListener(this);
        networkModeButton = (TextView) findViewById(R.id.network_mode_button);
        networkModeButton.setOnClickListener(this);

        hostET = (EditText) findViewById(R.id.host_et);
        dashboardTV = (TextView) findViewById(R.id.dashboard_tv);

        findViewById(R.id.back_button).setOnClickListener(this);
        findViewById(R.id.reset_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.local_mode_button:
                if (networkMode == (CLIENT | HOST))
                {
                    networkMode = HOST;
                    hostET.setVisibility(VISIBLE);
                    hostET.setInputType(TYPE_NULL);
                    localModeButton.setVisibility(GONE);
                    networkModeButton.setVisibility(GONE);
                    int ipAddress = ((WifiManager) getSystemService(WIFI_SERVICE))
                            .getConnectionInfo()
                            .getIpAddress();

                    hostET.setText(String.format(Locale.ENGLISH, "%s: %d.%d.%d.%d",
                            getString(R.string.host_address),
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff)));
                } else if (networkMode == HOST)
                    startGameNetwork(null);
                else
                {
                    networkMode = NONE;
                    menuContainer.animate().alpha(0).withEndAction(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            menuContainer.setVisibility(GONE);
                        }
                    });
                    gameMode = LOCAL_MODE;
                    startGameLocal();
                }
                break;
            case R.id.network_mode_button:
                if (networkMode == (CLIENT | HOST))
                {
                    hostET.setVisibility(VISIBLE);
                    localModeButton.setVisibility(GONE);
                    networkMode = CLIENT;
                } else if (networkMode == CLIENT)
                    startGameNetwork(hostET.getText().toString());
                else
                {
                    localModeButton.setText(R.string.create_game);
                    networkModeButton.setText(R.string.connect_game);
                    networkMode = CLIENT | HOST;
                }
                break;
            case R.id.title_tv:
                resetUI();
                break;
            case R.id.reset_button:
                game.reset();
                break;
            case R.id.back_button:
                game.rollBack(0);
                break;

            default:
                for (int i = 0; i < viewMap.values().size(); i++)
                    if (new ArrayList<>(viewMap.values()).get(i) == v)
                    {
                        List<Integer> coords = Utils
                                .parseCoordinates(new ArrayList<>(viewMap.keySet())
                                        .get(i));
                        if (coords != null)
                            game.tryStep(0, coords.get(0), coords.get(1));
                        break;
                    }
                break;
        }
    }


    private void startGameLocal()
    {
        generateField();

        game = HorseGame.getInstance(false);
        game.setOnStateChange(this);
        game.setOnGameOverListener(this);
    }

    private void startGameNetwork(String host)
    {
        networkManager = NetworkManager.getInstance();
        if (host != null)
            networkManager.createGame();
        else
            networkManager.connectGame(host);
        networkManager.setOnStateChange(this);
        networkManager.setOnConnectedListener(this);
        networkManager.setOnGameOverListener(this);
    }

    private void generateField()
    {
        viewMap = new HashMap<>();
        findViewById(R.id.game_container).setVisibility(VISIBLE);
        int cellWidth = gameContainer.getMeasuredWidth() / 10;
        int cellHeight = gameContainer.getMeasuredHeight() / 10;

        for (int y = 0; y < 10; y++)
            for (int x = 0; x < 10; x++)
            {
                ImageView cell = new ImageView(getApplicationContext());
                cell.setBackgroundResource(R.drawable.cell_background);
                cell.setOnClickListener(MainActivity.this);

                List<Integer> coords = new ArrayList<>();
                coords.add(x);
                coords.add(y);
                viewMap.put(Utils.packCoordinates(coords), cell);

                gameContainer.addView(cell);
                FrameLayout.LayoutParams cellLayoutParams = (FrameLayout.LayoutParams) cell.getLayoutParams();
                cellLayoutParams.width = cellWidth;
                cellLayoutParams.height = cellHeight;
                cellLayoutParams.leftMargin = cellWidth * x;
                cellLayoutParams.topMargin = cellHeight * y;
            }
    }

    @Override
    public void onGameOver(Horse looser)
    {
        Toast.makeText(this, R.string.you_loose, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStateChange(Horse horse)
    {
        GameMap map = game.getMap();
        if (horse != null)
            dashboardTV.setText(String.format("%s", horse.getScore()));
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getHeight(); y++)
            {
                List<Integer> coords = new ArrayList<>();
                coords.add(x);
                coords.add(y);
                ImageView view = viewMap.get(Utils.packCoordinates(coords));
                switch (map.getCell(x, y))
                {
                    case EMPTY:
                        view.setImageBitmap(null);
                        view.setBackgroundResource(R.drawable.cell_background);
                        break;
                    case HORSE:
                        view.setImageResource(R.mipmap.horse);
                        break;
                    case STEP:
                        view.setImageResource(R.mipmap.horseshoe);
                        break;
                }
            }
    }

    @Override
    public void onConnected()
    {
        Toast.makeText(this, R.string.connected, Toast.LENGTH_LONG).show();
    }

    private void resetUI()
    {
        localModeButton.setVisibility(VISIBLE);
        networkModeButton.setVisibility(VISIBLE);
        localModeButton.setText(R.string.local_mode);
        networkModeButton.setText(R.string.network_mode);
        hostET.setVisibility(GONE);
        hostET.setText(null);
        hostET.setInputType(TYPE_CLASS_TEXT);
        gameContainer.removeAllViews();
        findViewById(R.id.game_container).setVisibility(GONE);
        if (gameMode != NONE)
        {
            menuContainer.setVisibility(VISIBLE);
            menuContainer.animate().alpha(1);
        }
        networkMode = NONE;
        gameMode = NONE;
    }

    @Override
    public void onBackPressed()
    {
        if (gameMode != NONE || networkMode != NONE) resetUI();
        else super.onBackPressed();
    }
}
