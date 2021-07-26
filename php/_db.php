<?php 

// class KLocation {
//     function __construct($lat, $lng) {
//         $this->lat = $lat;
//         $this->lng = $lng;
//     }

//     public $lat = 0;
//     public $lng = 0;
// }

// class KUserIncident {
//     public $title;
//     public $uuid;
//     public $report_time;
//     public $location;
// }

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

function hasResult($result) {
    return mysql_num_rows($result) > 0;
}

function currentTime() {
    return date_create()->format('Y-m-d H:i:s');
}

function genUUID() {
    return uniqid();
}

function db_clean_old_incident() {
    $conn = connection();
    $sql = "DELETE FROM tb_incident WHERE TIME_TO_SEC(now()) - TIME_TO_SEC(reportTime) > 3600";
    mysqli_query($conn, $sql);
    $conn->close();
}

function response($code, $message) {
    $response = [
        "status" => $code, 
        "message" => $message
    ];
    echo(var_dump($response));
}

function response_success() {
    response(200, "Success");
}

function response_failure($message) {
    response(500, $message);
}

function db_save_incident($lat, $lng, $title) {
    $newUUID = genUUID();
    $conn = connection();

    $sql = "INSERT INTO tb_incident (id, title, lat, lng, reportTime) VALUES ('$newUUID', '$title', '$lat', '$lng', now())";
    mysqli_query($conn, $sql);
    $conn->close();
}

function db_search_incident($minLat, $minLng, $maxLat, $maxLng) {
    $current = currentTime();
}

function db_check_duplicate_incident($lat, $lng) {
    $conn = connection();
    $d = 0.003;
    $minLat = $lat - $d;
    $maxLat = $lat + $d;
    $minLng = $lng - $d;
    $maxLng = $lng + $d;

    $sql = "SELECT * FROM tb_incident WHERE lat > $minLat AND lat < $maxLat AND lng > $minLng AND lng < $maxLng";
    $result = $conn->query($sql);
    $hasResult = hasResult($result);

    $conn->close();
    return $hasResult;
}