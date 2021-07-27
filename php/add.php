<?php
include '_db.php';

$lat = $_POST['lat'];
$lng = $_POST['lng'];
$title = $_POST['title'];

db_clean_old_incident();
if(!db_check_duplicate_incident($lat, $lng)) {
    db_save_incident($lat, $lng, $title);
    response_update_success();
} else {
    response(500, "Duplicate Incident", null);
}
