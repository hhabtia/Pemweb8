<?php
include "koneksi.php";

$conn = koneksidb();

if ($conn) {
    echo "✅ Koneksi ke database berhasil!";
} else {
    echo "❌ Gagal konek database.";
}
?>
