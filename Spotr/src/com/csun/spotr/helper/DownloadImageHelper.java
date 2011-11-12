package com.csun.spotr.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DownloadImageHelper {
	private static URLConnection connection;
	private static URL url;
	private static InputStream openHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
		url = new URL(urlString);
		connection = url.openConnection();

		if (!(connection instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");
		try {
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		}
		catch (Exception ex) {
			throw new IOException("Error connecting");
		}
		return in;
	}

	public static Bitmap downloadImage(String URL) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = openHttpConnection(URL);
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		}
		catch (IOException e) {
			Log.d("[DownloadImageHelper] downloadImage(URL)", e.getMessage());
		}
		return bitmap;
	}
}
