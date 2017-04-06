package com.example.maveric.craigslistdiffchecker.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.maveric.craigslistdiffchecker.R;
import com.example.maveric.craigslistdiffchecker.ui.ManageSearchesScreenActivity;

/**
 * Created by Monday on 8/6/2016.
 */
public class DeleteSearchDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = getActivity().getLayoutInflater().inflate(R.layout.remove_search_fragment, null, false);

        if (getArguments().containsKey("name")) {
            final String providedName = (String) getArguments().get("name");
            TextView removeConfirmText = (TextView) root.findViewById(R.id.text_removeConfirm);
            removeConfirmText.setText("Confirm removal of search '" + providedName + "' ?");

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Remove Search")
                    .setView(root)
                    .setPositiveButton("Remove",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((ManageSearchesScreenActivity) getActivity()).removeSearch(providedName);
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // do nothing
                                }
                            }).create();
        }
        throw new RuntimeException("Attempting to delete a search without providing a name");
    }
}
