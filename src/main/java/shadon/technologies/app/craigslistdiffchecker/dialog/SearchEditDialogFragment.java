package shadon.technologies.app.craigslistdiffchecker.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import shadon.technologies.app.craigslistdiffchecker.R;
import shadon.technologies.app.craigslistdiffchecker.craigsObjects.SavedSearch;
import shadon.technologies.app.craigslistdiffchecker.ui.ManageSearches;

/**
 * Created by Monday on 8/6/2016.
 */
public class SearchEditDialogFragment extends DialogFragment {

    private final String TAG = "SearchEditDialog";

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

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Configure Search")
                .setView(root)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = ((EditText) getDialog().findViewById(R.id.text_NameInput)).getText().toString();
                                String url = ((EditText) getDialog().findViewById(R.id.text_urlInput)).getText().toString();

                                // if we were given a name, this is an update
                                ((ManageSearches) getActivity()).addSearch(new SavedSearch(name, url), getArguments().containsKey("name"));
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing
                            }
                        }).create();

        final EditText editTextUrlInputField = (EditText) root.findViewById(R.id.text_urlInput);

        editTextUrlInputField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Log.i(TAG, "Return key called");
                    InputMethodManager imm = (InputMethodManager) editTextUrlInputField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive())
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }

                Log.d(TAG, "A key was captured and it was not the return key: " + event);

                return false;
            }
        });

        return alertDialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
