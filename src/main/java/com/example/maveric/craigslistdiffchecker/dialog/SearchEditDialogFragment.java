package com.example.maveric.craigslistdiffchecker.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.maveric.craigslistdiffchecker.R;
import com.example.maveric.craigslistdiffchecker.service.CraigSearch;
import com.example.maveric.craigslistdiffchecker.ui.ManageSearchesScreenActivity;

/**
 * Created by Monday on 8/6/2016.
 */
public class SearchEditDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = getActivity().getLayoutInflater().inflate(R.layout.edit_search_fragment, null, false);

        if (getArguments().containsKey("name")) {
            String providedName = (String) getArguments().get("name");
            EditText nameInput = (EditText) root.findViewById(R.id.text_NameInput);

            nameInput.setEnabled(false);
            nameInput.setText(providedName);
        }

        if (getArguments().containsKey("url")) {
            String providedUrl = (String) getArguments().get("url");
            ((EditText) root.findViewById(R.id.text_urlInput)).setText(providedUrl);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("Configure Search")
                .setView(root)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = ((EditText) getDialog().findViewById(R.id.text_NameInput)).getText().toString();
                                String url = ((EditText) getDialog().findViewById(R.id.text_urlInput)).getText().toString();

                                // if we were given a name, this is an update
                                ((ManageSearchesScreenActivity) getActivity()).addSearch(new CraigSearch(name, url), getArguments().containsKey("name"));
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
}
