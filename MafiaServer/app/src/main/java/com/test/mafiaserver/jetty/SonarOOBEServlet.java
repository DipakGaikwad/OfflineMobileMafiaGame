package com.test.mafiaserver.jetty;

import android.util.Log;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SonarOOBEServlet extends HttpServlet {

    private static final String TAG = SonarOOBEServlet.class.getSimpleName();

    public SonarOOBEServlet() {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Log.i(TAG,"doPost called");
    }
}
