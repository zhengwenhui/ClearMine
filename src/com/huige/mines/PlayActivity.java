package com.huige.mines;

import java.util.Random;

import net.youmi.android.AdManager;
import net.youmi.android.AdView;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PlayActivity extends Activity implements OnClickListener,OnLongClickListener{
	private TableLayout tableLayout;
	private ImageButton[][] views;
	private byte [][] field;
	private Status[][] status;
	private int x_count=0,y_count=0;

	private final static int PROBABILITY = 6;

	private TextView timerTextView;
	private TextView minesTextView;
	//	private Button openButton;
	private int seconds = 0;
	private int mines = 0;

	private SoundPool soundPool;
	private int clickSound;  
	private int longclickSound; 

	private boolean countTime = false;
	private boolean gameing = false;
	private boolean OpenSound = true;

	private int difficulty;

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		public void run() {
			if(countTime){
				seconds++;
				timerTextView.setText(String.valueOf(seconds));
			} 
			handler.postDelayed(this, 1000);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AdManager.init(this,"d72e9f61b58f5ee5", "fb9dfc2456866caf",  30, false);
		setContentView(R.layout.play_layout);

		final FrameLayout view = (FrameLayout) findViewById(R.id.ad);
		//初始化广告视图
		AdView adView = new AdView(this);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		//设置广告出现的位置(悬浮于屏幕右下角)		 
		params.gravity=Gravity.BOTTOM|Gravity.RIGHT; 
		//将广告视图加入Activity中
		view.addView(adView,params);


		Button delete = new Button(this);
		delete.setBackgroundResource(R.drawable.delete_selector);
		delete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//				view.setVisibility(View.GONE);
				Animation alAnimation = new AlphaAnimation(1.0f, 0.0f);
				alAnimation.setFillAfter(true);
				alAnimation.setDuration(500);
				view.setAnimation(alAnimation);
			}
		});

		FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		view.addView(delete,params2);

		initSoundPool();
		tableLayout = (TableLayout)findViewById(R.id.mainfiled);
		timerTextView = (TextView)findViewById(R.id.timer);
		minesTextView = (TextView)findViewById(R.id.mines); 
		//		openButton = (Button)findViewById(R.id.open); 
		initCount();
		openButtonClick(null);
		handler.postDelayed(runnable, 1000);
	}

	private void initCount(){
		difficulty = SetActivity.getDifficulty(this);
		switch (difficulty) {
		case 0:
			x_count=4;
			y_count=6;
			break;
		case 1:
			x_count=6;
			y_count=9;
			break;
		case 2:
			x_count=8;
			y_count=12;
			break;

		default:
			x_count=6;
			y_count=9;
			difficulty = 0;
			break;
		}
	}

	public void openButtonClick(View view){
		playSoundPool(1);
		gameing = true;
		countTime = false;
		seconds=0;
		timerTextView.setText(String.valueOf(seconds));
		Display display = getWindowManager().getDefaultDisplay();
		tableLayout.removeAllViews();
		addViews(x_count,y_count,display.getWidth(),display.getHeight());
		InitMineField(x_count,y_count);
	}

	private void addViews(int x,int y,int width,int height){
		views = new ImageButton[x][y];
		TableRow tableRow;
		ImageButton button;
		
		int w = (width-6)/x;
		int h = (height-145)/y;

		tableLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		//		tableLayout.setPadding(10, 10, 10, 10);
		for(int row=0,line; row<y; row++){
			tableRow = new TableRow(this);
			tableRow.setGravity(Gravity.CENTER);
			for(line=0; line<x; line++){
				button = new ImageButton(this); 
				button.setBackgroundResource(R.drawable.normal);
				button.setImageResource(R.drawable.space);
				button.setOnClickListener(this);
				button.setOnLongClickListener(this);
				views[line][row] = button;
				tableRow.addView(button,new TableRow.LayoutParams(w, h));
			}
			tableLayout.addView(tableRow);
		}
		tableLayout.requestLayout();
	}

	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if(!gameing){
			return true;
		}

		playSoundPool(0);

		countTime = true;

		int line = 0,row = 0;
		for(line =0; line<x_count; line++){
			for(row =0; row<y_count; row++){
				if(views[line][row]==v){
					longClick(v,line,row);
					break;
				}
			}
		}
		return true;
	}

	public void longClick(View view,int line,int row){
		Status dot = status[line][row];
		switch (dot) {
		case UNKNOW:
		case FLAG:
			Open(line,row);
			break;
		case OPEN:
			OpenSurround(line,row);
			break;
		default:
			break;
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(!gameing){
			return;
		}
		playSoundPool(1);
		int line,row = 0;

		countTime = true;

		for(line =0; line<x_count; line++){
			for(row =0; row<y_count; row++){
				if(views[line][row]==v){
					click(v,line,row);
					break;
				}
			}
		}

	}
	private void click(View view,int line,int row){
		ImageButton btn = (ImageButton)view;

		switch (status[line][row]) {
		case UNKNOW:
			status[line][row] = Status.FLAG;
			minusMines();
			btn.setImageResource(R.drawable.flag);
			break;
		case FLAG:
			status[line][row] = Status.UNKNOW;
			btn.setImageResource(R.drawable.space);
			plusMines();
			break;
			/*case OPEN:
			break;*/
		default:
			break;
		}
	}

	private void OpenSurround(int line, int row){
		byte dot = field[line][row];
		if(dot>0){
			int flagCount = flagCountSurround(line, row);
			int unknowCount = unknowCountSurround(line, row);

			if(flagCount == dot){
				//open all unknow
				openAllUnknowSurround(line, row);
			}
			else if((flagCount+unknowCount) == dot){
				//flag all unknow
				flagAllUnknowSurround(line, row);
			}
		}
	}

	private void setOpenImage(int line, int row){
		int resid = 0;
		switch (field[line][row]) {
		case 1:
			resid = R.drawable.one;
			break;
		case 2:
			resid = R.drawable.two;
			break;
		case 3:
			resid = R.drawable.three;
			break;
		case 4:
			resid = R.drawable.four;
			break;
		case 5:
			resid = R.drawable.five;
			break;
		case 6:
			resid = R.drawable.six;
			break;
		case 7:
			resid = R.drawable.seven;
			break;
		case 8:
			resid = R.drawable.eight;
			break;	
		default:
			break;
		}
		if(resid>=0){
			((ImageButton)views[line][row]).setImageResource(resid);
		}

	}

	private void openMulti(int line, int row){
		try {
			if(status[line][row]!=Status.OPEN){

				if(status[line][row]==Status.FLAG){
					plusMines();
				}

				//open this
				views[line][row].setBackgroundResource(R.drawable.open);
				status[line][row] = Status.OPEN;
				setOpenImage(line, row);
				if(0 == field[line][row]){
					openMulti(line-1,row-1);
					openMulti(line-1,row);
					openMulti(line-1,row+1);
					openMulti(line,row-1);
					openMulti(line,row+1);
					openMulti(line+1,row-1);
					openMulti(line+1,row);
					openMulti(line+1,row+1);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 *  状态
	 **/
	private enum Status{
		UNKNOW,      //未打开
		FLAG,		 //标记
		OPEN,        //已打开
	}

	private void countSurrendMines(int line,int row){
		try {
			if(field[line][row] != -1){
				field[line][row]++;
			}
		} catch (Exception e) {
			// TODO: handle exception 1
		}
	}

	/**
	 *  初始化地雷阵
	 **/
	private void InitMineField(int width,int height){
		field = new byte[width][height];
		status = new Status[width][height];
		Random random = new Random();
		int line,row;
		mines = 0;
		for(line =0; line<width; line++){
			for(row =0; row<height; row++){
				status[line][row] = Status.UNKNOW;
				if (0==random.nextInt(PROBABILITY)){
					//该位置是地雷
					field[line][row] = -1;      
					mines++; 
					//周围位置的数字加1（如果不是地雷）
					countSurrendMines(line-1,row-1);
					countSurrendMines(line-1,row);
					countSurrendMines(line-1,row+1);
					countSurrendMines(line,row-1);		
					countSurrendMines(line,row+1);
					countSurrendMines(line+1,row-1);
					countSurrendMines(line+1,row);
					countSurrendMines(line+1,row+1);
				}
			}
		}
		minesTextView.setText(String.valueOf(mines));
		
		if(mines<1){
			openButtonClick(null);
		}
	}
	/**
	 * 当前位置的状态是否为UNKNOW，若是UNKNOW返回1，该位置不存在或者不为UNKNOW返回0
	 **/
	private int isUnknow(int line, int row){
		try {
			if(Status.UNKNOW == status[line][row]){
				return 1;
			}
		} catch (Exception e) {
			// TODO: handle exception 1
			return 0;
		}
		return 0;
	}

	/**
	 * 返回当前位置周围的状态为Flag的个数
	 **/
	private int unknowCountSurround(int line, int row){
		int count = 0;
		count += isUnknow(line-1, row-1);
		count += isUnknow(line-1, row);
		count += isUnknow(line-1, row+1);

		count += isUnknow(line, row-1);
		count += isUnknow(line, row+1);

		count += isUnknow(line+1, row-1);
		count += isUnknow(line+1, row);
		count += isUnknow(line+1, row+1);	

		return count;
	}


	/**
	 * 当前位置的状态是否为FLAG，若是FLAG返回1，该位置不存在或者不为FLAG返回0
	 **/
	private int isFlag(int line, int row){
		try {
			if(Status.FLAG == status[line][row]){
				return 1;
			}
		} catch (Exception e) {
			// TODO: handle exception 1
			return 0;
		}
		return 0;
	}

	/**
	 * 返回当前位置周围的状态为Flag的个数
	 **/
	private int flagCountSurround(int line, int row){
		int count = 0;
		count += isFlag(line-1, row-1);
		count += isFlag(line-1, row);
		count += isFlag(line-1, row+1);

		count += isFlag(line, row-1);
		count += isFlag(line, row+1);

		count += isFlag(line+1, row-1);
		count += isFlag(line+1, row);
		count += isFlag(line+1, row+1);	

		return count;
	}
	/**
	 * 遍历该位置周围，将周围状态为UNKNOW的位置标记为OPEN
	 **/
	private void openAllUnknowSurround(int line,int row){
		openIfUnknow(line-1,row-1);
		openIfUnknow(line-1,row);
		openIfUnknow(line-1,row+1);

		openIfUnknow(line,row-1);
		openIfUnknow(line,row+1);

		openIfUnknow(line+1,row-1);
		openIfUnknow(line+1,row);
		openIfUnknow(line+1,row+1);
	}

	private void openIfUnknow(int line,int row){
		try {
			if(Status.UNKNOW == status[line][row]){
				Open(line, row);
			}
		} catch (Exception e) {
			// TODO: handle exception 8
		}
	}

	private void Open(int line, int row) {
		// TODO Auto-generated method stub
		if(-1 == field[line][row]){
			//game over
			views[line][row].setBackgroundResource(R.drawable.ismine);
			views[line][row].setImageResource(R.drawable.mine);
			mines = 0;
			countTime = false;
			gameing = false;
			//			Toast.makeText(this, "Game Over", Toast.LENGTH_LONG).show();
			lostDialog();
		}
		else{
			openMulti(line, row);
		}
	}

	/**
	 * 遍历该位置周围，将周围状态为UNKNOW的位置标记为FLAG
	 **/
	private void flagAllUnknowSurround(int line,int row){
		flag(line-1,row-1);
		flag(line-1,row);
		flag(line-1,row+1);

		flag(line,row-1);
		flag(line,row+1);

		flag(line+1,row-1);
		flag(line+1,row);
		flag(line+1,row+1);
	}

	/**
	 * 如果该处状态为UNKNOW，标记该处，FLAG
	 **/
	private void flag(int line,int row){
		try {
			if(Status.UNKNOW == status[line][row]){
				status[line][row] = Status.FLAG;
				views[line][row].setImageResource(R.drawable.flag);
				minusMines();    //地雷数目减去1
			}
		} catch (Exception e) {
			// TODO: handle exception 8
		}
	}

	/**
	 * 地雷的数目减去1，如果数目已经等于0或者小于0，游戏结束
	 **/
	private void minusMines(){
		if((--mines)==0 && isWin()){
			countTime = false;
			gameing = false;
			//Toast.makeText(this, "You Win!", Toast.LENGTH_LONG).show();
			win();
		}
		minesTextView.setText(String.valueOf(mines));
	}

	private boolean isWin(){
		int line,row;
		for(line =0; line<x_count; line++){
			for(row =0; row<y_count; row++){
				if( -1==field[line][row] ^ Status.FLAG==status[line][row] ){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 地雷的数目减去1，如果数目已经等于0或者小于0，游戏结束
	 **/
	private void plusMines(){
		mines++;
		minesTextView.setText(String.valueOf(mines));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Do something.
			handler.removeCallbacks(runnable);
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean initSoundPool(){

		OpenSound = SetActivity.getOpenSound(this);
		if(OpenSound){
			//指定声音池的最大音频流数目为10，声音品质为5  
			soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);  
			//载入音频流，返回在池中的id  
			longclickSound = soundPool.load(this, R.raw.command, 0);  
			clickSound = soundPool.load(this, R.raw.start, 0);
		}
		return OpenSound;
	}

	private void playSoundPool(int type){
		if(OpenSound){
			switch (type) {
			case 0:
				soundPool.play(longclickSound, 0.5f, 0.5f, 0, 0, 1);
				break;
			case 1:
				soundPool.play(clickSound, 0.5f, 0.5f, 0, 0, 1);
				break;

			default:
				break;
			}
		}
	}

	private void win(){

		int value;
		String message = "成功了！";

		int line,row;
		for(line =0; line<x_count; line++){
			for(row =0; row<y_count; row++){
				if(status[line][row] == Status.UNKNOW){
					Open(line,row);
				}
			}
		}


		switch (difficulty) {
		case 0:
			value = SetActivity.getPrimary(this);
			if(value<=0 || (value>0 && seconds<value)){
				message += "\n初级 最佳 ";
				SetActivity.setPrimary(this, seconds);
			}

			break;
		case 1:
			value = SetActivity.getMiddle(this);
			if(value<0 || (value>0 && seconds<value)){
				message += "\n中级 最佳 ";
				SetActivity.setMiddle(this, seconds);
			}
			break;
		case 2:
			value = SetActivity.getHigh(this);
			if(value<0 || (value>0 && seconds<value)){
				message += "\n高级 最佳 ";
				SetActivity.setHigh(this, seconds);
			}
			break;

		default:
			break;
		}
		message +="\n用时"+seconds+"秒";


		winDialog(message);
		//		Toast.makeText(this, "You Win!\n"+message+seconds, Toast.LENGTH_LONG).show();
	}

	private void lostDialog(){

		int line,row;
		for(line =0; line<x_count; line++){
			for(row =0; row<y_count; row++){
				if(-1 == field[line][row] && status[line][row] != Status.FLAG){
					//game over
					views[line][row].setBackgroundResource(R.drawable.ismine);
					views[line][row].setImageResource(R.drawable.mine);
				}
			}
		}

		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.dialog_layout);
		Button okButton = (Button) dialog.findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				playSoundPool(1);
				dialog.cancel();
			}
		});
	}
	private void winDialog(String message){
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.show();
		dialog.getWindow().setContentView(R.layout.dialog_layout);


		Drawable d = getResources().getDrawable(R.drawable.win);
		d.setBounds(new Rect(0, 0, 50, 50));
		TextView title = (TextView) dialog.findViewById(R.id.title);
		title.setCompoundDrawables(d, null, null, null);

		TextView msgTextView = (TextView) dialog.findViewById(R.id.message);

		msgTextView.setText(message);

		Button okButton = (Button) dialog.findViewById(R.id.ok);
		okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				playSoundPool(1);
				dialog.cancel();
			}
		});
	}
}
