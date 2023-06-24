package it.uniba.dib.sms222321;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/*
Dialog which will be displyed when an user is trying to insert new data about an animal
it occurs for every insertion that has something to do with money
 */
public class MyDialogFragment extends DialogFragment {


    /*
    Listener tha is used inside Animal Profile to manage the actions related to MyDialog
     */
    public interface DialogListener {
        void onDialogPositiveClick(DialogFragment dialog, String descrizione, String data, String spesa, int flag);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private DialogListener listener;

    private EditText etValue1, etValue2, etValue3;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public static MyDialogFragment newInstance(int flag) {
        MyDialogFragment dialogFragment = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putInt("flag", flag);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }


    /*
    method used to define what the dialog actual does
    it manages the control for datas insertions and the positive and negative button actions
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();

        int flag = getArguments().getInt("flag");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.inserisci_dati);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);
        builder.setView(dialogView);

        etValue1 = dialogView.findViewById(R.id.editValue1);
        etValue2 = dialogView.findViewById(R.id.editValue2);
        etValue3 = dialogView.findViewById(R.id.editValue3);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String descrizione = etValue1.getText().toString();
                String data = etValue2.getText().toString();
                String spesa = etValue3.getText().toString();

                if (descrizione.isEmpty() || data.isEmpty() || spesa.isEmpty() || (!isNumeric(spesa))) {
                    Toast.makeText(getActivity(), R.string.inserisci_un_numero_in_spesa, Toast.LENGTH_LONG).show();
                } else {
                    if (listener != null) {
                        listener.onDialogPositiveClick(MyDialogFragment.this, descrizione, data, spesa, flag);
                    }
                    //dialog.dismiss();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onDialogNegativeClick(MyDialogFragment.this);
                }
            }
        });

        return builder.create();
    }


    /*
    method used to define if a string contains a number
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        int decimalCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c == '.' || c == ',') && decimalCount == 0) {
                decimalCount++;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }


}




