package com.test.mafiaserver.jetty;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

public class JettyServerService extends IntentService {
    private static final String TAG = JettyServerService.class.getName();
    private static final int PORT_NUMBER = 8080;
    private Server server;

    /**
     * Constructor.
     */
    public JettyServerService() {
        super("JETTYServerService");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.i(TAG, "onHandleIntent");
        try {
            server = new Server(PORT_NUMBER);

            // Setup the context for servlets
            ServletContextHandler context = new ServletContextHandler();
            // Set the context for all filters and servlets
            // Required for the internal servlet & filter ServletContext
            // to be sane
            context.setContextPath("/");

            // Add a servlet
            context.addServlet(SonarOOBEServlet.class, "/*");

            // Add the filter, and then use the provided FilterHolder to
            // configure it
            FilterHolder cors = context.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
            cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "/*");
            cors.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "/*");
            cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "/*");
            cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                    "X-Requested-With,Content-Type,Accept,Origin");

            // Create the server level handler list.
            HandlerList handlers = new HandlerList();
            // Make sure DefaultHandler is last (for error handling
            // reasons)
            handlers.setHandlers(new Handler[]{context, new DefaultHandler()});

            server.setHandler(handlers);
            server.start();

            server.join();
        } catch (Exception e) {
            Log.e(TAG, "Server could not be started because ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        try {
            server.stop();
        } catch (Exception e) {
            Log.e(TAG, "unable to stop Jetty server");
        }
    }
}
