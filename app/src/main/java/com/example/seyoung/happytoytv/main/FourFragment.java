package com.example.seyoung.happytoytv.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.happytoytv.R;
import com.example.seyoung.happytoytv.ethereum.TokenERC20;
import com.example.seyoung.happytoytv.ethereum.WalletCreateActivity;
import com.example.seyoung.happytoytv.ethereum.WalletSendActivity;
import com.google.zxing.integration.android.IntentIntegrator;

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

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by seyoung on 2018-07-06.
 */

public class FourFragment extends Fragment{
    public static FourFragment newInstance() {
        return new FourFragment();
    }
    TextView pname,walletadress,takeToken;                                //사용자의 이름을 보여줄 텍스스트뷰 입니다.
    ImageView pimage;                               //사용자의 프로필 사진을 보여줄 이미지뷰입니다.
    ImageButton gowallet;
    Button walletcreatebutton,sendbutton,qrScanbutton;
    String userid,profilepic;
    EditText towalletadress,tokenprice;
    public RequestManager mGlideRequestManager;

    WalletCreate wc = new WalletCreate();
    Web3j web3 ;

    String smartcontract = "0xc45ba41f64b3d21c2d99e90c5cb538f5e1e2afd2";
    String passwordwallet;
    File DataDir;
    ImageView qr_small, qr_big;
    BigInteger GasPrice, GasLimit;
    SharedPreferences pref;
    IntentIntegrator qrScan;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fourfragment, container, false);
        mGlideRequestManager = Glide.with(this);

        pname =  view.findViewById(R.id.p_name);            //사용자의 이름
        pimage = view.findViewById(R.id.p_image);           //사용자의 사진
        walletcreatebutton = view.findViewById(R.id.walletbutton);
        gowallet = view.findViewById(R.id.gowallet);
        pref = getContext().getSharedPreferences("pref", MODE_PRIVATE);
        userid= pref.getString("ing", "s");
        profilepic= pref.getString(userid, "s");
        pname.setText(userid);

        mGlideRequestManager.load(profilepic)
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .override(300,300)
                        .fitCenter()
                        .centerCrop()
                        .circleCrop()
                )
                .into(pimage);

        web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/13da6e18b5574362bd48d8dd3e1ac44f"));


        pref = getContext().getSharedPreferences("pref", MODE_PRIVATE);
        passwordwallet = pref.getString("password", "");

        DataDir = getContext().getExternalFilesDir("/keys/");


        walletcreatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WalletCreateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("data", "nova");
                startActivityForResult(intent,1);
            }
        });
        gowallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WalletSendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
      File KeyDir = new File(this.DataDir.getAbsolutePath());
        Log.e("파일 경로", this.DataDir.getAbsolutePath());

        File[] listfiles = KeyDir.listFiles();
        Log.e("지갑 몇개?",listfiles.length+"");
        if (listfiles.length == 0 ) {
            walletcreatebutton.setVisibility(View.VISIBLE);

        } else {
            gowallet.setVisibility(View.VISIBLE);
        }

        Log.e("onCreate","");

        return view;
    }

    //////////////////// END QR SCAN ////////////////////////

    ///////////////////// Create and Load Wallet /////////////////
    public class WalletCreate extends AsyncTask<Void, Integer, JSONObject> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            /**
             // Получаем список файлов в каталоге
             // Get list files in folder
             */
            File KeyDir = new File(DataDir.getAbsolutePath());
            File[] listfiles = KeyDir.listFiles();
            File file = new File(String.valueOf(listfiles[0]));
            Log.e("Load wallet which :",file.toString());
            try {
                /**
                 // Загружаем файл кошелька и получаем адрес
                 // Upload the wallet file and get the address
                 */
                Credentials credentials = WalletUtils.loadCredentials(passwordwallet, file);
                String address = credentials.getAddress();
                Log.e("wallet ","Eth Address: " + address);

                /**
                 // Получаем Баланс
                 // Get balance Ethereum
                 */
                EthGetBalance etherbalance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
                String ethbalance = Convert.fromWei(String.valueOf(etherbalance.getBalance()), Convert.Unit.ETHER).toString();
                Log.e("wallet ","Eth Balance: " + ethbalance);

                /**
                 // Загружаем Токен
                 // Download Token
                 */
                GasPrice = Convert.toWei("70000",Convert.Unit.GWEI).toBigInteger();
                GasLimit = BigInteger.valueOf(Integer.valueOf(String.valueOf("70000")));

                TokenERC20 token = TokenERC20.load(smartcontract, web3, credentials, GasPrice, GasLimit);
                Log.e("gasPrice",GasPrice+"");
                Log.e("gasLimit",GasLimit+"");
                /**
                 // Получаем название токена
                 // Get the name of the token
                 */
                String tokenname = token.name().send();
                Log.e("wallet ","Token Name: " + tokenname);

                /**
                 // Получаем Символ Токена
                 // Get Symbol marking token
                 */
                String tokensymbol = token.symbol().send();
                Log.e("wallet ","Symbol Token: " + tokensymbol);

                /**
                 // Получаем адрес Токена
                 // Get The Address Token
                 */
                String tokenaddress = token.getContractAddress();
                Log.e("wallet ","Address Token: " + tokenaddress);

                /**
                 // Получаем общее количество выпускаемых токенов
                 // Get the total amount of issued tokens
                 */
                BigInteger totalSupply = token.totalSupply().send();
                Log.e("wallet ","Supply Token: "+totalSupply.toString());

                /**
                 // Получаем количество токенов в кошельке
                 // Receive the Balance of Tokens in the wallet
                 */
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
            if (result != null ){
                walletcreatebutton.setVisibility(View.GONE);
                gowallet.setVisibility(View.VISIBLE);
            }
            else{
                Log.e("wallet create ","Error!!!");
            }

        }
    }

}
