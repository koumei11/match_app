package jp.gr.java_conf.datingapp.progressbar;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import jp.gr.java_conf.datingapp.R;

public class SaveProgressButton {
    private CardView cardView;
    private ConstraintLayout layout;
    private ProgressBar progressBar;
    private TextView textView;
    private boolean isNew;

    public SaveProgressButton(View view, boolean isNew) {
        cardView = view.findViewById(R.id.save);
        layout = view.findViewById(R.id.pro_save_layout);
        progressBar = view.findViewById(R.id.pro_save_progressBar);
        textView = view.findViewById(R.id.pro_save_textView);
        this.isNew = isNew;
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
        textView.setText(isNew ? R.string.add : R.string.save);
    }

    public TextView getText() {
        return textView;
    }
}
