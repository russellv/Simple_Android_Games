package com.funapps.attackofthealiens;


import com.funapps.attackofthealiens.AlienWaveModel.Alien;
import com.funapps.attackofthealiens.AlienWaveModel.State;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class PlanetView extends SurfaceView implements SurfaceHolder.Callback{

	GameModel myModel;
	
    //There is a background, and an alien image
    private Bitmap background;
    private Drawable spaceshipImage;
    
    TextView levelTextView;
    
    public PlanetView(Context context, AttributeSet attrs) {
    	
        super(context, attrs);
        
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        //Get image resources
        Resources resources = context.getResources();
        background = BitmapFactory.decodeResource(resources,
                R.drawable.earthrise);
        spaceshipImage = resources.getDrawable(
                R.drawable.lander_plain);
    }
	
    public void UpdateView() {
            Canvas c = null;
            try {
                c = getHolder().lockCanvas(null);
                Draw(c);
            } finally {
                if (c != null) {
                	getHolder().unlockCanvasAndPost(c);
                }
            }
    }
    
    //Draw the background and aliens
	private void Draw(Canvas canvas){
		/*
		if(canvas==null){
			return;
		}
		*/
		
		//Draw background
		canvas.drawBitmap(background, 0, 0, null);
        
		//Draw aliens
		for(int i=0;i<myModel.alienWave.alienCount;i++){
			if(myModel.alienWave.alienArray[i].state!=State.inactive){
				int x=(int)myModel.alienWave.alienArray[i].x;
				int y=(int)myModel.alienWave.alienArray[i].y;
                spaceshipImage.setBounds(x-Alien.halfWidth, y-Alien.halfWidth, x+Alien.halfWidth, y+Alien.halfWidth);
				//spaceshipImage.setBounds(480, 480, 520, 520);
                spaceshipImage.draw(canvas);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		myModel.alienWave.Shoot((int)event.getX(), (int)event.getY());
	    return true;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
        background = Bitmap.createScaledBitmap(
                background, width, height, true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		myModel.Resume();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		myModel.Pause();
	}
}
