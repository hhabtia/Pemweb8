<?php
session_start();
include_once "koneksi.php";

$ingat_aku = isset($_COOKIE['ingat_aku']) ? $_COOKIE['ingat_aku'] : '';

function login()
{
    if (empty($_POST['username']) || empty($_POST['password'])) {
        return;
    }

    $conn = koneksidb();
    $username = $_POST['username'];
    $password = $_POST['password'];

    $sql = "SELECT * FROM pengguna WHERE username = $1";
    pg_prepare($conn, "my_query", $sql);
    $result = pg_execute($conn, "my_query", array($username));

    if (!$result || pg_num_rows($result) <= 0) {
        echo "<script>alert('Gagal login: username atau password salah!'); window.location.href='login.php';</script>";
        pg_close($conn);
        return;
    }

    $row = pg_fetch_array($result);
    $password_db = $row["password"];

    if ($password_db == $password) {
        // Set session dan redirect ke main.php
        $_SESSION['username'] = $username;

        setcookie("ingat_aku", $username, time() + (60 * 60 * 24), "/");

        pg_close($conn);
        echo "<script>alert('Login berhasil!'); window.location.href='main.php';</script>";
        exit();
    } else {
        echo "<script>alert('Gagal login: username atau password salah!'); window.location.href='login.php';</script>";
        pg_close($conn);
    }
}

login();
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login X Style</title>
    <link rel="stylesheet" href="login.css">
    <style>
        /* Overlay untuk latar belakang saat form muncul */
        .overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 5;
            display: none; /* Sembunyikan secara default */
        }

        .overlay.show {
            display: block;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="left-side">
            <img src="images/x.jpg" alt="X Logo" class="logo">
        </div>
        <div class="right-side">
            <h1 class="main-title">Happening now</h1>
            <h2 class="sub-title">Join today.</h2>

            <form id="login-form" action="login.php" method="POST">
    <div class="form-group">
        <input type="text" name="username" placeholder="Phone number, username, or email"
               value="<?= htmlspecialchars($ingat_aku) ?>" required>
    </div>
    <div class="form-group">
        <input type="password" name="password" placeholder="Password" required>
    </div>
</form>



<button type="submit" form="login-form" class="login-btn">Masuk</button>

<div class="or">or</div>

<a href="create.php" class="create-account-btn">Create account</a>


            <p class="agreement">
                By signing up, you agree to the
                <a href="#">Terms of Service</a> and
                <a href="#">Privacy Policy</a>, including
                <a href="#">Cookie Use</a>.
            </p>

        
    
        </div>
    </div>

    <div class="signup-container">
        <div class="header">
            <button class="close-btn">
                <svg viewBox="0 0 24 24" aria-hidden="true" class="r-1cvkan6 r-4qtqp9 r-yyyyoo r-dnmrzf r-bnwqim r-1plcrui r-lrvibr r-1xvli5t r-5njf8e r-1otgn73 r-l5o3uw r-10paoce r-1i6wzkk r-o7ynqc r-6416eg r-13qz1uu">
                    <g>
                        <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-7.7-10.172-7.952 10.172H5.575l7.98-10.454-8.872-11.78H18.244zm-11.615 1.56L18.02 21.45H5.2l6.41-18.19z"></path>
                    </g>
                </svg>
            </button>
            <div class="x-logo-header">
                <svg viewBox="0 0 24 24" aria-hidden="true" class="r-1cvkan6 r-4qtqp9 r-yyyyoo r-dnmrzf r-bnwqim r-1plcrui r-lrvibr r-1xvli5t r-5njf8e r-1otgn73 r-l5o3uw r-10paoce r-1i6wzkk r-o7ynqc r-6416eg r-13qz1uu">
                    <g>
                        <path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-7.7-10.172-7.952 10.172H5.575l7.98-10.454-8.872-11.78H18.244zm-11.615 1.56L18.02 21.45H5.2l6.41-18.19z"></path>
                    </g>
                </svg>
            </button>
            <h1>Create your account</h1>
        </div>
        <div class="form-group">
            <label for="name">Name</label>
            <input type="text" id="name" placeholder="Name" maxlength="50">
            <div class="character-count">0 / 50</div>
        </div>
        <div class="form-group">
            <label for="email">Email</label>
            <input type="email" id="email" placeholder="Email">
        </div>
        <div class="date-of-birth">
            <label>Date of birth</label>
            <p class="dob-info">This will not be shown publicly. Confirm your own age, even if this account is for a business, a pet, or something else.</p>
            <div class="dob-selects">
                <div class="select-group">
                    <select id="month">
                        <option value="" disabled selected>Month</option>
                        <option value="1">January</option>
                        <option value="2">February</option>
                        </select>
                    <div class="select-arrow"></div>
                </div>
                <div class="select-group">
                    <select id="day">
                        <option value="" disabled selected>Day</option>
                        </select>
                    <div class="select-arrow"></div>
                </div>
                <div class="select-group">
                    <select id="year">
                        <option value="" disabled selected>Year</option>
                        </select>
                    <div class="select-arrow"></div>
                </div>
            </div>
        </div>
        <button class="next-btn">Next</button>
    </div>

    <div class="overlay"></div>

    <!-- <script>
        const createAccountBtn = document.querySelector('.create-account-btn');
        const signupContainer = document.querySelector('.signup-container');
        const closeBtn = document.querySelector('.close-btn');
        const overlay = document.querySelector('.overlay');

        createAccountBtn.addEventListener('click', () => {
            signupContainer.classList.add('show');
            overlay.classList.add('show');
        });

        closeBtn.addEventListener('click', () => {
            signupContainer.classList.remove('show');
            overlay.classList.remove('show');
        });

        overlay.addEventListener('click', () => {
            signupContainer.classList.remove('show');
            overlay.classList.remove('show');
        });
    </script> -->
</body>
</html>