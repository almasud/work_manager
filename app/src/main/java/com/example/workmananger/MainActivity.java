package com.example.workmananger;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.workmananger.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.buttonOneTimeRequest.setOnClickListener(view -> {
            Log.d(TAG, "onCreate: buttonOneTimeRequest button pressed");
            // Prepare input data to send with work request
            Data inputData = new Data.Builder()
                    .putString(TestWorker.NOTIFICATION_TITLE, "One time request")
                    .putString(TestWorker.NOTIFICATION_BODY, "One time work request assigned to work manager")
                    .putInt(TestWorker.NOTIFICATION_ID, TestWorker.NOTIFICATION_ID_ONE_TIME_REQUEST)
                    .build();
            Constraints constraints = new Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Make a one time work request
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(TestWorker.class)
                    .setInitialDelay(5, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .build();

            // observe progress
            WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                    .observe(this, workInfo -> {
                        if (workInfo != null) {
                            Data progressData = workInfo.getProgress();
                            int progress = progressData.getInt(TestWorker.PROGRESS, 0);
                            binding.progressBar.setProgress(progress);

                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                binding.buttonOneTimeRequest.setEnabled(true);
                            }
                        }
                    });

            // Enqueue the work request into work manager
            WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequest);
            binding.buttonOneTimeRequest.setEnabled(false);
        });

        binding.buttonOneTimeForegroundRequest.setOnClickListener(view -> {
            Log.d(TAG, "onCreate: buttonOneTimeRequest button pressed");
            // Prepare input data to send with work request
            Data inputData = new Data.Builder()
                    .putString(TestWorker.NOTIFICATION_TITLE, "One time foreground worker")
                    .putString(TestWorker.NOTIFICATION_BODY, "A one time foreground request worker is running...")
                    .putInt(TestWorker.NOTIFICATION_ID, TestWorker.NOTIFICATION_ID_ONE_TIME_FOREGROUND_REQUEST)
                    .build();
            Constraints constraints = new Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Make a one time work request
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(TestWorker.class)
                    .setInitialDelay(5, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .addTag("oneTimeForeground")
                    .build();

            WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                    "oneTimeForeground", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest
            );
        });

        binding.buttonPeriodicRequest.setOnClickListener(view -> {
            Log.d(TAG, "onCreate: buttonPeriodicRequest button pressed");
            // Prepare input data to send with work request
            Data inputData = new Data.Builder()
                    .putString(TestWorker.NOTIFICATION_TITLE, "Periodic request")
                    .putString(TestWorker.NOTIFICATION_BODY, "Periodic work request assigned to work manager")
                    .putInt(TestWorker.NOTIFICATION_ID, TestWorker.NOTIFICATION_ID_PERIODIC_REQUEST)
                    .build();
            Constraints constraints = new Constraints.Builder()
//                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Make a periodic work request
            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                    TestWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                    .setInitialDelay(5, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .addTag("periodic")
                    .build();

            if (binding.buttonPeriodicRequest.getText().equals(
                    getResources().getString(R.string.start_periodic_request))) {
                binding.buttonPeriodicRequest.setText(getResources().getString(R.string.stop_periodic_request));
                binding.buttonPeriodicRequest.setBackgroundColor(getResources().getColor(R.color.teal_200));

                // Enqueue the work request into work manager
                WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                        "periodic", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
            } else {
                binding.buttonPeriodicRequest.setText(getResources().getString(R.string.start_periodic_request));
                binding.buttonPeriodicRequest.setBackgroundColor(getResources().getColor(R.color.teal_700));

                WorkManager.getInstance(getApplicationContext()).cancelWorkById(periodicWorkRequest.getId());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}