package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnGetRequest;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView listView;
    private ArrayAdapter adapter;
    private ArrayList<String> nearByDriveRequests, requestCarUsernames;
    private ArrayList<Double> passengerLatitudes, passengerLongitudes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_driver_request_list );

        btnGetRequest = findViewById( R.id.btnGetRequests );
        btnGetRequest.setOnClickListener( this );
        listView = findViewById( R.id.requestListView );
        nearByDriveRequests = new ArrayList<>();
        adapter = new ArrayAdapter( this, android.R.layout.simple_list_item_1, nearByDriveRequests );
        listView.setAdapter( adapter );
        nearByDriveRequests.clear();
        passengerLatitudes = new ArrayList<>();
        passengerLongitudes = new ArrayList<>();
        requestCarUsernames = new ArrayList<>();
        locationManager = (LocationManager) getSystemService( LOCATION_SERVICE );
        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,0,0,locationListener );
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        }
        listView.setOnItemClickListener( this );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.driver_menu, menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.driverLogoutItem){
            ParseUser.logOutInBackground( new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null)
                        finish();
                }
            } );
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onClick(View view) {

        if (Build.VERSION.SDK_INT < 23) {
            if (checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                Location currentDriverLocation = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                updateRequestListView( currentDriverLocation );
                return;
            }
        }else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission( DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions( DriverRequestListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000 );
            } else {
                //locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, locationListener );
                Location currentDriverLocation = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                updateRequestListView( currentDriverLocation );
            }
        }
    }

    private void updateRequestListView(Location driverLocation) {
        if (driverLocation != null) {
            double latitude = driverLocation.getLatitude();
            double longitude = driverLocation.getLongitude();

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint( driverLocation.getLatitude(), driverLocation.getLongitude() );
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery( "RequestCar" );
            requestCarQuery.whereNear( "passengerLocation", driverCurrentLocation );
            //requestCarQuery.whereDoesNotExist( "MyDriver" );
            requestCarQuery.findInBackground( new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {

                            if (nearByDriveRequests.size() > 0) {
                                nearByDriveRequests.clear();
                            }
                            if (passengerLatitudes.size() > 0){
                                passengerLatitudes.clear();
                            }
                            if (passengerLongitudes.size() > 0){
                                passengerLongitudes.clear();
                            }
                            if (requestCarUsernames.size() > 0){
                                requestCarUsernames.clear();
                            }

                            for (ParseObject nearRequests : objects) {
                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequests.get( "passengerLocation" );
                                Double distanceToPassenger = driverCurrentLocation.distanceInKilometersTo( pLocation );

                                float roundedDistance = Math.round( distanceToPassenger * 10 ) / 10;
                                nearByDriveRequests.add( "There are " + roundedDistance + " kilometers to " + nearRequests.get( "username" ) );

                                passengerLatitudes.add( pLocation.getLatitude() );
                                passengerLongitudes.add( pLocation.getLongitude() );
                                requestCarUsernames.add( nearRequests.get( "username" ) + "" );
                            }
                        } else {
                            Toasty.info( DriverRequestListActivity.this, "Sorry there are no Requests yet", Toasty.LENGTH_SHORT ).show();
                        }adapter.notifyDataSetChanged();
                    }
                }
            } );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission( DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, locationListener );
                Location currentDriverLocation = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                updateRequestListView( currentDriverLocation );
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (cdLocation != null) {
                Intent intent = new Intent( this, ViewLocationsMapActivity.class );
                intent.putExtra( "dLatitude", cdLocation.getLatitude() );
                intent.putExtra( "dLongitude", cdLocation.getLongitude() );
                intent.putExtra( "pLatitude", passengerLatitudes.get( position ) );
                intent.putExtra( "pLongitude", passengerLongitudes.get( position ) );
                intent.putExtra( "requestUsername", requestCarUsernames.get( position ) );
                startActivity( intent );
            }
        }
    }
}