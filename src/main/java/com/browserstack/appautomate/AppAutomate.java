package com.browserstack.appautomate;

import java.io.FileNotFoundException;
import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.model.AppUploadResponse;

public interface AppAutomate {

  public AppUploadResponse uploadApp(String filePath)
      throws AppAutomateException, FileNotFoundException, InvalidFileExtensionException;

}
