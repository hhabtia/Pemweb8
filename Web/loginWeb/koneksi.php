<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

$host = '172.30.224.1'; // IP Windows dari WSL
$port = '5432';
$dbname = 'twitter';
$user = 'postgres';
$password = 'yupiburger';

function koneksidb() {
    global $host, $port, $dbname, $user, $password;
    $conn = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$password");
    if (!$conn) {
        echo "❌ Koneksi gagal.";
    } else {
        echo " ";
    }
    return $conn;
}

// PANGGIL FUNGSI-NYA DI SINI
koneksidb();
?>
