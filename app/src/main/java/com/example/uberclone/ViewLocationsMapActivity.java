package com.example.uberclone;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ViewLocationsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_view_locations_map );
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById( R.id.map );
        mapFragment.getMapAsync( this );

        btnRide = findViewById( R.id.btnRide );
        btnRide.setText( "Start " +  getIntent().getStringExtra( "requestUsername" ) + "'s Ride");

        btnRide.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toasty.success( ViewLocationsMapActivity.this, getIntent().getStringExtra( "requestUsername" ), Toasty.LENGTH_SHORT).show();
                ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery( "RequestCar" );
                carRequestQuery.whereEqualTo( "username", getIntent().getStringExtra( "requestUsername" ) );
                carRequestQuery.findInBackground( new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null) {
                            for (ParseObject uberRequest : objects){
                                uberRequest.put( "requestAccepted", true );
                                uberRequest.put( "MyDriver", ParseUser.getCurrentUser().getUsername() );
                                uberRequest.saveInBackground( new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Intent googleIntent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr="
                                                            + getIntent().getDoubleExtra("dLatitude",
                                                            0) + ","
                                                            + getIntent().getDoubleExtra("dLongitude",
                                                            0) + "&" + "daddr="
                                                            + getIntent().getDoubleExtra("pLatitude",
                                                            0) + "," +
                                                            getIntent().getDoubleExtra("pLongitude",
                                                                    0)));
                                            startActivity(googleIntent);
                                        }
                                    }
                                } );

                            }
                        }
                    }
                } );
            }
        } );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        Toasty.success( this, getIntent().getDoubleExtra( "dLatidude", 0) + "", Toasty.LENGTH_SHORT ).show();
//        // Add a marker in Sydney and move the camera
        LatLng dLocation = new LatLng( getIntent().getDoubleExtra( "dLatitude",0 ), getIntent().getDoubleExtra( "dLongitude",0 ) );
//        mMap.addMarker( new MarkerOptions().position( dLocation ).title( "Driver Location" ) );
//        mMap.moveCamera( CameraUpdateFactory.newLatLng( dLocation ) );

        LatLng pLocation = new LatLng( getIntent().getDoubleExtra( "pLatitude", 0 ), getIntent().getDoubleExtra( "pLongitude",0 ) );
//        mMap.addMarker( new MarkerOptions().position( pLocation ).title( "Passenger Location" ) );
//        mMap.moveCamera( CameraUpdateFactory.newLatLng( pLocation ) );

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker driverMarker = mMap.addMarker( new MarkerOptions().position(dLocation).title( "Driver Location") );
        Marker passengerMarker = mMap.addMarker( new MarkerOptions().position(pLocation) );

        ArrayList<Marker> driverPassengerLocationMarker = new ArrayList<>(  );
        driverPassengerLocationMarker.add( driverMarker );
        driverPassengerLocationMarker.add( passengerMarker );

        for (Marker marker : driverPassengerLocationMarker) {
            builder.include( marker.getPosition() );
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds( bounds, 0 );
        mMap.animateCamera( cameraUpdate );
    }
}
