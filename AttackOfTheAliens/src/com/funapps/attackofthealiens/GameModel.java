package com.funapps.attackofthealiens;

import java.util.Random;

import android.os.Handler;
import android.view.MotionEvent;

public class GameModel {

	GameControllerAndView gameControllerAndView;
	
	int screenWidth, screenHeight;//used in various places
	
	//Model of overall game state
	GameState gameState;
	
	enum GameState{
		start,//initial state
		running,//comes after tapping start or paused screen
		paused,//after minimizing
		lose,//one hits the ground
		win//killed last one
	}

	//Model of actual gameplay
	RunningModel runningModel;
	
	int level, wave;
	
	public GameModel(int width, int height){//positive
		screenWidth=width;
		screenHeight=height;
        gameState=GameState.start;
        runningModel=new RunningModel();
	}
	
	public void TouchEvent(MotionEvent event){

		System.out.println("TOUCH3 "+gameState);
		if(gameState==GameState.start){
			gameState=GameState.running;
			StartNewGame();
			Resume();
		}else if(gameState==GameState.running){
			runningModel.alienWave.Shoot((int)event.getX(), (int)event.getY());
		}else if(gameState==GameState.paused){
			gameState=GameState.running;
			runningModel.Resume();
		}else if(gameState==GameState.lose){
			gameState=GameState.running;
			StartNewGame();
			Resume();
		}else if(gameState==GameState.win){
			//you've won!
		}
		
	}
	
	public void Resume(){
		if(gameState==GameState.start){
			gameControllerAndView.SetStartView();
		}else if(gameState==GameState.running){
			runningModel.Resume();
		}else if(gameState==GameState.paused){
			gameControllerAndView.SetPausedView();
		}else if(gameState==GameState.lose){
			gameControllerAndView.SetLoseView();
		}else if(gameState==GameState.win){
			gameControllerAndView.SetWinView();
		}
	}
	
	public void Pause(){
		if(gameState==GameState.start){
			
		}else if(gameState==GameState.running){
			gameState=GameState.paused;
			runningModel.Pause();
		}else if(gameState==GameState.paused){
			
		}else if(gameState==GameState.lose){
			
		}else if(gameState==GameState.win){
			//you've already won!
		}
	}
	
	private void Win(){
		Pause();
		gameState=GameState.win;
		gameControllerAndView.NotifyWin();
	}
	
	private void Lose(){
		System.out.println("LOSE");
		Pause();
		gameState=GameState.lose;
		gameControllerAndView.NotifyLose();
	}
	
	private void StartNewGame(){
		level=1;
		wave=1;
		RunWave();
	}
	
	private void RunWave(){
		runningModel.alienWave.PrepareWave(level, wave);
		runningModel.StartSpawn();
	}
	
	private void OnLevelComplete(){
		if(wave==3){
			if(level==6){
				Win();
				return;
			}else{
				level++;
				wave=1;
			}
		}else{
			wave++;
		}
		RunWave();
	}
	
	private void OnGameOver(){
		StartNewGame();
	}
	
	/*
	 * The RunningModel class controls the Handler and Runnables which send actions to the AlienWave
	 * 
	 * There are two Runnables. The first one (updateRun) is simple.
	 * 
	 * It runs between Resume and Pause. Initially, the class is considered paused.
	 */

	//State of an individual Alien
	enum State{
		inactive,
		alive,
		fall,
		dead
	}
	
	public class RunningModel {

		public AlienWave alienWave;
		
		//The Runnables run in the Handler until paused.
		private Handler gameHandler;
		private boolean blockNewSpawnCalls, blockNewUpdateCalls;
		
		private Runnable spawnRun=new Runnable(){

			@Override
			public void run() {
				if(blockNewSpawnCalls)return;
				
				if(alienWave.SpawnAlien())
					gameHandler.postDelayed(this, 200);
				
				if(blockNewSpawnCalls)
					gameHandler.removeCallbacks(this);
			}
			
		};
		
		private Runnable updateRun=new Runnable(){

			@Override
			public void run() {
				//If blocked here, good.
				if(blockNewUpdateCalls)return;
				
				alienWave.UpdateAliens();
				//A new one could be posted, but the following code removes it.
				//If the new one posted starts happening before the remove, it will be blocked as well.
				gameHandler.postDelayed(this, 30);
				
				if(blockNewUpdateCalls)
				gameHandler.removeCallbacks(this);
			}
			
		};
		
		public RunningModel(){
			alienWave=new AlienWave();
			gameHandler=new Handler();
			blockNewSpawnCalls=false;
			blockNewUpdateCalls=false;
		}
		
		void StartSpawn(){
			blockNewSpawnCalls=true;
			gameHandler.removeCallbacks(spawnRun);
			blockNewSpawnCalls=false;
			gameHandler.post(spawnRun);
		}
		
		public void Pause(){
			blockNewSpawnCalls=true;
			blockNewUpdateCalls=true;
		}
		
		public void Resume(){
			blockNewUpdateCalls=false;
			gameHandler.post(updateRun);
			
			blockNewSpawnCalls=false;
			StartSpawn();
		}
		
		/*
		 * The AlienWave class models the behaviour of a group of "Alien"s (a nested class):
		 * 
		 * public void PrepareWave(int level, int wave)
		 * -Prepares the AlienWave based on level and wave numbers
		 * -Call this before spawning aliens
		 * 
		 * public boolean SpawnAlien()
		 * -Spawn a single alien; return true if successful, return false if they've all spawned
		 * 
		 * public void UpdateAliens()
		 * -Update the movement of all aliens
		 * 
		 * public void Shoot(int x, int y)
		 * -Attack aliens near these coordinates
		 */
		

		public class AlienWave{
			
			public final int alienArraySize=30;
			public Alien alienArray[];
			private int aliensNotInactive;
			private int aliensDead;
			
			private final int dest_Count=4;
			private int spawn_X;
			private int spawn_Y;
			private int [] dest_X;
			private int [] dest_Y;
			
			private int alienSpeed;
			private final int alienGravity=2;
			private int ground_Y;
			
			public AlienWave(){
				
				alienArray=new Alien[alienArraySize]; 
				for(int i=0;i<alienArraySize;i++)
					alienArray[i]=new Alien();
				aliensNotInactive=0;
				aliensDead=0;

				dest_X=new int[dest_Count];
				dest_Y=new int[dest_Count];
				dest_X[3]=screenWidth/2;
				dest_Y[3]=screenHeight;
				
				spawn_X=screenWidth/2;
				spawn_Y=-Alien.halfWidth;
				
				ground_Y=screenHeight;
				
			}
			
			//Combine the wave dependent functions into one
			public void PrepareWave(int level, int wave){
				InactivateAliens();
				SetCourse();
				SetAlienSpeed(level, wave);
			}
			
			//Make aliens disappear, done before wave
			private void InactivateAliens(){
				for(int i=0;i<alienArraySize;i++)
					alienArray[i].state=State.inactive;
				aliensNotInactive=0;
				aliensDead=0;
			}

			//One of the inactive aliens is brought to life; don't do anything if they're all alive
			public boolean SpawnAlien(){
				if(aliensNotInactive>=alienArraySize)return false;
				alienArray[aliensNotInactive].spawn_();
				aliensNotInactive++;
				return true;
			}
			
			//Update all the aliens
			public void UpdateAliens(){
				for(int i=0;i<alienArraySize;i++)
					alienArray[i].Update();
				gameControllerAndView.SetRunningView(level);
			}
			
			//Touch the screen, shoot down aliens at this coordinate
			public void Shoot(int x, int y){
				for(int i=0;i<alienArraySize;i++){
					if(alienArray[i].x-Alien.halfWidth<=x && x<=alienArray[i].x+Alien.halfWidth
							&& alienArray[i].y-Alien.halfWidth<=y && y<=alienArray[i].y+Alien.halfWidth){
						alienArray[i].state=State.fall;
					}
				}
			}
			
			//Prepare the course, done before wave
			private void SetCourse(){
				Random ran = new Random();
				for(int i=0;i<dest_Count-1;i++){
					dest_X[i]=ran.nextInt() % screenWidth;
					if(dest_X[i]<0)dest_X[i]=-dest_X[i];
					dest_Y[i]=ran.nextInt()%(screenHeight/2);
					if(dest_Y[i]<0)dest_Y[i]=-dest_Y[i];
					dest_Y[i]+=i*screenHeight/4;
				}
			}
			
			//Set alienSpeed based on level and wave; done before wave
			private void SetAlienSpeed(int level, int wave){
				alienSpeed=5*level+level*wave;
			}
			
			
			public class Alien {
				
				/*
				 * Model of the alien
				 * state: how it behaves on an Update call and how it is drawn
				 * x, y: position
				 * dx, dy: velocity
				 * dest_Number: goal; does something when it reaches here while alive
				 */
				public State state;//public only for getting
				public double x, y;//public only for getting
				private double dx, dy;
				private byte dest_Number;
				
				//Aliens are treated like a square
				//Let's set the half-screenWidth of the square to 100 pixels, assuming the phone has a reasonable resolution
				static final int halfWidth=100;
				
				public Alien(){
					state=State.inactive;
					x=y=dx=dy=0;
					dest_Number=0;
				}
				
				public void spawn_(){
					state=State.alive;
					x=spawn_X;
					y=spawn_Y;
					dest_Number=0;
					SetVelocity();
				}

				public void Update(){
					if(state==State.inactive){
						
					}else if(state==State.alive){
						//Are we at the dest_?
						if(x-halfWidth<=dest_X[dest_Number] && dest_X[dest_Number]<=x+halfWidth
								&& y-halfWidth<=dest_Y[dest_Number] && dest_Y[dest_Number]<=y+halfWidth){
							if(dest_Number==3){
								//reached end, game over
								Lose();
								return;
							}
							dest_Number++;
							SetVelocity();
						}else{
							x+=dx;
							y+=dy;
						}
					}else if(state==State.fall){
						if(y>ground_Y){
							state=State.dead;
							aliensDead++;
							if(aliensDead==alienArraySize){
								OnLevelComplete();
							}
							return;
						}
						dy+=alienGravity;
						x+=dx;
						y+=dy;
					}else if(state==State.dead){
						//Chance of exploding! TODO
					}
				}
				
				//Set the velocity based on the dest_ination
				private void SetVelocity()
				{
					dx=(dest_X[dest_Number]-x);
					dy=(dest_Y[dest_Number]-y);
					double drad=Math.sqrt(dx*dx+dy*dy);
					dx*=alienSpeed/drad;
					dy*=alienSpeed/drad;
				}
				
			}
		}
	}
}
