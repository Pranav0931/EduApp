package com.hdaf.eduapp.parent;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hdaf.eduapp.R;

public class ParentLoginDialog extends DialogFragment {

    private OnSuccessListener listener;

    public interface OnSuccessListener {
        void onSuccess();
    }

    public void setOnSuccessListener(OnSuccessListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_parent_login, null);

        EditText editPin = view.findViewById(R.id.edit_pin);
        
        builder.setView(view)
                .setTitle("Parent Zone")
                .setMessage("Enter PIN to access (Default: 1234)")
                .setPositiveButton("Enter", (dialog, id) -> {
                         // Will be overridden
                })
                .setNegativeButton("Cancel", (dialog, id) -> dismiss());

        AlertDialog dialog = builder.create();
        
        // Override positive button to prevent auto-dismiss on error
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String pin = editPin.getText().toString();
                if ("1234".equals(pin)) { // Simple hardcoded PIN for now
                    if (listener != null) listener.onSuccess();
                    dismiss();
                } else {
                    editPin.setError("Incorrect PIN");
                }
            });
        });
        
        return dialog;
    }
}
