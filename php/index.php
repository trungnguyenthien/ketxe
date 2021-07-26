<?php
include '_db.php';

$conn = connection();

$sql = "SELECT * FROM tb_incident";
$result = $conn->query($sql);


while($row = $result->fetch_assoc()) {
    $jsonArray[]= $row;
}

$conn->close();

echo json_encode($jsonArray);
