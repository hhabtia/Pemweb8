<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Create Account - Twitter</title>
    <link rel="stylesheet" href="create.css">
</head>
<body>
    <div class="create-account-container">
        <div class="twitter-logo">𝕏</div>
        <h2 class="title">Join Twitter today</h2>
        
        <button class="facebook-btn">
            <i class="icon">📘</i> Sign up with Facebook
        </button>

        <div class="divider">
            <span></span>
            <span class="or">OR</span>
            <span></span>
        </div>

        <form class="signup-form" method="post" action="submit.php">
            <input type="text" name="name" placeholder="Name" required>
            <input type="email" name="email" placeholder="Email" required>
            <input type="text" name="username" placeholder="Username" required>
            <input type="password" name="password" placeholder="Password" required>
            <p class="info">
                By signing up, you agree to our 
                <a href="#">Terms</a>, <a href="#">Privacy Policy</a>, 
                and <a href="#">Cookies Policy</a>.
            </p>
            <button type="submit" class="signup-btn">Sign up</button>
            <p class="login-redirect">
    Already have an account? <a href="login.php">Log in</a>
</p>

        </form>
    </div>
</body>
</html>
