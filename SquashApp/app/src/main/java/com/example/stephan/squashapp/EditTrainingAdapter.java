package com.example.stephan.squashapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Stephan on 4-6-2016.
 */
public class EditTrainingAdapter extends ArrayAdapter<Training>{
    ArrayList<Training> trainingList;  // the items.
    Context context;

    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

    /**
     * Initialize adapter
     */
    public EditTrainingAdapter(Context context_of_screen, ArrayList<Training> trainings) {
        super(context_of_screen, R.layout.single_training, trainings);

        context = context_of_screen;
        trainingList = trainings;
    }

    /**
     * Initialize View.
     */
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.single_training, parent, false);
        }

        // find Views.
        final TextView date = (TextView) view.findViewById(R.id.date);
        final TextView info = (TextView) view.findViewById(R.id.info);
        final TextView time = (TextView) view.findViewById(R.id.time);
        final TextView trainer = (TextView) view.findViewById(R.id.trainer);
        final TextView cp = (TextView) view.findViewById(R.id.currentPlayers);
        final TextView mp = (TextView) view.findViewById(R.id.maxPlayers);


        final Training item = trainingList.get(position);

        date.setText(item.get_date());
        info.setText(item.get_info());
        String timeText = item.get_start() + " until " + item.get_end();
        time.setText(timeText);
        cp.setText("Registered: " + item.get_current());
        mp.setText("Max players: " + item.get_max());

        String trainerName = item.get_trainer();


        if (!trainerName.isEmpty()) {
            trainer.setText("By: " + trainerName);
        } else {
            trainer.setText("");
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make layout
                LayoutInflater li = LayoutInflater.from(context);
                final View layout = li.inflate(R.layout.add_training, null);

                // get all items
                final EditText editDate = (EditText) layout.findViewById(R.id.date);
                final EditText editStart = (EditText) layout.findViewById(R.id.startTime);
                final EditText editEnd = (EditText) layout.findViewById(R.id.endTime);
                final EditText editTrainer = (EditText) layout.findViewById(R.id.trainer);
                final EditText editInfo = (EditText) layout.findViewById(R.id.info);
                final EditText editMax = (EditText) layout.findViewById(R.id.maxPlayers);

                editDate.setText(item.get_date());
                editStart.setText(item.get_start());
                editEnd.setText(item.get_end());
                editInfo.setText(item.get_info());
                editMax.setText(item.get_max().toString());
                editTrainer.setText(item.get_trainer());

                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle("Edit Training")
                        .setCancelable(true)
                        .setView(layout)
                        .setPositiveButton(
                            "Add",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    item.change_date(editDate.getText().toString());
                                    item.change_start(editStart.getText().toString());
                                    item.change_end(editEnd.getText().toString());
                                    item.change_info_short(editInfo.getText().toString());
                                    item.change_trainer(editTrainer.getText().toString());
                                    item.change_max(Integer.parseInt(editMax.getText().toString()));

                                    HashMap<String, Object> result = new HashMap<>();
                                    result.put("by", editTrainer.getText().toString());
                                    result.put("date", editDate.getText().toString());
                                    result.put("start", editStart.getText().toString());
                                    result.put("max", Integer.parseInt(editMax.getText().toString()));
                                    result.put("end", editEnd.getText().toString());
                                    result.put("info", editInfo.getText().toString());

                                    rootRef.child("trainingen").child(item.get_child())
                                            .updateChildren(result);

                                    notifyDataSetChanged();
                                    dialog.cancel();

                                }
                            });

                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }

        });

        return view;
    }
}
