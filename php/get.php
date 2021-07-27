<?php
include '_db.php';

$area = $_GET["area"];

$com = explode(",", $area);

$minLat = $com[0];
$minLng = $com[1];
$maxLat = $com[2];
$maxLng = $com[3];

db_clean_old_incident();
db_search_incident($minLat, $minLng, $maxLat, $maxLng);