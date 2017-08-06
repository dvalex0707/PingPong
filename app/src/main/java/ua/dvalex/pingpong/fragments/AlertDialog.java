package ua.dvalex.pingpong.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.dvalex.pingpong.R;

/**
 * Created by alex on 06.08.17
 */
public class AlertDialog extends DialogFragment {

    private Fragment fragment;
    private int title, message;
    private boolean isRed = false, hasCancel = true;
    private View.OnClickListener onClickListener = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(title);
        View view = inflater.inflate(R.layout.dialog_alert, container, false);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        View btnCancel = view.findViewById(R.id.btnCancel);

        if (isRed) {
            tvMessage.setTextColor(Color.RED);
        }
        tvMessage.setText(message);
        View.OnClickListener listener = getOnClickListener();
        view.findViewById(R.id.btnOk).setOnClickListener(listener);
        if (hasCancel) {
            btnCancel.setOnClickListener(listener);
        } else {
            btnCancel.setVisibility(View.GONE);
        }

        return view;
    }

    @NonNull
    private View.OnClickListener getOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnOk:
                        if (onClickListener != null) {
                            onClickListener.onClick(v);
                        }
                        //No break required!
                    case R.id.btnCancel:
                        getDialog().dismiss();
                        break;
                }
            }
        };
    }

    public AlertDialog setup(Fragment fragment, int title, int message) {
        this.fragment = fragment;
        this.title = title;
        this.message = message;
        return this;
    }

    public AlertDialog setIsRed() {
        isRed = true;
        return this;
    }

    public AlertDialog setWithoutCancel() {
        hasCancel = false;
        return this;
    }

    public AlertDialog setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public void start() {
        fragment.getFragmentManager().beginTransaction().add(this, null).addToBackStack(null).commit();
    }
}
