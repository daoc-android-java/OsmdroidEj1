package ute.griinf.daoc.osmdroid;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

//https://github.com/osmdroid/osmdroid

//Revise el build.gradle de módulo !!!!
//necesita aumentar ahí el repositorio y la dependencia.
//Revise también el manifest por los permisos.
//Recuerde también que si API23+ debe pedir permiso al usuario

public class MainActivity extends Activity {
    private LocationManager locationManager;
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private RotationGestureOverlay rotationGestureOverlay;
    private CompassOverlay compassOverlay;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Incializa osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        map.setMultiTouchControls(true);//permite usar gestos (dos dedos) para zoom o rotar, p.ej.

        //Permite rastrear la ubicación
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);

        //Permite rotar el mapa
        rotationGestureOverlay = new RotationGestureOverlay(map);
        rotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(rotationGestureOverlay);

        //Presenta la brújula
        compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);

        //Permite cambiar el sector visualizado
        mapController = map.getController();
        mapController.setZoom(20.0);

        iniciaUbicar();
    }

    public void onResume(){
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void iniciaUbicar() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager == null) {
            Toast.makeText(this, "No hay GPS en el dispositivo", Toast.LENGTH_SHORT).show();
        } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            muestraUbicacion(location, true);
        } else {
            Toast.makeText(this, "El GPS no está habilitado", Toast.LENGTH_SHORT).show();
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            muestraUbicacion(location, false);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void muestraUbicacion(Location location, boolean startPoint) {
        if(location == null) return;
        //GeoPoint startPoint = new GeoPoint(-0.1817, -78.5077);
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        if(startPoint) {
            mapController.setCenter(point);
        } else {
            mapController.animateTo(point);
        }
    }
}