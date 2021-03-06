package jp.gr.java_conf.datingapp.progressbar;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import jp.gr.java_conf.datingapp.R;

public class SignInProgressButton {
    private CardView cardView;
    private ConstraintLayout layout;
    private ProgressBar progressBar;
    private TextView textView;
    private boolean isEmail;

    public SignInProgressButton(View view, boolean isEmail) {
        cardView = view.findViewById(R.id.signin_card_view);
        layout = view.findViewById(R.id.signin_constraint_layout);
        progressBar = view.findViewById(R.id.signin_progressBar);
        textView = view.findViewById(R.id.signin_textView);
        this.isEmail = isEmail;
    }

    public void buttonActivated() {
        cardView.setEnabled(false);
        layout.setBackgroundColor(cardView.getResources().getColor(R.color.colorLightRed));
        progressBar.setVisibility(View.VISIBLE);
        textView.setText(R.string.processing);
    }

    public void buttonFinished() {
        cardView.setEnabled(true);
        layout.setBackgroundColor(cardView.getResources().getColor(R.color.colorRed));
        progressBar.setVisibility(View.GONE);
        if (isEmail) {
            textView.setText(R.string.send);
        } else {
            textView.setText(R.string.signin);
        }
    }
}
