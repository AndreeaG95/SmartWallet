package com.upt.cti.smartwallet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This class holds the data for the new activity that is created in order to
 * modify an existing entry in the database, delete one or create a new one.
*/
public class NewPayment extends AppCompatActivity {

    private Spinner spinner;
    private EditText name, price;
    private TextView timestamp;

    final List<String> paymentTypes = new ArrayList<>();
    private Payment payment;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_payment);

        name = (EditText) findViewById(R.id.eName);
        price = (EditText) findViewById(R.id.ePrice);
        spinner = (Spinner) findViewById(R.id.spinner);
        timestamp = (TextView) findViewById(R.id.eTimestamp);

        paymentTypes.add("food");
        paymentTypes.add("clothes");
        paymentTypes.add("entertaiment");
        paymentTypes.add("phone");

        // Connect to firebase database.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, paymentTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Initialize UI if editing.
        payment = AppState.get().getCurrentPayment();
        if (payment != null) {
            name.setText(payment.getName());
            price.setText(String.valueOf(payment.getCost()));
            timestamp.setText("Timestamp:  " + payment.timestamp);
            try {
                spinner.setSelection(paymentTypes.indexOf(payment.getType()));
            } catch (Exception e) {
            }
        } else {
            timestamp.setText("");
        }
    }

    public void saveAndClose(String timestamp){
        // Save data to Firebase.
        // TODO: validate input.
        Payment newPay = new Payment(name.getText().toString(), spinner.getSelectedItem().toString(), Double.parseDouble(price.getText().toString()));

        DatabaseReference walletRef = databaseReference.child("wallet").child(timestamp);
        walletRef.setValue(newPay);

        // Backup data locally.
        //AppState.updateLocalBackup(this, newPay, null);

        // Go back to main screen.
        Intent intent = new Intent(getApplicationContext(), Lab6.class);
        finish();
        startActivity(intent);
    }

    public static String getCurrentTimeDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        return sdfDate.format(now);
    }

    private void delete(String timestamp) {
        AppState.get().getDatabaseReference().child("wallet").child(timestamp).removeValue();
        // Backup data locally.
       // AppState.updateLocalBackup(this, null, timestamp);

        // Go back to main activity.
        Intent intent = new Intent(getApplicationContext(), Lab6.class);
        finish();
        startActivity(intent);
    }

    public void click(View view){
        switch(view.getId()) {
            case R.id.bSave:
                if (payment != null)
                    saveAndClose(payment.timestamp);
                else
                    saveAndClose(getCurrentTimeDate());
                break;
            case R.id.bDelete:
                if (payment != null) {
                    delete(payment.timestamp);
                }else
                    Toast.makeText(this, "Payment does not exist", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
