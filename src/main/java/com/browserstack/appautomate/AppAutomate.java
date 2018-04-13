package com.browserstack.appautomate;

import java.io.FileNotFoundException;

import com.browserstack.automate.exception.AppAutomateException;
import com.browserstack.automate.exception.InvalidFileExtensionException;
import com.browserstack.automate.exception.SessionNotFound;
import com.browserstack.automate.model.Session;

/**
 * @author Hitesh Raghuvanshi
 */
public interface AppAutomate {

    public Session getSession(String sessionId) throws SessionNotFound, AppAutomateException;

    public String uploadApp(String filePath)
            throws AppAutomateException, FileNotFoundException, InvalidFileExtensionException;

}