package com.example.toVisit_Aman_C0772344_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.toVisit_Aman_C0772344_android.DataModel.SelectedPlaceDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SelectedPlaceDatabase appDb;
    private TextView tvListFound;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private SelectedPlaceAdapter selectedPlaceAdapter;
    private ArrayList<SelectedPlaceData> favouriteList = new ArrayList<>();
    private AlertDialog.Builder dialogBoxBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        tvListFound = findViewById(R.id.tv_no_list_found);
        floatingActionButton = findViewById(R.id.floating);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        selectedPlaceAdapter = new SelectedPlaceAdapter(MainActivity.this, favouriteList, new SelectedPlaceAdapter.OnClickListener() {
            @Override
            public void onClick(SelectedPlaceData selectedPlaceData) {
                showDialogBoxBuilder(selectedPlaceData);
            }
        });
        recyclerView.setAdapter(selectedPlaceAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getSelectedPlaceList();

    }

    private void getSelectedPlaceList() {
        @SuppressLint("StaticFieldLeak")
        class GetTasks extends AsyncTask<Void, Void, ArrayList<SelectedPlaceData>> {

            @Override
            protected ArrayList<SelectedPlaceData> doInBackground(Void... voids) {
                appDb = SelectedPlaceDatabase.getInstance(MainActivity.this);
                favouriteList.clear();
                favouriteList.addAll(appDb.favouriteDataDao().getFavouriteList());

                return favouriteList;
            }

            @Override
            protected void onPostExecute(ArrayList<SelectedPlaceData> tasks) {
                super.onPostExecute(tasks);
                selectedPlaceAdapter.notifyDataSetChanged();
                if (favouriteList.size() == 0) {
                    tvListFound.setVisibility(View.VISIBLE);
                } else {
                    tvListFound.setVisibility(View.GONE);
                }
            }
        }

        GetTasks getTasks = new GetTasks();
        getTasks.execute();
    }

    private void showDialogBoxBuilder(final SelectedPlaceData selectedPlaceData) {
        dialogBoxBuilder = new AlertDialog.Builder(this);
        dialogBoxBuilder.setMessage("Are you sure")
                .setTitle("Delete " + selectedPlaceData.getTitle() + "?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        appDb.favouriteDataDao().deleteFavouritePlace(selectedPlaceData);
                        getSelectedPlaceList();
                        selectedPlaceAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();

                    }
                });
        AlertDialog alert = dialogBoxBuilder.create();
        alert.show();
    }
}