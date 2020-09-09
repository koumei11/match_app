package jp.gr.java_conf.datingapp.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Locale;

import jp.gr.java_conf.datingapp.R;

public class DatePickFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private TextView mDate;

    public DatePickFragment(TextView date) {
        mDate = date;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        String date = String.format(Locale.US, "%4d/%02d/%02d", year, month + 1, day);
        mDate.setText(date);
        mDate.setTextColor(getResources().getColor(R.color.colorWhite));
    }
}
