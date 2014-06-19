package com.funapps.attackofthealiens;

import java.util.Random;

import android.os.Handler;

public class AlienWaveModel{
	
	GameModel gameModel;
	PlanetView gameView;
	
	/*
	 * Model defining the wave of aliens
	 * 
	 * alienCount and alienArray - the array of aliens
	 * spawnX, spawnY - where they spawn
	 * destX, destY, destCount - where they travel, CHANGE EVERY WAVE
	 * speed - the speed in pixels per update, CHANGE EVERY WAVE
	 * gravity - when they are falling
	 * groundHeight - how far they fall
	 * 
	 * Public functions:
	 * PrepareWave
	 * Spawn
	 * Update
	 * Shoot
	 */
	
	private int width, height;//Parameters depend on this
	public final int alienCount=30;
	public Alien alienArray[];//need to access alien properties
	private int notInactiveCount;
	private int deathCount;
	private final int destCount=4;
	private int spawnX;
	private int spawnY;
	private int [] destX;
	private int [] destY;
	private int speed;
	private final int gravity=2;
	private int groundHeight;
	
	public AlienWaveModel(int _width, int _height, GameModel _gameModel){
		width=_width;
		height=_height;
		gameModel=_gameModel;
		
		alienArray=new Alien[alienCount]; 
		for(int i=0;i<alienCount;i++)
			alienArray[i]=new Alien();
		notInactiveCount=0;
		deathCount=0;

		destX=new int[destCount];
		destY=new int[destCount];
		destX[3]=width/2;
		destY[3]=height;
		
		spawnX=width/2;
		spawnY=-Alien.halfWidth;
		
		groundHeight=height;
		
	}
	
	//Combine the wave dependent functions into one
	public void PrepareWave(int level, int wave){
		InactivateAliens();
		SetCourse();
		SetSpeed(level, wave);
	}
	
	//Make aliens disappear, done before wave
	private void InactivateAliens(){
		for(int i=0;i<alienCount;i++)
			alienArray[i].state=State.inactive;
		notInactiveCount=0;
		deathCount=0;
	}

	//One of the inactive aliens is brought to life; don't do anything if they're all alive
	public boolean SpawnAlien(){
		if(notInactiveCount>=alienCount)return false;
		alienArray[notInactiveCount].Spawn();
		notInactiveCount++;
		return true;
	}
	
	//Update all the aliens
	public void UpdateAliens(){
		for(int i=0;i<alienCount;i++)
			alienArray[i].Update();
		gameView.UpdateView();
	}
	
	//Touch the screen, shoot down aliens at this coordinate
	public void Shoot(int x, int y){
		for(int i=0;i<alienCount;i++){
			if(alienArray[i].x-Alien.halfWidth<=x && x<=alienArray[i].x+Alien.halfWidth
					&& alienArray[i].y-Alien.halfWidth<=y && y<=alienArray[i].y+Alien.halfWidth){
				alienArray[i].state=State.fall;
			}
		}
	}
	
	//Prepare the course, done before wave
	private void SetCourse(){
		Random ran = new Random();
		for(int i=0;i<destCount-1;i++){
			destX[i]=ran.nextInt() % width;
			if(destX[i]<0)destX[i]=-destX[i];
			destY[i]=ran.nextInt()%(height/2);
			if(destY[i]<0)destY[i]=-destY[i];
			destY[i]+=i*height/4;
		}
	}
	
	//Set speed based on level and wave; done before wave
	private void SetSpeed(int level, int wave){
		speed=5*level+level*wave;
	}
	
	enum State{
		inactive,
		alive,
		fall,
		dead
	}
	
	public class Alien {
		
		/*
		 * Model of the alien
		 * state: how it behaves on an Update call and how it is drawn
		 * x, y: position
		 * dx, dy: velocity
		 * destNumber: goal; does something when it reaches here while alive
		 */
		public State state;//public only for getting
		public double x, y;//public only for getting
		private double dx, dy;
		private byte destNumber;
		
		//Aliens are treated like a square
		//Let's set the half-width of the square to 100 pixels, assuming the phone has a reasonable resolution
		static final int halfWidth=100;
		
		public Alien(){
			state=State.inactive;
			x=y=dx=dy=0;
			destNumber=0;
		}
		
		public void Spawn(){
			state=State.alive;
			x=spawnX;
			y=spawnY;
			destNumber=0;
			SetVelocity();
		}

		public void Update(){
			if(state==State.inactive){
				
			}else if(state==State.alive){
				//Are we at the dest?
				if(x-halfWidth<=destX[destNumber] && destX[destNumber]<=x+halfWidth
						&& y-halfWidth<=destY[destNumber] && destY[destNumber]<=y+halfWidth){
					if(destNumber==3){
						//reached end, game over
						gameModel.GameOver();
						return;
					}
					destNumber++;
					SetVelocity();
				}else{
					x+=dx;
					y+=dy;
				}
			}else if(state==State.fall){
				if(y>groundHeight){
					state=State.dead;
					deathCount++;
					if(deathCount==alienCount){
						gameModel.LevelComplete();
					}
					return;
				}
				dy+=gravity;
				x+=dx;
				y+=dy;
			}else if(state==State.dead){
				//Chance of exploding! TODO
			}
		}
		
		//Set the velocity based on the destination
		private void SetVelocity()
		{
			dx=(destX[destNumber]-x);
			dy=(destY[destNumber]-y);
			double drad=Math.sqrt(dx*dx+dy*dy);
			dx*=speed/drad;
			dy*=speed/drad;
		}
		
	}
}