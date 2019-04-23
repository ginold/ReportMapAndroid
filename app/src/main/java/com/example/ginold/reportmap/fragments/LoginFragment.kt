package com.example.ginold.reportmap.fragments


import android.os.Bundle
import android.app.Fragment
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.example.ginold.reportmap.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast

import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.app.ProgressDialog






/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment(), View.OnClickListener  {

    private var mAuth: FirebaseAuth? = null
    private var email: String = ""
    private var password: String = ""
    private var mStatusTextView: TextView? = null
    private var mDetailTextView: TextView? = null
    private var mEmailField: EditText? = null
    private var mPasswordField: EditText? = null

    private var signInButton: Button? = null
    private var signOutButton: Button? = null
    private var createAccountButton: Button? = null
    private var verifyEmailButton: Button? = null


    private var v: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false) as View
        this.v = view

        // Views
        mStatusTextView = view.findViewById(R.id.status)
        mDetailTextView = view.findViewById(R.id.detail)
        mEmailField = view.findViewById(R.id.field_email)
        mPasswordField = view.findViewById(R.id.field_password)

        // Buttons
        signInButton = view.findViewById(R.id.email_sign_in_button) as Button
        createAccountButton = view.findViewById(R.id.email_create_account_button) as Button
        signOutButton = view.findViewById(R.id.sign_out_button) as Button
        verifyEmailButton = view.findViewById(R.id.verify_email_button) as Button

        signInButton!!.setOnClickListener(this);
        createAccountButton!!.setOnClickListener(this);
        signOutButton!!.setOnClickListener(this);
        verifyEmailButton!!.setOnClickListener(this);


        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.currentUser

        updateUI(currentUser)
        return view
    }

    override fun onClick(v: View) {
        val i = v.id
        when(i) {
            R.id.email_create_account_button->{
                createAccount(mEmailField!!.getText().toString(), mPasswordField!!.getText().toString())
            }R.id.email_sign_in_button->{
                signIn(mEmailField!!.getText().toString(), mPasswordField!!.getText().toString())
            }R.id.sign_out_button->{
                signOut()
            }R.id.verify_email_button->{
             //   sendEmailVerification()
            }
        }
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
//        if (!validateForm()) {
//            return
//        }
      //  showProgressDialog()
        // [START sign_in_with_email]
        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = mAuth!!.getCurrentUser()
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                    // [START_EXCLUDE]
                    if (!task.isSuccessful) {
                        mStatusTextView!!.setText(R.string.auth_failed)
                    }
                  //  hideProgressDialog()
                    // [END_EXCLUDE]
                })
        // [END sign_in_with_email]
    }
    private fun openMapFragment() {
        val mapFragment = MapFragment()
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.main_fragment_content, mapFragment)
                .setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null).commit()
    }
    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
//        if (!validateForm()) {
//            return
//        }
       // showProgressDialog()
        // [START create_user_with_email]
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity,  { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth!!.getCurrentUser()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
                // [START_EXCLUDE]
                //  hideProgressDialog()
                // [END_EXCLUDE]
            })

        // [END create_user_with_email]
    }
    private fun signOut() {
        mAuth!!.signOut()
        updateUI(null)
    }
    private fun updateUI(user: FirebaseUser?) {
      //  hideProgressDialog()
        if (user != null) {
            Log.i("USER", "USER EXISTS")
            mStatusTextView!!.setText(getString(R.string.emailpassword_status_fmt,
                    user.email, user.isEmailVerified))
            mDetailTextView!!.setText(getString(R.string.firebase_status_fmt, user.uid))

            this.v!!.findViewById<LinearLayout>(R.id.email_password_buttons).setVisibility(View.GONE)
            this.v!!.findViewById<LinearLayout>(R.id.email_password_fields).setVisibility(View.GONE)
            this.v!!.findViewById<LinearLayout>(R.id.signed_in_buttons).setVisibility(View.VISIBLE)
            this.verifyEmailButton!!.setEnabled(!user.isEmailVerified)

            openMapFragment()
        } else {
            Log.i("USER", "USER DOES NOT EXIST")
            mStatusTextView!!.setText(R.string.signed_out)
            mDetailTextView!!.setText(null)

            this.v!!.findViewById<LinearLayout>(R.id.email_password_buttons).setVisibility(View.VISIBLE)
            this.v!!.findViewById<LinearLayout>(R.id.email_password_fields).setVisibility(View.VISIBLE)
            this.v!!.findViewById<LinearLayout>(R.id.signed_in_buttons).setVisibility(View.GONE)
        }
    }

}// Required empty public constructor
