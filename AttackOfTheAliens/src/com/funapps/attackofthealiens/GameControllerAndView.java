package com.funapps.attackofthealiens;

import com.funapps.attackofthealiens.GameModel.RunningModel.AlienWave.Alien;
import com.funapps.attackofthealiens.GameModel.State;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class GameControllerAndView extends SurfaceView implements SurfaceHolder.Callback{
	
	GameModel gameModel;
	
	//Data for view of different states
	final String startText="Your planet is being invaded by aliens. Shoot them out of the sky before one of them safely lands.";
    private Bitmap runningBackground;
    private Drawable runningSpaceshipImage;
    final String pausedText="Game paused. Tap the screen to continue.";
    final String loseText="An alien has safely landed. Soon thousands more will show up! Suddenly your planet doesn’t feel like it’s worth living on anymore.";
    private Drawable winTip;
    
    //A TextView for displaying messages
    TextView centerTextView;
    
    public GameControllerAndView(Context context, AttributeSet attrs) {
    	
        super(context, attrs);
        
        //React to view callbacks
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        //Get image resources
        Resources resources = context.getResources();
        runningBackground = BitmapFactory.decodeResource(resources,
                R.drawable.earthrise);
        runningSpaceshipImage = resources.getDrawable(
                R.drawable.fedoraship);
        winTip = resources.getDrawable(
                R.drawable.congratulations);
    }
    
	//User interactions
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gameModel.TouchEvent(event);
	    return true;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		runningBackground = Bitmap.createScaledBitmap(
				runningBackground, width, height, true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		gameModel.Resume();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		gameModel.Pause();
	}

	public void NotifyLose(){
		SetLoseView();
	}
	
	public void NotifyWin(){
		SetWinView();
	}
    
    
    
    
    
    
	public void SetStartView(){
		centerTextView.setText(startText);
	}

    public void SetRunningView(int level) {
		centerTextView.setText("Level "+level+"/6");
            Canvas c = null;
            try {
                c = getHolder().lockCanvas(null);
                RunningDraw(c);
            } finally {
                if (c != null) {
                	getHolder().unlockCanvasAndPost(c);
                }
            }
    }
    
    //Draw the background and aliens
	private void RunningDraw(Canvas canvas){
		if(canvas==null)return;
		
		//Draw background
		canvas.drawBitmap(runningBackground, 0, 0, null);
        
		//Draw aliens
		for(int i=0;i<gameModel.runningModel.alienWave.alienArraySize;i++){
			if(gameModel.runningModel.alienWave.alienArray[i].state!=State.inactive){
				int x=(int)gameModel.runningModel.alienWave.alienArray[i].x;
				int y=(int)gameModel.runningModel.alienWave.alienArray[i].y;
                runningSpaceshipImage.setBounds(x-Alien.halfWidth, y-Alien.halfWidth, x+Alien.halfWidth, y+Alien.halfWidth);
				//spaceshipImage.setBounds(480, 480, 520, 520);
                runningSpaceshipImage.draw(canvas);
			}
		}
	}
	
	public void SetPausedView(){
		centerTextView.setText(pausedText);
	}
	
	public void SetLoseView(){
		
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	
        		centerTextView.setText(loseText);
            }
        }, 200);
	}
	
	public void SetWinView(){
		
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Canvas c = null;
                try {
                    c = getHolder().lockCanvas(null);

                    winTip.setBounds(0, 0, gameModel.screenWidth, gameModel.screenHeight);
                    winTip.draw(c);
                } finally {
                    if (c != null) {
                    	getHolder().unlockCanvasAndPost(c);
                    }
                }
        		centerTextView.setText("");
            }
        }, 200);
	}
}
