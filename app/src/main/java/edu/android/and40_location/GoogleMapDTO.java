package edu.android.and40_location;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapDTO {
    private List<Routes> routes;

    public GoogleMapDTO(List<Routes> routes) {
        this.routes = new ArrayList<>();
    }

    public List<Routes> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Routes> routes) {
        this.routes = routes;
    }

    //    var routes = new ArrayList<Routes>();
}

class Routes {
    private List<Legs> legs;

    public Routes() {
        this.legs = new ArrayList<>();
    }

    public List<Legs> getLegs() {
        return legs;
    }

    public void setLegs(List<Legs> legs) {
        this.legs = legs;
    }
}

class Legs {
    private Distance distance;
    private Duration duration;
    private String start_address;
    private String end_address;
    private Location start_location;
    private Location end_location;
    private List<Steps> steps;

    public Legs() {
        this.distance = new Distance();
        this.duration = new Duration();
        this.start_address = "";
        this.end_address = "";
        this.start_location = new Location();
        this.end_location = new Location();
        this.steps = new ArrayList<>();

    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getStart_address() {
        return start_address;
    }

    public void setStart_address(String start_address) {
        this.start_address = start_address;
    }

    public String getEnd_address() {
        return end_address;
    }

    public void setEnd_address(String end_address) {
        this.end_address = end_address;
    }

    public Location getStart_location() {
        return start_location;
    }

    public void setStart_location(Location start_location) {
        this.start_location = start_location;
    }

    public Location getEnd_location() {
        return end_location;
    }

    public void setEnd_location(Location end_location) {
        this.end_location = end_location;
    }

    public List<Steps> getSteps() {
        return steps;
    }

    public void setSteps(List<Steps> steps) {
        this.steps = steps;
    }

    /* var distance = Distance()
    var duration = Duration()
        var end_address = ""
        var start_address = ""
        var end_location =Location()
        var start_location = Location()
        var steps = ArrayList<Steps>()*/
}

class Steps {
    private Distance distance;
    private Duration duration;
    private String start_address;
    private String end_address;
    private Location start_location;
    private Location end_location;
    private PolyLine polyLine;
    private String travel_mode;
    private String maneuver;

    public Steps() {
        this.distance = new Distance();
        this.duration = new Duration();
        this.start_address = "";
        this.end_address = "";
        this.start_location = new Location();
        this.end_location = new Location();
        this.polyLine = new PolyLine();
        this.travel_mode = "";
        this.maneuver = "";
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getStart_address() {
        return start_address;
    }

    public void setStart_address(String start_address) {
        this.start_address = start_address;
    }

    public String getEnd_address() {
        return end_address;
    }

    public void setEnd_address(String end_address) {
        this.end_address = end_address;
    }

    public Location getStart_location() {
        return start_location;
    }

    public void setStart_location(Location start_location) {
        this.start_location = start_location;
    }

    public Location getEnd_location() {
        return end_location;
    }

    public void setEnd_location(Location end_location) {
        this.end_location = end_location;
    }

    public PolyLine getPolyLine() {
        return polyLine;
    }

    public void setPolyLine(PolyLine polyLine) {
        this.polyLine = polyLine;
    }

    public String getTravel_mode() {
        return travel_mode;
    }

    public void setTravel_mode(String travel_mode) {
        this.travel_mode = travel_mode;
    }

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

    /*var distance = Distance()
    var duration = Duration()
    var end_address = ""
    var start_address = ""
    var end_location =Location()
    var start_location = Location()
    var polyline = PolyLine()
    var travel_mode = ""
    var maneuver = ""*/
}

class Distance {
    private String text;
    private int value;

    public Distance() {
        this.text = "";
        this.value = 0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /*var text = ""
    var value = 0*/
}

class Duration {
    private String text;
    private int value;

    public Duration() {
        this.text = "";
        this.value = 0;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /*var text = ""
    var value = 0*/
}


class PolyLine {
    private String points;

    public PolyLine() {
        this.points = "";
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    //    var points = ""
}

class Location {
    private String lat;
    private String lng;

    public Location() {
        this.lat = "";
        this.lng = "";
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    /*var lng = ""
    var lat = ""*/
}

