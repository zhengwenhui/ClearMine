package com.huige.mines;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SetActivity extends Activity {
	//	static final String SETTING_INFOS = "SETTING_INFOS";
	static final String OPEN_SOUND = "OPEN_SOUND";
	static final String DIFFICULTY = "DIFFICULTY";
	static final String PRIMARY = "PRIMARY";
	static final String MIDDLE = "MIDDLE";
	static final String HIGH = "HIGH";

	private CheckBox openSound;
	private RadioGroup difficulty;

	private SoundPool soundPool;
	private int clickSound;
	private boolean OpenSound = true;  

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_layout);
		initSoundPool();
		openSound = (CheckBox)findViewById(R.id.openSoundCheckBox);
		difficulty = (RadioGroup)findViewById(R.id.myRadioGroup);
		RadioButton radioButton = (RadioButton)(difficulty.getChildAt(getDifficulty(this)));
		radioButton.setChecked(true);
		openSound.setChecked(getOpenSound(this));
	}

	public static int getPrimary(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		//		SharedPreferences settings = context.getSharedPreferences(SETTING_INFOS, 0);
		return settings.getInt(PRIMARY, -1);
	}
	public static int getMiddle(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		//		SharedPreferences settings = context.getSharedPreferences(SETTING_INFOS, 0);
		return settings.getInt(MIDDLE, -1);
	}
	public static int getHigh(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		//		SharedPreferences settings = context.getSharedPreferences(SETTING_INFOS, 0);
		return settings.getInt(HIGH, -1);
	}

	public static void setPrimary(Context context,int value){
		Editor editor  = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putInt(PRIMARY, value);
		editor.commit();
	}
	public static void setMiddle(Context context,int value){
		Editor editor  = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putInt(MIDDLE, value);
		editor.commit();
	}
	public static void setHigh(Context context,int value){
		Editor editor  = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putInt(HIGH, value);
		editor.commit();
	}

	public static boolean getOpenSound(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		//		SharedPreferences settings = context.getSharedPreferences(SETTING_INFOS, 0);
		return settings.getBoolean(OPEN_SOUND, true);
	}

	public static int getDifficulty(Context context){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		//		SharedPreferences settings = context.getSharedPreferences(SETTING_INFOS, 0);
		Log.i("zheng", "difficulty settings:"+settings.getInt(DIFFICULTY, 0));
		return settings.getInt(DIFFICULTY, 0);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Editor editor  = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editor.putBoolean(OPEN_SOUND, openSound.isChecked());
			RadioButton rButton;
			for(int i = 0; i<difficulty.getChildCount(); i++){
				rButton = (RadioButton) difficulty.getChildAt(i);
				if(rButton.isChecked()){
					editor.putInt(DIFFICULTY, i);
					break;
				}
			}
			editor.commit();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onClick(View view){
		if(view.getId()==R.id.openSoundCheckBox){
			OpenSound = ((CheckBox)view).isChecked();
		}
		playSoundPool();
	}

	private boolean initSoundPool(){

		OpenSound = SetActivity.getOpenSound(this);
		//指定声音池的最大音频流数目为10，声音品质为5  
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);  
		//载入音频流，返回在池中的id  
		clickSound = soundPool.load(this, R.raw.command, 0);
		return OpenSound;
	}

	private void playSoundPool(){
		if(OpenSound){
			soundPool.play(clickSound, 0.5f, 0.5f, 0, 0, 1);
		}
	}
}
