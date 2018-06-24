/*
 * =============================================================================
 *
 *                       Copyright (c), NXP Semiconductors
 *
 *                        (C)NXP Electronics N.V.2013
 *         All rights are reserved. Reproduction in whole or in part is
 *        prohibited without the written consent of the copyright owner.
 *    NXP reserves the right to make changes without notice at any time.
 *   NXP makes no warranty, expressed, implied or statutory, including but
 *   not limited to any implied warranty of merchantability or fitness for any
 *  particular purpose, or that the use will not infringe any third party patent,
 *   copyright or trademark. NXP must not be liable for any loss or damage
 *                            arising from its use.
 *
 * =============================================================================
 */

package com.nxp.sampletaplinx;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.NFCiCodeRead.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.icode.ICode;
import com.nxp.nfclib.icode.ICodeFactory;
import com.nxp.nfclib.icode.IICodeSLIX;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * @author nxp70496 Main start activity.
 */
public class MainActivity extends Activity {


    /**
     * Package Key.
     */
    static String packageKey = "2362d4b6d6adcb1f69a07a2bb9a82d83";
    /**
     * NxpNfclib instance.
     */
    private NxpNfcLib libInstance = null;
    /**
     * ICodeSLIX card object.
     */
    private IICodeSLIX icodeSLIX;

    private TextView tv = null;
    /**
     * Image view inastance.
     */
    private ImageView mImageView = null;
    /**
     * byte array.
     */
    byte[] data;
    /**
     * Constant for permission
     */
    private static final int STORAGE_PERMISSION_WRITE = 113;

    /**
     * Android Handler for handling messages from the threads.
     */
    private static Handler mHandler;

    private boolean bWriteAllowed = false;

    private boolean mIsPerformingCardOperations = false;


    private CardType mCardType = CardType.UnknownCard;

    private LoginButton btnLogin;
    private CallbackManager callbackManager;
    private LoginManager loginManager;

    private String NumPhone;
    private String webURL;
    private String youtubeURL;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());

        /*boolean readPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!readPermission) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_WRITE
            );
        }*/


        /* Initialize the library and register to this activity */
        initializeLibrary();

		/* Get text view handle to be used further */
        initializeView();

		/* Set the UI handler */
        initializeUIhandler();




        Button btn1,btn2,btn3;

        btn1 = (Button) findViewById(R.id.btnVdo);
        btn2 = (Button) findViewById(R.id.btnCall);
        btn3 = (Button) findViewById(R.id.btnWeb);



        btn1.setVisibility(View.INVISIBLE);
        btn2.setVisibility(View.INVISIBLE);
        btn3.setVisibility(View.INVISIBLE);




        /*ImageView bgmain = (ImageView) findViewById(R.id.bgmungring);
        bgmain.setVisibility(View.INVISIBLE);*/

		/* Show About dialog */
//            showDisclaimer();


        /*callbackManager = CallbackManager.Factory.create();
        btnLogin = (LoginButton) findViewById(R.id.login_fb);

        btnLogin.setReadPermissions(Arrays.asList("user_photos","email","public_profile"));

        btnLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this,"Success "+loginResult.getAccessToken().getUserId(),Toast.LENGTH_SHORT).show();

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,last_name,link,email,picture");
                GraphRequest request =  GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        try {
                            String str_email = jsonObject.getString("email");
                            Toast.makeText(MainActivity.this,str_email,Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.i("user",jsonObject.toString());
                    }
                });
                request.setParameters(parameters);
                request.executeAsync();



            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this,"Cancel ",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(MainActivity.this,"Error "+e,Toast.LENGTH_SHORT).show();
            }
        });*/
    }



    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Initializing the UI thread.
     */
    private void initializeUIhandler() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mImageView.getLayoutParams().width = (size.x * 2) / 3;
        mImageView.getLayoutParams().height = size.y / 3;

        mHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                String messageResponse = msg.getData().getString("message");
                char where = msg.getData().getChar("where");
                if (messageResponse != null) {
                    showMessage(messageResponse, where);
                }
            }
        };

        mImageView.setImageResource(R.drawable.main);
    }


    /**
     * Initializing the widget, and Get text view handle to be used further.
     */
    private void initializeView() {

		/* Get text view handle to be used further */
        tv = (TextView) findViewById(R.id.tvLog);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText(R.string.info_string);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/AvenirNextLTPro-MediumCn.otf");
        tv.setTypeface(face);
        tv.setTextColor(Color.BLACK);

		/* Get image view handle to be used further */
        mImageView = (ImageView) findViewById(R.id.cardSnap);
    }

    /**
     * Initialize the library and register to this activity.
     */
    @TargetApi(19)
    private void initializeLibrary() {
        libInstance = NxpNfcLib.getInstance();
        try {
            libInstance.registerActivity(this, packageKey);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * (non-Javadoc).
     *
     * @param intent NFC intent from the android framework.
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    public void onNewIntent(final Intent intent) {
        cardLogic(intent);
        super.onNewIntent(intent);
    }


    private void cardLogic(final Intent intent) {
        CardType type = CardType.UnknownCard;
        try {
            type = libInstance.getCardType(intent);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        switch (type) {
            case ICodeSLIX:
                mCardType = CardType.ICodeSLIX;
                icodeSLIX = ICodeFactory.getInstance().getICodeSLIX(libInstance.getCustomModules());
                try {
                    icodeSLIX.getReader().connect();
                    iCodeSLIXCardLogic();
                } catch (Throwable t) {
                    showMessage("Unknown Error Tap Again!", 't');
                }
                break;
        }
    }


    private void onCardNotSupported(Tag tag) {

    }


    /**
     * ICODE SLIX card logic.
     */
    //GonGGanG
    private void iCodeSLIXCardLogic() {

        byte[] b0 = null;
        byte[] b1 = null;
        byte[] b2 = null;
        byte[] b3 = null;
        byte[] b4 = null;
        byte[] b5 = null;
        byte[] b6 = null;
        byte[] b7 = null;
        byte[] b8 = null;
        byte[] b9 = null;
        byte[] bA = null;
        byte[] bB = null;
        byte[] bC = null;
        byte[] bD = null;
        byte[] bE = null;
        byte[] bF = null;
        byte[] b10 = null;
        byte[] b11 = null;
        byte[] b12 = null;
        byte[] b13 = null;
        byte[] b14 = null;
        byte[] b15 = null;
        byte[] b16 = null;
        byte[] b17 = null;
        byte[] b18 = null;
        byte[] b19 = null;
        byte[] b1A = null;
        byte[] b1B = null;

        showImageSnap(R.drawable.ic_logo);
        try {
            b0 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x00);
            b1 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x01);
            b2 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x02);
            b3 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x03);
            b4 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x04);
            b5 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x05);
            b6 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x06);
            b7 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x07);
            b8 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x08);
            b9 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x09);
            bA = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x0A);
            bB = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x0B);
            bC = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x0C);
            bD = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x0D);
            bE = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x0E);
            bF = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x0F);
            b10 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x10);
            b11 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x11);
            b12 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x12);
            b13 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x13);
            b14 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x14);
            b15 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x15);
            b16 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x16);
            b17 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x17);
            b18 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x18);
            b19 = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x19);
            b1A = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x1A);
            b1B = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, (byte) 0x1B);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //String
        String I = new String(b0);
        showMessage("#ID: " + I, 'd');

        //String
        String a = new String("กขคง");
        String S = new String();
        String P = new String();
        String W = new String();
        String Y = new String();
        String U = new String();
        String M = new String();

        try {
            S = S + new String(b1, "TIS620") + new String(b2, "TIS620");
            S = S + new String(b3, "TIS620") + new String(b4, "TIS620");
            S = S + new String(b5, "TIS620") + new String(b6, "TIS620");
            S = S + new String(b7, "TIS620") + new String(b8, "TIS620");
            P = P + new String(b9, "TIS620") + new String(bA, "TIS620");
            P = P + new String(bB, "TIS620") + new String(bC, "TIS620");
            P = P + new String(bD, "TIS620") + new String(bE, "TIS620");
            P = P + new String(bF, "TIS620");
            W = W + new String(b10, "TIS620");
            Y = Y + new String(b11, "TIS620");
            U = U + new String(b16, "TIS620") + new String(b17, "TIS620");
            M = M + new String(b18, "TIS620") + new String(b19, "TIS620");



        } catch (UnsupportedEncodingException uee) {
        }


        String[] WW = W.split("(?<=\\G.{2})");
        String w1 = WW[0];
        String w2 = WW[1];

        String[] YY = Y.split("(?<=\\G.{2})");
        String y1 = YY[0];
        String y2 = YY[1];

        webURL = U;
        youtubeURL = M;

        showMessage("#Name: " + S, 'd');

        showMessage("#Place: " + P, 'd');

        showMessage("#Time: " + w1+w2+y1+y2,'d');

        showMessage("#www: " + U, 'd');

        showMessage("#youtube: " + M, 'd');



        String call = String.format("%02x%02x%02x%02x", b14[0], b14[1], b14[2], b14[3])+ String.format("%02x%02x%02x%02x", b15[0], b15[1], b15[2], b15[3]);


        String intput = call;

        String number ="";

        for(int i = 0 ; i < intput.length() ;i++){

            char c = intput.charAt(i);

            if(Character.isDigit(c)){

                number += c;

            }

        }

        System.out.println("output : " +number);
        NumPhone = number;


        /*//Number
        showMessage("#ID: " + Integer.toString(ByteBuffer.wrap(b6).getInt()), 'd');*/

        //BCD
        showMessage("#Date: " + String.format("%02x/%02x/%02x%02x", b11[0], b11[1], b11[2], b11[3]), 'd');

        //BCD
        showMessage("#End: " + String.format("%02x/%02x/%02x%02x", b12[0], b12[1], b12[2], b12[3]), 'd');



        TextView title1;
        title1 = (TextView) findViewById(R.id.tvname);
        title1.setMovementMethod(new ScrollingMovementMethod());
        title1.setText("" + S.trim());


        TextView title2;
        title2 = (TextView) findViewById(R.id.tvlocation);
        title2.setMovementMethod(new ScrollingMovementMethod());
        title2.setText("    สถานที่จัดงาน :   " + P.trim());

        TextView title3;
        title3 = (TextView) findViewById(R.id.tvtime);
        title3.setMovementMethod(new ScrollingMovementMethod());
        title3.setText("    เวลาจัดงาน :       " +w1+ ":" +w2+ " น."+ "  ถึง  " + y1 +":"+y2+"  น.");


        TextView title4;
        title4 = (TextView) findViewById(R.id.tvdate);
        title4.setMovementMethod(new ScrollingMovementMethod());
        if (b11[0] == 0) {
            title4.setText("    วันที่จัดงาน :       " + String.format("%02x/%02x/%02x%02x", b12[1], b12[0], b12[2], b12[3]));
        } else {
            title4.setText("    วันที่จัดงาน :       " + String.format("%02x/%02x/%02x%02x   ถึง   ", b12[1], b12[0], b12[2], b12[3])+ String.format("%02x/%02x/%02x%02x", b13[1], b13[0], b13[2], b13[3]));
        }



        Button btn1,btn2,btn3;

        btn1 = (Button) findViewById(R.id.btnVdo);
        btn2 = (Button) findViewById(R.id.btnCall);
        btn3 = (Button) findViewById(R.id.btnWeb);


        btn1.setVisibility(View.VISIBLE);
        btn2.setVisibility(View.VISIBLE);
        btn3.setVisibility(View.VISIBLE);


    }

    @Override
    protected void onPause() {
        super.onPause();
        libInstance.stopForeGroundDispatch();
    }



    @Override
    protected void onResume() {
        super.onResume();
        libInstance.startForeGroundDispatch();
    }

    /**
     * Update the card image on the screen.
     *
     * @param cardTypeId resource image id of the card image
     */

    private void showImageSnap(final int cardTypeId) {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mImageView.getLayoutParams().width = size.x / 3;
        mImageView.getLayoutParams().height = size.y / 3;
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mImageView.setImageResource(cardTypeId);
                mImageView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomnrotate));
            }
        }, 1250);
        mImageView.setImageResource(R.drawable.ic_logo);
        mImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
    }

    /**
     * This will display message in toast or logcat or on screen or all three.
     *
     * @param str   String to be logged or displayed
     * @param where 't' for Toast; 'l' for Logcat; 'd' for Display in UI; 'n' for
     *              logcat and textview 'a' for All
     */
    protected void showMessage(final String str, final char where) {

        switch (where) {

            case 't':
                Toast.makeText(MainActivity.this, "\n" + str, Toast.LENGTH_SHORT)
                        .show();
                break;
            case 'd':
                tv.setText(tv.getText() + "\n-----------------------------------\n"
                        + str);
                break;
            default:
                break;
        }
        return;
    }


    /**
     * This will send the message to the handler with required String and
     * character.
     *
     * @param stringMessage message to be send
     * @param codeLetter    't' for Toast; 'l' for Logcat; 'd' for Display in UI; 'a' for
     *                      All
     */
    protected void sendMessageToHandler(final String stringMessage,
                                        final char codeLetter) {
        Bundle b = new Bundle();
        b.putString("message", stringMessage);
        b.putChar("where", codeLetter);
        Message msg = mHandler.obtainMessage();
        msg.setData(b);
        mHandler.sendMessage(msg);
    }

    private String getApplicationVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Disclaimer Section contain Details About product.
     */
    private void showDisclaimer() {

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("About");
        alert.setCancelable(false);
        String[] cards = libInstance.getSupportedCards();
        showMessage("Supported Cards", 'l');
        String message = getString(R.string.about_text);

        String alertMessage = message + "\n";

        alertMessage += "\n";
        String appVer = getApplicationVersion();
        if (appVer != null)
            alertMessage += "Application version is: " + appVer + "\n";

        alertMessage += "\n\n" + "Supported Cards: "
                + Arrays.toString(cards) + "\n";

        alert.setMessage(alertMessage);

        alert.setIcon(R.drawable.tapnfc);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog,
                                final int whichButton) {

            }
        });

        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Requested permission granted", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(MainActivity.this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }


    public void onCallPhone(View v) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            String phone = NumPhone;
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            startActivity(intent);

        }

    }

    public void onWebsite(View v){
        Intent newActivity = new Intent(Intent.ACTION_VIEW);
        newActivity.setData(Uri.parse("https://goo.gl/"+ webURL ));
        startActivity(newActivity);
    }


    public void onYoutube(View v) {
        Intent newActivity = new Intent(Intent.ACTION_VIEW);
        newActivity.setData(Uri.parse("https://goo.gl/" +youtubeURL));
        startActivity(newActivity);
    }
}
