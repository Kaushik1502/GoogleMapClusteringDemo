package com.googlemapclusteringdemo;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.googlemapclusteringdemo.model.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomMarkerClusteringDemoActivity extends MainActivity implements
        ClusterManager.OnClusterClickListener<Person>,
        ClusterManager.OnClusterInfoWindowClickListener<Person>,
        ClusterManager.OnClusterItemClickListener<Person>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
    private ClusterManager<Person> mClusterManager;
    private Random mRandom = new Random(1984);
    private Person clickedClusterItem;
    //private HashMap<String, Person> providerDetailHashMap = new HashMap<>();

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final CircleImageView mImageView;
        private final CircleImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (CircleImageView) multiProfile.findViewById(R.id.image);

            mImageView = new CircleImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            //mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
            mIconGenerator.setBackground(null);
            mClusterIconGenerator.setBackground(null);
        }

        @Override
        protected void onBeforeClusterItemRendered(final Person person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            if (!TextUtils.isEmpty(person.profilePhoto)) {
                Glide
                        .with(CustomMarkerClusteringDemoActivity.this)
                        .load(person.profilePhoto)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                mImageView.setImageDrawable(resource);
                                Bitmap icon = mIconGenerator.makeIcon();
                                Marker markerToChange = null;
                                for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                                    if (marker.getPosition().equals(person.getPosition())) {
                                        markerToChange = marker;
                                    }
                                }
                                // if found - change icon
                                if (markerToChange != null) {
                                    markerToChange.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                                }
                            }
                        });
                Bitmap icon = mIconGenerator.makeIcon();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            } else {
                Glide
                        .with(CustomMarkerClusteringDemoActivity.this)
                        .load(R.mipmap.ic_launcher)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                mImageView.setImageDrawable(resource);
                                Bitmap icon = mIconGenerator.makeIcon();
                                Marker markerToChange = null;
                                for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                                    if (marker.getPosition().equals(person.getPosition())) {
                                        markerToChange = marker;
                                    }
                                }
                                // if found - change icon
                                if (markerToChange != null) {
                                    markerToChange.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                                }
                            }
                        });
                Bitmap icon = mIconGenerator.makeIcon();
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            }

        }

        @Override
        protected void onBeforeClusterRendered(final Cluster<Person> cluster,final MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            final List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            final int width = mDimension;
            final int height = mDimension;

            int i = 0;

            for (final Person p : cluster.getItems()) {
                // Draw 4 at most.
                i++;
                Glide
                        .with(CustomMarkerClusteringDemoActivity.this)
                        .load(p.profilePhoto)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                resource.setBounds(0, 0, width, height);
                                profilePhotos.add(resource);
                                MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
                                multiDrawable.setBounds(0, 0, width, height);

                                mClusterImageView.setImageDrawable(multiDrawable);
                                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
                            }
                        });

                if (i == 4) break;
            }
            mIconGenerator.setBackground(null);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }

        @Override
        protected void onClusterRendered(final Cluster<Person> cluster,final Marker marker) {
            super.onClusterRendered(cluster, marker);
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            final List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            final int width = mDimension;
            final int height = mDimension;

            int i = 0;

            for (final Person p : cluster.getItems()) {
                // Draw 4 at most.
                i++;
                Glide
                        .with(CustomMarkerClusteringDemoActivity.this)
                        .load(p.profilePhoto)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                resource.setBounds(0, 0, width, height);
                                profilePhotos.add(resource);
                                MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
                                multiDrawable.setBounds(0, 0, width, height);

                                mClusterImageView.setImageDrawable(multiDrawable);
                                Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                            }
                        });

                if (i == 4) break;
            }
            /*Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));*/
        }

        @Override
        protected void onClusterItemRendered(final Person person, Marker marker) {
            super.onClusterItemRendered(person, marker);
            // Draw a single person.
            // Set the info window to show their name.
            if (!TextUtils.isEmpty(person.profilePhoto)) {
                Glide
                        .with(CustomMarkerClusteringDemoActivity.this)
                        .load(person.profilePhoto)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                mImageView.setImageDrawable(resource);
                                Bitmap icon = mIconGenerator.makeIcon();
                                Marker markerToChange = null;
                                for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                                    if (marker.getPosition().equals(person.getPosition())) {
                                        markerToChange = marker;
                                    }
                                }
                                // if found - change icon
                                if (markerToChange != null) {
                                    markerToChange.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                                }
                            }
                        });
                Bitmap icon = mIconGenerator.makeIcon();
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            } else {
                Glide
                        .with(CustomMarkerClusteringDemoActivity.this)
                        .load(R.mipmap.ic_launcher)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<GlideDrawable>() {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                mImageView.setImageDrawable(resource);
                                Bitmap icon = mIconGenerator.makeIcon();
                                Marker markerToChange = null;
                                for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                                    if (marker.getPosition().equals(person.getPosition())) {
                                        markerToChange = marker;
                                    }
                                }
                                // if found - change icon
                                if (markerToChange != null) {
                                    markerToChange.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                                }
                            }
                        });
                Bitmap icon = mIconGenerator.makeIcon();
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            }
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
        clickedClusterItem = item;
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
        //Cluster item InfoWindow clicked, set title as action
        Toast.makeText(CustomMarkerClusteringDemoActivity.this,item.getTitle(),Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 9.5f));

        mClusterManager = new ClusterManager<Person>(this, getMap());
        mClusterManager.setRenderer(new PersonRenderer());
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        addItems();
        mClusterManager.cluster();
        getMap().setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(
                new CustomInfoWindowAdapter());
    }

    private void addItems() {
        // http://www.flickr.com/photos/sdasmarchives/5036248203/
        mClusterManager.addItem(new Person(position(), "Walter",
                "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSfLER1OaZj4TjrWF9LFWRbSrwhLSmQm-V6uuivobigYmf-c96BTA"));

        // http://www.flickr.com/photos/usnationalarchives/4726917149/
        mClusterManager.addItem(new Person(position(), "Gran",
                "http://www.mydevfactory.com/~mousumi/Iservices/userfiles/display_images(6)_1470285461.jpg"));

        // http://www.flickr.com/photos/nypl/3111525394/
        mClusterManager.addItem(new Person(position(), "Ruth",
                "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSfLER1OaZj4TjrWF9LFWRbSrwhLSmQm-V6uuivobigYmf-c96BTA"));

        // http://www.flickr.com/photos/smithsonian/2887433330/
        mClusterManager.addItem(new Person(position(), "Stefan",
                "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSfLER1OaZj4TjrWF9LFWRbSrwhLSmQm-V6uuivobigYmf-c96BTA"));

        // http://www.flickr.com/photos/library_of_congress/2179915182/
        mClusterManager.addItem(new Person(position(), "Mechanic",
                "http://www.mydevfactory.com/~mousumi/Iservices/userfiles/display_images(6)_1470285461.jpg"));

        // http://www.flickr.com/photos/nationalmediamuseum/7893552556/
        mClusterManager.addItem(new Person(position(), "Yeats",
                ""));

        // http://www.flickr.com/photos/sdasmarchives/5036231225/
        mClusterManager.addItem(new Person(position(), "John",
                "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSfLER1OaZj4TjrWF9LFWRbSrwhLSmQm-V6uuivobigYmf-c96BTA"));

        // http://www.flickr.com/photos/anmm_thecommons/7694202096/
        mClusterManager.addItem(new Person(position(), "Trevor the Turtle",
                "http://www.mydevfactory.com/~mousumi/Iservices/userfiles/display_images(6)_1470285461.jpg"));

        // http://www.flickr.com/photos/usnationalarchives/4726892651/
        mClusterManager.addItem(new Person(position(), "Teach",
                "http://www.mydevfactory.com/~mousumi/Iservices/userfiles/display_images(6)_1470285461.jpg"));
    }

    private LatLng position() {
        return new LatLng(random(51.6723432, 51.38494009999999), random(0.148271, -0.3514683));
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        //private Activity mActivity;
        private View view;

        public CustomInfoWindowAdapter() {
            //this.mActivity = activity;
            view = getLayoutInflater().inflate(R.layout.custom_info_window,
                    null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (marker != null
                    && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            String url = clickedClusterItem.profilePhoto;

            ImageView image = ((ImageView) view.findViewById(R.id.badge));

            if (url != null && !url.equalsIgnoreCase("null")
                    && !url.equalsIgnoreCase("")) {
                Glide.with(CustomMarkerClusteringDemoActivity.this).load(url).asBitmap()/*.override(50, 50)*/.listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!isFromMemoryCache) marker.showInfoWindow();
                        return false;
                    }
                }).into(image);
                getInfoContents(marker);
            } else {
                image.setImageResource(R.mipmap.ic_launcher);
            }

            final String title = clickedClusterItem.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            final String snippet = marker.getSnippet();
            final TextView snippetUi = ((TextView) view
                    .findViewById(R.id.snippet));
            Geocoder gcd = new Geocoder(CustomMarkerClusteringDemoActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                StringBuilder sb = null;
                LatLng latlng = clickedClusterItem.getPosition();
                addresses = gcd.getFromLocation(latlng.latitude, latlng.longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    sb = new StringBuilder();
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i)).append(" ");
                    }
                }
                if (sb != null) {
                    snippetUi.setText(sb.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*if (snippet != null) {
                snippetUi.setText(snippet);
            } else {
                snippetUi.setText(clickedClusterItem.getSnippet());
            }*/
            RatingBar customerRatingBar = (RatingBar) view.findViewById(R.id.customerRatingBar);
            //if (providerDetailHashMap.get(marker.getId()).getRating() != null && providerDetailHashMap.get(marker.getId()).getRating().length() > 0)
            customerRatingBar.setRating(3.5f);
            return view;
        }
    }
}