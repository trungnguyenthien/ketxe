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