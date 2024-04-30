package edu.illinois.cs.cs124.ay2023.mp.network;

import static edu.illinois.cs.cs124.ay2023.mp.helpers.Helpers.CHECK_SERVER_RESPONSE;
import static edu.illinois.cs.cs124.ay2023.mp.helpers.Helpers.OBJECT_MAPPER;

import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import edu.illinois.cs.cs124.ay2023.mp.application.CourseableApplication;
import edu.illinois.cs.cs124.ay2023.mp.helpers.ResultMightThrow;
import edu.illinois.cs.cs124.ay2023.mp.models.Course;
import edu.illinois.cs.cs124.ay2023.mp.models.Rating;
import edu.illinois.cs.cs124.ay2023.mp.models.Summary;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Course API client.
 *
 * <p>You will add functionality to the client to complete the project.
 */
public final class Client {
  private static final String TAG = Client.class.getSimpleName();

  /**
   * Retrieve the list of summaries.
   *
   * @param callback the callback that will receive the result
   */
  public void getSummary(
      //        consumer is interface callback method passed to other function
      @NonNull Consumer<ResultMightThrow<List<Summary>>> callback) { // making HTTP network request
    StringRequest // StringRequest object
        summaryRequest = // app asks server for info on uiuc courses, asking for list of summaries
        new StringRequest(
                Request.Method.GET, // Tell it it's a Get request
                CourseableApplication.SERVER_URL
                    + "/summary/", // tell request where to get data from (THE ADDRESS)
                response -> { // when gets server response, takes STRING result and turns into LIST
                  // callback method (lambda), the response is passed
                  // what you want to happen when you get a server response
                  // OF SUMMARIES
                  try { // DESERIALIZES DATA!!!
                    List<Summary> summaries = // list of summaries!!!!
                        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
                    // deserializes the string to objects
                    callback.accept(new ResultMightThrow<>(summaries));
                    // wrap result in result might throw with deserialization
                  } catch (JsonProcessingException e) {
                    callback.accept(new ResultMightThrow<>(e));
                  }
                },
                error -> callback.accept(new ResultMightThrow<>(error)));
    // called if there is an error with the request if server is bad
    // then need to actually ADD to request queue (set up, then add)
    requestQueue.add(summaryRequest); // returns summaries to the MAINACTIVITY
  }

  // return 1 course
  // after getting server data and deserializing... return course not list of summaries
  public void getCourse(Summary summary, @NonNull Consumer<ResultMightThrow<Course>> callback) {
    // finish getCourse() method
    // if summary is CS 102 --> construct path /course/CS/102
    String path = "/course/" + summary.getSubject() + "/" + summary.getNumber();

    //        consumer is interface callback method passed to other function
    // making HTTP network request
    StringRequest // StringRequest object
        courseRequest = // app asks server for info on uiuc courses, asking for list of summaries
        new StringRequest(
                Request.Method.GET, // Tell it it's a Get request
                CourseableApplication.SERVER_URL
                    + path, // tell request where to get data from (THE ADDRESS)
                response -> { // when gets server response, takes STRING result and turns into LIST
                  // callback method (lambda), the response is passed
                  // what you want to happen when you get a server response
                  // OF SUMMARIES
                  try { // DESERIALIZES DATA!!!
                    Course course = // single course!!!!
                        OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
                    // deserializes the string to objects
                    callback.accept(new ResultMightThrow<Course>(course));
                    // wrap result in result might throw with deserialization
                  } catch (JsonProcessingException e) {
                    callback.accept(new ResultMightThrow<>(e));
                  }
                },
                error -> callback.accept(new ResultMightThrow<>(error)));
    // called if there is an error with the request if server is bad
    // then need to actually ADD to request queue (set up, then add)
    requestQueue.add(courseRequest); // returns summaries to the MAINACTIVITY

    // callback.accept(
    //    new ResultMightThrow<>(
    //        new IllegalStateException("TODO"))); // this stub prevent method from timing out
  }

  public void getRating(
      @NonNull Summary summary, @NonNull Consumer<ResultMightThrow<Rating>> callback) {
    // placeholder for making the getRating method
    // move data from server to client
    // callback.accept(new ResultMightThrow<>(new IllegalStateException()));
    String path = "/rating/" + summary.getSubject() + "/" + summary.getNumber();

    //        consumer is interface callback method passed to other function
    // making HTTP network request
    StringRequest // StringRequest object
        ratingRequest = // app asks server for info on uiuc courses, asking for list of summaries
        new StringRequest(
                Request.Method.GET, // Tell it it's a Get request
                CourseableApplication.SERVER_URL
                    + path, // tell request where to get data from (THE ADDRESS)
                response -> { // when gets server response, takes STRING result and turns into LIST
                  // callback method (lambda), the response is passed
                  // what you want to happen when you get a server response
                  // OF SUMMARIES
                  try { // DESERIALIZES DATA!!!
                    Rating rating = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
                    // deserializes the string to objects
                    callback.accept(new ResultMightThrow<Rating>(rating));
                    // wrap result in result might throw with deserialization
                  } catch (JsonProcessingException e) {
                    callback.accept(new ResultMightThrow<>(e));
                  }
                },
                error -> callback.accept(new ResultMightThrow<>(error)));
    // called if there is an error with the request if server is bad
    // then need to actually ADD to request queue (set up, then add)
    requestQueue.add(ratingRequest); // returns summaries to the MAINACTIVITY

    // return serialized rating rather than course object
  }

  public void postRating(
      @NonNull Rating rating, @NonNull Consumer<ResultMightThrow<Rating>> callback) {
    // more data from client to server (change the rating ? )
    // get is retrieve post is putting the mail into the server
    StringRequest ratingRequest =
        new StringRequest(
            Request.Method.POST,
            CourseableApplication.SERVER_URL + "/rating/",
            response -> callback.accept(new ResultMightThrow<>(rating)),
            error -> callback.accept(new ResultMightThrow<>(error))) {
          @Override
          public String getBodyContentType() {
            return "application/json; charset=utf-8";
          }

          @Override
          public byte[] getBody() throws AuthFailureError {
            // take rating passed to this method
            // serialize it
            try {
              return OBJECT_MAPPER.writeValueAsBytes(rating);
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
            // use this override to put it in the body of your request
            // convert string to byte[]
          }
        };
    requestQueue.add(ratingRequest);
  }

  // You should not need to modify the code below

  /** Client instance to implement the singleton pattern. */
  private static Client instance;

  /**
   * Start the API client.
   *
   * @return an API client instance
   */
  public static Client start() {
    if (instance == null) {
      instance = new Client();
    }
    return instance;
  }

  /** Initial connection delay. */
  private static final int INITIAL_CONNECTION_RETRY_DELAY = 1000;

  /** Max retries to connect to the server. */
  private static final int MAX_STARTUP_RETRIES = 8;

  /** Size of the thread pool. */
  private static final int THREAD_POOL_SIZE = 1;

  /** Queue for our requests. */
  private final RequestQueue requestQueue;

  /** Allow getConnected to wait for startup to complete. */
  private final CompletableFuture<Boolean> connected = new CompletableFuture<>();

  /**
   * Return whether the client is connected or not.
   *
   * @return whether the client is connected
   */
  public boolean getConnected() {
    // Retrieve the result from the CompletableFuture
    try {
      return connected.get();
    } catch (Exception e) {
      return false;
    }
  }

  /** Private constructor to implement the singleton pattern. */

  // this code makes requests to our server
  private Client() {
    // Whether we're in a testing configuration
    boolean testing = Build.FINGERPRINT.equals("robolectric");
    // Disable debug logging
    VolleyLog.DEBUG = false;
    // Follow redirects so POST works
    HttpURLConnection.setFollowRedirects(true);

    // Configure our request queue
    Cache cache = new NoCache();
    Network network = new BasicNetwork(new HurlStack());
    if (testing) {
      requestQueue =
          new RequestQueue(
              cache,
              network,
              THREAD_POOL_SIZE,
              new ExecutorDelivery(Executors.newSingleThreadExecutor()));
    } else {
      requestQueue = new RequestQueue(cache, network);
    }

    // Make sure the backend URL is valid
    URL serverURL;
    try {
      serverURL = new URL(CourseableApplication.SERVER_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Bad server URL: " + CourseableApplication.SERVER_URL);
      return;
    }

    // Start a background thread to establish the server connection
    new Thread(
            () -> {
              for (int i = 0; i < MAX_STARTUP_RETRIES; i++) {
                try {
                  // Issue a GET request for the root URL
                  HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
                  String body =
                      new BufferedReader(new InputStreamReader(connection.getInputStream()))
                          .lines()
                          .collect(Collectors.joining("\n"));
                  if (!body.equals(CHECK_SERVER_RESPONSE)) {
                    throw new IllegalStateException("Invalid response from server");
                  }
                  connection.disconnect();

                  // Once this succeeds, we're connected and can start the Volley queue
                  connected.complete(true);
                  requestQueue.start();
                  return;
                } catch (Exception ignored) {
                }
                // If the connection fails, delay and then retry
                try {
                  Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY);
                } catch (InterruptedException ignored) {
                }
              }
              Log.e(TAG, "Client couldn't connect");
            })
        .start();
  }
}
