package com.emercy.shake_alarm;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */

	private Button btn;									// 設定鬧鐘按钮
	private ToggleButton btn_enClk;						// 開\關按钮
	private ToggleButton togbtn_AlarmStyle;

	private SharedPreferences sharedData;
	SharedPreferences.Editor edit;
	private static boolean alarmStyle = true;			// 鬧鐘提示方式 (true:鈴聲;false:振動)

	Calendar c = Calendar.getInstance();

	final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

	static MainActivity instance;
	static String shakeSenseValue;

	public static void setAlarmStyle(boolean style)
	{
		alarmStyle = style;
	}

	public static boolean getAlarmStyle()
	{
		return alarmStyle;
	}

	private void loadData()
	{
		sharedData = getSharedPreferences("main_activity", MODE_PRIVATE);
		edit = sharedData.edit();
		btn.setText(sharedData.getString("time",
				sdf.format(new Date(c.getTimeInMillis()))));
		btn_enClk.setChecked(sharedData.getBoolean("on_off", false));
	}

	private void saveData()
	{
		edit.putString("time", btn.getText().toString());
		edit.putBoolean("on_off", btn_enClk.isChecked());
		edit.commit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		instance = this;										// 用於在ShakeAlarm中關閉此activity
		shakeSenseValue = getResources().getString(R.string.shakeSenseValue_2);
		String timeOnBtn = "";

		timeOnBtn = sdf.format(new Date(c.getTimeInMillis()));

		ButtonListener buttonListener = new ButtonListener();	// 註冊設定時間按钮事件
		btn = (Button) findViewById(R.id.btn_setClock);
		btn.setText(timeOnBtn);
		btn.setOnClickListener(buttonListener);

		btn_enClk = (ToggleButton) findViewById(R.id.btn_enClk); // 註冊開關按钮事件
		btn_enClk.setOnClickListener(buttonListener);

		loadData();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		saveData();
	}

	class ButtonListener implements OnClickListener
	{
		private TimePicker timePicker;			// 時間設定

		private PendingIntent pi;
		private Intent intent;
		AlarmManager alarmManager;
		LayoutInflater inflater;
		LinearLayout setAlarmLayout;

		/**
		 * 在ButtonListener中加载對話框的布局
		 */
		public ButtonListener()
		{
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);		// 用於加载alertdialog布局
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			setAlarmLayout = (LinearLayout) inflater.inflate(
					R.layout.alarm_dialog, null);
		}

		private void enableClk()
		{
			timePicker = (TimePicker) setAlarmLayout
					.findViewById(R.id.timepicker);
			c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());        // 設置鬧鐘小時
			c.set(Calendar.MINUTE, timePicker.getCurrentMinute());            // 設置鬧鐘的分鐘
			c.set(Calendar.SECOND, 0); // 設置鬧鐘的秒
			c.set(Calendar.MILLISECOND, 0); // 設置鬧鐘的毫秒

			// if (c.getTimeInMillis() - System.currentTimeMillis() < 0)
			// {
			// c.roll(Calendar.DATE, 1);
			// }

			btn.setText(sdf.format(new Date(c.getTimeInMillis())));
			intent = new Intent(MainActivity.this, AlarmReceiver.class);    // 創建Intent對象
			pi = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);    // 創建PendingIntent

			alarmManager.setRepeating(AlarmManager.RTC,    // 設置鬧鐘，當到時間就喚醒
					c.getTimeInMillis(), 24 * 60 * 60 * 1000, pi);
		}

		private void disableClk()
		{
			alarmManager.cancel(pi);
		}

		@Override
		public void onClick(View v)
		{

			switch (v.getId())
			{
			case R.id.btn_setClock:

				setAlarmLayout = (LinearLayout) inflater.inflate(
						R.layout.alarm_dialog, null);

				togbtn_AlarmStyle = (ToggleButton) setAlarmLayout
						.findViewById(R.id.togbtn_alarm_style);
				togbtn_AlarmStyle.setChecked(sharedData.getBoolean("style",
						false));
				timePicker = (TimePicker) setAlarmLayout
						.findViewById(R.id.timepicker);
				timePicker.setIs24HourView(true);

				new AlertDialog.Builder(MainActivity.this)
						.setView(setAlarmLayout)
						.setTitle("設定鬧鐘時間")
						.setPositiveButton("確定",
								new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog,
											int which)
									{
										disableClk();
										enableClk();
										if (togbtn_AlarmStyle.isChecked())
										{
											MainActivity.setAlarmStyle(true);
										}
										else
										{
											MainActivity.setAlarmStyle(false);
										}

										edit.putBoolean("style",
												togbtn_AlarmStyle.isChecked());
										btn_enClk.setChecked(true);
										Toast.makeText(MainActivity.this,
												"鬧鐘設定成功", Toast.LENGTH_LONG)
												.show();// 提示用户
									}
								}).setNegativeButton("取消", null).show();
				break;

			case R.id.btn_enClk:
				if (btn_enClk.isChecked())
				{
					enableClk();
				}
				else
				{
					disableClk();
				}
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		SubMenu subMenu = menu.addSubMenu("搖晃力度");
		subMenu.add(1, 1, 1, "温柔甩");
		subMenu.add(1, 2, 2, "正常甩");
		subMenu.add(1, 3, 3, "暴力甩");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case 1:
			shakeSenseValue = getResources().getString(
					R.string.shakeSenseValue_1);
			Toast.makeText(this, "温柔甩設定成功", Toast.LENGTH_SHORT).show();
			break;

		case 2:
			shakeSenseValue = getResources().getString(
					R.string.shakeSenseValue_2);
			Toast.makeText(this, "正常甩設定成功", Toast.LENGTH_SHORT).show();
			break;

		case 3:
			shakeSenseValue = getResources().getString(
					R.string.shakeSenseValue_3);
			Toast.makeText(this, "暴力甩設定成功", Toast.LENGTH_SHORT).show();
			break;

		case R.id.menu_about:
			new AlertDialog.Builder(this).setTitle("關於").setMessage("摇摇鬧鐘")
					.setNegativeButton("確定", null).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}