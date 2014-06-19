package com.funapps.attackofthealiens;

import android.os.Handler;


public class GameModel {

	/*
	 * This class controls
	 */
	private int level, wave;
	public AlienWaveModel alienWave;
	
	//The Runnables run in the Handler until paused.
	private Handler gameHandler;
	private boolean blockNewSpawnCalls, blockNewUpdateCalls;
	
	/*
	 * The spawn thread should turn on after a wave is prepared, and turn off once they have all spawned.
	 * This becomes complicated when the game is paused and resumed.
	 * 
	 * When the game is resumed right before a call to this Runnable, we have two calls, so they spawn twice
	 * as fast. We fix this by starting the spawn with a function that removes existing calls.
	 */
	private Runnable spawnRun=new Runnable(){

		@Override
		public void run() {
			if(blockNewSpawnCalls)return;
			if(alienWave.SpawnAlien())
				gameHandler.postDelayed(this, 200);
		}
		
	};
	
	private Runnable updateRun=new Runnable(){

		@Override
		public void run() {
			if(blockNewUpdateCalls)return;
			alienWave.UpdateAliens();
			gameHandler.postDelayed(this, 30);
		}
		
	};
	
	public GameModel(int width, int height){
		level=1;
		wave=1;
		alienWave=new AlienWaveModel(width, height, this);
		gameHandler=new Handler();
		blockNewSpawnCalls=false;
		blockNewUpdateCalls=false;
	}
	
	public void NewGame(){
		level=1;
		wave=1;
		alienWave.gameView.levelTextView.setText("Level "+level+"/6");
		alienWave.PrepareWave(level, wave);
		StartSpawn();
	}
	
	void GameOver(){
		NewGame();
	}
	
	void LevelComplete(){
		if(wave==3){
			if(level==6){
				//WIN THE GAME TODO
				return;
			}else{
				level++;
				wave=1;
			}
		}else{
			wave++;
		}
		alienWave.gameView.levelTextView.setText("Level "+level+"/6");
		alienWave.PrepareWave(level, wave);
		StartSpawn();
	}
	
	void StartSpawn(){
		blockNewSpawnCalls=true;
		gameHandler.removeCallbacks(spawnRun);
		blockNewSpawnCalls=false;
		gameHandler.post(spawnRun);
	}
	
	public void Pause(){
		blockNewSpawnCalls=true;
		gameHandler.removeCallbacks(spawnRun);
		blockNewUpdateCalls=true;
		gameHandler.removeCallbacks(updateRun);
	}
	
	public void Resume(){
		blockNewSpawnCalls=false;
		blockNewUpdateCalls=false;
		StartSpawn();
		gameHandler.post(updateRun);
	}
}
