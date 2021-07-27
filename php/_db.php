<?php 

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
    return count(resultToArrayRows($result)) > 0;
}

function resultToArrayRows($result) {
    while($row = $result->fetch_assoc()) {
        $jsonArray[]= $row;
    }
    return $jsonArray;
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

function response($code, $message, $data) {
    $response = [
        "status" => $code, 
        "message" => $message,
        "data" => $data
    ];
    echo(json_encode($response));
}

function response_update_success() {
    response(200, "Update Success", null);
}

function response_failure($message) {
    response(500, $message, null);
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
    $conn = connection();
    $sql = "SELECT * FROM tb_incident WHERE lat > $minLat AND lat < $maxLat AND lng > $minLng AND lng < $maxLng";
    $result = $conn->query($sql);
    $data = resultToArrayRows($result);
    $conn->close();
    response(200, "Success", $data);
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