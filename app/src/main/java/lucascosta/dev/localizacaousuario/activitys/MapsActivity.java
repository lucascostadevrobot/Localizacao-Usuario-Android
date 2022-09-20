package lucascosta.dev.localizacaousuario.activitys;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lucascosta.dev.localizacaousuario.R;
import lucascosta.dev.localizacaousuario.databinding.ActivityMapsBinding;
import lucascosta.dev.localizacaousuario.utills.Permissoes;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Declarando nossos atributos
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //Adicionando as permissões na classe principal que precisamos trabalhar
    //Para isso criaremos um array de Strings com o nome de permissoes.
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Validando as permissões declaradas na classe principal
        //Chamando nosso objetivo permissoes para validar
        Permissoes.validarPermissoes(permissoes, this, 1);

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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Objeto responsável por gerenciar a localização do usuário
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); //Recuperamos locationmanager
        //Objeto instanciado do nosso atributo locationListener
        locationListener = new LocationListener() {
            @Override
            //
            public void onLocationChanged(@NonNull Location location) {
                Log.d("Localização", "onLocationChanged: " + location.toString());
                // Add a marker in Sydney and move the camera
                //Criando variavel latitude e longitude no escopo do metodo
                mMap.clear(); //Limpar os marcados antes de criar um novo
                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();
                LatLng localusuario = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(localusuario).title("Meu Local"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localusuario, 25)); //Dando zoom na localizaçao do usuario dentro do mapa


                /*
                * Geocoding -> processo de transformar um endereço ou descrição de um local em latitude/longitude
                * Reverse Geocoding -> processo de transformar latitude/longitude em um endereço
                * */

                //1º Começaremos a realizar o processo de Geocoding
                //Locale.getDefault é necessário para que o Geocoder saiba a localizacao que vai tratar os endereços
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude, 1); //Podemos definir quantos resultados queremos (double lat, double long, int maxResults)
                    if(listaEndereco != null && listaEndereco.size() > 0){
                        Address endereco = listaEndereco.get(0);
                        endereco.toString(); //Convertendo objeto endereços em strings
                        Log.d("local", "OnlocationChanged: ");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
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

        /* Segunda vez que executamos o metodo de gerenciamento de permissões.
         * 1)Provedor da localização
         * 2)Tempo mínimo entre atualizações de localizações (milesegundos)
         * 3)Distancia mínima entre atualizações de localizações
         * 4)Location listener (para recebermos as atualizações)
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            ); //Atualizando a localizacao do user
        }

        //Chamando o arquivo json com a estilização do mapa através desse metodo
        //Através dessa funcionalidade conseguimos chamar um mapa estilizado no arquivo JSON
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
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
            builder.setPositiveButton("Confirmar", (dialogInterface, i) -> finish());

            AlertDialog dialog = builder.create();
            dialog.show();
        }

}
