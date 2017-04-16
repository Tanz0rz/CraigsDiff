package shadon.technologies.app.craigslistdiffchecker.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
/**
 * Created by Maveric on 4/15/2017.
 */

public class InfoDialog extends DialogFragment {


        String message = "No message pulled from Constructor";
        String title = "No title pulled from constructor";

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            title = getArguments().getString("title");
            builder.setTitle(title);

            message = getArguments().getString("message");
            builder.setMessage(message);

            builder.setPositiveButton("ok", null);

            Dialog dialog = builder.create();
            dialog.setCancelable(false);
            return dialog;
        }


}
