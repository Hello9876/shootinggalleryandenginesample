package com.dallonf.tuts.shootinggallery;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class GameOverActivity extends Activity {
	
	public static final String INTENT_EXTRA_SCORE = "score";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_over);
		TextView score = (TextView)findViewById(R.id.score_text_view);
		score.setText(score.getText() +
				Integer.toString(getIntent().getIntExtra(INTENT_EXTRA_SCORE, 0)));
	}

}
