package com.example.seyoung.happytoytv.ethereum;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.config.Config;
import com.example.seyoung.happytoytv.main.CaptureActivityAnyOrientation;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.simple.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigInteger;

/**
 * 사용자가 모바일 지갑의 정보와 토큰확인, 토큰 전송하는 액티비티입니다.
 * 큐알코드로 상대방의 지갑 주소를 확인할 수 있으며
   내 계좌정보와 토큰 전송 후 결과 값을 이더스캔 홈페이지로 이동시켜줍니다.
 */

public class WalletSendActivity extends AppCompatActivity {
    TextView walletadress,takeToken;      // 해당 지갑의 주소와 토큰 갯수를 보여줄 텍스트뷰
    Button sendbutton;                    // 다른 사용자에게 토큰을 보낼 때 누르는 보내기 버튼
    Button qrScanbutton;                  // 해당 지갑의 주소를 나타낼 QR버튼. 누르면 큰 QR화면을 보여준다.
    Button goetherscan;                   // 토큰을 보냈을 경우, 해당 트랜잭션 이더스캔 홈페이지로 이동된다.
    Button myetherscan;                   // 해당 지갑의 계좌정보를 보여줄 이더스캔 홈페이지 이동 버튼
    EditText towalletadress,tokenprice;   // 보낼 사용자의 지갑 주소와 토큰 갯수를 적는 에딧텍스트
    String transaction;                   // 유저가 토큰을 보내고 난 뒤, 받아온 트랜잭션을 넣을 String
    Web3j web3;                           // 이더리움 블록체인 네트워크에 쉽게 접속시켜줄 web3j
    Config config = new Config();         // 사용자의 지갑 비밀번호나 트랜잭션을 저장할 config.java
    String smartcontract = "0xc45ba41f64b3d21c2d99e90c5cb538f5e1e2afd2";    // 스마트 컨트랙트 값
    String passwordwallet;                // 지갑의 비밀번호를 담을 문자
    String address;                       // 지갑의 주소
    File DataDir;                         // 모바일 지갑을 저장할 내장파일 주소
    ImageView qr_small, qr_big;           // 해당 모바일 지갑의 qr이미지를 작고 크게 보여준다.
    BigInteger GasPrice = BigInteger.valueOf(70000), GasLimit = BigInteger.valueOf(72000);  // 사용자가 토큰을 보낼 때, 사용하는 가스 비용과 가스 제한
    SharedPreferences pref;               // 지갑의 비밀번호를 담을 쉐어드
    IntentIntegrator qrScan;              // 사용자가 상대방 QR이미지를 인식하고 싶을 때 불러오는 Intent
    private SwipeRefreshLayout mSwipeRefreshLayout; // 사용자가 화면을 아래로 당길 때 새로고침 프로세스 다이얼로그
    boolean Swipetrue = false;            // 지갑 불러오는 다이얼로그와 스와이프 다이얼로그 유무 불린값

    @Nullable
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walletsend);
        walletadress = findViewById(R.id.walletadress);
        takeToken = findViewById(R.id.takeToken);
        sendbutton = findViewById(R.id.sendbutton);
        goetherscan = findViewById(R.id.goetherscan);
        myetherscan = findViewById(R.id.myetherscan);
        qr_small = findViewById(R.id.qr_small);
        qrScanbutton = findViewById(R.id.qrScan);
        qrScan = new IntentIntegrator(this);

        towalletadress = findViewById(R.id.towalletadress);
        tokenprice = findViewById(R.id.tokenprice);

        //web3j로 이더리움 네트워크에 접속한다.
        web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/13da6e18b5574362bd48d8dd3e1ac44f"));

        //쉐어드에 저장된 해당 모바일 지갑의 비밀번호를 가져온다.
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        passwordwallet = pref.getString("password", "");
        //토큰 전송 후 해당 트랜잭션 값을 가져온다.
        transaction = config.gettransaction();
        //모바일지갑(keystore)의 위치를 찾는다.
        DataDir = getExternalFilesDir("/keys/");

        //상대방에게 토큰전송 버튼을 눌렀을 때, 토큰 전송 메서드를 실행시킨다.
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendingToken st = new SendingToken();
                st.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        //해당 지갑의 작은 qr코드를 눌렀을 때 실행되는 클릭메서드
        qr_small.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(WalletSendActivity.this);
                dialog.setContentView(R.layout.qr_view);        //해당 모바일 qr이미지를 다이얼로그에서 커다랗게 보여준다.
                qr_big = (ImageView) dialog.findViewById(R.id.qr_big);
                qr_big.setImageBitmap(QRGen(walletadress.getText().toString(), 600, 600));
                dialog.show();
            }
        });
        //상대방의 qr이미지를 인식하고싶을 때 누르는 qr버튼
        qrScanbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrScan.setCaptureActivity(CaptureActivityAnyOrientation.class);
                qrScan.setOrientationLocked(false);
                qrScan.setBarcodeImageEnabled(true);
                qrScan.initiateScan();  //해당 액티비티에서 qr인식 카메라를 불러온다.
            }
        });
        //토큰전송 완료 후 해당 트랜잭션의 이더스캔으로 이동하는 버튼
        goetherscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletSendActivity.this,Etherwebview.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("url", "https://ropsten.etherscan.io/tx/"+transaction);
                startActivity(intent);
            }
        });
        //내 지갑의 계좌정보를 이더스캔으로 볼 수 있게 연결해주는 버튼 클릭리스너
        myetherscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletSendActivity.this,Etherwebview.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("url","https://ropsten.etherscan.io/address/"+address);
                startActivity(intent);
            }
        });
        //해당 지갑의 파일 위치에서 지갑의 갯수를 구합니다.
        File KeyDir = new File(this.DataDir.getAbsolutePath());
        Log.e("파일 경로", this.DataDir.getAbsolutePath());

        File[] listfiles = KeyDir.listFiles();
        Log.e("지갑 몇개?",listfiles.length+"");

        //지갑이 0개일 경우 지갑이 없다는 정보를 띄웁니다.
        if (listfiles.length == 0 ) {
            Toast.makeText(WalletSendActivity.this, "지갑이 없어요!", Toast.LENGTH_LONG).show();
        } else {    //지갑이 있을 경우, 해당 지갑의 정보를 불러옵니다.
            WalletCreate wc = new WalletCreate();
            wc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        //사용자가 화면을 아래로 새로고침할 때 나타낼 다이얼로그.
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {       //새로고침할 때 해당 지갑의 정보도 다시 불러옵니다.
                Swipetrue = true;
                WalletCreate wc = new WalletCreate();
                wc.execute();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    //해당 지갑의 주소를 가지고 QRImage를 만드는 함수입니다.
    public Bitmap QRGen(String Value, int Width, int Heigth) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Bitmap bitmap = null;
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(Value, BarcodeFormat.DATA_MATRIX.QR_CODE, Width, Heigth);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.e("onActivityResult","");
       if (result != null) {
            if (result.getContents() == null) { //사용자가 상대방의 QRImage를 스캔하지 않거나 빈 값을 가져올 때
               // Toast.makeText(WalletSendActivity.this, "Result Not Found", Toast.LENGTH_LONG).show();
                Log.e("Result Not Found","");
            } else { // 상대방의 QRImage를 인식해, 주소값을 꺼냅니다.
                towalletadress.setText(result.getContents().replace("ethereum:", ""));
                Toast.makeText(WalletSendActivity.this, result.getContents().replace("ethereum:", ""), Toast.LENGTH_LONG).show();
                Log.e("Result Found",result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //해당 지갑의 정보를 가져오는 AsyncTask입니다.
    public class WalletCreate extends AsyncTask<Void, Integer, JSONObject> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            if(!Swipetrue)
            mDialog = ProgressDialog.show(WalletSendActivity.this,"Please wait...", "Load Wallet ...", true);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            //해당 지갑의 keystore를 가져옵니다.
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));
            Log.e("Load wallet which :",file.toString());
            try {
                 //지갑 파일을 업로드하고 해당 지갑의 주소를 얻습니다.
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                address = credentials.getAddress();
                Log.e("wallet ","Eth Address: " + address);

                //지갑의 이더리움 갯수를 받아옵니다.
                EthGetBalance etherbalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalance = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                Log.e("wallet ","Eth Balance: " + ethbalance);

                //토큰의 정보를 가져옵니다.
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GasPrice, GasLimit);
                Log.e("gasPrice",GasPrice+"");
                Log.e("gasLimit",GasLimit+"");

                //토큰 이름을 가져옵니다.
                String tokenname = token.name().send();
                Log.e("wallet ","Token Name: " + tokenname);

                //해당 토큰의 심볼을 받아옵니다.
                String tokensymbol = token.symbol().send();
                Log.e("wallet ","Symbol Token: " + tokensymbol);

                //토큰을 받을 수 있는 주소값
                String tokenaddress = token.getContractAddress();
                Log.e("wallet ","Address Token: " + tokenaddress);

                //해당 토큰의 처음 생성햇던 전체갯수를 받아옵니다.
                BigInteger totalSupply = token.totalSupply().send();
                Log.e("wallet ","Supply Token: "+totalSupply.toString());

                //해당 토큰의 갯수를 가져옵니다.
                BigInteger tokenbalance = token.balanceOf(address).send();
                Log.e("wallet ","Balance Token: "+ tokenbalance.toString());

                JSONObject result = new JSONObject();
                result.put("ethaddress",address);
                result.put("ethbalance", ethbalance);
                result.put("tokenbalance", tokenbalance.toString());
                result.put("tokenname", tokenname);
                result.put("tokensymbol", tokensymbol);
                result.put("tokenaddress",tokenaddress);
                result.put("tokensupply", totalSupply.toString());
                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if(!Swipetrue)
                mDialog.dismiss();
            if (result != null ){   //해당 지갑의 주소와 토큰 갯수를 텍스트뷰에 지정합니다.
                walletadress.setText(result.get("ethaddress").toString());
                takeToken.setText(result.get("tokenbalance").toString());
                qr_small.setImageBitmap(QRGen(result.get("ethaddress").toString(), 200, 200));
                mSwipeRefreshLayout.setRefreshing(false);
                Swipetrue = false;
            }
            else{
                Log.e("wallet create ","Error!!!");
            }

        }
    }
    // 토큰을 보낼 때 사용하는 AsyncTask
    public class SendingToken extends AsyncTask<Void, Integer, JSONObject> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = ProgressDialog.show(WalletSendActivity.this,"Please wait...", "Sending Token ...", true);
        }

        @Override
        protected JSONObject doInBackground(Void... param) {

            //해당 지갑의 keystore를 가져옵니다.
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));

            try {
                //해당 지갑의 주소값을 가져옵니다.
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                Log.e("sending token ","Eth Address: " + address);


                GasPrice = Convert.toWei("100",Convert.Unit.GWEI).toBigInteger();
                GasLimit = BigInteger.valueOf(Integer.valueOf(String.valueOf("70000")));

                //해당 지갑의 토큰 정보를 받아옵니다.
                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials,GasPrice, GasLimit);

                String status = null;   //토큰이 전송완료되면 받아올 트랜잭션 값
                String balance = null;  // 토큰의 남은 갯수를 넣을 문자
                // 사용자가 보낼 토큰 값

                BigInteger sendvalue = BigInteger.valueOf(Long.parseLong(String.valueOf(tokenprice.getText())));
                // 보낼 토큰 값을 뺀 뒤, 해당 트랜잭션값을 담습니다.
                status = token.transfer(String.valueOf(towalletadress.getText()), sendvalue).send().getTransactionHash();

                // 해당 토큰의 원래 갯수를 넣어줍니다.
                BigInteger tokenbalance = token.balanceOf(address).send();
                Log.e("sending token ","Balance Token: "+ tokenbalance.toString());
                balance = tokenbalance.toString();

                JSONObject result = new JSONObject();
                result.put("status",status);
                result.put("balance",balance);

                return result;
            } catch (Exception ex) {System.out.println("ERROR:" + ex);}

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            mDialog.dismiss();
            Log.e("토큰 스트링",result.toString());
            if (result != null) {
                //남은 토큰의 갯수를 텍스트뷰에 지정합니다.
                takeToken.setText(result.get("balance").toString());
                Toast toast = Toast.makeText(WalletSendActivity.this,"토큰 전송 완료!", Toast.LENGTH_LONG);
                toast.show();
                //완료된 트랜잭션값 지정과, 해당 트랜잭션 이더스캔 이동 버튼을 보여줍니다.
                transaction = result.get("status").toString();
                config.settransaction(transaction);
                goetherscan.setVisibility(View.VISIBLE);
            } else {System.out.println();}
        }
    }
}
