<?php
session_start();
header('Content-Type: application/json');

if (!isset($_SESSION['username'])) {
    http_response_code(403);
    echo json_encode(['error' => 'Unauthorized']);
    exit();
}

include_once "koneksi.php";
$conn = koneksidb();

$content = trim($_POST['content'] ?? '');

if ($content === '') {
    http_response_code(400);
    echo json_encode(['error' => 'Konten tidak boleh kosong.']);
    exit();
}

// Ambil ID pengguna dari username
$username = $_SESSION['username'];
$sql_user = "SELECT id FROM pengguna WHERE username = $1";
pg_prepare($conn, "get_user_id", $sql_user);
$result_user = pg_execute($conn, "get_user_id", array($username));

if ($row = pg_fetch_assoc($result_user)) {
    $id_pengguna = $row['id'];

    // Insert post
    $sql_insert = "INSERT INTO post (id_pengguna, content, created_at) VALUES ($1, $2, CURRENT_TIMESTAMP)";
pg_prepare($conn, "insert_post", $sql_insert);
$insert_result = pg_execute($conn, "insert_post", array($id_pengguna, $content));

    if ($insert_result) {
        echo json_encode(['success' => true]);
        exit();
    } else {
        http_response_code(500);
        echo json_encode(['error' => 'Gagal menyimpan postingan.']);
        exit();
    }
} else {
    http_response_code(404);
    echo json_encode(['error' => 'Pengguna tidak ditemukan.']);
    exit();
}
?>