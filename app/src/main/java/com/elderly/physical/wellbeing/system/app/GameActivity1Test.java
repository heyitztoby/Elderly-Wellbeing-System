package com.elderly.physical.wellbeing.system.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elderly.physical.wellbeing.system.R;
import com.elderly.physical.wellbeing.system.ble.BleUtils;
import com.elderly.physical.wellbeing.system.ble.UartPacket;
import com.elderly.physical.wellbeing.system.ble.UartPacketManagerBase;
import com.elderly.physical.wellbeing.system.ble.central.BlePeripheral;
import com.elderly.physical.wellbeing.system.ble.central.BlePeripheralUart;
import com.elderly.physical.wellbeing.system.ble.central.BleScanner;
import com.elderly.physical.wellbeing.system.ble.central.UartPacketManager;
import com.elderly.physical.wellbeing.system.mqtt.MqttManager;
import com.elderly.physical.wellbeing.system.style.UartStyle;
import com.elderly.physical.wellbeing.system.utils.DialogUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.view.View.INVISIBLE;

public class GameActivity1Test extends AppCompatActivity implements UartPacketManagerBase.Listener{

    /* User interface */
    public static TextView txt_timer, txt_high_score, txt_score_over, txt_hs, txtView, txtView2, txt_high_score2, txt_score_over2;
    public static RelativeLayout r1_game_over, r1_score_history, over_r1, r1_selection, r1_help, over_r2, r1_success;
    public static Button btn_start, btn_scoreHistory, btn_Back, btn_Reset, btn_test, btn_help;
    public static Button btn_easy, btn_medium, btn_hard, btn_selectBack, btn_helpBack;

    public static String gameScore = "0", gameScore2 = "0";
    private GameView gv;
    private MediaPlayer mediaPlayer;

    /* Variables for tabulating score & high score */
    private int score = 0, highScore = 0;
    public static long ticks, ticks2;
    public static long easyTimer = 60000, mediumTimer = 60000, hardTimer = 90000;
    public static long timerInterval = 1000;
    public static int easyScore = 10, mediumScore = 15, hardScore = 20;
    public static int calc = 0;
    public static int hold = 0;
    public static int counter = 0;
    public static CountDownTimer cTimer;

    /* Recycler view layout */
    private RecyclerView scoreRecyclerView, mGameRecyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private String[] favorites;

    private Map<String, Integer> mColorForPeripheral = new HashMap<>();
    public String formattedData = "0";

    /* Data connection */
    protected UartPacketManagerBase mUartData;
    protected MqttManager mMqttManager;
    protected TimestampItemAdapter2 mGameItemAdapter;
    protected List<BlePeripheralUart> mBlePeripheralsUart = new ArrayList<>();
    protected final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private int mPacketsCacheLastSize = 0;
    private boolean mIsTimestampDisplayMode = true;
    private int maxPacketsToPaintAsText;
    private volatile SpannableStringBuilder mTextSpanBuffer = new SpannableStringBuilder();
    private final static int kInfoColor = Color.parseColor("#F21625");
    public final static int kDefaultMaxPacketsToPaintAsText = 500;

    /* Runnable that continuously detect for arm rotation */
    private Handler mUIRefreshTimerHandler = new Handler(Looper.getMainLooper());
    private Runnable mUIRefreshTimerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUITimerRunning) {
                if (cTimer != null) {
                    if (gameScore2.equals(gameScore)) {
                        gv.testing = false;
                    } else {
                        gv.testing = true;
                        counter++;
                    }
                    reloadData();
                    gameScore2 = gameScore;
                }
                mUIRefreshTimerHandler.postDelayed(this, 10);
            }
        }
    };
    private boolean isUITimerRunning = false;

    @NonNull
    private BlePeripheral mBlePeripheral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences sp2 = getSharedPreferences("gamesetting", MODE_PRIVATE);
        if (sp2 != null) {
            highScore = sp2.getInt("highscore", 0);
        }

        cTimer = null;

        /* Getting width and height of Android device */
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Constants.SCREEN_WIDTH = dm.widthPixels;
        Constants.SCREEN_HEIGHT = dm.heightPixels;

        setContentView(R.layout.fragment_game1);
        Context context = this.getApplicationContext();

        /* Declaration for all the user interface variables */
        txt_timer = findViewById(R.id.txt_timer);
        txt_high_score = findViewById(R.id.txt_high_score);
        txt_score_over = findViewById(R.id.txt_score_over);
        txt_hs = findViewById(R.id.txt_hs);
        r1_game_over = findViewById(R.id.r1_game_over);
        r1_score_history = findViewById(R.id.r1_score_history);
        btn_start = findViewById(R.id.btn_start);
        btn_scoreHistory = findViewById(R.id.btn_scoreHistory);
        btn_Back = findViewById(R.id.btn_Back);
        btn_Reset = findViewById(R.id.btn_Reset);
        txtView = findViewById(R.id.txtView);
        txtView2 = findViewById(R.id.txtView2);
        mGameRecyclerView = findViewById(R.id.gameRecyclerView);
        gv = findViewById(R.id.gv);
        scoreRecyclerView = findViewById(R.id.scoreRecyclerView);
        btn_test = findViewById(R.id.btn_test);
        over_r1 = findViewById(R.id.over_r1);
        over_r2 = findViewById(R.id.over_r2);
        r1_selection = findViewById(R.id.r1_selection);
        btn_easy = findViewById(R.id.btn_easy);
        btn_medium = findViewById(R.id.btn_medium);
        btn_hard = findViewById(R.id.btn_hard);
        btn_selectBack = findViewById(R.id.btn_selectBack);
        btn_help = findViewById(R.id.btn_help);
        btn_helpBack = findViewById(R.id.btn_helpBack);
        r1_help = findViewById(R.id.r1_help);
        r1_success = findViewById(R.id.r1_success);
        txt_high_score2 = findViewById(R.id.txt_high_score2);
        txt_score_over2 = findViewById(R.id.txt_score_over2);

        txt_hs.setText("High Score: " + highScore);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(INVISIBLE);
                btn_scoreHistory.setVisibility(INVISIBLE);
                txt_hs.setVisibility(INVISIBLE);
                btn_help.setVisibility(INVISIBLE);
                r1_selection.setVisibility(View.VISIBLE);
            }
        });

        /* Incomplete game attempt */
        r1_game_over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(View.VISIBLE);
                txt_hs.setVisibility(View.VISIBLE);
                btn_scoreHistory.setVisibility(View.VISIBLE);
                btn_help.setVisibility(View.VISIBLE);
                r1_game_over.setVisibility(INVISIBLE);
                over_r1.setVisibility(INVISIBLE);

                MyUtility.addFavoriteItem(GameActivity1Test.this, Integer.toString(GameView.score));
                favorites = MyUtility.getFavoriteList(GameActivity1Test.this);

                recyclerViewLayoutManager = new LinearLayoutManager(context);
                scoreRecyclerView.setLayoutManager(recyclerViewLayoutManager);
                recyclerViewAdapter = new TimestampItemAdapter(context, favorites);
                scoreRecyclerView.setAdapter(recyclerViewAdapter);

                SharedPreferences sp = gv.context.getSharedPreferences("gamesetting", MODE_PRIVATE);
                SharedPreferences sp2 = getSharedPreferences("gamesetting", MODE_PRIVATE);

                if (sp != null) {
                    highScore = sp.getInt("highscore", 0);
                    SharedPreferences.Editor editor = sp2.edit();
                    editor.putInt("highscore", highScore);
                    editor.apply();
                }
                txt_hs.setText("High Score: " + highScore);

                onClickRestart();
                gv.setStart(false);
                gv.reset();
            }
        });

        /* Complete game attempt */
        r1_success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(View.VISIBLE);
                txt_hs.setVisibility(View.VISIBLE);
                btn_scoreHistory.setVisibility(View.VISIBLE);
                btn_help.setVisibility(View.VISIBLE);
                r1_success.setVisibility(INVISIBLE);
                over_r2.setVisibility(INVISIBLE);

                MyUtility.addFavoriteItem(GameActivity1Test.this, Integer.toString(score));
                favorites = MyUtility.getFavoriteList(GameActivity1Test.this);

                recyclerViewLayoutManager = new LinearLayoutManager(context);
                scoreRecyclerView.setLayoutManager(recyclerViewLayoutManager);
                recyclerViewAdapter = new TimestampItemAdapter(context, favorites);
                scoreRecyclerView.setAdapter(recyclerViewAdapter);

                SharedPreferences sp = gv.context.getSharedPreferences("gamesetting", MODE_PRIVATE);
                SharedPreferences sp2 = getSharedPreferences("gamesetting", MODE_PRIVATE);

                if (sp != null) {
                    highScore = sp.getInt("highscore", 0);
                    SharedPreferences.Editor editor = sp2.edit();
                    editor.putInt("highscore", highScore);
                    editor.apply();
                }
                txt_hs.setText("High Score: " + highScore);

                onClickRestart();
                gv.setStart(false);
                gv.reset();
            }
        });

        btn_scoreHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r1_score_history.setVisibility(View.VISIBLE);
                btn_start.setVisibility(INVISIBLE);
                txt_hs.setVisibility(INVISIBLE);
                btn_scoreHistory.setVisibility(INVISIBLE);
                btn_help.setVisibility(INVISIBLE);

                favorites = MyUtility.getFavoriteList(GameActivity1Test.this);// returns {"Sports","Entertainment"};
                recyclerViewLayoutManager = new LinearLayoutManager(context);
                scoreRecyclerView.setLayoutManager(recyclerViewLayoutManager);
                recyclerViewAdapter = new TimestampItemAdapter(context, favorites);
                scoreRecyclerView.setAdapter(recyclerViewAdapter);
            }
        });

        /* Back button of score history */
        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r1_score_history.setVisibility(INVISIBLE);
                btn_start.setVisibility(View.VISIBLE);
                txt_hs.setVisibility(View.VISIBLE);
                btn_scoreHistory.setVisibility(View.VISIBLE);
                btn_help.setVisibility(View.VISIBLE);
            }
        });

        btn_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAlert();
            }
        });

        btn_easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStart();
                counter = 0;
                txt_timer.setVisibility(View.VISIBLE);
                r1_selection.setVisibility(INVISIBLE);
                gv.testing = false;
                gv.diff = 1;
                gameScore = "0";
                gameScore2 = "0";
                gv.setStart(true);
                startTimer(gv.diff);
            }
        });

        btn_medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStart();
                counter = 0;
                txt_timer.setVisibility(View.VISIBLE);
                r1_selection.setVisibility(INVISIBLE);
                gv.testing = false;
                gv.diff = 2;
                gameScore = "0";
                gameScore2 = "0";
                gv.setStart(true);

                startTimer(gv.diff);
            }
        });

        btn_hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStart();
                counter = 0;
                txt_timer.setVisibility(View.VISIBLE);
                r1_selection.setVisibility(INVISIBLE);
                gv.testing = false;
                gv.diff = 3;
                gameScore = "0";
                gameScore2 = "0";
                gv.setStart(true);

                startTimer(gv.diff);
            }
        });

        /* Back button of difficulty selection page */
        btn_selectBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(View.VISIBLE);
                btn_scoreHistory.setVisibility(View.VISIBLE);
                txt_hs.setVisibility(View.VISIBLE);
                btn_help.setVisibility(View.VISIBLE);
                r1_selection.setVisibility(INVISIBLE);
            }
        });

        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(INVISIBLE);
                txt_hs.setVisibility(INVISIBLE);
                btn_scoreHistory.setVisibility(INVISIBLE);
                btn_help.setVisibility(INVISIBLE);
                r1_help.setVisibility(View.VISIBLE);
            }
        });

        /* Back button for game help */
        btn_helpBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_start.setVisibility(View.VISIBLE);
                txt_hs.setVisibility(View.VISIBLE);
                btn_scoreHistory.setVisibility(View.VISIBLE);
                btn_help.setVisibility(View.VISIBLE);
                r1_help.setVisibility(INVISIBLE);
            }
        });

        /* Background music for game */
        mediaPlayer = MediaPlayer.create(this, R.raw.sillychipsong);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        if (context != null) {
            mGameRecyclerView = findViewById(R.id.gameRecyclerView);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
            Drawable lineSeparatorDrawable = ContextCompat.getDrawable(context, R.drawable.simpledivideritemdecoration);
            assert lineSeparatorDrawable != null;
            itemDecoration.setDrawable(lineSeparatorDrawable);
            mGameRecyclerView.addItemDecoration(itemDecoration);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mGameRecyclerView.setLayoutManager(layoutManager);
            mGameItemAdapter = new TimestampItemAdapter2(context);
            mGameRecyclerView.setAdapter(mGameItemAdapter);
        }
        setupUart();
        maxPacketsToPaintAsText = kDefaultMaxPacketsToPaintAsText;
        onClickSend();
    }

    /* Alert display before resetting scores from history */
    private void displayAlert() {
        new AlertDialog.Builder(this).setMessage("Are you sure you want to reset the scores?")
                .setTitle("My Alert")
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton){
                                MyUtility.deleteItem(GameActivity1Test.this, "favorites");
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
        isUITimerRunning = false;
        mUIRefreshTimerHandler.removeCallbacksAndMessages(mUIRefreshTimerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
        isUITimerRunning = true;
        mUIRefreshTimerHandler.postDelayed(mUIRefreshTimerRunnable, 0);
    }

    /* Stop countdown timer when Android back button is pressed */
    @Override
    public void onBackPressed()
    {
        if (cTimer != null) {
            cTimer.cancel();
            ticks = 0;
        }
        super.onBackPressed();
    }

    public void startTimer(int diff) {
        if (cTimer != null) {
            cTimer.cancel();
            ticks = 0;
        }

        /* Easy difficulty */
        if (diff == 1) {
            cTimer = new CountDownTimer(easyTimer, timerInterval) {
                public void onTick(long millisUntilFinished) {
                    txt_timer.setText("" + millisUntilFinished / 1000);
                    ticks = millisUntilFinished;
                }

                public void onFinish() {
                    Pipe.speed = 0;
                    onClickRestart();
                    gv.setStart(false);

                    ticks2 = (int) Math.ceil((easyTimer - ticks) / 1000.0);
                    calc = (int)ticks2 + counter;
                    score = easyScore * calc;

                    txt_score_over2.setText("" + score);
                    gv.counter = 0;
                    counter = 0;

                    SharedPreferences sp = gv.context.getSharedPreferences("gamesetting", MODE_PRIVATE);

                    if (sp != null) {
                        highScore = sp.getInt("highscore", 0);
                    }

                    if (score > highScore) {
                        highScore = score;
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("highscore", highScore);
                        editor.apply();
                    }

                    txt_high_score2.setText("High Score: " + highScore);

                    if (over_r1.getVisibility()==INVISIBLE) {
                        txt_timer.setVisibility(INVISIBLE);
                        over_r2.setVisibility(View.VISIBLE);
                        r1_success.setVisibility(View.VISIBLE);
                    }
                }
            };
            cTimer.start();
        }

        /* Medium difficulty */
        else if (diff == 2) {
            cTimer = new CountDownTimer(mediumTimer, timerInterval) {
                public void onTick(long millisUntilFinished) {
                    txt_timer.setText("" + millisUntilFinished / 1000);
                    ticks = millisUntilFinished;
                }

                public void onFinish() {
                    Pipe.speed = 0;
                    onClickRestart();
                    gv.setStart(false);

                    ticks2 = (int) Math.ceil((mediumTimer - ticks) / 1000.0);
                    calc = (int)ticks2 + counter;
                    score = mediumScore * calc;

                    txt_score_over2.setText("" + score);
                    gv.counter = 0;
                    counter = 0;

                    SharedPreferences sp = gv.context.getSharedPreferences("gamesetting", MODE_PRIVATE);

                    if (sp != null) {
                        highScore = sp.getInt("highscore", 0);
                    }

                    if (score > highScore) {
                        highScore = score;
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("highscore", highScore);
                        editor.apply();
                    }

                    txt_high_score2.setText("High Score: " + highScore);

                    if (over_r1.getVisibility()==INVISIBLE) {
                        txt_timer.setVisibility(INVISIBLE);
                        over_r2.setVisibility(View.VISIBLE);
                        r1_success.setVisibility(View.VISIBLE);
                    }
                }
            };
            cTimer.start();
        }

        /* Hard difficulty */
        else if (diff == 3) {
            cTimer = new CountDownTimer(hardTimer, timerInterval) {
                public void onTick(long millisUntilFinished) {
                    txt_timer.setText("" + millisUntilFinished / 1000);
                    ticks = millisUntilFinished;
                }

                public void onFinish() {
                    Pipe.speed = 0;
                    onClickRestart();
                    gv.setStart(false);

                    ticks2 = (int) Math.ceil((hardTimer - ticks) / 1000.0);
                    calc = (int)ticks2 + counter;
                    score = hardScore * calc;

                    txt_score_over2.setText("" + score);
                    gv.counter = 0;
                    counter = 0;

                    SharedPreferences sp = gv.context.getSharedPreferences("gamesetting", MODE_PRIVATE);

                    if (sp != null) {
                        highScore = sp.getInt("highscore", 0);
                    }

                    if (score > highScore) {
                        highScore = score;
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("highscore", highScore);
                        editor.apply();
                    }

                    txt_high_score2.setText("High Score: " + highScore);

                    if (over_r1.getVisibility()==INVISIBLE) {
                        txt_timer.setVisibility(INVISIBLE);
                        over_r2.setVisibility(View.VISIBLE);
                        r1_success.setVisibility(View.VISIBLE);
                    }
                }
            };
            cTimer.start();
        }
    }

    @Override
    public void onUartPacket(UartPacket packet) {
    }

    /* Utility class for adding or deleting scores & high scores */
    public abstract static class MyUtility {

        public static boolean addFavoriteItem(Activity activity, String favoriteItem){
            //Get previous favorite items
            String favoriteList = getStringFromPreferences(activity,null,"favorites");
            // Append new Favorite item
            if(favoriteList!=null){
                favoriteList = favoriteList+","+favoriteItem;
            }else{
                favoriteList = favoriteItem;
            }
            // Save in Shared Preferences
            return putStringInPreferences(activity,favoriteList,"favorites");
        }
        public static String[] getFavoriteList(Activity activity){
            String favoriteList = getStringFromPreferences(activity,null,"favorites");
            return convertStringToArray(favoriteList);
        }
        private static boolean putStringInPreferences(Activity activity,String nick,String key){
            SharedPreferences sharedPreferences = activity.getPreferences(Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, nick);
            editor.commit();
            return true;
        }
        private static String getStringFromPreferences(Activity activity,String defaultValue,String key){
            SharedPreferences sharedPreferences = activity.getPreferences(Activity.MODE_PRIVATE);
            String temp = sharedPreferences.getString(key, defaultValue);
            return temp;
        }

        private static String[] convertStringToArray(String str){
            if (str == null) {
                return null;
            }
            else {
                String[] arr = str.split(",");
                return arr;
            }
        }

        private static boolean deleteItem(Activity activity,String key) {
            SharedPreferences sharedPreferences = activity.getPreferences(Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.commit();
            return true;
        }
    }

    class TimestampItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        // Data
        private Context mContext;
        private String[] favorites;

        TimestampItemAdapter(@NonNull Context context, String[] favorites) {
            super();
            mContext = context;
            this.favorites = favorites;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            ViewGroup mainViewGroup;
            TextView scoreTextView;

            ItemViewHolder(View view) {
                super(view);
                mainViewGroup = view.findViewById(R.id.mainViewGroup);
                scoreTextView = view.findViewById(R.id.scoreTextView);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_score_recycler_view, parent, false);
            return new TimestampItemAdapter.ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.scoreTextView.setText(favorites[position]);

            itemViewHolder.mainViewGroup.setBackgroundColor(position % 2 == 0 ? Color.WHITE : 0xeeeeee);
        }

        @Override
        public int getItemCount() {
            if (favorites == null)
            {
                return 0;
            }
                return favorites.length;
        }
    }

    class TimestampItemAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        // Data
        private Context mContext;
        private boolean mIsEchoEnabled;
        private boolean mShowDataInHexFormat;
        private UartPacketManagerBase mUartData;
        private List<UartPacket> mTableCachedDataBuffer;
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        TimestampItemAdapter2(@NonNull Context context) {
            super();
            mContext = context;
        }

        // ViewHolder
        class ItemViewHolder2 extends RecyclerView.ViewHolder {
            ViewGroup mainViewGroup;
            TextView timestampTextView;
            TextView dataTextView;

            ItemViewHolder2(View view) {
                super(view);
                mainViewGroup = view.findViewById(R.id.mainViewGroup);
                timestampTextView = view.findViewById(R.id.timestampTextView);
                dataTextView = view.findViewById(R.id.dataTextView);
            }
        }

        void setUartData(@Nullable UartPacketManagerBase uartData) {
            mUartData = uartData;
            notifyDataSetChanged();
        }

        int getCachedDataBufferSize() {
            return mTableCachedDataBuffer != null ? mTableCachedDataBuffer.size() : 0;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.layout_uart_packetitem, parent, false);
            return new TimestampItemAdapter2.ItemViewHolder2(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemViewHolder2 itemViewHolder = (ItemViewHolder2) holder;
            UartPacket packet = mTableCachedDataBuffer.get(position);

            final byte[] bytes = packet.getData();
            final String currentDateTimeString = mDateFormat.format(new Date(packet.getTimestamp()));//DateFormat.getTimeInstance().format(new Date(packet.getTimestamp()));
            final String modeString = mContext.getString(packet.getMode() == UartPacket.TRANSFERMODE_RX ? R.string.uart_timestamp_direction_rx : R.string.uart_timestamp_direction_tx);
            final int color = colorForPacket(packet);
            final boolean isBold = isFontBoldForPacket(packet);

            itemViewHolder.timestampTextView.setText(String.format("%s %s", currentDateTimeString, modeString));

            SpannableString text = stringFromPacket(packet, mShowDataInHexFormat, color, isBold);
            formattedData = BleUtils.bytesToText(bytes, true);

            if (cTimer != null) {
                txtView2.setText(formattedData);
                gameScore = formattedData;
                itemViewHolder.dataTextView.setText(text);
            }
            else
            {
                txtView2.setText(formattedData);
                gameScore = formattedData;
                gameScore2 = gameScore;
                itemViewHolder.dataTextView.setText(text);
            }
        }

        @Override
        public int getItemCount() {
            if (mUartData == null) {
                return 0;
            }

            if (mIsEchoEnabled) {
                mTableCachedDataBuffer = mUartData.getPacketsCache();
            } else {
                if (mTableCachedDataBuffer == null) {
                    mTableCachedDataBuffer = new ArrayList<>();
                } else {
                    mTableCachedDataBuffer.clear();
                }

                List<UartPacket> packets = mUartData.getPacketsCache();
                for (int i = 0; i < packets.size(); i++) {
                    UartPacket packet = packets.get(i);
                    if (packet != null && packet.getMode() == UartPacket.TRANSFERMODE_RX) {
                        mTableCachedDataBuffer.add(packet);
                    }
                }
            }
            return mTableCachedDataBuffer.size();
        }
    }

    protected int colorForPacket(UartPacket packet) {
        int color = Color.BLACK;
        final String peripheralId = packet.getPeripheralId();

        if (peripheralId != null) {
            Integer peripheralColor = mColorForPeripheral.get(peripheralId);
            if (peripheralColor != null) {
                color = peripheralColor;
            }
        }
        return color;
    }

    private boolean isFontBoldForPacket(UartPacket packet) {
        return packet.getMode() == UartPacket.TRANSFERMODE_TX;
    }

    private static SpannableString stringFromPacket(UartPacket packet, boolean useHexMode, int color, boolean isBold) {
        final byte[] bytes = packet.getData();
        final String formattedData = BleUtils.bytesToText(bytes, true);
        final String test = formattedData.replace("\n", "");
        final SpannableString formattedString = new SpannableString(test);

        formattedString.setSpan(new ForegroundColorSpan(color), 0, formattedString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            formattedString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, formattedString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return formattedString;
    }

    protected boolean isInMultiUartMode() {
        return mBlePeripheral == null;
    }

    protected void setupUart() {
        // Init
        Context context = getApplicationContext();
        if (context == null) {
            return;
        }
        mUartData = new UartPacketManager(context, this, true, mMqttManager);           // Note: mqttmanager should have been initialized previously
        mGameItemAdapter.setUartData(mUartData);

        // Colors assigned to peripherals
        final int[] colors = UartStyle.defaultColors();

        // Enable uart
        if (isInMultiUartMode()) {
            mColorForPeripheral.clear();
            List<BlePeripheral> connectedPeripherals = BleScanner.getInstance().getConnectedPeripherals();
            for (int i = 0; i < connectedPeripherals.size(); i++) {
                BlePeripheral blePeripheral = connectedPeripherals.get(i);
                mColorForPeripheral.put(blePeripheral.getIdentifier(), colors[i % colors.length]);

                if (!BlePeripheralUart.isUartInitialized(blePeripheral, mBlePeripheralsUart)) {
                    BlePeripheralUart blePeripheralUart = new BlePeripheralUart(blePeripheral);
                    mBlePeripheralsUart.add(blePeripheralUart);
                    blePeripheralUart.uartEnable(mUartData, status -> {

                        String peripheralName = blePeripheral.getName();
                        if (peripheralName == null) {
                            peripheralName = blePeripheral.getIdentifier();
                        }

                        String finalPeripheralName = peripheralName;
                        mMainHandler.post(() -> {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                // Done
                                Log.d("Uart", "Uart enabled for: " + finalPeripheralName);

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                AlertDialog dialog = builder.setMessage(String.format(getString(R.string.uart_error_multipleperiperipheralinit_format), finalPeripheralName))
                                        .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> {
                                        })
                                        .show();
                                DialogUtils.keepDialogOnOrientationChanges(dialog);
                            }
                        });

                    });
                }
            }
        } else {
            if (!BlePeripheralUart.isUartInitialized(mBlePeripheral, mBlePeripheralsUart)) { // If was not previously setup (i.e. orientation change)
                mColorForPeripheral.clear();
                mColorForPeripheral.put(mBlePeripheral.getIdentifier(), colors[0]);
                BlePeripheralUart blePeripheralUart = new BlePeripheralUart(mBlePeripheral);
                mBlePeripheralsUart.add(blePeripheralUart);
                blePeripheralUart.uartEnable(mUartData, status -> mMainHandler.post(() -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Done
                        Log.d("Uart", "Uart enabled");
                    } else {
                        WeakReference<BlePeripheralUart> weakBlePeripheralUart = new WeakReference<>(blePeripheralUart);
                        Context context1 = getApplicationContext();
                        if (context1 != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context1);
                            AlertDialog dialog = builder.setMessage(R.string.uart_error_peripheralinit)
                                    .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> {
                                        BlePeripheralUart strongBlePeripheralUart = weakBlePeripheralUart.get();
                                        if (strongBlePeripheralUart != null) {
                                            strongBlePeripheralUart.disconnect();
                                        }
                                    })
                                    .show();
                            DialogUtils.keepDialogOnOrientationChanges(dialog);
                        }
                    }
                }));
            }
        }
    }

    private void onClickSend() {
        send("r");
        send("g");
    }

    public void onClickRestart() {
        send("r");
    }

    public void onClickStart() {
        send("g");
    }

    protected void send(String message) {
        UartPacketManager uartData = (UartPacketManager) mUartData;
        BlePeripheralUart blePeripheralUart = mBlePeripheralsUart.get(0);
        uartData.send(blePeripheralUart, message);
    }

    public void reloadData() {
        List<UartPacket> packetsCache = mUartData.getPacketsCache();
        final int packetsCacheSize = packetsCache.size();
        if (mPacketsCacheLastSize != packetsCacheSize) {        // Only if the buffer has changed

            if (mIsTimestampDisplayMode) {
                mGameItemAdapter.notifyDataSetChanged();
                final int bufferSize = mGameItemAdapter.getCachedDataBufferSize();
                mGameRecyclerView.smoothScrollToPosition(Math.max(bufferSize - 1, 0));
            }

            else {
                if (packetsCacheSize > maxPacketsToPaintAsText) {
                    mPacketsCacheLastSize = packetsCacheSize - maxPacketsToPaintAsText;
                    mTextSpanBuffer.clear();
                    addTextToSpanBuffer(mTextSpanBuffer, getString(R.string.uart_text_dataomitted) + "\n", kInfoColor, false);
                }
                for (int i = mPacketsCacheLastSize; i < packetsCacheSize; i++) {
                    final UartPacket packet = packetsCache.get(i);
                }
                txtView.setText(mTextSpanBuffer);
            }
            mPacketsCacheLastSize = packetsCacheSize;
        }
    }

    private void addTextToSpanBuffer(SpannableStringBuilder spanBuffer, String text, int color, boolean isBold) {
        final int from = spanBuffer.length();
        spanBuffer.append(text);
        spanBuffer.setSpan(new ForegroundColorSpan(color), from, from + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (isBold) {
            spanBuffer.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), from, from + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
