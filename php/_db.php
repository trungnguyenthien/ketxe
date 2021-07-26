<?php 

class KLocation {
    function __construct($lat, $lng) {
        $this->lat = $lat;
        $this->lng = $lng;
    }

    public $lat = 0;
    public $lng = 0;
}

class KUserIncident {
    public $title;
    public $uuid;
    public $report_time;
    public $location;
}

function connection() {
    $address = "localhost";
    $username = "ketxexyz_admin";
    $password = "123456";
    $database = "ketxexyz_kx01";

    // Create connection
    $conn =  mysqli_connect($address, $username, $password, $database);

    // Check connection
    if ($conn->connect_error) {
        die("Connection failed: " . $conn->connect_error);
    } 

    return $conn;
}

function currentTime() {
    return date('Y/m/d H:i');
}

function genUUID() {
    return uniqid();
}

function db_save_incident($lat, $lng, $title) {
    $newUUID = genUUID();
    $current = currentTime();
}

function db_search_incident($minLat, $minLng, $maxLat, $maxLng) {
    $current = currentTime();
}