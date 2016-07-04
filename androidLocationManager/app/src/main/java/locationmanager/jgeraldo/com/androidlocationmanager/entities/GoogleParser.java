package locationmanager.jgeraldo.com.androidlocationmanager.entities;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import locationmanager.jgeraldo.com.androidlocationmanager.utils.Util;

public final class GoogleParser {

    private URL mFeedUrl;

    private JSONObject jsonLegs;

    private static final int BUFFER_SIZE = 1024;

    private static final int POLYLINE_CONSTANT_63 = 63;

    private static final int POLYLINE_CONSTANT_5 = 5;

    private static final int POLYLINE_CONSTANT_HEX_1 = 0x1f;

    private static final int POLYLINE_CONSTANT_HEX_20 = 0x20;

    private static final double POLYLINE_CONSTANT_1E5 = 1E5;

    public GoogleParser(final String feedUrl) {
        try {
            this.mFeedUrl = new URL(feedUrl);
            try {
                jsonLegs();
            } catch (final JSONException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Route parseRoute() {
        final Route route = new Route();
        try {
            final JSONArray steps = jsonLegs.getJSONArray("steps");
            JSONObject step;

            for (int i = 0; i < steps.length(); i++) {
                step = steps.getJSONObject(i);
                route.addPoints(decodePolyLine(step.getJSONObject("polyline")
                        .getString("points")));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return route;
    }

    public String parseDistanceRoute() {
        String distance = "-";
        try {
            distance = jsonLegs.getJSONObject("distance").getString("text");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return distance;
    }

    public String parseTimeRoute() {
        String time = "-";
        try {
            time = jsonLegs.getJSONObject("duration").getString("text");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    private void jsonLegs() throws JSONException, IOException {
        JSONObject json;
        json = new JSONObject(Util.convertStreamToString(mFeedUrl.openConnection()
                .getInputStream()));

        Log.e("__Json", json.toString());

        final JSONObject jsonRoute = json.getJSONArray("routes").getJSONObject(
                0);
        jsonLegs = jsonRoute.getJSONArray("legs").getJSONObject(0);
    }

    private String convertStreamToString(final InputStream input)
            throws IOException {

        if (input == null) {
            return "";
        } else {
            final Writer writer = new StringWriter();

            final char[] buffer = new char[BUFFER_SIZE];
            try {
                final Reader reader = new BufferedReader(new InputStreamReader(
                        input, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                input.close();
            }
            return writer.toString();
        }
    }

    private List<LatLng> decodePolyLine(final String poly) {
        int index = 0;
        int lng = 0;
        int lat = 0;
        final List<LatLng> decoded = new ArrayList<LatLng>();

        while (index < poly.length()) {
            int b, resultLat = 0, resultLng = 0, shift = 0;
            boolean isEndDecode = false, isEndLat = false, isEndLng = false;

            while (!isEndDecode) {
                b = poly.charAt(index++) - POLYLINE_CONSTANT_63;
                if (!isEndLat) {
                    resultLat |= (b & POLYLINE_CONSTANT_HEX_1) << shift;
                    shift += POLYLINE_CONSTANT_5;
                    if (b < POLYLINE_CONSTANT_HEX_20) {
                        shift = 0;
                        isEndLat = true;
                    }
                } else if (!isEndLng) {
                    resultLng |= (b & POLYLINE_CONSTANT_HEX_1) << shift;
                    shift += POLYLINE_CONSTANT_5;
                    if (b < POLYLINE_CONSTANT_HEX_20) {
                        shift = 0;
                        isEndLng = true;
                    }
                }
                isEndDecode = isEndLat && isEndLng;
            }

            if ((resultLat & 1) == 0) {
                lat += resultLat >> 1;
            } else {
                lat += ~(resultLat >> 1);
            }

            if ((resultLng & 1) == 0) {
                lng += resultLng >> 1;
            } else {
                lng += ~(resultLng >> 1);
            }

            decoded.add(new LatLng((float) (lat / POLYLINE_CONSTANT_1E5),
                    (float) (lng / POLYLINE_CONSTANT_1E5)));
        }
        return decoded;
    }
}
