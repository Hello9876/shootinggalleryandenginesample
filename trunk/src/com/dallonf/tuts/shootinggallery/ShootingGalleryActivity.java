// Copyright Dallon Feldner 2010
package com.dallonf.tuts.shootinggallery;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.shape.modifier.AlphaModifier;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;

public class ShootingGalleryActivity extends BaseGameActivity {
	
	private static final int CAMERA_HEIGHT = 480;
	private static final int CAMERA_WIDTH = 854;
	
	public final float MAX_TARGET_SPEED = 1500;
	public final float TARGET_SPEED_INCREASE = 75;
	
	public float mTargetSpeed = 200;
	
	private Camera mCamera;
	
	private Texture mTargetTexture;
	private TextureRegion mTargetTextureRegion;
	
	private Texture mFontTexture;
	private Font mFont;
	
	private Target[] mTargets;
	private TimerHandler mTargetSpawner;
	private TimerHandler mDifficultyTimer;
	
	private ChangeableText mScoreText;
	private ChangeableText mLivesText;
	
	private Rectangle mFlash;
	private AlphaModifier mFlashAnim;
	
	private int mScore;
	private int mLives;
	
	private Vibrator mVibrator;
	
	@Override
	public Engine onLoadEngine() {
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera));
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("View Source");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/shootinggalleryandenginesample/"));
		startActivity(intent);
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onLoadResources() {
		mTargetTexture = new Texture(128, 128);
		mTargetTextureRegion = TextureRegionFactory.createFromAsset(mTargetTexture, this,
				"gfx/target.png", 0, 0);
		
		mFontTexture = new Texture(256, 256);
		mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
				35, true, Color.WHITE);
		
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getTextureManager().loadTexture(mTargetTexture);
		
		mEngine.getFontManager().loadFont(mFont);
		
		mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
	}
	
	@Override
	public Scene onLoadScene() {
		Scene scene = new Scene(1) {
			 public boolean onSceneTouchEvent(TouchEvent pSceneTouchEvent) {
				 if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					mFlash.setVisible(true);
					mFlashAnim.reset();
				}
				return super.onSceneTouchEvent(pSceneTouchEvent);
			 }
		};
		
		mTargets = new Target[10];
		for (int i = 0; i < mTargets.length; i++) {
			mTargets[i] = new Target(mTargetTextureRegion, this);
			scene.getTopLayer().addEntity(mTargets[i]);
			scene.registerTouchArea(mTargets[i]);
		}
		
		mTargetSpawner = new TimerHandler(1.5f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				launchTarget();
			}
		});
		
		mDifficultyTimer = new TimerHandler(5, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				mTargetSpeed = Math.min(mTargetSpeed + TARGET_SPEED_INCREASE, MAX_TARGET_SPEED);
			}
		});
		
		scene.registerUpdateHandler(mTargetSpawner);
		scene.registerUpdateHandler(mDifficultyTimer);
		
		HUD hud = new HUD(1);
		
		mScoreText = new ChangeableText(4, 4, mFont, "Score: XXX");
		hud.getTopLayer().addEntity(mScoreText);
		
		setScore(0);
		
		mLivesText = new ChangeableText(4, 4, mFont, "Lives: XX");
		mLivesText.setPosition(CAMERA_WIDTH - mLivesText.getWidth() - 4, 4);
		hud.getTopLayer().addEntity(mLivesText);
		
		setLives(5);
		
		mFlash = new Rectangle(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		mFlash.setColor(1, 1, 1);
		mFlash.setVisible(false);
		mFlash.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		mFlashAnim = new AlphaModifier(.5f, 1, 0);
		mFlashAnim.setRemoveWhenFinished(false);
		mFlash.addShapeModifier(mFlashAnim);
		
		hud.getTopLayer().addEntity(mFlash);
		
		mCamera.setHUD(hud);
		
		return scene;
	}

	@Override
	public void onLoadComplete() {		
	}
	
	public void launchTarget() {
		for (int i = 0; i < mTargets.length; i++) {
			if (!mTargets[i].isActive()) {
				mTargets[i].reset();
				break;
			}
		}
	}
	
	public int getScore() {
		return mScore;
	}
	
	public void setScore(int score) {
		mScore = score;
		mScoreText.setText("Score: " + score);
	}
	
	public void addScore(int amount) {
		setScore(mScore + amount);
	}
	
	public int getLives() {
		return mLives;
	}
	
	public void setLives(int lives) {
		if (mLives > lives) {
			mVibrator.vibrate(100);
			if (lives < 0) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Intent intent = new Intent(ShootingGalleryActivity.this, GameOverActivity.class);
						intent.putExtra(GameOverActivity.INTENT_EXTRA_SCORE, mScore);
						startActivity(intent);
						finish();
					}
				});

			}
		}
		mLives = lives;
		mLivesText.setText("Lives: " + lives);
	}
	
	public void addLives(int amount) {
		setLives(mLives + amount);
	}
	
	class Target extends Sprite
	{
		public final float[] LANES = {
				0,
				120,
				240,
				360
		};
		
		private ShootingGalleryActivity mActivity;
		
		private boolean mActive;
		
		public Target(TextureRegion pTextureRegion, ShootingGalleryActivity pActivity)
		{
			super(0, 0,
					pTextureRegion);
			
			mActivity = pActivity;
			
			setActive(false);
		}
		
		public boolean isActive() {
			return mActive;
		}
		
		public void setActive(boolean active) {
			mActive = active;
			setIgnoreUpdate(!active);
			setVisible(active);
		}
		
		public void reset() {
			int randIdx = (int) Math.round(Math.random() * 3);
			float randY = LANES[randIdx];
			if (randIdx % 2 != 0) {
				setPosition(-getWidth(), randY);
				setVelocityX(mActivity.mTargetSpeed);
			} else {
				setPosition(CAMERA_WIDTH, randY);
				setVelocityX(-mActivity.mTargetSpeed);
			}
			setActive(true);
		}
		
		@Override
		public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
				float pTouchAreaLocalX, float pTouchAreaLocalY) {
			if (mActive) {
				setActive(false);
				mActivity.addScore(1);
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		protected void onManagedUpdate(float pSecondsElapsed) {
			if (mActive) {
				if (getX() > CAMERA_WIDTH || getX() < -getWidth()) {
					setActive(false);
					mActivity.addLives(-1);
				}
			}
			super.onManagedUpdate(pSecondsElapsed);
			
		}
	}
	
}