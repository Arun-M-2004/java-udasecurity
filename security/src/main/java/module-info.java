module com.udacity.catpoint.security {
    requires com.udacity.catpoint.image;
    requires com.google.gson;
    requires com.google.common;
    requires java.desktop;
    requires java.prefs;

    opens com.udacity.catpoint.data to com.google.gson;
    opens com.udacity.catpoint.service;
    opens com.udacity.catpoint.application;
    exports com.udacity.catpoint.data;
    exports com.udacity.catpoint.service;
    exports com.udacity.catpoint.application;
}