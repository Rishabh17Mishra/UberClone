package com.example.uberclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        if (edtDriverorPassenger.getText().toString().equals("Driver") || edtDriverorPassenger.getText().toString().equals("driver") ||
                edtDriverorPassenger.getText().toString().equals( "Passenger" ) || edtDriverorPassenger.getText().toString().equals( "passenger" )) {
            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn( new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null) {
                            Toasty.success( MainActivity.this, "We have an Anonymus User", Toasty.LENGTH_SHORT ).show();
                            user.put( "as", edtDriverorPassenger.getText().toString() );
                            user.saveInBackground( new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                    transitionToDriverRequestListActivity();
                                }
                            } );
                        }
                    }
                } );
            } else {
                Toasty.info( MainActivity.this, "Are you a Driver or a Passenger ? ", Toasty.LENGTH_SHORT ).show();
                return;
            }
        }
    }

    enum State{
        SIGNUP, LOGIN
    }

    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private RadioButton driverRadioButton, passengerRadioButton;
    private EditText edtUsername, edtPassword, edtDriverorPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null) {
            //ParseUser.logOut();
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }
        btnSignUpLogin = findViewById( R.id.btnSignUpLogin );
        btnOneTimeLogin = findViewById( R.id.btnOneTimeLogin );
        driverRadioButton = findViewById( R.id.rdbDriver );
        passengerRadioButton = findViewById( R.id.rdbPassenger );
        btnOneTimeLogin.setOnClickListener( this );
        state = State.SIGNUP;
        edtUsername = findViewById( R.id.edtUserName );
        edtPassword = findViewById( R.id.edtPassword );
        edtDriverorPassenger = findViewById( R.id.edtDOrP );
        if (ParseUser.getCurrentUser() != null){
            // Transition Options to be inserted here
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }
        btnSignUpLogin.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == State.SIGNUP){
                    if (!driverRadioButton.isChecked() && !passengerRadioButton.isChecked()){
                        Toasty.info( MainActivity.this, "Are you a Driver or a Passenger ? ", Toasty.LENGTH_SHORT ).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername( edtUsername.getText().toString() );
                    appUser.setPassword( edtPassword.getText().toString() );
                    if (driverRadioButton.isChecked()){ appUser.put( "as", "Driver" );}
                    else if (passengerRadioButton.isChecked()){ appUser.put( "as", "Passenger" );}
                    appUser.signUpInBackground( new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toasty.success( MainActivity.this, "Signed Up", Toasty.LENGTH_SHORT ).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    } );
                } else if (state == State.LOGIN){
                    ParseUser.logInInBackground( edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null) {
                                Toasty.success( MainActivity.this, "Logged In", Toasty.LENGTH_SHORT ).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    } );
                }
            }
        } );
    }

    private void transitionToPassengerActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get( "as" ).equals( "Passenger" )) {
                Intent intent = new Intent( MainActivity.this, PassengerActivity.class );
                startActivity( intent );
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_signup_activity, menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.loginItem :
                if (state == State.SIGNUP) {
                    state = State.LOGIN;
                    item.setTitle( "Sign Up" );
                    btnSignUpLogin.setText( "Log In" );
                } else if (state == State.LOGIN) {
                    state = State.SIGNUP;
                    item.setTitle( "Log In" );
                    btnSignUpLogin.setText( "Sign Up" );
                }
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    private void transitionToDriverRequestListActivity(){
        if (ParseUser.getCurrentUser() != null) {
            if (Objects.equals( ParseUser.getCurrentUser().get( "as" ), "Driver" )) {
                Intent intent = new Intent(this, DriverRequestListActivity.class);
                startActivity(intent);
            }
        }
    }
}
