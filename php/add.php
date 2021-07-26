<?php
include '_db.php';

$lat = $_POST['lat'];
$lng = $_POST['lng'];
$title = $_POST['title'];

db_clean_old_incident();
db_save_incident($lat, $lng, $title);
response_success();