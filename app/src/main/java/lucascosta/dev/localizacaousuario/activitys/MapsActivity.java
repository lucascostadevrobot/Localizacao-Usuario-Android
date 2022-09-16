package lucascosta.dev.localizacaousuario.activitys;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.location.LocationListenerCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import lucascosta.dev.localizacaousuario.R;
import lucascosta.dev.localizacaousuario.databinding.ActivityMapsBinding;
import lucascosta.dev.localizacaousuario.util.Permissoes;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Declarando nossos atributos
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //Adicionando as permissões na classe principal que precisamos trabalhar
    //Para isso criaremos um array de Strings com o nome de permissoes.
    private String[] permissoes =  new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Validando as permissões declaradas na classe principal
        //Chamando nosso objetivo permissoes para validar
        Permissoes.validarPermissoes(permissoes, this, 1);

        //Objeto responsável por gerenciar a localização do usuário
        locationManager =  (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); //Recuperamos locationmanager
        //Objeto instanciado do nosso atributo locationListener
        locationListener = new LocationListener() {
            @Override
            //
            public void onLocationChanged(Location location) {
                Log.d("Localização","onLocationChanged: " + location.toString());
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    //Sobrescrevendo o metodo onRequestPermissionResults para passar as permissoes
    //Dentro desse metodo iremos criar um for para percorrer as permissões durante a execução
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //for para percorrer as permissoes
        for (int permissaoResultado : grantResults) {
            //If para identificar se a permissão quando percorrida será negada pelo user
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                //Alerta caso a permissão seja aceitada
                alertaValidacaoPermissao();    //Metodo para alertar validacao
            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {
                /*
                Começamos aqui recuperar a localização do user
                Mas para cair nesse else o mesmo precisa da PERMISSAO_GRANTED
                Neste caso trabalhamos todas as atualizações em cada etapa do passo do user
                Se ele se mover com 10 passos o mesmo vai atualizar o provedor de GPS
                e dentro do tempo estabelecido por nós, em segundos receberemos atualizações.

                * 1)Provedor da localização
                * 2)Tempo mínimo entre atualizações de localizações (milesegundos)
                * 3)Distancia mínima entre atualizações de localizações
                * 4)Location listener (para recebermos as atualizações)*/
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0,
                            locationListener
                    ); //Atualizando a localizacao do user
                }

            }

        }
    }

        //Metodo chamado no for para validar as permissões
        //Com esse metodo usamos o alertDialog para exibir uma mensagem ao user
        private void alertaValidacaoPermissao() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permissões Negagadas");
            builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
            builder.setCancelable(false);
            builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
}
