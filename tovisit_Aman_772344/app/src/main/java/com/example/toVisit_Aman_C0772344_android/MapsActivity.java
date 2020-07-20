package com.example.toVisit_Aman_C0772344_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.toVisit_Aman_C0772344_android.DataModel.SelectedPlaceDatabase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private MarkerOptions markerOptions = new MarkerOptions();
    private SelectedPlaceDatabase roomDatabase;
    private double lat;
    private double lng;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LatLng userLocation;
    private final int REQUEST_CODE = 1;
    private String address;
    private Spinner mapType;
    private AlertDialog.Builder dialogBoxBuilder;
    private String selectedLocationTitle;
    private boolean isFromList = false;
    private double selectedLatitude;
    private double selectedLongitude;
    private int selectedPlaceId;
    private int count = 0;
    private boolean isNearByPlacesVisible = false;
    private Button btnNearByPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        requestPermission();
        checkPermission();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            selectedPlaceId = bundle.getInt("placeId", 0);
            selectedLatitude = bundle.getDouble("latitude", 0);
            selectedLongitude = bundle.getDouble("longitude", 0);
            selectedLocationTitle = bundle.getString("title", null);
            isFromList = bundle.getBoolean("isFromList", false);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mapType = findViewById(R.id.spinner);
        btnNearByPlaces = findViewById(R.id.btn_nearby_places);
        dialogBoxBuilder = new AlertDialog.Builder(this);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.typesofmaps, R.layout.layout_spinner);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mapType.setAdapter(adapter);
        mapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 4:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getUserLocation();
        setLocationManager();
        roomDatabase = SelectedPlaceDatabase.getInstance(MapsActivity.this);
        if (!isFromList) {
            findViewById(R.id.btn_direction).setVisibility(View.GONE);

            btnNearByPlaces.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNearByPlacesVisible) {
                        setComponentVisibility(true);
                        isNearByPlacesVisible = false;
                    } else {
                        setComponentVisibility(false);
                        isNearByPlacesVisible = true;
                    }
                }
            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    mMap.clear();
                    lat = latLng.latitude;
                    lng = latLng.longitude;
                    mMap.addMarker(markerOptions
                            .position(latLng));
                }
            });
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    lat = marker.getPosition().latitude;
                    lng = marker.getPosition().longitude;
                    final LatLng latLng = new LatLng(lat, lng);
                    showDialogBoxBuilder(latLng);
                    return true;
                }
            });
        } else {

            btnNearByPlaces.setVisibility(View.GONE);

            LatLng latLng = new LatLng(selectedLatitude, selectedLongitude);
            googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title(selectedLocationTitle)
                    .draggable(true));
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                    double latitude = marker.getPosition().latitude;
                    double longitude = marker.getPosition().longitude;
                    LatLng latLng = new LatLng(latitude, longitude);
                    getAddressFromGeoCoder(latLng);
                    placeAddress(latLng);

                }
            });
        }
    }

    private void setComponentVisibility(boolean isNearByPlacesVisible) {
        if (isNearByPlacesVisible) {
            this.isNearByPlacesVisible = false;
            findViewById(R.id.btn_restaurant).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_museum).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_cafe).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_clear).setVisibility(View.VISIBLE);
        } else {
            this.isNearByPlacesVisible = true;
            findViewById(R.id.btn_restaurant).setVisibility(View.GONE);
            findViewById(R.id.btn_museum).setVisibility(View.GONE);
            findViewById(R.id.btn_cafe).setVisibility(View.GONE);
            findViewById(R.id.btn_clear).setVisibility(View.GONE);
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
        setUserLocation();
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {
        StringBuilder placeUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        int SEARCH_RADIUS = 40000;
        placeUrl.append("location=").append(latitude).append(",").append(longitude);
        placeUrl.append("&radius=").append(SEARCH_RADIUS);
        placeUrl.append("&type=").append(nearbyPlace);
        placeUrl.append("&key=AIzaSyB45lwuNXNnXYsc3WHA1QyJKIkxqE-Rb7A");
        System.out.println(placeUrl.toString());
        return placeUrl.toString();
    }

    private void setUserLocation() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    MapsActivity.this.userLocation = userLocation;
                    if (count == 0) {
                        count++;
                        CameraPosition cameraPosition = CameraPosition.builder()
                                .target(userLocation)
                                .zoom(15)
                                .bearing(0)
                                .tilt(45)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mMap.addMarker(new MarkerOptions().position(userLocation)
                                .title("My Location"));
                    }
                }
            }
        };
    }


    private void setLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        LatLng latLng = new LatLng(lat, lng);
        userLocation = latLng;
        Log.d("LOCATION", latLng.latitude + "," + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
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

    private boolean checkPermission() {
        requestPermission();
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUserLocation();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private void placeAddress(LatLng latLng) {

        Location currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(userLocation.latitude);
        currentLocation.setLongitude(userLocation.longitude);

        Location newLocation = new Location("newLocation");
        newLocation.setLatitude(latLng.latitude);
        newLocation.setLongitude(latLng.longitude);

        float distance = currentLocation.distanceTo(newLocation) / 1000; // in km

        SelectedPlaceData selectedPlaceData = new SelectedPlaceData();
        selectedPlaceData.setLatitude(latLng.latitude);
        selectedPlaceData.setLongitude(latLng.longitude);
        selectedPlaceData.setTitle(address);
        selectedPlaceData.setDistance(distance);
        if (isFromList) {
            selectedPlaceData.setPlace_id(selectedPlaceId);
            roomDatabase.favouriteDataDao().updateFavouriteData(selectedPlaceData);

        } else {
            roomDatabase.favouriteDataDao().insertFavouritePlaceData(selectedPlaceData);
        }
    }

    private String getAddressFromGeoCoder(LatLng latLng) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy");
        String addDate = simpleDateFormat.format(calendar.getTime());
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                address = addresses.get(0).getLocality() + " " + addresses.get(0).getAddressLine(0);
                System.out.println(addresses.get(0).getAddressLine(0));
                if (address.equals("null")) {
                    address = addDate;
                }
            } else {
                address = addDate;
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        return address;
    }

    public void btnClick(View view) {
        Object[] dataTransfer = new Object[2];
        String url;
        NearByPlaces getNearbyPlaceData = new NearByPlaces();

        switch (view.getId()) {
            case R.id.btn_restaurant:
                setComponentVisibility(false);
                url = getUrl(userLocation.latitude, userLocation.longitude, "restaurant");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlaceData.execute(dataTransfer);
                break;

            case R.id.btn_museum:
                setComponentVisibility(false);

                url = getUrl(userLocation.latitude, userLocation.longitude, "museum");
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlaceData.execute(dataTransfer);
                break;

            case R.id.btn_cafe:
                setComponentVisibility(false);
                url = getUrl(userLocation.latitude, userLocation.longitude, "cafe");
                dataTransfer = new Object[2];
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlaceData.execute(dataTransfer);
                break;

            case R.id.btn_clear:
                setComponentVisibility(false);
                mMap.clear();
                count = 0;
                break;

            case R.id.btn_direction:
                LatLng customMarker = new LatLng(selectedLatitude, selectedLongitude);
                dataTransfer = new Object[4];
                url = getDirectionUrl();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = customMarker;
                FetchDirectionData getDirectionsData = new FetchDirectionData();
                getDirectionsData.execute(dataTransfer);
                break;
        }
    }

    private String getDirectionUrl() {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionUrl.append("origin=").append(userLocation.latitude).append(",").append(userLocation.longitude);
        googleDirectionUrl.append("&destination=").append(selectedLatitude).append(",").append(selectedLongitude);
        googleDirectionUrl.append("&key=AIzaSyB45lwuNXNnXYsc3WHA1QyJKIkxqE-Rb7A");
        Log.d("", "getDirectionUrl: " + googleDirectionUrl);
        return googleDirectionUrl.toString();
    }

    private void showDialogBoxBuilder(final LatLng latLng) {
        dialogBoxBuilder.setMessage("Add " + getAddressFromGeoCoder(latLng) + " to your favourite list")
                .setTitle("Make Favourite")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        placeAddress(latLng);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });
        AlertDialog alert = dialogBoxBuilder.create();
        alert.show();
    }
}