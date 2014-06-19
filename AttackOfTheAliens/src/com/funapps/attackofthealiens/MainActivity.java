package com.funapps.attackofthealiens;


import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
/*
APP DESCRIPTION:
Aliens are invading your planet, not that it's very interesting.
Knock their fedora-shaped spaceships out of the sky before they take over and ruin everything!

Extra features:
-show fedora person at end of 10 levels
-have dead spaceships randomly burst into flames

Have planet at the bottom, most of the screen is the sky

Level starts
They come from the top in 3 waves of 10, stopping at random x coordinates
They speed up with the level

There should also be a pause function

 */
public class MainActivity extends Activity {

	GameModel myModel;
	PlanetView myView;

	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("WHY");
		
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	    //Remove notification bar
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	   //set content view AFTER ABOVE sequence (to avoid crash)
		setContentView(R.layout.activity_main);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		
		myModel=new GameModel(width, height);
		myView=(PlanetView) findViewById(R.id.myPlanetView);

		
		myModel.alienWave.gameView=myView;
		myView.myModel=myModel;
		
		
        myView.levelTextView=((TextView) findViewById(R.id.text));
        
		//Game starts once PlanetView is ready (when surfaceCreated is called)
        myModel.NewGame();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}

}
