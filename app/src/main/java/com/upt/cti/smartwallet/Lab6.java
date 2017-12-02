package com.upt.cti.smartwallet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;

import UI.PaymentAdapter;
import UI.PaymentType;

public class Lab6 extends AppCompatActivity {

    /*
    * UI.
    */
    private TextView tStatus;
    private Button bPrevious, bNext;
    private FloatingActionButton fabAdd;
    private ListView listPayments;

    private DatabaseReference databaseReference;
    private List<Payment> payments = new ArrayList<>();
    private int currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab6);

        tStatus = (TextView) findViewById(R.id.tStatus);
        bPrevious = (Button) findViewById(R.id.bPrevious);
        bNext = (Button) findViewById(R.id.bNext);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        listPayments = (ListView) findViewById(R.id.listPayments);

        final PaymentAdapter adapter = new PaymentAdapter(this, R.layout.item_payment, payments);
        listPayments.setAdapter(adapter);

        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        //currentMonth = prefs.getInt(TAG_MONTH, -1);
       // if (currentMonth == -1)
        //    currentMonth = Month.monthFromTimestamp(AppState.getCurrentTimeDate());

        if (!AppState.isNetworkAvailable(this)) {
            // has local storage already
            if (AppState.get().hasLocalStorage(this)) {
              //  payments = /**/
                        //tStatus.setText("Found " + payments.size() + " payments for " + Month.intToMonthName(currentMonth) + ".");
            } else {
                Toast.makeText(this, "This app needs an internet connection!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        AppState.get().setDatabaseReference(databaseReference);

        /*
        * Populate the listview with the data from firebase.
        */
        databaseReference.child("wallet").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                tStatus.setText("Found " + String.valueOf(dataSnapshot.getChildrenCount())  + " payments");
                    try {
                        Payment payment = dataSnapshot.getValue(Payment.class);
                        payment.timestamp = dataSnapshot.getKey().toString();
                        payments.add(payment);
                    } catch (Exception e) {
                    }
                    adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*
        *  Open new activity from a selected item from the listview.
        *  This item can be deleted or edited.
        *  We use a singleton pattern the get the state of the objects in the new activity.
        */
        listPayments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "Clicked on " + payments.get(i), Toast.LENGTH_SHORT).show();
                AppState.get().setCurrentPayment(payments.get(i));
                startActivity(new Intent(getApplicationContext(), NewPayment.class));
            }
        });
    }

    /*
    * Launch create payment activity.
    */
    public void newPayment(View v)
    {
        AppState.get().setCurrentPayment(null);
        Intent intent = new Intent(getApplicationContext(), NewPayment.class);
        startActivity(intent);
    }
}
