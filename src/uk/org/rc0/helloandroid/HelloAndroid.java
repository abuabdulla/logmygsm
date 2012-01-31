package uk.org.rc0.helloandroid;

import android.app.Activity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HelloAndroid extends Activity {

  private TextView latText;
  private TextView lonText;
  private TextView accText;
  private TextView ageText;
  private TextView satText;
  private TextView cidText;
  private TextView lacText;
  private TextView netTypeText;
  private TextView mccmncText;
  private TextView handoffText;
  private TextView dBmText;
  private TextView countText;
  private ToggleButton toggleButton;
  private TextView cidHistoryText;

  private ComponentName myService;
  private DisplayUpdateReceiver myReceiver;

  /** Called when the activity is first created. */
  @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main_new);
      latText = (TextView) findViewById(R.id.latitude);
      lonText = (TextView) findViewById(R.id.longitude);
      accText = (TextView) findViewById(R.id.accuracy);
      ageText = (TextView) findViewById(R.id.age);
      satText = (TextView) findViewById(R.id.sat);
      cidText = (TextView) findViewById(R.id.cid);
      lacText = (TextView) findViewById(R.id.lac);
      netTypeText = (TextView) findViewById(R.id.network_type);
      mccmncText = (TextView) findViewById(R.id.mccmnc);
      handoffText = (TextView) findViewById(R.id.handoffs);
      dBmText = (TextView) findViewById(R.id.dBm);
      countText = (TextView) findViewById(R.id.count);
      toggleButton = (ToggleButton) findViewById(R.id.toggleBgLog);
      cidHistoryText = (TextView) findViewById(R.id.cid_history);
    }

  @Override
    public void onStart() {
      super.onStart();
    }

  @Override
    public void onStop() {
      super.onStop();
    }

    @Override
    public void onResume () {
      Logger.stop_tracing = false;
      myService = startService(new Intent(this, Logger.class));
      IntentFilter filter;
      filter = new IntentFilter(Logger.DISPLAY_UPDATE);
      myReceiver = new DisplayUpdateReceiver();
      registerReceiver(myReceiver, filter);
      updateDisplay();
      super.onResume();
    }

    @Override
    public void onPause() {
      unregisterReceiver(myReceiver);
      if (toggleButton.isChecked()) {
        // We are going to keep the service alive as a background logger
      } else {
        Logger.stop_tracing = true;
        stopService(new Intent(this, myService.getClass()));
      }
      super.onPause();
    }

  private void updateCidHistory(long current_time) {
    StringBuffer out = new StringBuffer();
    // There's no point in showing the current cell as that's shown in other fields
    for (int i=1; i<Logger.MAX_RECENT; i++) {
      if ((Logger.recent_cids != null) &&
          (Logger.recent_cids[i] != null) &&
          (Logger.recent_cids[i].cid >= 0)) {
          long age = (500 + current_time - Logger.recent_cids[i].lastMillis) / 1000;
          if (age < 60) {
            String temp = String.format("%8d    0:%02d %4d\n",
                Logger.recent_cids[i].cid,
                age,
                Logger.recent_cids[i].handoff);
            out.append(temp);
          } else {
            String temp = String.format("%8d  %3d:%02d %4d\n",
                Logger.recent_cids[i].cid,
                age / 60,
                age % 60,
                Logger.recent_cids[i].handoff);
            out.append(temp);
          }
      }
    }

    cidHistoryText.setText(out);
  }

  private void updateDisplay() {
    long current_time = System.currentTimeMillis();
    if (Logger.validFix) {
      long age = (500 + current_time - Logger.lastFixMillis) / 1000;
      String latString = String.format("%+09.4f", Logger.lastLat);
      String lonString = String.format("%+09.4f", Logger.lastLon);
      String accString = String.format("%dm", Logger.lastAcc);
      String ageString = String.format("%ds", age);
      latText.setText(latString);
      lonText.setText(lonString);
      accText.setText(accString);
      ageText.setText(ageString);
    } else {
      latText.setText("???");
      lonText.setText("???");
      accText.setText("?m");
      ageText.setText("?s");
    }
    String satString = String.format("%d/%d/%d",
        Logger.last_fix_sats,
        Logger.last_ephem_sats, Logger.last_alman_sats);
    String cidString = String.format("%d",
        Logger.lastCid);
    String lacString = String.format("%d", Logger.lastLac);
    String mccmncString = String.format("%s", Logger.lastMccMnc);
    String handoffString = String.format("%d ha",
        Logger.nHandoffs);
    String dBmString = String.format("%ddBm", Logger.lastdBm);
    satText.setText(satString);

    cidText.setText(cidString);
    switch (Logger.lastState) {
      case 'A':
        cidText.setTextColor(Color.WHITE);
        break;
      default:
        cidText.setTextColor(Color.RED);
        break;
    }

    lacText.setText(lacString);
    netTypeText.setText(Logger.lastNetworkTypeLong);
    mccmncText.setText(mccmncString);
    handoffText.setText(handoffString);
    dBmText.setText(dBmString);

    String countString = String.format("%d pt", Logger.nReadings);
    countText.setText(countString);

    updateCidHistory(current_time);
  }

  // --------------------------------------------------------------------------
  //

  public class DisplayUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      updateDisplay();
    }
  }

}
