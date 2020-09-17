package jp.gr.java_conf.datingapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import jp.gr.java_conf.datingapp.R;

public class SelectionDialog extends DialogFragment {

    private String [] mStrings;
    private String mPlaceholder;
    private TextView mTextView;

    public SelectionDialog(String[] strings, String placeholder, TextView textView) {
        mStrings = strings;
        mPlaceholder = placeholder;
        mTextView = textView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mPlaceholder)
                .setItems(mStrings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mTextView.setText(mStrings[i]);
                        mTextView.setTextColor(getResources().getColor(R.color.colorWhite));
                    }
                })
                .setPositiveButton("閉じる", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        return builder.create();
    }
}
