package com.ktu.svylaklavke.tripmaster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.GamesMetadata;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.wallet.MaskedWallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private Button find_path_button;
    EditText origin_TextView, destination_TextView;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    public static ArrayList<InterestingPoint> points = new ArrayList<InterestingPoint>();
    public int current_route = 0;
    private ProgressDialog progressDialog;
    public ProgressDialog pd;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private TextView lab= null;
    public Instructions instructions;
    public Button next_point;
    public List<Route> routes;
    public boolean secondTime = false;
    public String waypoints;
    public static String lol = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //UI elements that are used in program
        find_path_button = (Button) findViewById(R.id.submit);
        origin_TextView = (EditText) findViewById(R.id.from);
        destination_TextView = (EditText) findViewById(R.id.to);
        find_path_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendRequest();
            }
        });


    }
    public void change_direction()
    {
        int main = findMainRoute(routes);
        Route mainRoute = routes.get(main);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mainRoute.instructions.get_LatLng(), 15));
        mMap.addMarker(new MarkerOptions().position(mainRoute.instructions.get_LatLng()).title("Starting point"));
        lab.setText(mainRoute.instructions.get_instruction());
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


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }
    private void sendRequest()
    {
        String origin = origin_TextView.getText().toString();
        String destination = destination_TextView.getText().toString();
        origin_TextView.setVisibility(View.GONE);
        destination_TextView.setVisibility(View.GONE);
        find_path_button.setVisibility(View.GONE);
        lab =(TextView) findViewById(R.id.lab_from);
        next_point = (Button) findViewById(R.id.next_direction);
        next_point.setVisibility(View.VISIBLE);
        findViewById(R.id.lab_to).setVisibility(View.INVISIBLE);

        //Checking if user has inputed both: Starting point and destination.
        if (origin.isEmpty()) {
            Toast.makeText(this, "You forgot starting point of the journey!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "You forgot the destination of your journey!", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            new DirectionFinder (this,origin,destination,waypoints).execute();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction!", true);
        drop_old_markers();
    }

    public void drop_old_markers() {
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(final List<Route> routes, Instructions instructions) {
        progressDialog.dismiss();
        next_point.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                change_direction();
            }
        });
        this.routes = routes;
        this.instructions = instructions;
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
if (secondTime == false) {
    mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(Polyline polyline) {
            for (Route route : routes) {
                if (route.points.containsAll(polyline.getPoints()))
                    route.mainRoute = true;
                else
                    route.mainRoute = false;
                if ((route.points.containsAll(polyline.getPoints())) && route.mainRoute == true) {
                    mMap.clear();

                    for (Polyline poly : polylinePaths) {
                        poly.remove();
                    }
                    int main = findMainRoute(routes);
                    PolylineOptions polylineOptions = new PolylineOptions().
                            geodesic(true).
                            color(Color.BLUE).
                            width(20);
                    draw_points(routes.get(main));
                    generatePlaces(routes.get(main).points_of_interest);
                    secondTime = true;
                    sendRequest();


                    for (int i = 0; i < route.points.size(); i++) {
                        polylineOptions.add(routes.get(main).points.get(i));

                    }
                    polylinePaths.add(mMap.addPolyline(polylineOptions));

                    //gauto pagr kelio ir jo vietu marsruto redagavimas
                    //generatePlaces(route.points_of_interest);
                    //sendRequest();
                    //making polyline unclickable
                    mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                        @Override
                        public void onPolylineClick(Polyline polyline) {
                            Log.d("pol", "onPolylineClick: a ir nieko nebun");
                        }
                    });
                    //-----------------------------

                }
            }
            polylinePaths.clear();
        }
    });
}


        setPolylinesPaths(routes);
        current_route = 0;
        for(Route route : routes)
        {
            //visos lankytinos vietos aplink visus kelius

            for (LatLng location:route.points) {
                Log.d("url", create_url(location.latitude,location.longitude,100));
                //new InterestingPlacesJson().execute(create_url(location.latitude,location.longitude,100));
                InterestingPlacesJson placeGetter = new InterestingPlacesJson();
                try {
                    placeGetter.execute(create_url(location.latitude,location.longitude,100),Integer.toString(current_route)).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                Log.d("draw", "vietos asd asd asd " + lol);
                Log.d("vardai", "onDirectionFinderSuccess: routes ilgis" + points.size());
            }
            for(InterestingPoint pnt : points)
                Log.d("vardai", "onDirectionFinderSuccess: " + pnt.Name);

            draw_points(route);
        }

        //draw_route(routes);
    }

    public static void addPoint(InterestingPoint pt)
    {
        Log.d("json", "addPoint: prideda taska ");
        points.add(pt);
        Log.d("json", "addPoint: ilgis "+ points.size());

    }
    public void draw_points(Route route){
        Log.d("draw", "kelias " + route.startAddress);
        for (InterestingPoint point:route.points_of_interest){
            Log.d("draw", "vietos viduj " + point.Name);
            mMap.addMarker(new MarkerOptions().position(point.Coordinates).title(point.Name));
        }
    }

    public void draw_route(List<Route> routes) {
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            mMap.addMarker(new MarkerOptions().position(route.startLocation).title("Starting point"));
            mMap.addMarker(new MarkerOptions().position(route.endLocation).title("Destination"));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(20);

            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
        lab.setText(instructions.get_instruction() + " ");
        mMap.addMarker(new MarkerOptions().position(instructions.get_LatLng()).title("Starting point"));
    }

    public void generatePlaces(List<InterestingPoint> points)
    {
        int cnt = 0;
        waypoints="&waypoints=";
        if  (points != null) {
            for (InterestingPoint place : points) {
                 if (cnt > 0 && cnt <23)
                waypoints =waypoints + place.Coordinates.latitude + "%2C" + place.Coordinates.longitude+'|';
                cnt++;
            }
        }
    }

    private void setPolylinesPaths(List<Route> routes) {
        if (secondTime == false)
        routesSwap(routes);
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            polylinePaths.add(createPolylineFromPoints(route));
        }
    }

    private Polyline createPolylineFromPoints(Route route) {
        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(route.getRouteColor()).
                width(route.getWidth());

        for (int i = 0; i < route.points.size(); i++) {
            polylineOptions.add(route.points.get(i));
        }
        Polyline poly = mMap.addPolyline(polylineOptions);
        poly.setClickable(true);
        return poly;
    }

    void routesSwap(List<Route> list) {
        int i = findMainRoute(list);
        Route r = list.get(i);
        list.remove(i);
        list.add(r);
    }

    int findMainRoute(List<Route> list) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).mainRoute)
                return i;
        //Siaip, grazinti -1 negali, saugumo sumetimais gal geriau rasyti 0?
        return -1;
    }

    public String create_url(double lat, double lng, int radius)
    {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+','+lng+ "&radius="+radius+ "&type=point_of_interest&key=AIzaSyCOyvhOhyvg3m26pqN6JB3FgUsTJM5HCOs";
    }


    private class InterestingPlacesJson extends AsyncTask<String, String, String> {
        int index = 0;
        protected void onPreExecute() {
            super.onPreExecute();

            points = new ArrayList<>();
            pd = new ProgressDialog(MapsActivity.this);
            pd.setMessage("Please wait good kid maad city");
            pd.setCancelable(true);
          //  if (pd.isShowing() == false)
              //  pd.show();
        }

        protected String doInBackground( String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                index = Integer.parseInt(params[1]);

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }
                if (pd.isShowing()){
                 //   pd.dismiss();
                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            try {

                parseJSONPlaces(result);
                draw_points(routes.get(index));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        public void parseJSONPlaces(String data) throws JSONException {
            Log.d("json", "parseJSONPlaces: ATEINA?");

            JSONObject placesObject = new JSONObject(data);
            String status = placesObject.getString("status");
            if (status.equals("OK")) {
                //JSONArray results = new JSONArray("results");
                JSONArray results = placesObject.getJSONArray("results");
                Log.d("json", "parseJSONPlaces: results ilgis  " + results.length());
                for (int i = 0; i < results.length(); i++) {
                    JSONObject element = results.getJSONObject(i);
                    JSONObject geometry = element.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    String name = element.getString("name");
                    String id = element.getString("id");
                    InterestingPoint point = new InterestingPoint();
                    point.Name = name;
                    point.Coordinates = new LatLng(lat, lng);
                    point.Description = id;
                    Log.d("json", "parseJSONPlaces: " + point.Coordinates.latitude + " ir " + point.Coordinates.longitude);
                  //  mMap.addMarker(new MarkerOptions().position(point.Coordinates).title(point.Name);
                    Route route = routes.get(index);
                    route.points_of_interest.add(point);
                    routes.set(index,route);

                }
            }
            
        }





    }
}
