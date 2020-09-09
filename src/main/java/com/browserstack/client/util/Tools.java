package com.browserstack.client.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Tools {

  // Checks if a string is null or empty
  public static boolean isStringEmpty(String str) {
    return (str == null || str.isEmpty());
  }

  public static final DateFormat SESSION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
}
