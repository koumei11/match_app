package jp.gr.java_conf.datingapp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import jp.gr.java_conf.datingapp.R;

public class AddressDialog extends DialogFragment {

    private String [] mStrings;
    private TextView mTextView;

    public AddressDialog (String[] strings, TextView textView) {
        mStrings = strings;
        mTextView = textView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.enter_address))
                .setItems(getResources().getStringArray(R.array.address_list), new DialogInterface.OnClickListener() {
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
