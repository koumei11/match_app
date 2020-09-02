package jp.gr.java_conf.datingapp.progressbar;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import jp.gr.java_conf.datingapp.R;

public class SignUpProgressButton {
    private CardView cardView;
    private ConstraintLayout layout;
    private ProgressBar progressBar;
    private TextView textView;

    public SignUpProgressButton(Context ctx, View view) {
        cardView = view.findViewById(R.id.signup_card_view);
        layout = view.findViewById(R.id.signup_constraint_layout);
        progressBar = view.findViewById(R.id.signup_progressBar);
        textView = view.findViewById(R.id.signup_textView);
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
        textView.setText(R.string.signup);
    }

    public TextView getText() {
        return textView;
    }
}
