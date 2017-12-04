package com.upt.cti.smartwallet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreeagb on 11/9/2017.
 */

/*
* This class is used for passing instances between activities.
*/
public class AppState {
    private static AppState singletonObject;

    public static synchronized AppState get() {
        if (singletonObject == null) {
            singletonObject = new AppState();
        }
        return singletonObject;
    }

    // reference to Firebase used for reading and writing data
    private DatabaseReference databaseReference;
    // current payment to be edited or deleted
    private Payment currentPayment;

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public void setCurrentPayment(Payment currentPayment) {
        this.currentPayment = currentPayment;
    }

    public Payment getCurrentPayment() {
        return currentPayment;
    }

    /* Save a payment to the local database. */
    public static void updateLocalBackup(Context context, Payment payment, String timestamp){

        try {
            if (timestamp == null) {
                // save to file
                FileOutputStream fos = context.openFileOutput(payment.timestamp, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                Log.d("Backup","Wrote to local storage:" + payment.timestamp);
                os.writeObject(payment);
                os.close();
                fos.close();
            } else {
                context.deleteFile(timestamp);
            }
        } catch (IOException e) {
            Toast.makeText(context, "Cannot access local data.", Toast.LENGTH_SHORT).show();
        }
    }

    /* Return true if there is something saved locally. */
    public boolean hasLocalStorage(Context context) {
        return context.getFilesDir().listFiles().length > 0;
    }

    /* Return a payment from a local backup */
    public static List<Payment> loadFromLocalBackup(Context context, String currentMonth){
        try {
            List<Payment> payments = new ArrayList<>();
            Log.d("load0", "Start loading");
            for (File file : context.getFilesDir().listFiles()) {
                Log.d("load1", context.getFilesDir().getPath());
               if (file.canRead()){
                    FileInputStream fis = context.openFileInput(file.getName());
                    Log.d("load2", file.getName());

                    // TODO(andreeagb): This is not working.
                    ObjectInputStream is = new ObjectInputStream(fis);
                    if (currentMonth == "July") {
                        Payment payment = (Payment) is.readObject();
                    }
                    Log.d("load3", "fuck you");
                    //payments.add(payment);
                    is.close();
                    fis.close();

                    /* This will be added onlt if the filtering method from lab7 is implemented.
                    if ( current month only )
                        payments.add(payment);
                    */
                }else{
                   Log.d("canRead", "Can not read file");
               }
            }

            return payments;
        } catch (IOException e) {
            Toast.makeText(context, "Cannot access local data.", Toast.LENGTH_SHORT).show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
